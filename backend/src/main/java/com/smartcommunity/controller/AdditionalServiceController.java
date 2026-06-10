package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AdditionalServiceOrderCreateReq;
import com.smartcommunity.dto.response.AdditionalServiceOrderVO;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.service.AdditionalServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/additional-services")
@RequiredArgsConstructor
public class AdditionalServiceController {

    private final AdditionalServiceOrderService additionalServiceOrderService;

    @GetMapping("/catalog")
    public Result<List<ServiceRecommendation>> catalog() {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query additional services");
        }
        return Result.success(additionalServiceOrderService.catalog(userId));
    }

    @PostMapping("/orders")
    public Result<Map<String, Object>> createOrder(@Valid @RequestBody AdditionalServiceOrderCreateReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can book additional services");
        }
        AdditionalServiceOrderVO order = additionalServiceOrderService.createOrder(userId, req);
        Integer balance = additionalServiceOrderService.getCurrentPoints(userId);
        return Result.success(Map.of(
                "order", order,
                "balance", balance
        ));
    }

    @GetMapping("/orders/my")
    public Result<List<AdditionalServiceOrderVO>> myOrders(@RequestParam(defaultValue = "5") Integer limit) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can query additional service orders");
        }
        return Result.success(additionalServiceOrderService.listMyOrders(userId, limit));
    }

    @GetMapping("/admin/orders")
    public Result<Map<String, Object>> adminOrders(@RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "200") Integer limit) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(Map.of(
                "summary", additionalServiceOrderService.adminSummary(),
                "orders", additionalServiceOrderService.listAdminOrders(status, keyword, limit)
        ));
    }

    @PostMapping("/admin/orders/{id}/status")
    public Result<AdditionalServiceOrderVO> updateOrderStatus(@PathVariable Long id,
                                                              @RequestBody Map<String, Integer> body) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        Integer status = body == null ? null : body.get("status");
        return Result.success(additionalServiceOrderService.updateStatus(id, status));
    }
}
