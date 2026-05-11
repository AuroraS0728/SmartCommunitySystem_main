package com.smartcommunity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AdjustCreditReq;
import com.smartcommunity.entity.CreditLog;
import com.smartcommunity.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/logs")
    public Result<Page<CreditLog>> logs(@RequestParam(required = false) Long userId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role == 1) {
            userId = uid;
        }
        return Result.success(creditService.getCreditLog(userId, page, size));
    }

    @PostMapping("/adjust")
    public Result<Void> adjust(@RequestBody AdjustCreditReq req) {
        Integer role = AuthContext.getRole();
        if (role == null || role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can adjust credit");
        }
        if (req == null || req.getUserId() == null || req.getChangeValue() == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "userId and changeValue are required");
        }
        creditService.changeCredit(req.getUserId(), req.getChangeValue(), req.getReason());
        return Result.success("success", null);
    }
}
