package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AssignRepairReq;
import com.smartcommunity.dto.request.EvaluateRepairReq;
import com.smartcommunity.dto.request.RepairWorkerFeeItemReq;
import com.smartcommunity.dto.request.ReviewRepairFeeObjectionReq;
import com.smartcommunity.dto.request.RepairPriorityReq;
import com.smartcommunity.dto.request.SubmitRepairReq;
import com.smartcommunity.dto.request.SubmitRepairFeeObjectionReq;
import com.smartcommunity.dto.request.UpdateRepairStatusReq;
import com.smartcommunity.entity.PointsConsumptionRecord;
import com.smartcommunity.entity.PointsRechargeRecord;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.RepairFeeBill;
import com.smartcommunity.entity.RepairFeeDetail;
import com.smartcommunity.entity.RepairFeeObjection;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairOrderWorker;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.WorkerStaffing;
import com.smartcommunity.mapper.PointsConsumptionRecordMapper;
import com.smartcommunity.mapper.PointsRechargeRecordMapper;
import com.smartcommunity.mapper.RepairEvaluationMapper;
import com.smartcommunity.mapper.RepairFeeBillMapper;
import com.smartcommunity.mapper.RepairFeeDetailMapper;
import com.smartcommunity.mapper.RepairFeeObjectionMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairOrderWorkerMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.WorkerStaffingMapper;
import com.smartcommunity.service.RepairEvaluationService;
import com.smartcommunity.service.RepairGradingService;
import com.smartcommunity.service.SlaMonitorService;
import com.smartcommunity.service.WorkerRecommendService;
import com.smartcommunity.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {

    private static final int STATUS_WAIT_DISPATCH = 1;
    private static final int STATUS_IN_SERVICE = 2;
    private static final int STATUS_WAIT_EVALUATE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELED = 5;
    private static final int SERVICE_TYPE_REPAIR = 1;
    private static final int SERVICE_TYPE_HOUSEKEEPING = 2;
    private static final int STAFF_TYPE_FIXED_CLEAN = 1;
    private static final int STAFF_TYPE_PLUMBER = 2;
    private static final int STAFF_TYPE_ELECTRICIAN = 3;
    private static final int STAFF_TYPE_APPLIANCE = 4;
    private static final int STAFF_TYPE_OUTSOURCE = 5;
    private static final int STAFF_STATUS_IDLE = 1;
    private static final int ORDER_WORKER_ROLE_PRIMARY = 1;
    private static final int ORDER_WORKER_ROLE_COLLABORATOR = 2;
    private static final int OBJECTION_STATUS_PENDING = 0;
    private static final int OBJECTION_STATUS_REJECTED = 1;
    private static final int OBJECTION_STATUS_ACCEPTED = 2;
    private static final int POINTS_BUSINESS_REPAIR_FEE_OBJECTION = 4;
    private static final int DEFAULT_OBJECTION_DEPOSIT_POINTS = 20;

    private static final long VERIFY_CODE_EXPIRE_SECONDS = 4 * 60 * 60;
    private static final long VERIFY_PASS_EXPIRE_SECONDS = 24 * 60 * 60;
    private static final List<AppointmentSlotDef> APPOINTMENT_SLOT_DEFS = List.of(
            new AppointmentSlotDef("09:00-11:00", "09:00-11:00"),
            new AppointmentSlotDef("11:00-13:00", "11:00-13:00"),
            new AppointmentSlotDef("13:00-15:00", "13:00-15:00"),
            new AppointmentSlotDef("15:00-17:00", "15:00-17:00"),
            new AppointmentSlotDef("17:00-19:00", "17:00-19:00")
    );
    private static final Set<String> OUTSOURCE_MAJORS = Set.of("房屋结构", "家具维修", "智能设备", "其他", "专项服务", "养老护理");

    private final RepairOrderMapper repairOrderMapper;
    private final RepairEvaluationMapper repairEvaluationMapper;
    private final RepairFeeBillMapper repairFeeBillMapper;
    private final RepairFeeDetailMapper repairFeeDetailMapper;
    private final RepairFeeObjectionMapper repairFeeObjectionMapper;
    private final RepairOrderWorkerMapper repairOrderWorkerMapper;
    private final PointsConsumptionRecordMapper pointsConsumptionRecordMapper;
    private final PointsRechargeRecordMapper pointsRechargeRecordMapper;
    private final UserMapper userMapper;
    private final WorkerStaffingMapper workerStaffingMapper;
    private final RepairEvaluationService repairEvaluationService;
    private final RepairGradingService repairGradingService;
    private final SlaMonitorService slaMonitorService;
    private final WorkerRecommendService workerRecommendService;
    private final RedisUtil redisUtil;

    @PostMapping("/submit")
    public Result<RepairOrder> submit(@RequestBody SubmitRepairReq req) {
        String serviceMajor = StringUtils.hasText(req.getServiceMajor()) ? req.getServiceMajor().trim() : "";
        String serviceSubType = StringUtils.hasText(req.getServiceSubType()) ? req.getServiceSubType().trim() : "";
        String category = StringUtils.hasText(req.getCategory()) ? req.getCategory().trim() : "";
        if (!StringUtils.hasText(serviceSubType) && StringUtils.hasText(category)) {
            serviceSubType = category;
        }
        if (!StringUtils.hasText(category) && StringUtils.hasText(serviceSubType)) {
            category = serviceSubType;
        }
        if (!StringUtils.hasText(category) || !StringUtils.hasText(req.getDescription())) {
            return Result.fail(StatusCode.BAD_REQUEST, "service type or description is empty");
        }
        if (!StringUtils.hasText(serviceMajor)) {
            return Result.fail(StatusCode.BAD_REQUEST, "serviceMajor is empty");
        }
        LocalDate appointmentDate = parseAppointmentDate(req.getAppointmentDate());
        if (appointmentDate == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "appointmentDate is invalid, required yyyy-MM-dd");
        }
        String appointmentTimeSlot = StringUtils.hasText(req.getAppointmentTimeSlot()) ? req.getAppointmentTimeSlot().trim() : "";
        if (!isValidAppointmentSlot(appointmentTimeSlot)) {
            return Result.fail(StatusCode.BAD_REQUEST, "appointmentTimeSlot is invalid");
        }
        RepairOrder virtualOrder = new RepairOrder();
        virtualOrder.setServiceType(normalizeServiceType(req.getServiceType(), serviceMajor));
        virtualOrder.setServiceMajor(serviceMajor);
        virtualOrder.setServiceSubType(serviceSubType);
        virtualOrder.setCategory(serviceSubType);
        Map<String, Integer> slotAvailability = availableWorkerCountBySlot(virtualOrder, appointmentDate);
        if (slotAvailability.getOrDefault(appointmentTimeSlot, 0) <= 0) {
            return Result.fail(StatusCode.BAD_REQUEST, "selected appointment time slot has no available worker");
        }
        RepairOrder order = new RepairOrder();
        Long currentUserId = AuthContext.getUserId();
        order.setUserId(currentUserId);
        order.setPropertyId(req.getPropertyId());
        Integer serviceType = virtualOrder.getServiceType();
        order.setServiceType(serviceType);
        order.setServiceMajor(serviceMajor);
        order.setServiceSubType(serviceSubType);
        order.setFacilityId(req.getFacilityId());
        order.setFacilityName(StringUtils.hasText(req.getFacilityName()) ? req.getFacilityName().trim() : null);
        order.setAppointmentDate(appointmentDate);
        order.setAppointmentTimeSlot(appointmentTimeSlot);
        order.setCategory(category);
        order.setDescription(req.getDescription().trim());
        order.setImages(StringUtils.hasText(req.getImages()) ? req.getImages() : "[]");
        order.setBeforeImages(null);
        order.setAfterImages(null);
        order.setChargeAmount(BigDecimal.ZERO);
        order.setChargeRemark(null);
        order.setNeedOutsource(isOutsourceMajor(serviceMajor) ? 1 : 0);
        order.setStatus(STATUS_WAIT_DISPATCH);
        Integer priority = repairGradingService.calculatePriority(req.getDescription(), currentUserId);
        order.setPriority(priority);
        order.setSuggestedWorkerId(pickAssignee(order));
        LocalDateTime now = LocalDateTime.now();
        order.setSlaDeadline(dispatchSlaDeadline(now));
        order.setDelayCount(0);
        order.setRemark("submitted");
        order.setOwnerFinishConfirmed(0);
        order.setOwnerFinishTime(null);
        order.setWorkerFinishConfirmed(0);
        order.setWorkerFinishTime(null);
        order.setCompletionTime(null);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(0);
        repairOrderMapper.insert(order);
        return Result.success(order);
    }

    @GetMapping("/available-slots")
    public Result<Map<String, Object>> availableSlots(@RequestParam(required = false) Integer serviceType,
                                                      @RequestParam(required = false) String serviceMajor,
                                                      @RequestParam(required = false) String serviceSubType,
                                                      @RequestParam String appointmentDate) {
        String normalizedMajor = StringUtils.hasText(serviceMajor) ? serviceMajor.trim() : "";
        String normalizedSubType = StringUtils.hasText(serviceSubType) ? serviceSubType.trim() : "";
        if (!StringUtils.hasText(normalizedMajor)) {
            return Result.fail(StatusCode.BAD_REQUEST, "serviceMajor is empty");
        }
        LocalDate targetDate = parseAppointmentDate(appointmentDate);
        if (targetDate == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "appointmentDate is invalid, required yyyy-MM-dd");
        }

        RepairOrder virtualOrder = new RepairOrder();
        Integer normalizedType = normalizeServiceType(serviceType, normalizedMajor);
        virtualOrder.setServiceType(normalizedType);
        virtualOrder.setServiceMajor(normalizedMajor);
        virtualOrder.setServiceSubType(normalizedSubType);
        virtualOrder.setCategory(normalizedSubType);

        List<ScoredWorker> scoredWorkers = collectScoredWorkers(virtualOrder);
        Map<String, Integer> availabilityMap = availableWorkerCountBySlot(virtualOrder, targetDate);

        List<Map<String, Object>> slots = new ArrayList<>();
        int totalCandidateCount = scoredWorkers.size();
        for (AppointmentSlotDef slotDef : APPOINTMENT_SLOT_DEFS) {
            int availableCount = availabilityMap.getOrDefault(slotDef.code(), 0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", slotDef.code());
            row.put("label", slotDef.label());
            row.put("available", availableCount > 0);
            row.put("availableCount", availableCount);
            slots.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("appointmentDate", targetDate.toString());
        data.put("slots", slots);
        data.put("candidateWorkerCount", totalCandidateCount);
        return Result.success(data);
    }

    @GetMapping("/list")
    public Result<List<RepairOrder>> list(@RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) Long assignee,
                                          @RequestParam(required = false) Integer priority) {
        LambdaQueryWrapper<RepairOrder> wrapper = buildRepairListWrapper(status, assignee, priority, null);
        wrapper.last("ORDER BY CASE WHEN priority IS NULL THEN 1 ELSE 0 END, priority ASC, id DESC");
        return Result.success(repairOrderMapper.selectList(wrapper));
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Long assignee,
                                            @RequestParam(required = false) Integer priority,
                                            @RequestParam(required = false) String keyword) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        LambdaQueryWrapper<RepairOrder> wrapper = buildRepairListWrapper(status, assignee, priority, keyword);
        wrapper.last("ORDER BY CASE WHEN priority IS NULL THEN 1 ELSE 0 END, priority ASC, id DESC");
        Page<RepairOrder> result = repairOrderMapper.selectPage(new Page<>(safePageNum, safePageSize), wrapper);
        Map<String, Object> payload = new HashMap<>();
        payload.put("records", repairOrderRows(result.getRecords()));
        payload.put("total", result.getTotal());
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    @GetMapping("/fee-bills")
    public Result<List<RepairFeeBill>> feeBills(@RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        LambdaQueryWrapper<RepairFeeBill> wrapper = new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(RepairFeeBill::getStatus, status);
        }
        if (role == 1) {
            wrapper.eq(RepairFeeBill::getUserId, uid);
        } else if (role == 3) {
            Set<Long> orderIdSet = new HashSet<>(repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                            .eq(RepairOrder::getAssignee, uid)
                            .eq(RepairOrder::getIsDeleted, 0))
                    .stream()
                    .map(RepairOrder::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
            orderIdSet.addAll(participantOrderIds(uid));
            List<Long> orderIds = new ArrayList<>(orderIdSet);
            if (orderIds.isEmpty()) {
                return Result.success(List.of());
            }
            wrapper.in(RepairFeeBill::getOrderId, orderIds);
        }
        wrapper.orderByDesc(RepairFeeBill::getId);
        return Result.success(repairFeeBillMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (!canViewOrder(order)) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }

        RepairEvaluation eval = repairEvaluationMapper.selectOne(new LambdaQueryWrapper<RepairEvaluation>()
                .eq(RepairEvaluation::getOrderId, id)
                .last("LIMIT 1"));
        RepairFeeBill repairFeeBill = repairFeeBillMapper.selectOne(new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getOrderId, id)
                .eq(RepairFeeBill::getIsDeleted, 0)
                .last("limit 1"));
        List<RepairFeeDetail> feeDetails = repairFeeDetailMapper.selectList(new LambdaQueryWrapper<RepairFeeDetail>()
                .eq(RepairFeeDetail::getOrderId, id)
                .eq(RepairFeeDetail::getIsDeleted, 0)
                .orderByAsc(RepairFeeDetail::getWorkerId));
        List<RepairFeeObjection> objections = repairFeeObjectionMapper.selectList(new LambdaQueryWrapper<RepairFeeObjection>()
                .eq(RepairFeeObjection::getOrderId, id)
                .eq(RepairFeeObjection::getIsDeleted, 0)
                .orderByDesc(RepairFeeObjection::getId));

        List<RepairOrderWorker> participants = activeParticipants(order);
        Map<Long, RepairOrderWorker> participantMap = participants.stream()
                .filter(item -> item.getWorkerId() != null)
                .collect(Collectors.toMap(RepairOrderWorker::getWorkerId, item -> item, (a, b) -> a));
        Map<Long, User> workerMap = participantUserMap(participants);

        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        ensureSuggestedWorker(order, role);
        boolean showVerifyCode = RoleUtils.isPropertyAdmin(role)
                || (role != null && role == 1 && uid != null && uid.equals(order.getUserId()));

        String verifyCode = null;
        boolean verifyPassed = allParticipantsVerified(participants);
        try {
            verifyCode = redisUtil.get(verifyCodeKey(id));
        } catch (Exception ignored) {
            // Allow running without redis in local dev.
        }

        Map<String, Object> data = new HashMap<>();
        boolean ownerFinishConfirmed = isConfirmed(order.getOwnerFinishConfirmed());
        boolean workerFinishConfirmed = allParticipantsFinished(participants);
        List<Map<String, Object>> participantViews = new ArrayList<>();
        List<Long> pendingWorkerIds = new ArrayList<>();
        for (RepairOrderWorker participant : participants) {
            if (!isConfirmed(participant.getVerifyPassed()) && participant.getWorkerId() != null) {
                pendingWorkerIds.add(participant.getWorkerId());
            }
            User worker = workerMap.get(participant.getWorkerId());
            Map<String, Object> participantView = new LinkedHashMap<>();
            participantView.put("workerId", participant.getWorkerId());
            participantView.put("workerName", worker == null ? null : worker.getNickname());
            participantView.put("workerPhone", worker == null ? null : worker.getPhone());
            participantView.put("roleType", participant.getRoleType());
            participantView.put("verifyPassed", isConfirmed(participant.getVerifyPassed()));
            participantView.put("verifyPassTime", participant.getVerifyPassTime());
            participantView.put("finishConfirmed", isConfirmed(participant.getFinishConfirmed()));
            participantView.put("finishTime", participant.getFinishTime());
            participantViews.add(participantView);
        }

        RepairOrderWorker currentWorkerParticipant = uid == null ? null : participantMap.get(uid);
        data.put("order", order);
        Map<Long, User> relatedUserMap = userMap(java.util.Arrays.asList(order.getUserId(), order.getAssignee(), order.getSuggestedWorkerId()));
        data.put("ownerName", displayName(relatedUserMap.get(order.getUserId()), "业主", order.getUserId()));
        data.put("assigneeName", displayName(relatedUserMap.get(order.getAssignee()), "维修员", order.getAssignee()));
        data.put("suggestedWorkerName", displayName(relatedUserMap.get(order.getSuggestedWorkerId()), "维修员", order.getSuggestedWorkerId()));
        data.put("evaluation", eval);
        data.put("repairFeeBill", repairFeeBill);
        data.put("repairFeeDetails", feeDetails);
        data.put("feeObjections", objections);
        data.put("participants", participantViews);
        data.put("pendingWorkerIds", pendingWorkerIds);
        data.put("statusText", statusText(order.getStatus()));
        data.put("verifyCode", showVerifyCode ? verifyCode : null);
        data.put("verifyPassed", verifyPassed);
        data.put("ownerFinishConfirmed", ownerFinishConfirmed);
        data.put("workerFinishConfirmed", workerFinishConfirmed);
        data.put("ownerFinishTime", order.getOwnerFinishTime());
        data.put("workerFinishTime", order.getWorkerFinishTime());
        data.put("canOwnerFinish", role != null
                && role == 1
                && uid != null
                && uid.equals(order.getUserId())
                && Integer.valueOf(STATUS_IN_SERVICE).equals(order.getStatus())
                && !ownerFinishConfirmed);
        data.put("canWorkerFinish", role != null
                && role == 3
                && uid != null
                && currentWorkerParticipant != null
                && Integer.valueOf(STATUS_IN_SERVICE).equals(order.getStatus())
                && !isConfirmed(currentWorkerParticipant.getFinishConfirmed()));
        data.put("canEvaluate", role != null && role == 1
                && uid != null
                && uid.equals(order.getUserId())
                && Integer.valueOf(STATUS_WAIT_EVALUATE).equals(order.getStatus()));
        return Result.success(data);
    }

    @PostMapping("/{id}/priority")
    public Result<RepairOrder> updatePriority(@PathVariable Long id, @RequestBody RepairPriorityReq req) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can update priority");
        }
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (req != null && req.getPriority() != null) {
            order.setPriority(req.getPriority());
        }
        if (req != null && req.getSuggestedWorkerId() != null) {
            order.setSuggestedWorkerId(req.getSuggestedWorkerId());
        }
        if (req != null && req.getSlaDeadline() != null) {
            order.setSlaDeadline(req.getSlaDeadline());
        }
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        return Result.success(order);
    }

    @PostMapping("/sla/scan")
    public Result<Map<String, Object>> scanSla() {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can scan sla");
        }
        int delayCount = slaMonitorService.scanAndUrge();
        return Result.success(Map.of("delayCount", delayCount));
    }

    @PostMapping("/autoAssign/{orderId}")
    @Transactional
    public Result<Map<String, Object>> autoAssign(@PathVariable Long orderId) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can auto assign");
        }
        if (orderId == null || orderId <= 0) {
            return Result.fail(StatusCode.BAD_REQUEST, "orderId is invalid");
        }

        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (!Integer.valueOf(STATUS_WAIT_DISPATCH).equals(order.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "only waiting orders can be auto assigned");
        }
        if (!StringUtils.hasText(order.getCategory())) {
            return Result.fail(StatusCode.BAD_REQUEST, "category is empty");
        }

        Long workerId = workerRecommendService.recommendWorker(order.getCategory());
        if (workerId == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "暂无可用维修员");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setAssignee(workerId);
        order.setSuggestedWorkerId(workerId);
        order.setAssignedTime(now);
        order.setStatus(STATUS_IN_SERVICE);
        order.setSlaDeadline(completionSlaDeadline(now));
        order.setRemark("auto assigned");
        order.setOwnerFinishConfirmed(0);
        order.setOwnerFinishTime(null);
        order.setWorkerFinishConfirmed(0);
        order.setWorkerFinishTime(null);
        order.setServiceStartTime(null);
        order.setServiceEndTime(null);
        order.setServiceDurationMinutes(0);
        order.setCompletionTime(null);
        order.setUpdateTime(now);
        repairOrderMapper.updateById(order);
        resetParticipants(order.getId(), List.of(workerId));

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("workerId", workerId);
        data.put("statusText", statusText(order.getStatus()));
        return Result.success(data);
    }

    @PostMapping("/assign")
    @Transactional
    public Result<Map<String, Object>> assign(@RequestBody AssignRepairReq req) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can assign");
        }
        if (req.getOrderId() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "orderId is empty");
        }

        RepairOrder order = repairOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (Integer.valueOf(STATUS_COMPLETED).equals(order.getStatus())
                || Integer.valueOf(STATUS_CANCELED).equals(order.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "order already closed");
        }
        if (!Integer.valueOf(STATUS_WAIT_DISPATCH).equals(order.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "only waiting orders can be assigned");
        }

        List<Long> assigneeIds = new ArrayList<>();
        if (req.getAssignees() != null && !req.getAssignees().isEmpty()) {
            assigneeIds.addAll(req.getAssignees().stream()
                    .filter(Objects::nonNull)
                    .filter(id -> id > 0)
                    .distinct()
                    .collect(Collectors.toList()));
        } else if (req.getAssignee() != null) {
            assigneeIds.add(req.getAssignee());
        } else {
            Long autoAssignee = pickAssignee(order);
            if (autoAssignee != null) {
                assigneeIds.add(autoAssignee);
            }
        }
        if (assigneeIds.isEmpty()) {
            return Result.fail(StatusCode.BAD_REQUEST, "no available worker for this service");
        }

        Map<Long, User> workerMap = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, assigneeIds)
                        .eq(User::getIsDeleted, 0))
                .stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
        for (Long assigneeId : assigneeIds) {
            User worker = workerMap.get(assigneeId);
            if (worker == null || worker.getRole() == null || worker.getRole() != 3) {
                return Result.fail(StatusCode.BAD_REQUEST, "assignee is not worker: " + assigneeId);
            }
            if (worker.getStatus() == null || worker.getStatus() != 1) {
                return Result.fail(StatusCode.BAD_REQUEST, "worker unavailable: " + assigneeId);
            }
        }

        boolean needOutsource = false;
        List<WorkerStaffing> staffingList = workerStaffingMapper.selectList(new LambdaQueryWrapper<WorkerStaffing>()
                .in(WorkerStaffing::getWorkerId, assigneeIds)
                .eq(WorkerStaffing::getIsDeleted, 0));
        for (WorkerStaffing staffing : staffingList) {
            if (Integer.valueOf(STAFF_TYPE_OUTSOURCE).equals(staffing.getStaffType())) {
                needOutsource = true;
                break;
            }
        }
        if (needOutsource) {
            order.setNeedOutsource(1);
        }

        List<RepairOrderWorker> oldParticipants = activeParticipants(order);

        LocalDateTime now = LocalDateTime.now();
        order.setAssignee(assigneeIds.get(0));
        order.setSuggestedWorkerId(assigneeIds.get(0));
        order.setAssignedTime(now);
        order.setStatus(STATUS_IN_SERVICE);
        order.setSlaDeadline(completionSlaDeadline(now));
        order.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : "assigned");
        order.setOwnerFinishConfirmed(0);
        order.setOwnerFinishTime(null);
        order.setWorkerFinishConfirmed(0);
        order.setWorkerFinishTime(null);
        order.setServiceStartTime(null);
        order.setServiceEndTime(null);
        order.setServiceDurationMinutes(0);
        order.setCompletionTime(null);
        order.setUpdateTime(now);
        repairOrderMapper.updateById(order);
        resetParticipants(order.getId(), assigneeIds);
        clearFeeDetails(order.getId());

        String verifyCode = randomVerifyCode();
        try {
            redisUtil.set(verifyCodeKey(order.getId()), verifyCode, VERIFY_CODE_EXPIRE_SECONDS);
            redisUtil.delete(verifyPassKey(order.getId()));
            for (RepairOrderWorker participant : oldParticipants) {
                if (participant.getWorkerId() != null) {
                    redisUtil.delete(verifyPassKey(order.getId(), participant.getWorkerId()));
                }
            }
            for (Long assigneeId : assigneeIds) {
                redisUtil.delete(verifyPassKey(order.getId(), assigneeId));
            }
        } catch (Exception ignored) {
            // Allow running without redis in local dev.
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("assignees", assigneeIds);
        data.put("verifyCode", verifyCode);
        data.put("statusText", statusText(order.getStatus()));
        return Result.success(data);
    }

    @GetMapping("/{id}/verify-code")
    public Result<Map<String, Object>> getVerifyCode(@PathVariable Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (!canViewOrder(order)) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        if (order.getAssignee() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "order not assigned");
        }

        String verifyCode;
        try {
            verifyCode = redisUtil.get(verifyCodeKey(id));
            if (!StringUtils.hasText(verifyCode)) {
                verifyCode = randomVerifyCode();
                redisUtil.set(verifyCodeKey(id), verifyCode, VERIFY_CODE_EXPIRE_SECONDS);
            }
        } catch (Exception ignored) {
            verifyCode = randomVerifyCode();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", id);
        data.put("verifyCode", verifyCode);
        data.put("statusText", statusText(order.getStatus()));
        return Result.success(data);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/status")
    public Result<RepairOrder> updateStatus(@RequestBody UpdateRepairStatusReq req) {
        if (req.getOrderId() == null || req.getStatus() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "orderId or status is empty");
        }
        RepairOrder order = repairOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }

        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        Integer target = req.getStatus();
        boolean isOwner = uid != null && uid.equals(order.getUserId());
        List<RepairOrderWorker> participants = activeParticipants(order);
        RepairOrderWorker currentParticipant = uid == null ? null : findParticipant(participants, uid);
        boolean isWorker = currentParticipant != null;

        if (Integer.valueOf(STATUS_COMPLETED).equals(order.getStatus())
                || Integer.valueOf(STATUS_CANCELED).equals(order.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "order already closed");
        }

        if (role != null && role == 3) {
            if (!isWorker) {
                return Result.fail(StatusCode.FORBIDDEN, "not your order");
            }
            if (Integer.valueOf(STATUS_CANCELED).equals(target)) {
                return Result.fail(StatusCode.FORBIDDEN, "worker can not cancel order");
            }
        } else if (role != null && role == 1) {
            if (!isOwner) {
                return Result.fail(StatusCode.FORBIDDEN, "forbidden");
            }
            boolean isCancel = Integer.valueOf(STATUS_CANCELED).equals(target);
            boolean isOwnerFinish = Integer.valueOf(STATUS_WAIT_EVALUATE).equals(target);
            if (!isCancel && !isOwnerFinish) {
                return Result.fail(StatusCode.FORBIDDEN, "owner can only confirm finish or cancel");
            }
        }

        if (Integer.valueOf(STATUS_IN_SERVICE).equals(target)) {
            if (Integer.valueOf(STATUS_WAIT_EVALUATE).equals(order.getStatus())) {
                return Result.fail(StatusCode.BAD_REQUEST, "order already waiting evaluation");
            }
            if (role != null && role == 3 && !allParticipantsVerified(participants)) {
                List<Long> pendingWorkerIds = participants.stream()
                        .filter(item -> !isConfirmed(item.getVerifyPassed()))
                        .map(RepairOrderWorker::getWorkerId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return Result.fail(StatusCode.BAD_REQUEST, "verify not completed for workers: " + pendingWorkerIds);
            }
            if (role != null && role == 3) {
                if (!StringUtils.hasText(req.getBeforeImages()) && !StringUtils.hasText(order.getBeforeImages())) {
                    return Result.fail(StatusCode.BAD_REQUEST, "beforeImages is required before service");
                }
                if (StringUtils.hasText(req.getBeforeImages())) {
                    order.setBeforeImages(req.getBeforeImages().trim());
                }
            }
            order.setStatus(STATUS_IN_SERVICE);
            order.setOwnerFinishConfirmed(0);
            order.setOwnerFinishTime(null);
            order.setWorkerFinishConfirmed(0);
            order.setWorkerFinishTime(null);
            resetParticipantFinishStatus(participants);
            if (order.getServiceStartTime() == null) {
                order.setServiceStartTime(LocalDateTime.now());
            }
            order.setServiceEndTime(null);
            order.setServiceDurationMinutes(0);
            order.setCompletionTime(null);
        } else if (Integer.valueOf(STATUS_WAIT_EVALUATE).equals(target)) {
            if (!Integer.valueOf(STATUS_IN_SERVICE).equals(order.getStatus())) {
                return Result.fail(StatusCode.BAD_REQUEST, "order not in service");
            }
            LocalDateTime now = LocalDateTime.now();
            if (role != null && role == 1) {
                order.setOwnerFinishConfirmed(1);
                if (order.getOwnerFinishTime() == null) {
                    order.setOwnerFinishTime(now);
                }
            } else if (role != null && role == 3) {
                if (!StringUtils.hasText(req.getAfterImages()) && StringUtils.hasText(req.getCompletionImages())) {
                    req.setAfterImages(req.getCompletionImages());
                }
                if (StringUtils.hasText(req.getAfterImages())) {
                    order.setAfterImages(req.getAfterImages().trim());
                }
                String feeError = saveWorkerFeeFromStatusRequest(order, uid, req);
                if (feeError != null) {
                    return Result.fail(StatusCode.BAD_REQUEST, feeError);
                }
                markParticipantFinish(currentParticipant, now);
                participants = activeParticipants(order);
                boolean allWorkersFinished = allParticipantsFinished(participants);
                order.setWorkerFinishConfirmed(allWorkersFinished ? 1 : 0);
                if (allWorkersFinished) {
                    if (!StringUtils.hasText(order.getAfterImages())) {
                        return Result.fail(StatusCode.BAD_REQUEST, "afterImages is required when all workers finish");
                    }
                    if (order.getWorkerFinishTime() == null) {
                        order.setWorkerFinishTime(now);
                    }
                    if (order.getServiceEndTime() == null) {
                        order.setServiceEndTime(now);
                    }
                    if (order.getServiceStartTime() != null && order.getServiceEndTime() != null) {
                        long minutes = java.time.Duration.between(order.getServiceStartTime(), order.getServiceEndTime()).toMinutes();
                        order.setServiceDurationMinutes((int) Math.max(minutes, 0));
                    }
                    applyAggregateFeeToOrder(order);
                    createOrUpdateRepairFeeBill(order, now);
                }
            } else if (RoleUtils.isPropertyAdmin(role)) {
                // Admin fallback: force both confirmations for exceptional handling.
                order.setOwnerFinishConfirmed(1);
                order.setWorkerFinishConfirmed(1);
                if (order.getOwnerFinishTime() == null) {
                    order.setOwnerFinishTime(now);
                }
                if (order.getWorkerFinishTime() == null) {
                    order.setWorkerFinishTime(now);
                }
                if (StringUtils.hasText(req.getAfterImages())) {
                    order.setAfterImages(req.getAfterImages().trim());
                }
                markAllParticipantsFinished(participants, now);
                if (order.getServiceEndTime() == null) {
                    order.setServiceEndTime(now);
                }
                if (order.getServiceStartTime() != null && order.getServiceEndTime() != null) {
                    long minutes = java.time.Duration.between(order.getServiceStartTime(), order.getServiceEndTime()).toMinutes();
                    order.setServiceDurationMinutes((int) Math.max(minutes, 0));
                }
                applyAggregateFeeToOrder(order);
                createOrUpdateRepairFeeBill(order, now);
            }

            if (isConfirmed(order.getOwnerFinishConfirmed()) && isConfirmed(order.getWorkerFinishConfirmed())) {
                if (!StringUtils.hasText(order.getBeforeImages()) || !StringUtils.hasText(order.getAfterImages())) {
                    return Result.fail(StatusCode.BAD_REQUEST, "beforeImages and afterImages are required to complete");
                }
                if (Integer.valueOf(1).equals(order.getNeedOutsource())) {
                    if (order.getChargeAmount() == null || order.getChargeAmount().compareTo(BigDecimal.ZERO) < 0) {
                        return Result.fail(StatusCode.BAD_REQUEST, "chargeAmount is required for outsource order");
                    }
                    if (!StringUtils.hasText(order.getChargeRemark())) {
                        return Result.fail(StatusCode.BAD_REQUEST, "chargeRemark is required for outsource order");
                    }
                }
                order.setStatus(STATUS_WAIT_EVALUATE);
                if (order.getCompletionTime() == null) {
                    order.setCompletionTime(now);
                }
                if (order.getSlaDeadline() != null && now.isAfter(order.getSlaDeadline())) {
                    order.setDelayCount((order.getDelayCount() == null ? 0 : order.getDelayCount()) + 1);
                }
            } else {
                order.setStatus(STATUS_IN_SERVICE);
            }
        } else if (Integer.valueOf(STATUS_CANCELED).equals(target)) {
            order.setStatus(STATUS_CANCELED);
        } else {
            return Result.fail(StatusCode.BAD_REQUEST, "unsupported status transition");
        }

        order.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : order.getRemark());
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        return Result.success(order);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{id}/evaluate")
    public Result<RepairEvaluation> evaluate(@PathVariable Long id,
                                             @RequestBody(required = false) EvaluateRepairReq req,
                                             @RequestParam(required = false) Integer rating,
                                             @RequestParam(required = false) String comment,
                                             @RequestParam(required = false) Integer anonymous) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }

        Long uid = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (role == null || role != 1 || uid == null || !uid.equals(order.getUserId())) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can evaluate");
        }
        if (!Integer.valueOf(STATUS_WAIT_EVALUATE).equals(order.getStatus())
                && !Integer.valueOf(STATUS_COMPLETED).equals(order.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "order not ready for evaluation");
        }

        Integer finalRating = req != null && req.getRating() != null ? req.getRating() : rating;
        String finalComment = req != null && req.getComment() != null ? req.getComment() : comment;
        Integer finalAnonymous = req != null && req.getAnonymous() != null ? req.getAnonymous() : anonymous;

        if (finalRating == null || finalRating < 1 || finalRating > 5) {
            return Result.fail(StatusCode.BAD_REQUEST, "rating must be 1-5");
        }
        if (finalAnonymous == null) {
            finalAnonymous = 0;
        }

        RepairEvaluation evaluation = repairEvaluationService.saveEvaluation(order, finalRating, finalComment, finalAnonymous);
        return Result.success(evaluation);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{id}/fee-objection")
    public Result<RepairFeeObjection> submitFeeObjection(@PathVariable Long id,
                                                         @RequestBody(required = false) SubmitRepairFeeObjectionReq req) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        Long uid = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (role == null || role != 1 || uid == null || !uid.equals(order.getUserId())) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can submit objection");
        }
        RepairFeeBill bill = repairFeeBillMapper.selectOne(new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getOrderId, id)
                .eq(RepairFeeBill::getIsDeleted, 0)
                .last("limit 1"));
        if (bill == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "repair fee bill not ready");
        }
        Long pendingCount = repairFeeObjectionMapper.selectCount(new LambdaQueryWrapper<RepairFeeObjection>()
                .eq(RepairFeeObjection::getOrderId, id)
                .eq(RepairFeeObjection::getStatus, OBJECTION_STATUS_PENDING)
                .eq(RepairFeeObjection::getIsDeleted, 0));
        if (pendingCount != null && pendingCount > 0) {
            return Result.fail(StatusCode.BAD_REQUEST, "existing pending objection");
        }
        int depositPoints = req == null || req.getDepositPoints() == null
                ? DEFAULT_OBJECTION_DEPOSIT_POINTS
                : req.getDepositPoints();
        if (depositPoints <= 0) {
            return Result.fail(StatusCode.BAD_REQUEST, "depositPoints must be greater than 0");
        }
        String reason = req == null ? null : req.getReason();
        if (!StringUtils.hasText(reason)) {
            return Result.fail(StatusCode.BAD_REQUEST, "reason is required");
        }

        User owner = lockUser(uid);
        int before = owner.getPoints() == null ? 0 : owner.getPoints();
        if (before < depositPoints) {
            return Result.fail(StatusCode.BAD_REQUEST, "insufficient points for objection deposit");
        }
        owner.setPoints(before - depositPoints);
        owner.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(owner);

        LocalDateTime now = LocalDateTime.now();
        RepairFeeObjection objection = new RepairFeeObjection();
        objection.setOrderId(id);
        objection.setBillId(bill.getId());
        objection.setUserId(uid);
        objection.setDepositPoints(depositPoints);
        objection.setReason(reason.trim());
        objection.setStatus(OBJECTION_STATUS_PENDING);
        objection.setResolutionRemark(null);
        objection.setResolverId(null);
        objection.setRefundPoints(0);
        objection.setResolveTime(null);
        objection.setCreateTime(now);
        objection.setUpdateTime(now);
        objection.setIsDeleted(0);
        repairFeeObjectionMapper.insert(objection);

        PointsConsumptionRecord consume = new PointsConsumptionRecord();
        consume.setUserId(uid);
        consume.setBusinessType(POINTS_BUSINESS_REPAIR_FEE_OBJECTION);
        consume.setBusinessId(objection.getId());
        consume.setPoints(depositPoints);
        consume.setBeforePoints(before);
        consume.setAfterPoints(before - depositPoints);
        consume.setCreateTime(now);
        pointsConsumptionRecordMapper.insert(consume);

        return Result.success(objection);
    }

    @GetMapping("/fee-objection/list")
    public Result<List<Map<String, Object>>> feeObjectionList(@RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query objections");
        }
        LambdaQueryWrapper<RepairFeeObjection> wrapper = new LambdaQueryWrapper<RepairFeeObjection>()
                .eq(RepairFeeObjection::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(RepairFeeObjection::getStatus, status);
        }
        wrapper.orderByDesc(RepairFeeObjection::getId);
        List<RepairFeeObjection> objections = repairFeeObjectionMapper.selectList(wrapper);
        if (objections.isEmpty()) {
            return Result.success(List.of());
        }

        Set<Long> orderIds = objections.stream().map(RepairFeeObjection::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> billIds = objections.stream().map(RepairFeeObjection::getBillId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = objections.stream().flatMap(item -> java.util.stream.Stream.of(item.getUserId(), item.getResolverId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, RepairOrder> orderMap = orderIds.isEmpty() ? Map.of() : repairOrderMapper.selectBatchIds(orderIds).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(RepairOrder::getId, item -> item, (a, b) -> a));
        Map<Long, RepairFeeBill> billMap = billIds.isEmpty() ? Map.of() : repairFeeBillMapper.selectBatchIds(billIds).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(RepairFeeBill::getId, item -> item, (a, b) -> a));
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() : userMapper.selectBatchIds(userIds).stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (RepairFeeObjection item : objections) {
            RepairOrder order = item.getOrderId() == null ? null : orderMap.get(item.getOrderId());
            RepairFeeBill bill = item.getBillId() == null ? null : billMap.get(item.getBillId());
            User owner = item.getUserId() == null ? null : userMap.get(item.getUserId());
            User resolver = item.getResolverId() == null ? null : userMap.get(item.getResolverId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("orderId", item.getOrderId());
            row.put("orderStatus", order == null ? null : order.getStatus());
            row.put("orderCategory", order == null ? null : order.getCategory());
            row.put("billId", item.getBillId());
            row.put("billAmount", bill == null ? null : bill.getAmount());
            row.put("billNeedPoints", bill == null ? null : bill.getNeedPoints());
            row.put("ownerId", item.getUserId());
            row.put("ownerName", owner == null ? null : owner.getNickname());
            row.put("depositPoints", item.getDepositPoints());
            row.put("reason", item.getReason());
            row.put("status", item.getStatus());
            row.put("statusText", objectionStatusText(item.getStatus()));
            row.put("resolutionRemark", item.getResolutionRemark());
            row.put("resolverId", item.getResolverId());
            row.put("resolverName", resolver == null ? null : resolver.getNickname());
            row.put("refundPoints", item.getRefundPoints());
            row.put("resolveTime", item.getResolveTime());
            row.put("createTime", item.getCreateTime());
            result.add(row);
        }
        return Result.success(result);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/fee-objection/{objectionId}/review")
    public Result<Map<String, Object>> reviewFeeObjection(@PathVariable Long objectionId,
                                                          @RequestBody ReviewRepairFeeObjectionReq req) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (!RoleUtils.isPropertyAdmin(role) || uid == null) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can review objection");
        }
        if (req == null || req.getStatus() == null
                || (!Integer.valueOf(OBJECTION_STATUS_REJECTED).equals(req.getStatus())
                && !Integer.valueOf(OBJECTION_STATUS_ACCEPTED).equals(req.getStatus()))) {
            return Result.fail(StatusCode.BAD_REQUEST, "status must be 1(reject) or 2(accept)");
        }
        RepairFeeObjection objection = repairFeeObjectionMapper.selectOne(new LambdaQueryWrapper<RepairFeeObjection>()
                .eq(RepairFeeObjection::getId, objectionId)
                .eq(RepairFeeObjection::getIsDeleted, 0)
                .last("for update"));
        if (objection == null) {
            return Result.fail(StatusCode.NOT_FOUND, "objection not found");
        }
        if (!Integer.valueOf(OBJECTION_STATUS_PENDING).equals(objection.getStatus())) {
            return Result.fail(StatusCode.BAD_REQUEST, "objection already reviewed");
        }

        int refundPoints = 0;
        LocalDateTime now = LocalDateTime.now();
        RepairFeeBill bill = repairFeeBillMapper.selectById(objection.getBillId());
        if (Integer.valueOf(OBJECTION_STATUS_ACCEPTED).equals(req.getStatus())) {
            refundPoints += Math.max(objection.getDepositPoints() == null ? 0 : objection.getDepositPoints(), 0);
            if (bill != null && req.getAdjustedAmount() != null && req.getAdjustedAmount().compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal adjusted = req.getAdjustedAmount().setScale(2, java.math.RoundingMode.HALF_UP);
                int oldNeedPoints = bill.getNeedPoints() == null ? amountToPoints(bill.getAmount()) : bill.getNeedPoints();
                int newNeedPoints = amountToPoints(adjusted);
                int paidPoints = bill.getPaidPoints() == null ? 0 : bill.getPaidPoints();
                if (paidPoints > newNeedPoints) {
                    refundPoints += (paidPoints - newNeedPoints);
                    bill.setPaidPoints(newNeedPoints);
                }
                bill.setAmount(adjusted);
                bill.setNeedPoints(newNeedPoints);
                bill.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : bill.getRemark());
                if (newNeedPoints <= 0) {
                    bill.setStatus(1);
                    bill.setPaymentTime(bill.getPaymentTime() == null ? now : bill.getPaymentTime());
                    bill.setTransactionId(StringUtils.hasText(bill.getTransactionId())
                            ? bill.getTransactionId()
                            : "FREE_REPAIR_" + bill.getOrderId());
                } else if (Integer.valueOf(1).equals(bill.getStatus()) && paidPoints > newNeedPoints) {
                    bill.setStatus(1);
                } else if (Integer.valueOf(1).equals(bill.getStatus()) && paidPoints < newNeedPoints) {
                    bill.setStatus(0);
                }
                bill.setUpdateTime(now);
                repairFeeBillMapper.updateById(bill);
                if (oldNeedPoints < newNeedPoints) {
                    refundPoints = Math.max(refundPoints - (newNeedPoints - oldNeedPoints), 0);
                }
            }
            refundPoints += Math.max(req.getRefundPoints() == null ? 0 : req.getRefundPoints(), 0);
            if (refundPoints > 0) {
                User owner = lockUser(objection.getUserId());
                int before = owner.getPoints() == null ? 0 : owner.getPoints();
                owner.setPoints(before + refundPoints);
                owner.setUpdateTime(now);
                userMapper.updateById(owner);

                PointsRechargeRecord recharge = new PointsRechargeRecord();
                recharge.setUserId(owner.getId());
                recharge.setOperatorId(uid);
                recharge.setAmount(refundPoints);
                recharge.setBeforePoints(before);
                recharge.setAfterPoints(before + refundPoints);
                recharge.setRemark("repair fee objection refund #" + objection.getId());
                recharge.setCreateTime(now);
                pointsRechargeRecordMapper.insert(recharge);
            }
        }

        objection.setStatus(req.getStatus());
        objection.setResolutionRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : null);
        objection.setResolverId(uid);
        objection.setRefundPoints(refundPoints);
        objection.setResolveTime(now);
        objection.setUpdateTime(now);
        repairFeeObjectionMapper.updateById(objection);

        Map<String, Object> data = new HashMap<>();
        data.put("objection", objection);
        data.put("bill", bill);
        data.put("refundPoints", refundPoints);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete");
        }
        repairOrderMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private LambdaQueryWrapper<RepairOrder> buildRepairListWrapper(Integer status,
                                                                   Long assignee,
                                                                   Integer priority,
                                                                   String keyword) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getIsDeleted, 0);
        if (role != null && role == 1) {
            wrapper.eq(RepairOrder::getUserId, uid);
        }
        if (role != null && role == 3) {
            List<Long> orderIds = participantOrderIds(uid);
            if (orderIds.isEmpty()) {
                wrapper.eq(RepairOrder::getAssignee, uid);
            } else {
                wrapper.and(w -> w.eq(RepairOrder::getAssignee, uid).or().in(RepairOrder::getId, orderIds));
            }
        }
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (assignee != null) {
            List<Long> assignedOrderIds = participantOrderIds(assignee);
            if (assignedOrderIds.isEmpty()) {
                wrapper.eq(RepairOrder::getAssignee, assignee);
            } else {
                wrapper.and(w -> w.eq(RepairOrder::getAssignee, assignee).or().in(RepairOrder::getId, assignedOrderIds));
            }
        }
        if (priority != null) {
            wrapper.eq(RepairOrder::getPriority, priority);
        }
        applyRepairKeyword(wrapper, keyword);
        return wrapper;
    }

    private List<Map<String, Object>> repairOrderRows(List<RepairOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = new HashSet<>();
        for (RepairOrder order : orders) {
            if (order.getUserId() != null) {
                userIds.add(order.getUserId());
            }
            if (order.getAssignee() != null) {
                userIds.add(order.getAssignee());
            }
            if (order.getSuggestedWorkerId() != null) {
                userIds.add(order.getSuggestedWorkerId());
            }
        }
        Map<Long, User> users = userMap(userIds);
        return orders.stream()
                .map(order -> repairOrderRow(order, users))
                .toList();
    }

    private Map<String, Object> repairOrderRow(RepairOrder order, Map<Long, User> users) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", order.getId());
        row.put("userId", order.getUserId());
        row.put("ownerName", displayName(users.get(order.getUserId()), "业主", order.getUserId()));
        row.put("propertyId", order.getPropertyId());
        row.put("serviceType", order.getServiceType());
        row.put("serviceMajor", order.getServiceMajor());
        row.put("serviceSubType", order.getServiceSubType());
        row.put("facilityId", order.getFacilityId());
        row.put("facilityName", order.getFacilityName());
        row.put("appointmentDate", order.getAppointmentDate());
        row.put("appointmentTimeSlot", order.getAppointmentTimeSlot());
        row.put("category", order.getCategory());
        row.put("description", order.getDescription());
        row.put("images", order.getImages());
        row.put("status", order.getStatus());
        row.put("assignee", order.getAssignee());
        row.put("assigneeName", displayName(users.get(order.getAssignee()), "维修员", order.getAssignee()));
        row.put("priority", order.getPriority());
        row.put("suggestedWorkerId", order.getSuggestedWorkerId());
        row.put("suggestedWorkerName", displayName(users.get(order.getSuggestedWorkerId()), "维修员", order.getSuggestedWorkerId()));
        row.put("assignedTime", order.getAssignedTime());
        row.put("completionTime", order.getCompletionTime());
        row.put("slaDeadline", order.getSlaDeadline());
        row.put("delayCount", order.getDelayCount());
        row.put("createTime", order.getCreateTime());
        row.put("updateTime", order.getUpdateTime());
        return row;
    }

    private Map<Long, User> userMap(Collection<Long> userIds) {
        if (userIds == null) {
            return Map.of();
        }
        List<Long> ids = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, ids)
                        .eq(User::getIsDeleted, 0))
                .stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
    }

    private String displayName(User user, String prefix, Long id) {
        if (user != null) {
            if (StringUtils.hasText(user.getNickname())) {
                return user.getNickname();
            }
            if (StringUtils.hasText(user.getPhone())) {
                return user.getPhone();
            }
        }
        return id == null ? "-" : prefix + id;
    }

    private void applyRepairKeyword(LambdaQueryWrapper<RepairOrder> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String text = keyword.trim();
        Long orderId = parseLong(text);
        List<Long> ownerIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getRole, 1)
                        .eq(User::getIsDeleted, 0)
                        .and(w -> w.like(User::getNickname, text).or().like(User::getPhone, text)))
                .stream()
                .map(User::getId)
                .filter(Objects::nonNull)
                .limit(100)
                .toList();

        wrapper.and(w -> {
            w.like(RepairOrder::getDescription, text)
                    .or()
                    .like(RepairOrder::getCategory, text);
            if (orderId != null) {
                w.or().eq(RepairOrder::getId, orderId);
            }
            if (!ownerIds.isEmpty()) {
                w.or().in(RepairOrder::getUserId, ownerIds);
            }
        });
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean canViewOrder(RepairOrder order) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return false;
        }
        if (RoleUtils.isPropertyAdmin(role)) {
            return true;
        }
        if (role == 1) {
            return uid.equals(order.getUserId());
        }
        if (role == 3 && uid.equals(order.getAssignee())) {
            return true;
        }
        if (role == 3) {
            return participantOrderIds(uid).contains(order.getId());
        }
        return false;
    }

    private String verifyCodeKey(Long orderId) {
        return "repair:verify:code:" + orderId;
    }

    private String verifyPassKey(Long orderId) {
        return "repair:verify:pass:" + orderId;
    }

    private String verifyPassKey(Long orderId, Long workerId) {
        return "repair:verify:pass:" + orderId + ":" + workerId;
    }

    private String randomVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private boolean isConfirmed(Integer value) {
        return value != null && value == 1;
    }

    private List<Long> participantOrderIds(Long workerId) {
        if (workerId == null) {
            return List.of();
        }
        return repairOrderWorkerMapper.selectList(new LambdaQueryWrapper<RepairOrderWorker>()
                        .eq(RepairOrderWorker::getWorkerId, workerId)
                        .eq(RepairOrderWorker::getIsDeleted, 0))
                .stream()
                .map(RepairOrderWorker::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<RepairOrderWorker> activeParticipants(RepairOrder order) {
        if (order == null || order.getId() == null) {
            return List.of();
        }
        List<RepairOrderWorker> participants = repairOrderWorkerMapper.selectList(new LambdaQueryWrapper<RepairOrderWorker>()
                .eq(RepairOrderWorker::getOrderId, order.getId())
                .eq(RepairOrderWorker::getIsDeleted, 0)
                .orderByAsc(RepairOrderWorker::getRoleType)
                .orderByAsc(RepairOrderWorker::getWorkerId));
        if (!participants.isEmpty()) {
            return participants;
        }
        if (order.getAssignee() == null) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        RepairOrderWorker fallback = new RepairOrderWorker();
        fallback.setOrderId(order.getId());
        fallback.setWorkerId(order.getAssignee());
        fallback.setRoleType(ORDER_WORKER_ROLE_PRIMARY);
        fallback.setVerifyPassed(0);
        fallback.setVerifyPassTime(null);
        fallback.setFinishConfirmed(isConfirmed(order.getWorkerFinishConfirmed()) ? 1 : 0);
        fallback.setFinishTime(order.getWorkerFinishTime());
        fallback.setCreateTime(now);
        fallback.setUpdateTime(now);
        fallback.setIsDeleted(0);
        repairOrderWorkerMapper.insert(fallback);
        return List.of(fallback);
    }

    private List<RepairOrderWorker> activeParticipants(Long orderId) {
        if (orderId == null) {
            return List.of();
        }
        return repairOrderWorkerMapper.selectList(new LambdaQueryWrapper<RepairOrderWorker>()
                .eq(RepairOrderWorker::getOrderId, orderId)
                .eq(RepairOrderWorker::getIsDeleted, 0)
                .orderByAsc(RepairOrderWorker::getRoleType)
                .orderByAsc(RepairOrderWorker::getWorkerId));
    }

    private Map<Long, User> participantUserMap(List<RepairOrderWorker> participants) {
        if (participants == null || participants.isEmpty()) {
            return Map.of();
        }
        List<Long> userIds = participants.stream()
                .map(RepairOrderWorker::getWorkerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds))
                .stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));
    }

    private RepairOrderWorker findParticipant(List<RepairOrderWorker> participants, Long workerId) {
        if (participants == null || participants.isEmpty() || workerId == null) {
            return null;
        }
        for (RepairOrderWorker participant : participants) {
            if (workerId.equals(participant.getWorkerId())) {
                return participant;
            }
        }
        return null;
    }

    private boolean allParticipantsVerified(List<RepairOrderWorker> participants) {
        return participants != null
                && !participants.isEmpty()
                && participants.stream().allMatch(item -> isConfirmed(item.getVerifyPassed()));
    }

    private boolean allParticipantsFinished(List<RepairOrderWorker> participants) {
        return participants != null
                && !participants.isEmpty()
                && participants.stream().allMatch(item -> isConfirmed(item.getFinishConfirmed()));
    }

    private void resetParticipants(Long orderId, List<Long> assigneeIds) {
        if (orderId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        repairOrderWorkerMapper.delete(new LambdaQueryWrapper<RepairOrderWorker>()
                .eq(RepairOrderWorker::getOrderId, orderId));
        AtomicInteger index = new AtomicInteger(0);
        for (Long workerId : assigneeIds) {
            if (workerId == null) {
                continue;
            }
            RepairOrderWorker participant = new RepairOrderWorker();
            participant.setOrderId(orderId);
            participant.setWorkerId(workerId);
            participant.setRoleType(index.getAndIncrement() == 0 ? ORDER_WORKER_ROLE_PRIMARY : ORDER_WORKER_ROLE_COLLABORATOR);
            participant.setVerifyPassed(0);
            participant.setVerifyPassTime(null);
            participant.setFinishConfirmed(0);
            participant.setFinishTime(null);
            participant.setCreateTime(now);
            participant.setUpdateTime(now);
            participant.setIsDeleted(0);
            repairOrderWorkerMapper.insert(participant);
        }
    }

    private void clearFeeDetails(Long orderId) {
        repairFeeDetailMapper.delete(new LambdaQueryWrapper<RepairFeeDetail>()
                .eq(RepairFeeDetail::getOrderId, orderId));
    }

    private void markParticipantFinish(RepairOrderWorker participant, LocalDateTime now) {
        if (participant == null) {
            return;
        }
        participant.setFinishConfirmed(1);
        if (participant.getFinishTime() == null) {
            participant.setFinishTime(now);
        }
        participant.setUpdateTime(now);
        repairOrderWorkerMapper.updateById(participant);
    }

    private void markAllParticipantsFinished(List<RepairOrderWorker> participants, LocalDateTime now) {
        if (participants == null || participants.isEmpty()) {
            return;
        }
        for (RepairOrderWorker participant : participants) {
            participant.setFinishConfirmed(1);
            if (participant.getFinishTime() == null) {
                participant.setFinishTime(now);
            }
            participant.setUpdateTime(now);
            repairOrderWorkerMapper.updateById(participant);
        }
    }

    private void resetParticipantFinishStatus(List<RepairOrderWorker> participants) {
        if (participants == null || participants.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (RepairOrderWorker participant : participants) {
            participant.setFinishConfirmed(0);
            participant.setFinishTime(null);
            participant.setUpdateTime(now);
            repairOrderWorkerMapper.updateById(participant);
        }
    }

    private String saveWorkerFeeFromStatusRequest(RepairOrder order, Long workerId, UpdateRepairStatusReq req) {
        if (order == null || order.getId() == null || workerId == null) {
            return "order or worker is invalid";
        }
        RepairWorkerFeeItemReq feeReq = null;
        if (req.getWorkerFees() != null && !req.getWorkerFees().isEmpty()) {
            for (RepairWorkerFeeItemReq item : req.getWorkerFees()) {
                if (item == null) {
                    continue;
                }
                Long targetWorkerId = item.getWorkerId() == null ? workerId : item.getWorkerId();
                if (!workerId.equals(targetWorkerId)) {
                    return "worker can only submit own fee details";
                }
                item.setWorkerId(targetWorkerId);
                feeReq = item;
                break;
            }
        }
        if (feeReq == null) {
            feeReq = new RepairWorkerFeeItemReq();
            feeReq.setWorkerId(workerId);
            feeReq.setTechFee(req.getChargeAmount());
            feeReq.setMaterialFee(BigDecimal.ZERO);
            feeReq.setHighAltitudeFee(BigDecimal.ZERO);
            feeReq.setOtherFee(BigDecimal.ZERO);
            feeReq.setRemark(req.getChargeRemark());
        }
        BigDecimal techFee = safeAmount(feeReq.getTechFee());
        BigDecimal materialFee = safeAmount(feeReq.getMaterialFee());
        BigDecimal highAltitudeFee = safeAmount(feeReq.getHighAltitudeFee());
        BigDecimal otherFee = safeAmount(feeReq.getOtherFee());
        if (techFee.compareTo(BigDecimal.ZERO) < 0
                || materialFee.compareTo(BigDecimal.ZERO) < 0
                || highAltitudeFee.compareTo(BigDecimal.ZERO) < 0
                || otherFee.compareTo(BigDecimal.ZERO) < 0) {
            return "fee items can not be negative";
        }
        BigDecimal total = techFee.add(materialFee).add(highAltitudeFee).add(otherFee)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        RepairFeeDetail detail = repairFeeDetailMapper.selectOne(new LambdaQueryWrapper<RepairFeeDetail>()
                .eq(RepairFeeDetail::getOrderId, order.getId())
                .eq(RepairFeeDetail::getWorkerId, workerId)
                .eq(RepairFeeDetail::getIsDeleted, 0)
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (detail == null) {
            detail = new RepairFeeDetail();
            detail.setOrderId(order.getId());
            detail.setWorkerId(workerId);
            detail.setCreateTime(now);
            detail.setIsDeleted(0);
        }
        detail.setTechFee(techFee);
        detail.setMaterialFee(materialFee);
        detail.setHighAltitudeFee(highAltitudeFee);
        detail.setOtherFee(otherFee);
        detail.setTotalAmount(total);
        detail.setRemark(StringUtils.hasText(feeReq.getRemark()) ? feeReq.getRemark().trim() : null);
        detail.setUpdateTime(now);
        if (detail.getId() == null) {
            repairFeeDetailMapper.insert(detail);
        } else {
            repairFeeDetailMapper.updateById(detail);
        }
        return null;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void applyAggregateFeeToOrder(RepairOrder order) {
        List<RepairFeeDetail> feeDetails = repairFeeDetailMapper.selectList(new LambdaQueryWrapper<RepairFeeDetail>()
                .eq(RepairFeeDetail::getOrderId, order.getId())
                .eq(RepairFeeDetail::getIsDeleted, 0));
        if (feeDetails.isEmpty()) {
            order.setChargeAmount(BigDecimal.ZERO);
            order.setChargeRemark(null);
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        List<String> remarks = new ArrayList<>();
        for (RepairFeeDetail detail : feeDetails) {
            BigDecimal part = detail.getTotalAmount() == null ? BigDecimal.ZERO : detail.getTotalAmount();
            total = total.add(part);
            if (StringUtils.hasText(detail.getRemark())) {
                remarks.add("W" + detail.getWorkerId() + ":" + detail.getRemark().trim());
            }
        }
        order.setChargeAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        order.setChargeRemark(remarks.isEmpty() ? "multi-worker fee details" : String.join(" | ", remarks));
    }

    private User lockUser(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .last("for update"));
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return user;
    }

    private Integer normalizeServiceType(Integer serviceType, String serviceMajor) {
        if (serviceType != null && (serviceType == SERVICE_TYPE_REPAIR || serviceType == SERVICE_TYPE_HOUSEKEEPING)) {
            return serviceType;
        }
        return isHousekeepingMajor(serviceMajor) ? SERVICE_TYPE_HOUSEKEEPING : SERVICE_TYPE_REPAIR;
    }

    private boolean isHousekeepingMajor(String serviceMajor) {
        return "日常保洁".equals(serviceMajor)
                || "家电清洗".equals(serviceMajor)
                || "养老护理".equals(serviceMajor)
                || "专项服务".equals(serviceMajor);
    }

    private boolean isOutsourceMajor(String serviceMajor) {
        return OUTSOURCE_MAJORS.contains(serviceMajor);
    }

    private Long pickAssignee(RepairOrder order) {
        return collectScoredWorkers(order).stream()
                .sorted(Comparator.comparingInt(ScoredWorker::score).reversed()
                        .thenComparingInt(ScoredWorker::load)
                        .thenComparingLong(ScoredWorker::workerId))
                .findFirst()
                .map(ScoredWorker::workerId)
                .orElse(null);
    }

    private List<ScoredWorker> collectScoredWorkers(RepairOrder order) {
        List<User> activeWorkers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 3)
                .eq(User::getStatus, 1)
                .eq(User::getIsDeleted, 0));
        if (activeWorkers.isEmpty()) {
            return List.of();
        }
        Map<Long, User> workerMap = activeWorkers.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        if (workerMap.isEmpty()) {
            return List.of();
        }

        List<WorkerStaffing> staffingList = workerStaffingMapper.selectList(new LambdaQueryWrapper<WorkerStaffing>()
                .in(WorkerStaffing::getWorkerId, workerMap.keySet())
                .eq(WorkerStaffing::getIsDeleted, 0)
                .eq(WorkerStaffing::getCurrentStatus, STAFF_STATUS_IDLE));
        if (staffingList.isEmpty()) {
            return List.of();
        }

        List<ScoredWorker> scored = new ArrayList<>();
        for (WorkerStaffing staffing : staffingList) {
            if (staffing.getWorkerId() == null || !workerMap.containsKey(staffing.getWorkerId())) {
                continue;
            }
            int load = currentOpenLoad(staffing.getWorkerId());
            int maxDaily = staffing.getMaxDailyOrders() == null ? 5 : staffing.getMaxDailyOrders();
            if (maxDaily > 0 && load >= maxDaily) {
                continue;
            }
            int score = computeMatchScore(order, staffing);
            scored.add(new ScoredWorker(staffing.getWorkerId(), score, load));
        }
        return scored;
    }

    private Map<String, Integer> availableWorkerCountBySlot(RepairOrder order, LocalDate appointmentDate) {
        Map<String, Integer> availability = new HashMap<>();
        for (AppointmentSlotDef slotDef : APPOINTMENT_SLOT_DEFS) {
            availability.put(slotDef.code(), 0);
        }
        if (appointmentDate == null) {
            return availability;
        }

        List<ScoredWorker> scoredWorkers = collectScoredWorkers(order);
        if (scoredWorkers.isEmpty()) {
            return availability;
        }
        Set<Long> candidateWorkerIds = scoredWorkers.stream()
                .map(ScoredWorker::workerId)
                .collect(Collectors.toSet());
        int totalCandidateCount = candidateWorkerIds.size();
        if (totalCandidateCount == 0) {
            return availability;
        }

        Map<String, Set<Long>> busyBySlot = new HashMap<>();
        List<RepairOrder> busyOrders = repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getAppointmentDate, appointmentDate)
                .in(RepairOrder::getStatus, STATUS_WAIT_DISPATCH, STATUS_IN_SERVICE, STATUS_WAIT_EVALUATE)
                .in(RepairOrder::getAssignee, candidateWorkerIds)
                .isNotNull(RepairOrder::getAppointmentTimeSlot)
                .eq(RepairOrder::getIsDeleted, 0));
        for (RepairOrder busyOrder : busyOrders) {
            if (busyOrder.getAssignee() == null || !StringUtils.hasText(busyOrder.getAppointmentTimeSlot())) {
                continue;
            }
            busyBySlot.computeIfAbsent(busyOrder.getAppointmentTimeSlot(), k -> new HashSet<>()).add(busyOrder.getAssignee());
        }

        for (AppointmentSlotDef slotDef : APPOINTMENT_SLOT_DEFS) {
            int busyCount = busyBySlot.getOrDefault(slotDef.code(), Set.of()).size();
            availability.put(slotDef.code(), Math.max(totalCandidateCount - busyCount, 0));
        }
        return availability;
    }

    private int currentOpenLoad(Long workerId) {
        Set<Long> participantOrderIds = new HashSet<>(participantOrderIds(workerId));
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .in(RepairOrder::getStatus, STATUS_WAIT_DISPATCH, STATUS_IN_SERVICE)
                .eq(RepairOrder::getIsDeleted, 0);
        if (participantOrderIds.isEmpty()) {
            wrapper.eq(RepairOrder::getAssignee, workerId);
        } else {
            wrapper.and(w -> w.eq(RepairOrder::getAssignee, workerId).or().in(RepairOrder::getId, participantOrderIds));
        }
        Long count = repairOrderMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    private int computeMatchScore(RepairOrder order, WorkerStaffing staffing) {
        int score = 0;
        Integer staffType = staffing.getStaffType();
        if (staffType != null) {
            if (matchesStaffType(order, staffType)) {
                score += 25;
            } else {
                score -= 10;
            }
        }
        String specialties = normalize(staffing.getSpecialties());
        String major = normalize(order.getServiceMajor());
        String subType = normalize(order.getServiceSubType());
        String category = normalize(order.getCategory());
        if (StringUtils.hasText(specialties)) {
            if (containsToken(specialties, subType)) {
                score += 80;
            }
            if (containsToken(specialties, major)) {
                score += 60;
            }
            if (containsToken(specialties, category)) {
                score += 40;
            }
        }
        score += certificateMatchScore(order, staffing);
        if (isOutsourceMajor(order.getServiceMajor()) && Objects.equals(STAFF_TYPE_OUTSOURCE, staffType)) {
            score += 30;
        }
        return score;
    }

    private int certificateMatchScore(RepairOrder order, WorkerStaffing staffing) {
        Set<String> required = requiredCertificateTokens(order);
        if (required.isEmpty()) {
            return 0;
        }
        String certificates = normalize(staffing.getCertificates());
        int score = 0;
        for (String token : required) {
            if (containsToken(certificates, token)) {
                score += 35;
            } else {
                score -= 20;
            }
        }
        return score;
    }

    private Set<String> requiredCertificateTokens(RepairOrder order) {
        Set<String> required = new HashSet<>();
        String major = normalize(order.getServiceMajor());
        String subType = normalize(order.getServiceSubType());
        String category = normalize(order.getCategory());

        if (order.getServiceType() != null && order.getServiceType() == SERVICE_TYPE_HOUSEKEEPING) {
            if (containsAnyToken(major, "养老护理", "nurse", "care")
                    || containsAnyToken(subType, "护理", "陪护", "助浴", "康复", "nurse", "caregiver")) {
                required.add("护理证");
                required.add("caregiver");
            } else {
                required.add("家政服务证");
                required.add("housekeeping");
                required.add("cleaning");
            }
            if (isOutsourceMajor(order.getServiceMajor())) {
                required.add("外包资质");
                required.add("outsource");
            }
            return required;
        }

        if (containsAnyToken(major, "水电维修", "plumbing", "electrical")
                || containsAnyToken(category, "plumbing", "electrical", "water", "power")) {
            boolean electricCase = containsAnyToken(subType, "电", "线路", "插座", "灯", "跳闸", "electric", "power");
            boolean plumbingCase = containsAnyToken(subType, "水", "管", "马桶", "下水", "漏", "热水", "plumber", "water", "drain");
            if (electricCase) {
                required.add("电工证");
                required.add("electrician");
            }
            if (plumbingCase) {
                required.add("水工证");
                required.add("plumber");
            }
            if (!electricCase && !plumbingCase) {
                required.add("electrician");
                required.add("plumber");
            }
        }

        if (containsAnyToken(major, "家电维修", "appliance")) {
            required.add("家电维修证");
            required.add("appliance");
        }

        if (isOutsourceMajor(order.getServiceMajor())) {
            required.add("外包资质");
            required.add("outsource");
        }
        return required;
    }

    private boolean matchesStaffType(RepairOrder order, Integer staffType) {
        if (order.getServiceType() != null && order.getServiceType() == SERVICE_TYPE_HOUSEKEEPING) {
            return Objects.equals(staffType, STAFF_TYPE_FIXED_CLEAN) || Objects.equals(staffType, STAFF_TYPE_OUTSOURCE);
        }
        String major = order.getServiceMajor();
        if ("水电维修".equals(major)) {
            return Objects.equals(staffType, STAFF_TYPE_PLUMBER)
                    || Objects.equals(staffType, STAFF_TYPE_ELECTRICIAN)
                    || Objects.equals(staffType, STAFF_TYPE_OUTSOURCE);
        }
        if ("家电维修".equals(major)) {
            return Objects.equals(staffType, STAFF_TYPE_APPLIANCE) || Objects.equals(staffType, STAFF_TYPE_OUTSOURCE);
        }
        if ("日常保洁".equals(major) || "家电清洗".equals(major)) {
            return Objects.equals(staffType, STAFF_TYPE_FIXED_CLEAN) || Objects.equals(staffType, STAFF_TYPE_OUTSOURCE);
        }
        return true;
    }

    private String normalize(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.replace(" ", "").toLowerCase();
    }

    private boolean containsToken(String source, String token) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(token)) {
            return false;
        }
        return source.contains(token.replace(" ", "").toLowerCase());
    }

    private boolean containsAnyToken(String source, String... tokens) {
        if (!StringUtils.hasText(source) || tokens == null || tokens.length == 0) {
            return false;
        }
        for (String token : tokens) {
            if (containsToken(source, token)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate parseAppointmentDate(String dateText) {
        if (!StringUtils.hasText(dateText)) {
            return null;
        }
        try {
            return LocalDate.parse(dateText.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private boolean isValidAppointmentSlot(String slotCode) {
        if (!StringUtils.hasText(slotCode)) {
            return false;
        }
        return APPOINTMENT_SLOT_DEFS.stream().anyMatch(slot -> slot.code().equals(slotCode));
    }

    private void ensureSuggestedWorker(RepairOrder order, Integer role) {
        if (order == null || !RoleUtils.isPropertyAdmin(role)) {
            return;
        }
        if (!Integer.valueOf(STATUS_WAIT_DISPATCH).equals(order.getStatus())
                || order.getSuggestedWorkerId() != null
                || !StringUtils.hasText(order.getCategory())) {
            return;
        }
        Long workerId = workerRecommendService.recommendWorker(order.getCategory());
        if (workerId == null) {
            return;
        }
        order.setSuggestedWorkerId(workerId);
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
    }

    private LocalDateTime dispatchSlaDeadline(LocalDateTime baseTime) {
        LocalDateTime base = baseTime == null ? LocalDateTime.now() : baseTime;
        return base.plusMinutes(30);
    }

    private LocalDateTime completionSlaDeadline(LocalDateTime baseTime) {
        LocalDateTime base = baseTime == null ? LocalDateTime.now() : baseTime;
        return base.plusHours(2);
    }

    private record AppointmentSlotDef(String code, String label) {
    }

    private record ScoredWorker(Long workerId, int score, int load) {
    }

    private void createOrUpdateRepairFeeBill(RepairOrder order, LocalDateTime now) {
        BigDecimal amount = order.getChargeAmount() == null ? BigDecimal.ZERO : order.getChargeAmount();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        RepairFeeBill bill = repairFeeBillMapper.selectOne(new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getOrderId, order.getId())
                .eq(RepairFeeBill::getIsDeleted, 0)
                .last("limit 1"));
        int needPoints = amountToPoints(amount);
        if (bill == null && needPoints <= 0) {
            return;
        }
        if (bill == null) {
            bill = new RepairFeeBill();
            bill.setOrderId(order.getId());
            bill.setUserId(order.getUserId());
            bill.setPropertyId(order.getPropertyId());
            bill.setCreateTime(now);
            bill.setIsDeleted(0);
        }

        if (bill.getStatus() != null && bill.getStatus() == 1) {
            return;
        }

        bill.setAmount(amount.setScale(2, java.math.RoundingMode.HALF_UP));
        bill.setNeedPoints(needPoints);
        bill.setRemark(order.getChargeRemark());
        bill.setUpdateTime(now);
        bill.setDueDate(now.plusDays(7));
        if (needPoints <= 0) {
            bill.setStatus(1);
            bill.setPaidPoints(0);
            bill.setPaymentTime(now);
            bill.setTransactionId("FREE_REPAIR_" + order.getId());
        } else {
            bill.setStatus(0);
            bill.setPaidPoints(0);
            bill.setPaymentTime(null);
            bill.setTransactionId(null);
        }

        if (bill.getId() == null) {
            repairFeeBillMapper.insert(bill);
        } else {
            repairFeeBillMapper.updateById(bill);
        }
    }

    private int amountToPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return amount.setScale(0, java.math.RoundingMode.UP).intValue();
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "unknown";
        }
        return switch (status) {
            case STATUS_WAIT_DISPATCH -> "待派单/待上门验证";
            case STATUS_IN_SERVICE -> "服务中";
            case STATUS_WAIT_EVALUATE -> "待评价";
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "unknown";
        };
    }

    private String objectionStatusText(Integer status) {
        if (status == null) {
            return "unknown";
        }
        return switch (status) {
            case OBJECTION_STATUS_PENDING -> "pending";
            case OBJECTION_STATUS_REJECTED -> "rejected";
            case OBJECTION_STATUS_ACCEPTED -> "accepted";
            default -> "unknown";
        };
    }
}
