package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AssignRepairReq;
import com.smartcommunity.dto.request.EvaluateRepairReq;
import com.smartcommunity.dto.request.SubmitRepairReq;
import com.smartcommunity.dto.request.UpdateRepairStatusReq;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.RepairFeeBill;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.WorkerStaffing;
import com.smartcommunity.mapper.RepairEvaluationMapper;
import com.smartcommunity.mapper.RepairFeeBillMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.WorkerStaffingMapper;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
    private final UserMapper userMapper;
    private final WorkerStaffingMapper workerStaffingMapper;
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
        order.setUserId(AuthContext.getUserId());
        order.setPropertyId(req.getPropertyId());
        Integer serviceType = virtualOrder.getServiceType();
        order.setServiceType(serviceType);
        order.setServiceMajor(serviceMajor);
        order.setServiceSubType(serviceSubType);
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
        order.setRemark("submitted");
        order.setOwnerFinishConfirmed(0);
        order.setOwnerFinishTime(null);
        order.setWorkerFinishConfirmed(0);
        order.setWorkerFinishTime(null);
        order.setCompletionTime(null);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
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
                                          @RequestParam(required = false) Long assignee) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        if (role != null && role == 1) {
            wrapper.eq(RepairOrder::getUserId, uid);
        }
        if (role != null && role == 3) {
            wrapper.eq(RepairOrder::getAssignee, uid);
        }
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (assignee != null) {
            wrapper.eq(RepairOrder::getAssignee, assignee);
        }
        wrapper.orderByDesc(RepairOrder::getId);
        return Result.success(repairOrderMapper.selectList(wrapper));
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
            List<Long> orderIds = repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                            .eq(RepairOrder::getAssignee, uid)
                            .eq(RepairOrder::getIsDeleted, 0))
                    .stream()
                    .map(RepairOrder::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
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

        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        boolean showVerifyCode = (role != null && role == 2)
                || (role != null && role == 1 && uid != null && uid.equals(order.getUserId()));

        String verifyCode = null;
        boolean verifyPassed = false;
        try {
            verifyCode = redisUtil.get(verifyCodeKey(id));
            verifyPassed = redisUtil.hasKey(verifyPassKey(id));
        } catch (Exception ignored) {
            // Allow running without redis in local dev.
        }

        Map<String, Object> data = new HashMap<>();
        boolean ownerFinishConfirmed = isConfirmed(order.getOwnerFinishConfirmed());
        boolean workerFinishConfirmed = isConfirmed(order.getWorkerFinishConfirmed());
        data.put("order", order);
        data.put("evaluation", eval);
        data.put("repairFeeBill", repairFeeBill);
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
                && uid.equals(order.getAssignee())
                && Integer.valueOf(STATUS_IN_SERVICE).equals(order.getStatus())
                && !workerFinishConfirmed);
        data.put("canEvaluate", role != null && role == 1
                && uid != null
                && uid.equals(order.getUserId())
                && Integer.valueOf(STATUS_WAIT_EVALUATE).equals(order.getStatus()));
        return Result.success(data);
    }

    @PostMapping("/assign")
    public Result<Map<String, Object>> assign(@RequestBody AssignRepairReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
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

        Long assigneeId = req.getAssignee();
        if (assigneeId == null) {
            assigneeId = pickAssignee(order);
            if (assigneeId == null) {
                return Result.fail(StatusCode.BAD_REQUEST, "no available worker for this service");
            }
        }

        User worker = userMapper.selectById(assigneeId);
        if (worker == null || worker.getRole() == null || worker.getRole() != 3) {
            return Result.fail(StatusCode.BAD_REQUEST, "assignee is not worker");
        }
        if (worker.getStatus() == null || worker.getStatus() != 1) {
            return Result.fail(StatusCode.BAD_REQUEST, "worker unavailable");
        }

        WorkerStaffing staffing = workerStaffingMapper.selectOne(new LambdaQueryWrapper<WorkerStaffing>()
                .eq(WorkerStaffing::getWorkerId, assigneeId)
                .eq(WorkerStaffing::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (staffing != null && Integer.valueOf(STAFF_TYPE_OUTSOURCE).equals(staffing.getStaffType())) {
            order.setNeedOutsource(1);
        }

        order.setAssignee(assigneeId);
        order.setAssignedTime(LocalDateTime.now());
        order.setStatus(STATUS_WAIT_DISPATCH);
        order.setRemark(StringUtils.hasText(req.getRemark()) ? req.getRemark().trim() : "assigned");
        order.setOwnerFinishConfirmed(0);
        order.setOwnerFinishTime(null);
        order.setWorkerFinishConfirmed(0);
        order.setWorkerFinishTime(null);
        order.setCompletionTime(null);
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);

        String verifyCode = randomVerifyCode();
        try {
            redisUtil.set(verifyCodeKey(order.getId()), verifyCode, VERIFY_CODE_EXPIRE_SECONDS);
            redisUtil.delete(verifyPassKey(order.getId()));
        } catch (Exception ignored) {
            // Allow running without redis in local dev.
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
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
        boolean isWorker = uid != null && uid.equals(order.getAssignee());

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
            boolean verified = false;
            try {
                verified = redisUtil.hasKey(verifyPassKey(order.getId()));
            } catch (Exception ignored) {
                // Allow running without redis in local dev.
            }
            if (role != null && role == 3 && !verified) {
                return Result.fail(StatusCode.BAD_REQUEST, "verify code not passed");
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
                if (req.getChargeAmount() == null) {
                    return Result.fail(StatusCode.BAD_REQUEST, "chargeAmount is required when worker completes order");
                }
                if (req.getChargeAmount().compareTo(BigDecimal.ZERO) < 0) {
                    return Result.fail(StatusCode.BAD_REQUEST, "chargeAmount can not be negative");
                }
                order.setChargeAmount(req.getChargeAmount());
                if (req.getChargeAmount().compareTo(BigDecimal.ZERO) > 0 && !StringUtils.hasText(req.getChargeRemark())) {
                    return Result.fail(StatusCode.BAD_REQUEST, "chargeRemark is required when chargeAmount > 0");
                }
                if (StringUtils.hasText(req.getChargeRemark())) {
                    order.setChargeRemark(req.getChargeRemark().trim());
                }
                order.setWorkerFinishConfirmed(1);
                if (order.getWorkerFinishTime() == null) {
                    order.setWorkerFinishTime(now);
                }
            } else if (role != null && role == 2) {
                // Admin fallback: force both confirmations for exceptional handling.
                order.setOwnerFinishConfirmed(1);
                order.setWorkerFinishConfirmed(1);
                if (order.getOwnerFinishTime() == null) {
                    order.setOwnerFinishTime(now);
                }
                if (order.getWorkerFinishTime() == null) {
                    order.setWorkerFinishTime(now);
                }
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
                createOrUpdateRepairFeeBill(order, now);
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

        RepairEvaluation current = repairEvaluationMapper.selectOne(new LambdaQueryWrapper<RepairEvaluation>()
                .eq(RepairEvaluation::getOrderId, id)
                .last("LIMIT 1"));
        RepairEvaluation evaluation = current == null ? new RepairEvaluation() : current;
        evaluation.setOrderId(id);
        evaluation.setRating(finalRating);
        evaluation.setComment(finalComment);
        evaluation.setIsAnonymous(finalAnonymous);
        evaluation.setUpdateTime(LocalDateTime.now());
        if (current == null) {
            evaluation.setCreateTime(LocalDateTime.now());
            evaluation.setIsDeleted(0);
            repairEvaluationMapper.insert(evaluation);
        } else {
            repairEvaluationMapper.updateById(evaluation);
        }

        order.setStatus(STATUS_COMPLETED);
        order.setUpdateTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        return Result.success(evaluation);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete");
        }
        repairOrderMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private boolean canViewOrder(RepairOrder order) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return false;
        }
        if (role == 2) {
            return true;
        }
        if (role == 1) {
            return uid.equals(order.getUserId());
        }
        return role == 3 && uid.equals(order.getAssignee());
    }

    private String verifyCodeKey(Long orderId) {
        return "repair:verify:code:" + orderId;
    }

    private String verifyPassKey(Long orderId) {
        return "repair:verify:pass:" + orderId;
    }

    private String randomVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private boolean isConfirmed(Integer value) {
        return value != null && value == 1;
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
        Long count = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getAssignee, workerId)
                .in(RepairOrder::getStatus, STATUS_WAIT_DISPATCH, STATUS_IN_SERVICE)
                .eq(RepairOrder::getIsDeleted, 0));
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
}
