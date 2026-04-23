package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.FaceLiveVerifyMultiReq;
import com.smartcommunity.dto.request.FaceRegisterReq;
import com.smartcommunity.dto.request.FaceVerifyReq;
import com.smartcommunity.dto.response.FaceLiveVerifyMultiItemResp;
import com.smartcommunity.dto.response.FaceLiveVerifyMultiResp;
import com.smartcommunity.dto.response.FaceLiveVerifyResp;
import com.smartcommunity.dto.response.FaceVerifyResp;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairOrderWorker;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairOrderWorkerMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.service.SeetaFaceService;
import com.smartcommunity.utils.RedisUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceController {

    private static final long VERIFY_PASS_EXPIRE_SECONDS = 24 * 60 * 60;

    private final SeetaFaceService seetaFaceService;
    private final RepairOrderMapper repairOrderMapper;
    private final RepairOrderWorkerMapper repairOrderWorkerMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody FaceRegisterReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can register worker face");
        }
        log.info("Face register request received. workerId={}", req.getWorkerId());
        String status = seetaFaceService.registerFace(req.getWorkerId(), req.getImageBase64());
        return Result.success(Map.of(
                "workerId", req.getWorkerId(),
                "status", status
        ));
    }

    @PostMapping("/verify")
    public Result<FaceVerifyResp> verify(@Valid @RequestBody FaceVerifyReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role == 3 && userId != null && req.getWorkerId() != null && !userId.equals(req.getWorkerId())) {
            return Result.fail(StatusCode.FORBIDDEN, "worker can only verify self face");
        }
        log.info("Face verify request received. role={}, userId={}, workerId={}", role, userId, req.getWorkerId());
        SeetaFaceService.FaceVerifyResult result = seetaFaceService.verifyFaceWithScore(
                req.getWorkerId(), req.getImageBase64()
        );
        FaceVerifyResp resp = new FaceVerifyResp();
        resp.setMatch(result.isMatch());
        resp.setScore(result.getScore());
        resp.setThreshold(result.getThreshold());
        return Result.success(resp);
    }

    @PostMapping("/live-verify")
    public Result<FaceLiveVerifyResp> liveVerify(@Valid @RequestBody FaceVerifyReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role == 3 && userId != null && req.getWorkerId() != null && !userId.equals(req.getWorkerId())) {
            return Result.fail(StatusCode.FORBIDDEN, "worker can only verify self face");
        }
        log.info("Face live verify request received. role={}, userId={}, workerId={}", role, userId, req.getWorkerId());
        SeetaFaceService.FaceLiveVerifyResult result = seetaFaceService.verifyLiveFaceWithScore(
                req.getWorkerId(), req.getImageBase64()
        );
        FaceLiveVerifyResp resp = new FaceLiveVerifyResp();
        resp.setMatch(result.isMatch());
        resp.setScore(result.getScore());
        resp.setThreshold(result.getThreshold());
        resp.setLivenessPassed(result.isLivenessPassed());
        resp.setLivenessStatus(result.getLivenessStatus());
        resp.setLivenessLabel(result.getLivenessLabel());
        resp.setMaskCheckSupported(result.isMaskCheckSupported());
        resp.setMaskPassed(result.isMaskPassed());
        resp.setMaskStatus(result.getMaskStatus());
        resp.setMaskLabel(result.getMaskLabel());
        resp.setEyeStateCheckSupported(result.isEyeStateCheckSupported());
        resp.setEyesOpenPassed(result.isEyesOpenPassed());
        resp.setLeftEyeState(result.getLeftEyeState());
        resp.setLeftEyeLabel(result.getLeftEyeLabel());
        resp.setRightEyeState(result.getRightEyeState());
        resp.setRightEyeLabel(result.getRightEyeLabel());
        resp.setQualityCheckSupported(result.isQualityCheckSupported());
        resp.setQualityPassed(result.isQualityPassed());
        resp.setQualityScore(result.getQualityScore());
        resp.setQualityLevel(result.getQualityLevel());
        resp.setQualityLevelLabel(result.getQualityLevelLabel());
        return Result.success(resp);
    }

    @PostMapping("/live-verify-multi")
    public Result<FaceLiveVerifyMultiResp> liveVerifyMulti(@Valid @RequestBody FaceLiveVerifyMultiReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2 && role != 3) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin or worker can verify");
        }

        RepairOrder order = repairOrderMapper.selectById(req.getOrderId());
        if (order == null) {
            return Result.fail(StatusCode.NOT_FOUND, "order not found");
        }
        if (role == 3 && !Objects.equals(order.getAssignee(), userId)) {
            boolean isParticipant = repairOrderWorkerMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairOrderWorker>()
                    .eq(RepairOrderWorker::getOrderId, order.getId())
                    .eq(RepairOrderWorker::getWorkerId, userId)
                    .eq(RepairOrderWorker::getIsDeleted, 0)) > 0;
            if (!isParticipant) {
                return Result.fail(StatusCode.FORBIDDEN, "not your order");
            }
        }

        List<RepairOrderWorker> participants = repairOrderWorkerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairOrderWorker>()
                        .eq(RepairOrderWorker::getOrderId, order.getId())
                        .eq(RepairOrderWorker::getIsDeleted, 0)
                        .orderByAsc(RepairOrderWorker::getRoleType)
                        .orderByAsc(RepairOrderWorker::getWorkerId)
        );
        if (participants.isEmpty() && order.getAssignee() != null) {
            RepairOrderWorker fallback = new RepairOrderWorker();
            fallback.setOrderId(order.getId());
            fallback.setWorkerId(order.getAssignee());
            fallback.setRoleType(1);
            fallback.setVerifyPassed(0);
            fallback.setVerifyPassTime(null);
            fallback.setFinishConfirmed(0);
            fallback.setFinishTime(null);
            fallback.setCreateTime(LocalDateTime.now());
            fallback.setUpdateTime(LocalDateTime.now());
            fallback.setIsDeleted(0);
            repairOrderWorkerMapper.insert(fallback);
            participants = List.of(fallback);
        }
        if (participants.isEmpty()) {
            return Result.fail(StatusCode.BAD_REQUEST, "order has no assigned workers");
        }

        Set<Long> workerIds = participants.stream()
                .map(RepairOrderWorker::getWorkerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> workerMap = userMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().in(User::getId, workerIds))
                .stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(User::getId, item -> item, (a, b) -> a));

        List<FaceLiveVerifyMultiItemResp> results = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (RepairOrderWorker participant : participants) {
            Long workerId = participant.getWorkerId();
            if (workerId == null) {
                continue;
            }
            SeetaFaceService.FaceLiveVerifyResult verifyResult;
            try {
                verifyResult = seetaFaceService.verifyLiveFaceWithScore(workerId, req.getImageBase64());
            } catch (Exception ex) {
                verifyResult = new SeetaFaceService.FaceLiveVerifyResult(
                        false,
                        0D,
                        0D,
                        false,
                        -1,
                        "ERROR",
                        false,
                        true,
                        -1,
                        "UNSUPPORTED",
                        false,
                        true,
                        -1,
                        "UNSUPPORTED",
                        -1,
                        "UNSUPPORTED",
                        false,
                        true,
                        0D,
                        0,
                        "UNSUPPORTED"
                );
                log.warn("live verify failed for workerId={}: {}", workerId, ex.getMessage());
            }
            FaceLiveVerifyMultiItemResp item = new FaceLiveVerifyMultiItemResp();
            User worker = workerMap.get(workerId);
            item.setWorkerId(workerId);
            item.setWorkerName(worker == null ? null : worker.getNickname());
            item.setMatch(verifyResult.isMatch());
            item.setScore(verifyResult.getScore());
            item.setThreshold(verifyResult.getThreshold());
            item.setLivenessPassed(verifyResult.isLivenessPassed());
            item.setLivenessStatus(verifyResult.getLivenessStatus());
            item.setLivenessLabel(verifyResult.getLivenessLabel());
            item.setMaskCheckSupported(verifyResult.isMaskCheckSupported());
            item.setMaskPassed(verifyResult.isMaskPassed());
            item.setMaskStatus(verifyResult.getMaskStatus());
            item.setMaskLabel(verifyResult.getMaskLabel());
            item.setEyeStateCheckSupported(verifyResult.isEyeStateCheckSupported());
            item.setEyesOpenPassed(verifyResult.isEyesOpenPassed());
            item.setLeftEyeState(verifyResult.getLeftEyeState());
            item.setLeftEyeLabel(verifyResult.getLeftEyeLabel());
            item.setRightEyeState(verifyResult.getRightEyeState());
            item.setRightEyeLabel(verifyResult.getRightEyeLabel());
            item.setQualityCheckSupported(verifyResult.isQualityCheckSupported());
            item.setQualityPassed(verifyResult.isQualityPassed());
            item.setQualityScore(verifyResult.getQualityScore());
            item.setQualityLevel(verifyResult.getQualityLevel());
            item.setQualityLevelLabel(verifyResult.getQualityLevelLabel());
            results.add(item);

            if (verifyResult.isMatch()) {
                participant.setVerifyPassed(1);
                if (participant.getVerifyPassTime() == null) {
                    participant.setVerifyPassTime(now);
                }
                participant.setUpdateTime(now);
                repairOrderWorkerMapper.updateById(participant);
                try {
                    redisUtil.set(verifyPassKey(order.getId(), workerId), "1", VERIFY_PASS_EXPIRE_SECONDS);
                } catch (Exception ignored) {
                    // allow local run without redis
                }
            }
        }

        List<RepairOrderWorker> refreshed = repairOrderWorkerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RepairOrderWorker>()
                        .eq(RepairOrderWorker::getOrderId, order.getId())
                        .eq(RepairOrderWorker::getIsDeleted, 0)
        );
        boolean allVerified = !refreshed.isEmpty()
                && refreshed.stream().allMatch(item -> item.getVerifyPassed() != null && item.getVerifyPassed() == 1);
        List<Long> pendingWorkerIds = refreshed.stream()
                .filter(item -> item.getVerifyPassed() == null || item.getVerifyPassed() != 1)
                .map(RepairOrderWorker::getWorkerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            if (allVerified) {
                redisUtil.set(verifyPassKey(order.getId()), "1", VERIFY_PASS_EXPIRE_SECONDS);
            } else {
                redisUtil.delete(verifyPassKey(order.getId()));
            }
        } catch (Exception ignored) {
            // allow local run without redis
        }

        FaceLiveVerifyMultiResp resp = new FaceLiveVerifyMultiResp();
        resp.setOrderId(order.getId());
        resp.setAllVerified(allVerified);
        resp.setPendingWorkerIds(pendingWorkerIds);
        resp.setResults(results);
        return Result.success(resp);
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestParam Long workerId) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2 && role != 3) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin or worker can query face status");
        }
        if (role == 3 && userId != null && !userId.equals(workerId)) {
            return Result.fail(StatusCode.FORBIDDEN, "worker can only query self face status");
        }
        log.info("Face status request received. role={}, userId={}, workerId={}", role, userId, workerId);
        boolean registered = seetaFaceService.hasRegisteredFace(workerId);
        return Result.success(Map.of(
                "workerId", workerId,
                "registered", registered
        ));
    }

    private String verifyPassKey(Long orderId) {
        return "repair:verify:pass:" + orderId;
    }

    private String verifyPassKey(Long orderId, Long workerId) {
        return "repair:verify:pass:" + orderId + ":" + workerId;
    }
}
