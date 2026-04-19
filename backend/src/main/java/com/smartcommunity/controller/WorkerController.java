package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AddWorkerReq;
import com.smartcommunity.dto.request.VerifyCodeReq;
import com.smartcommunity.dto.request.WorkerStaffingReq;
import com.smartcommunity.dto.request.WorkerScheduleReq;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.WorkerStaffing;
import com.smartcommunity.mapper.RepairEvaluationMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.WorkerStaffingMapper;
import com.smartcommunity.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private static final int STATUS_IN_SERVICE = 2;
    private static final int STATUS_WAIT_EVALUATE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final long VERIFY_PASS_EXPIRE_SECONDS = 24 * 60 * 60;
    private static final int STAFF_STATUS_IDLE = 1;

    private final UserMapper userMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final RepairEvaluationMapper repairEvaluationMapper;
    private final WorkerStaffingMapper workerStaffingMapper;
    private final RedisUtil redisUtil;

    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 3)
                .orderByAsc(User::getId)));
    }

    @GetMapping("/staffing/list")
    public Result<List<Map<String, Object>>> staffingList() {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query staffing");
        }
        List<User> workers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 3)
                .eq(User::getIsDeleted, 0)
                .orderByAsc(User::getId));
        if (workers.isEmpty()) {
            return Result.success(List.of());
        }
        Map<Long, WorkerStaffing> staffingMap = workerStaffingMapper.selectList(new LambdaQueryWrapper<WorkerStaffing>()
                        .in(WorkerStaffing::getWorkerId, workers.stream().map(User::getId).toList())
                        .eq(WorkerStaffing::getIsDeleted, 0))
                .stream()
                .filter(item -> item.getWorkerId() != null)
                .collect(Collectors.toMap(WorkerStaffing::getWorkerId, item -> item, (a, b) -> a));

        List<Map<String, Object>> result = workers.stream().map(worker -> {
            WorkerStaffing staffing = staffingMap.get(worker.getId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", worker.getId());
            row.put("nickname", worker.getNickname());
            row.put("phone", worker.getPhone());
            row.put("status", worker.getStatus());
            row.put("staffType", staffing == null ? null : staffing.getStaffType());
            row.put("position", staffing == null ? null : staffing.getPosition());
            row.put("shiftGroup", staffing == null ? null : staffing.getShiftGroup());
            row.put("certificates", staffing == null ? null : staffing.getCertificates());
            row.put("specialties", staffing == null ? null : staffing.getSpecialties());
            row.put("maxDailyOrders", staffing == null ? null : staffing.getMaxDailyOrders());
            row.put("currentStatus", staffing == null ? null : staffing.getCurrentStatus());
            return row;
        }).toList();
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<User> add(@RequestBody AddWorkerReq req) {
        String suffix = randomAvailableSuffix("JZWX");
        User user = new User();
        user.setOpenid("worker-" + System.currentTimeMillis());
        user.setAccount("JZWX" + suffix);
        user.setPassword(new StringBuilder(suffix).reverse().toString());
        user.setRole(3);
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        userMapper.insert(user);

        upsertStaffing(user.getId(), req);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody AddWorkerReq req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("worker not found");
        }
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        upsertStaffing(user.getId(), req);
        return Result.success(user);
    }

    @PostMapping("/staffing/save")
    public Result<WorkerStaffing> saveStaffing(@RequestBody WorkerStaffingReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can save staffing");
        }
        if (req.getWorkerId() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "workerId is empty");
        }
        User worker = userMapper.selectById(req.getWorkerId());
        if (worker == null || worker.getRole() == null || worker.getRole() != 3) {
            return Result.fail(StatusCode.BAD_REQUEST, "worker not found");
        }
        WorkerStaffing staffing = workerStaffingMapper.selectOne(new LambdaQueryWrapper<WorkerStaffing>()
                .eq(WorkerStaffing::getWorkerId, req.getWorkerId())
                .eq(WorkerStaffing::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (staffing == null) {
            staffing = new WorkerStaffing();
            staffing.setWorkerId(req.getWorkerId());
            staffing.setCreateTime(LocalDateTime.now());
            staffing.setIsDeleted(0);
        }
        staffing.setStaffType(req.getStaffType() == null ? 2 : req.getStaffType());
        staffing.setPosition(req.getPosition());
        staffing.setShiftGroup(req.getShiftGroup());
        staffing.setCertificates(req.getCertificates());
        staffing.setSpecialties(req.getSpecialties());
        staffing.setMaxDailyOrders(req.getMaxDailyOrders() == null ? 5 : req.getMaxDailyOrders());
        staffing.setCurrentStatus(req.getCurrentStatus() == null ? STAFF_STATUS_IDLE : req.getCurrentStatus());
        staffing.setUpdateTime(LocalDateTime.now());
        if (staffing.getId() == null) {
            workerStaffingMapper.insert(staffing);
        } else {
            workerStaffingMapper.updateById(staffing);
        }
        return Result.success(staffing);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        workerStaffingMapper.delete(new LambdaQueryWrapper<WorkerStaffing>().eq(WorkerStaffing::getWorkerId, id));
        return Result.success("deleted", null);
    }

    @PostMapping("/schedule")
    public Result<Map<String, Object>> schedule(@RequestBody WorkerScheduleReq req) {
        return Result.success(Map.of(
                "workerId", req.getWorkerId(),
                "scheduleDate", req.getScheduleDate(),
                "shift", req.getShift(),
                "message", "schedule saved (placeholder)"
        ));
    }

    @GetMapping("/tasks")
    public Result<List<RepairOrder>> tasks(@RequestParam Long workerId) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role != null && role == 3 && uid != null && !uid.equals(workerId)) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getAssignee, workerId)
                .orderByDesc(RepairOrder::getId)));
    }

    @PostMapping("/verify-code")
    public Result<Map<String, Object>> verifyCode(@RequestBody VerifyCodeReq req) {
        if (req.getOrderId() == null || !StringUtils.hasText(req.getCode())) {
            return Result.fail(StatusCode.BAD_REQUEST, "orderId or code is empty");
        }

        Long uid = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (role == null || role != 3) {
            return Result.fail(StatusCode.FORBIDDEN, "only worker can verify code");
        }

        RepairOrder order = repairOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (order.getAssignee() == null || !order.getAssignee().equals(uid)) {
            return Result.fail(StatusCode.FORBIDDEN, "not your order");
        }

        String code = req.getCode().trim();
        String expected;
        try {
            expected = redisUtil.get(verifyCodeKey(req.getOrderId()));
        } catch (Exception ignored) {
            expected = null;
        }
        if (!StringUtils.hasText(expected)) {
            return Result.fail(StatusCode.BAD_REQUEST, "verify code expired");
        }

        boolean pass = Objects.equals(expected, code);
        if (pass) {
            try {
                redisUtil.set(verifyPassKey(req.getOrderId()), "1", VERIFY_PASS_EXPIRE_SECONDS);
            } catch (Exception ignored) {
                // Allow local running without redis.
            }
            if (!Integer.valueOf(STATUS_IN_SERVICE).equals(order.getStatus())) {
                order.setStatus(STATUS_IN_SERVICE);
                order.setRemark("on-site verification passed, service in progress");
                order.setOwnerFinishConfirmed(0);
                order.setOwnerFinishTime(null);
                order.setWorkerFinishConfirmed(0);
                order.setWorkerFinishTime(null);
                order.setCompletionTime(null);
                order.setUpdateTime(LocalDateTime.now());
                repairOrderMapper.updateById(order);
            }
        }
        return Result.success(Map.of(
                "orderId", req.getOrderId(),
                "pass", pass,
                "message", pass ? "verified" : "invalid code",
                "status", pass ? STATUS_IN_SERVICE : order.getStatus()
        ));
    }

    @GetMapping("/performance")
    public Result<Map<String, Object>> performance(@RequestParam Long workerId) {
        List<RepairOrder> orders = repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getAssignee, workerId));
        long completed = orders.stream()
                .filter(o -> Integer.valueOf(STATUS_WAIT_EVALUATE).equals(o.getStatus())
                        || Integer.valueOf(STATUS_COMPLETED).equals(o.getStatus()))
                .count();
        List<Long> completedOrderIds = orders.stream()
                .filter(o -> Integer.valueOf(STATUS_WAIT_EVALUATE).equals(o.getStatus())
                        || Integer.valueOf(STATUS_COMPLETED).equals(o.getStatus()))
                .map(RepairOrder::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        double rating = 0.0;
        if (!completedOrderIds.isEmpty()) {
            List<RepairEvaluation> evaluations = repairEvaluationMapper.selectList(new LambdaQueryWrapper<RepairEvaluation>()
                    .in(RepairEvaluation::getOrderId, completedOrderIds));
            rating = evaluations.stream().mapToInt(e -> e.getRating() == null ? 0 : e.getRating()).average().orElse(0.0);
        }
        return Result.success(Map.of(
                "workerId", workerId,
                "completedCount", completed,
                "rating", rating == 0.0 ? 4.8 : Math.round(rating * 10.0) / 10.0,
                "income", completed * 120
        ));
    }

    private String randomAvailableSuffix(String prefix) {
        int attempts = 0;
        while (attempts++ < 100) {
            String suffix = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
            String account = prefix + suffix;
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
            if (count == null || count == 0L) {
                return suffix;
            }
        }
        throw new IllegalStateException("failed to generate unique worker account");
    }

    private String verifyCodeKey(Long orderId) {
        return "repair:verify:code:" + orderId;
    }

    private String verifyPassKey(Long orderId) {
        return "repair:verify:pass:" + orderId;
    }

    private void upsertStaffing(Long workerId, AddWorkerReq req) {
        if (workerId == null) {
            return;
        }
        WorkerStaffing staffing = workerStaffingMapper.selectOne(new LambdaQueryWrapper<WorkerStaffing>()
                .eq(WorkerStaffing::getWorkerId, workerId)
                .eq(WorkerStaffing::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (staffing == null) {
            staffing = new WorkerStaffing();
            staffing.setWorkerId(workerId);
            staffing.setCreateTime(LocalDateTime.now());
            staffing.setIsDeleted(0);
        }
        staffing.setStaffType(req.getStaffType() == null ? 2 : req.getStaffType());
        staffing.setPosition(req.getPosition());
        staffing.setShiftGroup(req.getShiftGroup());
        staffing.setCertificates(req.getCertificates());
        staffing.setSpecialties(StringUtils.hasText(req.getSkills()) ? req.getSkills().trim() : req.getSkills());
        staffing.setMaxDailyOrders(req.getMaxDailyOrders() == null ? 5 : req.getMaxDailyOrders());
        staffing.setCurrentStatus(req.getCurrentStatus() == null ? STAFF_STATUS_IDLE : req.getCurrentStatus());
        staffing.setUpdateTime(LocalDateTime.now());
        if (staffing.getId() == null) {
            workerStaffingMapper.insert(staffing);
        } else {
            workerStaffingMapper.updateById(staffing);
        }
    }
}
