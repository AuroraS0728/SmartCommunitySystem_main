package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.OwnerParkingExtraReq;
import com.smartcommunity.dto.request.ParkingBindVehicleReq;
import com.smartcommunity.dto.request.ParkingBindVisitorVehicleReq;
import com.smartcommunity.dto.request.ParkingEntryReq;
import com.smartcommunity.dto.request.ParkingExitReq;
import com.smartcommunity.dto.request.ParkingMonthCardReq;
import com.smartcommunity.dto.request.ParkingPayOrderReq;
import com.smartcommunity.dto.request.WechatPayCallbackReq;
import com.smartcommunity.entity.OwnerParkingQuota;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.UserVehicle;
import com.smartcommunity.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkingService parkingService;

    @PostMapping("/vehicle/bind")
    public Result<UserVehicle> bindOwnerVehicle(@Valid @RequestBody ParkingBindVehicleReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can bind own vehicle");
        }
        return Result.success(parkingService.bindOwnerVehicle(userId, req.getVehicleNo()));
    }

    @PostMapping("/visitor-vehicle/bind")
    public Result<UserVehicle> bindVisitorVehicle(@Valid @RequestBody ParkingBindVisitorVehicleReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can bind visitor vehicle");
        }
        LocalDateTime deadline = parseDateTime(req.getParkingDeadline(), "parkingDeadline");
        return Result.success(parkingService.bindVisitorVehicle(userId, req.getVehicleNo(), deadline));
    }

    @GetMapping("/vehicles")
    public Result<List<UserVehicle>> myVehicles(@RequestParam(defaultValue = "false") boolean visitor) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query own vehicles");
        }
        return Result.success(parkingService.listMyVehicles(userId, visitor));
    }

    @PostMapping("/entry")
    public Result<ParkingOrder> entry(@Valid @RequestBody ParkingEntryReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1 && role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner or admin can create parking entry");
        }
        return Result.success(parkingService.createEntryOrder(userId, req.getVehicleNo(), req.getSourceType()));
    }

    @PostMapping("/exit")
    public Result<Map<String, Object>> exit(@Valid @RequestBody ParkingExitReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1 && role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner or admin can settle parking");
        }
        return Result.success(parkingService.settleExitByVehicle(req.getVehicleNo()));
    }

    @GetMapping("/pay-entry")
    public Result<Map<String, Object>> payEntry(@RequestParam String vehicleNo) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(parkingService.queryPaymentEntrance(userId, role, vehicleNo));
    }

    @PostMapping("/month-card/renew")
    public Result<Map<String, Object>> renewMonthCard(@Valid @RequestBody ParkingMonthCardReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can renew month card");
        }
        return Result.success(parkingService.createMonthCardOrder(userId, req.getVehicleNo()));
    }

    @GetMapping("/month-card/status")
    public Result<Map<String, Object>> monthCardStatus(@RequestParam String vehicleNo) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query month card");
        }
        return Result.success(parkingService.monthCardStatus(userId, vehicleNo));
    }

    @GetMapping("/month-card/reminders")
    public Result<List<ParkingOrder>> monthCardReminders() {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(parkingService.listRenewReminders(userId, role));
    }

    @PostMapping("/pay/wechat")
    public Result<Map<String, Object>> payWechat(@Valid @RequestBody ParkingPayOrderReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(parkingService.createTempOrderPayment(userId, role, req.getOrderId()));
    }

    @PostMapping("/pay/callback")
    public Result<ParkingOrder> payCallback(@Valid @RequestBody WechatPayCallbackReq req) {
        return Result.success(parkingService.handlePayCallback(req.getOutTradeNo(), req.getSuccess()));
    }

    @PostMapping("/quota/extra")
    public Result<OwnerParkingQuota> setExtraHours(@Valid @RequestBody OwnerParkingExtraReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can set extra free hours");
        }
        return Result.success(parkingService.setExtraHours(userId, req.getMonthKey(), req.getExtraHours()));
    }

    @GetMapping("/quota")
    public Result<OwnerParkingQuota> quota(@RequestParam(required = false) String monthKey) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query free-hour quota");
        }
        return Result.success(parkingService.queryQuota(userId, monthKey));
    }

    @GetMapping("/visitor-reminders")
    public Result<List<UserVehicle>> visitorReminderList(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime before) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query visitor reminders");
        }
        LocalDateTime deadline = before == null ? LocalDateTime.now().plusHours(1) : before;
        List<UserVehicle> all = parkingService.listMyVehicles(userId, true);
        List<UserVehicle> target = all.stream()
                .filter(v -> v.getParkingDeadline() != null)
                .filter(v -> !v.getParkingDeadline().isBefore(LocalDateTime.now()))
                .filter(v -> !v.getParkingDeadline().isAfter(deadline))
                .toList();
        return Result.success(target);
    }

    private LocalDateTime parseDateTime(String text, String fieldName) {
        try {
            return LocalDateTime.parse(text, DATETIME_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + "格式错误，应为yyyy-MM-dd HH:mm:ss");
        }
    }
}
