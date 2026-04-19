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
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairEvaluationMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {

    private static final int STATUS_WAIT_DISPATCH = 1;
    private static final int STATUS_IN_SERVICE = 2;
    private static final int STATUS_WAIT_EVALUATE = 3;
    private static final int STATUS_COMPLETED = 4;
    private static final int STATUS_CANCELED = 5;

    private static final long VERIFY_CODE_EXPIRE_SECONDS = 4 * 60 * 60;
    private static final long VERIFY_PASS_EXPIRE_SECONDS = 24 * 60 * 60;

    private final RepairOrderMapper repairOrderMapper;
    private final RepairEvaluationMapper repairEvaluationMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @PostMapping("/submit")
    public Result<RepairOrder> submit(@RequestBody SubmitRepairReq req) {
        if (!StringUtils.hasText(req.getCategory()) || !StringUtils.hasText(req.getDescription())) {
            return Result.fail(StatusCode.BAD_REQUEST, "category or description is empty");
        }
        RepairOrder order = new RepairOrder();
        order.setUserId(AuthContext.getUserId());
        order.setPropertyId(req.getPropertyId());
        order.setCategory(req.getCategory().trim());
        order.setDescription(req.getDescription().trim());
        order.setImages(req.getImages());
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
        if (req.getOrderId() == null || req.getAssignee() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "orderId or assignee is empty");
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

        User worker = userMapper.selectById(req.getAssignee());
        if (worker == null || worker.getRole() == null || worker.getRole() != 3) {
            return Result.fail(StatusCode.BAD_REQUEST, "assignee is not worker");
        }
        if (worker.getStatus() == null || worker.getStatus() != 1) {
            return Result.fail(StatusCode.BAD_REQUEST, "worker unavailable");
        }

        order.setAssignee(req.getAssignee());
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
                order.setStatus(STATUS_WAIT_EVALUATE);
                if (order.getCompletionTime() == null) {
                    order.setCompletionTime(now);
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
