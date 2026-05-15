package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.dto.request.SmartWorkOrderDispatchReq;
import com.smartcommunity.dto.request.SmartWorkOrderStatusReq;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairOrderWorker;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairOrderWorkerMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartWorkOrderService {

    private static final int STATUS_WAIT_DISPATCH = 1;
    private static final int STATUS_IN_SERVICE = 2;
    private static final int STATUS_WAIT_EVALUATE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELED = 5;

    private final RepairOrderMapper repairOrderMapper;
    private final RepairOrderWorkerMapper repairOrderWorkerMapper;
    private final UserMapper userMapper;
    private final WorkerRecommendService workerRecommendService;
    private final SlaMonitorService slaMonitorService;

    @Transactional(readOnly = true)
    public Map<String, Object> page(int pageNum, int pageSize, Integer status, Integer priority, String keyword, Boolean overdueOnly) {
        Page<RepairOrder> page = repairOrderMapper.selectPage(
                new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100)),
                buildPageWrapper(status, priority, keyword, overdueOnly)
        );
        List<Map<String, Object>> rows = enrichRows(page.getRecords());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("records", rows);
        payload.put("total", page.getTotal());
        payload.put("pageNum", page.getCurrent());
        payload.put("pageSize", page.getSize());
        return payload;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long orderId) {
        RepairOrder order = requireOrder(orderId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.putAll(enrichRow(order));
        data.put("participants", activeParticipants(orderId));
        data.put("nextStatuses", nextStatuses(order.getStatus()));
        return data;
    }

    @Transactional
    public Map<String, Object> dispatch(Long orderId, SmartWorkOrderDispatchReq req) {
        RepairOrder order = requireOrder(orderId);
        if (Integer.valueOf(STATUS_COMPLETED).equals(order.getStatus()) || Integer.valueOf(STATUS_CANCELED).equals(order.getStatus())) {
            throw new IllegalArgumentException("closed order cannot be dispatched");
        }

        List<Long> assigneeIds = normalizeAssigneeIds(order, req);
        if (assigneeIds.isEmpty()) {
            throw new IllegalArgumentException("no available worker");
        }

        Map<Long, User> workerMap = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, assigneeIds)
                        .eq(User::getRole, 3)
                        .eq(User::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
        for (Long workerId : assigneeIds) {
            if (workerMap.get(workerId) == null) {
                throw new IllegalArgumentException("worker not found: " + workerId);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        order.setAssignee(assigneeIds.get(0));
        order.setSuggestedWorkerId(assigneeIds.get(0));
        order.setAssignedTime(now);
        order.setStatus(STATUS_IN_SERVICE);
        order.setSlaDeadline(now.plusHours(2));
        order.setRemark(StringUtils.hasText(req == null ? null : req.getRemark()) ? req.getRemark().trim() : "smart dispatch");
        order.setServiceStartTime(now);
        order.setUpdateTime(now);
        repairOrderMapper.updateById(order);

        resetParticipants(orderId, assigneeIds, now);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("assigneeIds", assigneeIds);
        result.put("statusText", statusText(order.getStatus()));
        return result;
    }

    @Transactional
    public RepairOrder updateStatus(Long orderId, SmartWorkOrderStatusReq req) {
        RepairOrder order = requireOrder(orderId);
        if (req == null || req.getStatus() == null) {
            throw new IllegalArgumentException("status is empty");
        }

        Integer targetStatus = req.getStatus();
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(targetStatus);
        order.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : order.getRemark());
        if (Integer.valueOf(STATUS_IN_SERVICE).equals(targetStatus) && order.getServiceStartTime() == null) {
            order.setServiceStartTime(now);
        }
        if (Integer.valueOf(STATUS_WAIT_EVALUATE).equals(targetStatus)) {
            order.setServiceEndTime(now);
            order.setWorkerFinishConfirmed(1);
            order.setWorkerFinishTime(now);
        }
        if (Integer.valueOf(STATUS_COMPLETED).equals(targetStatus)) {
            order.setCompletionTime(now);
            order.setOwnerFinishConfirmed(1);
            order.setOwnerFinishTime(now);
        }
        if (Integer.valueOf(STATUS_CANCELED).equals(targetStatus)) {
            order.setCompletionTime(null);
        }
        order.setUpdateTime(now);
        repairOrderMapper.updateById(order);
        return order;
    }

    @Transactional
    public RepairOrder updatePriority(Long orderId, Integer priority, Long suggestedWorkerId, LocalDateTime slaDeadline) {
        RepairOrder order = requireOrder(orderId);
        if (priority != null) {
            order.setPriority(priority);
        }
        if (suggestedWorkerId != null) {
            order.setSuggestedWorkerId(suggestedWorkerId);
        }
        if (slaDeadline != null) {
            order.setSlaDeadline(slaDeadline);
        }
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        return order;
    }

    @Transactional
    public Map<String, Object> scanSla() {
        int delayCount = slaMonitorService.scanAndUrge();
        return Map.of("delayCount", delayCount);
    }

    private LambdaQueryWrapper<RepairOrder> buildPageWrapper(Integer status, Integer priority, String keyword, Boolean overdueOnly) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getIsDeleted, 0)
                .orderByAsc(RepairOrder::getPriority)
                .orderByDesc(RepairOrder::getId);
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (priority != null) {
            wrapper.eq(RepairOrder::getPriority, priority);
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(RepairOrder::getDescription, text)
                    .or().like(RepairOrder::getCategory, text)
                    .or().like(RepairOrder::getServiceMajor, text)
                    .or().like(RepairOrder::getServiceSubType, text)
                    .or().apply("CAST(id AS CHAR) LIKE {0}", "%" + text + "%"));
        }
        if (Boolean.TRUE.equals(overdueOnly)) {
            wrapper.isNotNull(RepairOrder::getSlaDeadline)
                    .lt(RepairOrder::getSlaDeadline, LocalDateTime.now())
                    .notIn(RepairOrder::getStatus, List.of(STATUS_COMPLETED, STATUS_CANCELED));
        }
        return wrapper;
    }

    private RepairOrder requireOrder(Long orderId) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
            throw new IllegalArgumentException("repair order not found");
        }
        return order;
    }

    private List<Map<String, Object>> enrichRows(List<RepairOrder> orders) {
        return orders.stream().map(this::enrichRow).toList();
    }

    private Map<String, Object> enrichRow(RepairOrder order) {
        Map<Long, User> users = userMap(collectRelatedUserIds(order));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.getId());
        row.put("userId", order.getUserId());
        row.put("ownerName", displayName(users.get(order.getUserId()), "业主", order.getUserId()));
        row.put("category", order.getCategory());
        row.put("serviceMajor", order.getServiceMajor());
        row.put("serviceSubType", order.getServiceSubType());
        row.put("description", order.getDescription());
        row.put("priority", order.getPriority());
        row.put("priorityText", priorityText(order.getPriority()));
        row.put("status", order.getStatus());
        row.put("statusText", statusText(order.getStatus()));
        row.put("assignee", order.getAssignee());
        row.put("assigneeName", displayName(users.get(order.getAssignee()), "维修员", order.getAssignee()));
        row.put("suggestedWorkerId", order.getSuggestedWorkerId());
        row.put("suggestedWorkerName", displayName(users.get(order.getSuggestedWorkerId()), "维修员", order.getSuggestedWorkerId()));
        row.put("assignedTime", order.getAssignedTime());
        row.put("slaDeadline", order.getSlaDeadline());
        row.put("slaOverdue", isOverdue(order));
        row.put("overdueMinutes", overdueMinutes(order));
        row.put("createTime", order.getCreateTime());
        row.put("remark", order.getRemark());
        return row;
    }

    private List<Long> collectRelatedUserIds(RepairOrder order) {
        List<Long> ids = new ArrayList<>(3);
        if (order.getUserId() != null) {
            ids.add(order.getUserId());
        }
        if (order.getAssignee() != null) {
            ids.add(order.getAssignee());
        }
        if (order.getSuggestedWorkerId() != null) {
            ids.add(order.getSuggestedWorkerId());
        }
        return ids;
    }

    private Map<Long, User> userMap(Collection<Long> ids) {
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, validIds)
                        .eq(User::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
    }

    private List<Long> normalizeAssigneeIds(RepairOrder order, SmartWorkOrderDispatchReq req) {
        Set<Long> assigneeIds = new java.util.LinkedHashSet<>();
        if (req != null && req.getAssigneeIds() != null) {
            assigneeIds.addAll(req.getAssigneeIds().stream().filter(Objects::nonNull).filter(id -> id > 0).toList());
        }
        if (req != null && req.getAssigneeId() != null && req.getAssigneeId() > 0) {
            assigneeIds.add(req.getAssigneeId());
        }
        if (assigneeIds.isEmpty()) {
            Long suggested = workerRecommendService.recommendWorker(StringUtils.hasText(order.getCategory()) ? order.getCategory() : order.getServiceMajor());
            if (suggested != null) {
                assigneeIds.add(suggested);
            }
        }
        return new ArrayList<>(assigneeIds);
    }

    private void resetParticipants(Long orderId, List<Long> assigneeIds, LocalDateTime now) {
        repairOrderWorkerMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RepairOrderWorker>()
                .set(RepairOrderWorker::getIsDeleted, 1)
                .set(RepairOrderWorker::getUpdateTime, now)
                .eq(RepairOrderWorker::getOrderId, orderId)
                .eq(RepairOrderWorker::getIsDeleted, 0));
        for (int i = 0; i < assigneeIds.size(); i++) {
            RepairOrderWorker participant = new RepairOrderWorker();
            participant.setOrderId(orderId);
            participant.setWorkerId(assigneeIds.get(i));
            participant.setRoleType(i == 0 ? 1 : 2);
            participant.setVerifyPassed(0);
            participant.setFinishConfirmed(0);
            participant.setCreateTime(now);
            participant.setUpdateTime(now);
            participant.setIsDeleted(0);
            repairOrderWorkerMapper.insert(participant);
        }
    }

    private List<Map<String, Object>> activeParticipants(Long orderId) {
        List<RepairOrderWorker> participants = repairOrderWorkerMapper.selectList(new LambdaQueryWrapper<RepairOrderWorker>()
                .eq(RepairOrderWorker::getOrderId, orderId)
                .eq(RepairOrderWorker::getIsDeleted, 0)
                .orderByAsc(RepairOrderWorker::getRoleType));
        Map<Long, User> workerMap = userMap(participants.stream()
                .map(RepairOrderWorker::getWorkerId)
                .filter(Objects::nonNull)
                .toList());
        return participants.stream().map(item -> {
            Map<String, Object> row = new HashMap<>();
            row.put("workerId", item.getWorkerId());
            row.put("workerName", displayName(workerMap.get(item.getWorkerId()), "维修员", item.getWorkerId()));
            row.put("roleType", item.getRoleType());
            row.put("verifyPassed", item.getVerifyPassed());
            row.put("finishConfirmed", item.getFinishConfirmed());
            row.put("finishTime", item.getFinishTime());
            return row;
        }).toList();
    }

    private List<Map<String, Object>> nextStatuses(Integer status) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (Integer.valueOf(STATUS_WAIT_DISPATCH).equals(status)) {
            rows.add(statusOption(STATUS_IN_SERVICE));
            rows.add(statusOption(STATUS_CANCELED));
            return rows;
        }
        if (Integer.valueOf(STATUS_IN_SERVICE).equals(status)) {
            rows.add(statusOption(STATUS_WAIT_EVALUATE));
            rows.add(statusOption(STATUS_CANCELED));
            return rows;
        }
        if (Integer.valueOf(STATUS_WAIT_EVALUATE).equals(status)) {
            rows.add(statusOption(STATUS_COMPLETED));
        }
        return rows;
    }

    private Map<String, Object> statusOption(Integer status) {
        return Map.of("status", status, "label", statusText(status));
    }

    private boolean isOverdue(RepairOrder order) {
        return order.getSlaDeadline() != null
                && LocalDateTime.now().isAfter(order.getSlaDeadline())
                && !List.of(STATUS_COMPLETED, STATUS_CANCELED).contains(order.getStatus());
    }

    private long overdueMinutes(RepairOrder order) {
        if (!isOverdue(order)) {
            return 0L;
        }
        return Duration.between(order.getSlaDeadline(), LocalDateTime.now()).toMinutes();
    }

    private String displayName(User user, String prefix, Long fallbackId) {
        if (user == null) {
            return fallbackId == null ? "-" : prefix + fallbackId;
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        if (StringUtils.hasText(user.getAccount())) {
            return user.getAccount();
        }
        return fallbackId == null ? "-" : prefix + fallbackId;
    }

    private String statusText(Integer status) {
        return switch (status == null ? 0 : status) {
            case STATUS_WAIT_DISPATCH -> "待派单";
            case STATUS_IN_SERVICE -> "处理中";
            case STATUS_WAIT_EVALUATE -> "待评价";
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "未知";
        };
    }

    private String priorityText(Integer priority) {
        return switch (priority == null ? 0 : priority) {
            case 1 -> "紧急";
            case 2 -> "普通";
            case 3 -> "低";
            default -> "未分级";
        };
    }
}
