package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.dto.request.SmartWorkOrderDispatchReq;
import com.smartcommunity.dto.request.SmartWorkOrderStatusReq;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairOrderWorker;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.WorkerStaffing;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairOrderWorkerMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.WorkerStaffingMapper;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SmartWorkOrderService {

    private static final int STATUS_WAIT_DISPATCH = 1;
    private static final int STATUS_IN_SERVICE = 2;
    private static final int STATUS_WAIT_EVALUATE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELED = 5;
    private static final String MANUAL_PRIORITY_MARK = "[manual-priority]";

    private final RepairOrderMapper repairOrderMapper;
    private final RepairOrderWorkerMapper repairOrderWorkerMapper;
    private final UserMapper userMapper;
    private final WorkerStaffingMapper workerStaffingMapper;
    private final WorkerRecommendService workerRecommendService;
    private final RepairGradingService repairGradingService;
    private final SlaMonitorService slaMonitorService;

    @Transactional
    public Map<String, Object> page(int pageNum, int pageSize, Integer status, Integer priority, String keyword, Boolean overdueOnly) {
        backfillSmartFields();
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

    @Transactional
    public Map<String, Object> detail(Long orderId) {
        RepairOrder order = requireOrder(orderId);
        if (applySmartFields(order)) {
            repairOrderMapper.updateById(order);
        }
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
            if (!StringUtils.hasText(order.getRemark()) || !order.getRemark().contains(MANUAL_PRIORITY_MARK)) {
                String oldRemark = StringUtils.hasText(order.getRemark()) ? order.getRemark().trim() + " " : "";
                order.setRemark(oldRemark + MANUAL_PRIORITY_MARK);
            }
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

    private void backfillSmartFields() {
        List<RepairOrder> missingOrders = repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getIsDeleted, 0)
                .orderByDesc(RepairOrder::getId)
                .last("LIMIT 500"));
        for (RepairOrder order : missingOrders) {
            if (applySmartFields(order)) {
                repairOrderMapper.updateById(order);
            }
        }
    }

    private boolean applySmartFields(RepairOrder order) {
        if (order == null) {
            return false;
        }
        boolean changed = false;
        if (!StringUtils.hasText(order.getRemark()) || !order.getRemark().contains(MANUAL_PRIORITY_MARK)) {
            Integer calculatedPriority = repairGradingService.calculatePriority(order.getDescription(), order.getUserId());
            if (!Objects.equals(order.getPriority(), calculatedPriority)) {
                order.setPriority(calculatedPriority);
                changed = true;
            }
        }
        Long workerId = workerRecommendService.recommendWorker(recommendText(order));
        if (workerId != null && !Objects.equals(order.getSuggestedWorkerId(), workerId)) {
            order.setSuggestedWorkerId(workerId);
            changed = true;
        }
        if (order.getSlaDeadline() == null) {
            LocalDateTime base = order.getCreateTime() == null ? LocalDateTime.now() : order.getCreateTime();
            order.setSlaDeadline(slaDeadlineByPriority(base, order.getPriority()));
            changed = true;
        }
        if (changed) {
            order.setUpdateTime(LocalDateTime.now());
        }
        return changed;
    }

    // 将工单的多个文本字段拼成一段，给智能派单规则做关键词匹配。
    private String recommendText(RepairOrder order) {
        return Stream.of(order.getCategory(), order.getServiceMajor(), order.getServiceSubType(), order.getDescription())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
    }

    // SLA截止时间按优先级生成：紧急30分钟，普通2小时，低优先级24小时。
    private LocalDateTime slaDeadlineByPriority(LocalDateTime base, Integer priority) {
        return switch (priority == null ? 2 : priority) {
            case 1 -> base.plusMinutes(30);
            case 3 -> base.plusHours(24);
            default -> base.plusHours(2);
        };
    }

    private List<Map<String, Object>> enrichRows(List<RepairOrder> orders) {
        return orders.stream().map(this::enrichRow).toList();
    }

    private Map<String, Object> enrichRow(RepairOrder order) {
        Map<Long, User> users = userMap(collectRelatedUserIds(order));
        Map<Long, WorkerStaffing> staffing = workerStaffingMap(collectRelatedUserIds(order));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.getId());
        row.put("userId", order.getUserId());
        row.put("ownerName", displayName(users.get(order.getUserId()), "业主", order.getUserId()));
        row.put("category", order.getCategory());
        row.put("categoryText", categoryText(order));
        row.put("serviceMajor", order.getServiceMajor());
        row.put("serviceSubType", order.getServiceSubType());
        row.put("description", order.getDescription());
        row.put("priority", order.getPriority());
        row.put("priorityText", priorityText(order.getPriority()));
        row.put("status", order.getStatus());
        row.put("statusText", statusText(order.getStatus()));
        row.put("assignee", order.getAssignee());
        row.put("assigneeName", workerDisplayName(users.get(order.getAssignee()), staffing.get(order.getAssignee()), order.getAssignee()));
        row.put("suggestedWorkerId", order.getSuggestedWorkerId());
        row.put("suggestedWorkerName", workerDisplayName(users.get(order.getSuggestedWorkerId()), staffing.get(order.getSuggestedWorkerId()), order.getSuggestedWorkerId()));
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

    private Map<Long, WorkerStaffing> workerStaffingMap(Collection<Long> ids) {
        List<Long> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return Map.of();
        }
        return workerStaffingMapper.selectList(new LambdaQueryWrapper<WorkerStaffing>()
                        .in(WorkerStaffing::getWorkerId, validIds)
                        .eq(WorkerStaffing::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(WorkerStaffing::getWorkerId, item -> item, (a, b) -> a));
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
        Map<Long, WorkerStaffing> staffingMap = workerStaffingMap(participants.stream()
                .map(RepairOrderWorker::getWorkerId)
                .filter(Objects::nonNull)
                .toList());
        return participants.stream().map(item -> {
            Map<String, Object> row = new HashMap<>();
            row.put("workerId", item.getWorkerId());
            row.put("workerName", workerDisplayName(workerMap.get(item.getWorkerId()), staffingMap.get(item.getWorkerId()), item.getWorkerId()));
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

    private String workerDisplayName(User user, WorkerStaffing staffing, Long fallbackId) {
        if (fallbackId == null) {
            return "-";
        }
        String name = user != null && StringUtils.hasText(user.getNickname()) && containsChinese(user.getNickname())
                ? user.getNickname().trim()
                : workerAlias(fallbackId);
        return name + "-" + workerTrade(staffing);
    }

    private boolean containsChinese(String text) {
        return text != null && text.codePoints()
                .anyMatch(code -> Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN);
    }

    private String workerAlias(Long workerId) {
        if (workerId == null) {
            return "维修员";
        }
        return switch (workerId.intValue()) {
            case 20 -> "陈佳";
            case 21 -> "李娜";
            case 22 -> "王静";
            case 23 -> "赵敏";
            case 24 -> "周芳";
            case 25 -> "吴洁";
            case 26 -> "孙宁";
            case 27 -> "郑欣";
            case 28 -> "刘志国";
            case 29 -> "陈建华";
            case 30 -> "王立强";
            case 31 -> "赵明";
            default -> "维修员" + workerId;
        };
    }

    private String workerTrade(WorkerStaffing staffing) {
        if (staffing == null) {
            return "维修";
        }
        String position = staffing.getPosition() == null ? "" : staffing.getPosition().toLowerCase();
        String specialties = staffing.getSpecialties() == null ? "" : staffing.getSpecialties();
        if (position.contains("plumber") || specialties.contains("水管") || specialties.contains("下水")) {
            return "水工";
        }
        if (position.contains("electric") || specialties.contains("电路") || specialties.contains("灯具")) {
            return "电工";
        }
        if (position.contains("appliance") || specialties.contains("家电")) {
            return "家电维修";
        }
        if (position.contains("outsource") || specialties.contains("专项")) {
            return "综合维修";
        }
        if (position.contains("housekeeping") || specialties.contains("保洁")) {
            return "家政";
        }
        return "维修";
    }

    private String categoryText(RepairOrder order) {
        if (StringUtils.hasText(order.getCategory())) {
            return switch (order.getCategory().trim().toLowerCase()) {
                case "security" -> "安防门禁";
                case "plumbing" -> "管道疏通";
                case "elevator" -> "电梯设施";
                case "public" -> "公共设施";
                case "electrical", "electric" -> "电路照明";
                case "appliance" -> "家电维修";
                case "door" -> "门窗门锁";
                default -> order.getCategory();
            };
        }
        if (StringUtils.hasText(order.getServiceSubType())) {
            return order.getServiceSubType();
        }
        if (StringUtils.hasText(order.getServiceMajor())) {
            return order.getServiceMajor();
        }
        return "维修工单";
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
