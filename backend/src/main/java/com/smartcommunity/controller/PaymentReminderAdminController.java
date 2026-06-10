package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.PaymentReminder;
import com.smartcommunity.mapper.PaymentReminderMapper;
import com.smartcommunity.service.PaymentReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentReminderAdminController {

    private final PaymentReminderMapper paymentReminderMapper;
    private final PaymentReminderService paymentReminderService;

    @GetMapping("/api/payment/reminders")
    public Result<Map<String, Object>> reminders(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) Long feeBillId,
                                                 @RequestParam(required = false) Integer businessType,
                                                 @RequestParam(required = false) Long businessId,
                                                 @RequestParam(required = false) String method,
                                                 @RequestParam(required = false) Integer status) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query reminders");
        }
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        LambdaQueryWrapper<PaymentReminder> base = buildWrapper(userId, feeBillId, businessType, businessId, method, status);
        Long total = paymentReminderMapper.selectCount(base);
        List<PaymentReminder> records = List.of();
        if (total != null && total > 0) {
            records = paymentReminderMapper.selectList(buildWrapper(userId, feeBillId, businessType, businessId, method, status)
                    .orderByDesc(PaymentReminder::getId)
                    .last("LIMIT " + offset + "," + safePageSize));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("records", records);
        payload.put("total", total == null ? 0 : total);
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    @PostMapping("/api/payment/reminders/generate")
    public Result<List<PaymentReminder>> generate() {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can generate reminders");
        }
        return Result.success(paymentReminderService.generateRemindersNow());
    }

    private LambdaQueryWrapper<PaymentReminder> buildWrapper(Long userId, Long feeBillId, Integer businessType,
                                                             Long businessId, String method, Integer status) {
        LambdaQueryWrapper<PaymentReminder> wrapper = new LambdaQueryWrapper<PaymentReminder>()
                .eq(PaymentReminder::getIsDeleted, 0);
        if (userId != null) {
            wrapper.eq(PaymentReminder::getUserId, userId);
        }
        if (feeBillId != null) {
            wrapper.eq(PaymentReminder::getFeeBillId, feeBillId);
        }
        if (businessType != null) {
            wrapper.eq(PaymentReminder::getBusinessType, businessType);
        }
        if (businessId != null) {
            wrapper.eq(PaymentReminder::getBusinessId, businessId);
        }
        if (method != null && !method.isBlank()) {
            wrapper.eq(PaymentReminder::getMethod, method.trim());
        }
        if (status != null) {
            wrapper.eq(PaymentReminder::getStatus, status);
        }
        return wrapper;
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
