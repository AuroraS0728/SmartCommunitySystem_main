package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.PointsConsumeReq;
import com.smartcommunity.dto.request.PointsRechargeReq;
import com.smartcommunity.dto.response.PointsRechargeRecordVO;
import com.smartcommunity.dto.response.PointsRecordVO;
import com.smartcommunity.service.PointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/recharge")
    public Result<Map<String, Object>> recharge(@Valid @RequestBody PointsRechargeReq req) {
        Integer role = AuthContext.getRole();
        Long operatorId = AuthContext.getUserId();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can recharge points");
        }
        PointsService.RechargeResult result = pointsService.recharge(
                req.getUserId(),
                req.getAmount(),
                operatorId,
                req.getRemark()
        );
        log.info("Points recharge API success. operatorId={}, userId={}, amount={}",
                operatorId, req.getUserId(), req.getAmount());
        return Result.success(Map.of(
                "userId", result.getUserId(),
                "amount", result.getAmount(),
                "beforePoints", result.getBeforePoints(),
                "afterPoints", result.getAfterPoints(),
                "balance", result.getAfterPoints()
        ));
    }

    @PostMapping("/consume")
    public Result<Map<String, Object>> consume(@Valid @RequestBody PointsConsumeReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can consume points");
        }
        PointsService.ConsumeResult result = pointsService.consume(
                userId,
                req.getBusinessType(),
                req.getBusinessId()
        );
        return Result.success(Map.of(
                "userId", result.getUserId(),
                "businessType", result.getBusinessType(),
                "businessId", result.getBusinessId(),
                "consumePoints", result.getConsumePoints(),
                "beforePoints", result.getBeforePoints(),
                "afterPoints", result.getAfterPoints(),
                "balance", result.getAfterPoints()
        ));
    }

    @GetMapping("/balance")
    public Result<Map<String, Object>> balance() {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        Integer balance = pointsService.getBalance(userId);
        return Result.success(Map.of(
                "userId", userId,
                "points", balance
        ));
    }

    @GetMapping("/records")
    public Result<List<PointsRecordVO>> records() {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(pointsService.listUserRecords(userId));
    }

    @GetMapping("/recharge-records")
    public Result<List<PointsRechargeRecordVO>> rechargeRecords(
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Integer role = AuthContext.getRole();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only property admin can query recharge records");
        }
        return Result.success(pointsService.listRechargeRecords(ownerName, startTime, endTime));
    }
}
