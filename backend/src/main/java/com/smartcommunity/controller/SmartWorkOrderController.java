package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.RepairPriorityReq;
import com.smartcommunity.dto.request.SmartWorkOrderDispatchReq;
import com.smartcommunity.dto.request.SmartWorkOrderStatusReq;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.service.SmartWorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/smart-work-orders")
@RequiredArgsConstructor
public class SmartWorkOrderController {

    private final SmartWorkOrderService smartWorkOrderService;

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Integer priority,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Boolean overdueOnly) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.page(pageNum, pageSize, status, priority, keyword, overdueOnly));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.detail(id));
    }

    @PostMapping("/{id}/dispatch")
    public Result<Map<String, Object>> dispatch(@PathVariable Long id, @RequestBody SmartWorkOrderDispatchReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.dispatch(id, req));
    }

    @PostMapping("/{id}/status")
    public Result<RepairOrder> updateStatus(@PathVariable Long id, @RequestBody SmartWorkOrderStatusReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.updateStatus(id, req));
    }

    @PostMapping("/{id}/priority")
    public Result<RepairOrder> updatePriority(@PathVariable Long id, @RequestBody RepairPriorityReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.updatePriority(
                id,
                req == null ? null : req.getPriority(),
                req == null ? null : req.getSuggestedWorkerId(),
                req == null ? null : req.getSlaDeadline()
        ));
    }

    @PostMapping("/sla/scan")
    public Result<Map<String, Object>> scanSla() {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(smartWorkOrderService.scanSla());
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
