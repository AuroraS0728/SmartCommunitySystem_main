package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.PaymentReminder;
import com.smartcommunity.mapper.PaymentReminderMapper;
import com.smartcommunity.service.PaymentReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payment-reminder")
@RequiredArgsConstructor
public class PaymentReminderController {

    private final PaymentReminderMapper paymentReminderMapper;
    private final PaymentReminderService paymentReminderService;

    @GetMapping("/list")
    public Result<List<PaymentReminder>> list(@RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) Long feeBillId,
                                              @RequestParam(required = false) Integer businessType,
                                              @RequestParam(required = false) Long businessId,
                                              @RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        LambdaQueryWrapper<PaymentReminder> wrapper = new LambdaQueryWrapper<PaymentReminder>()
                .eq(PaymentReminder::getIsDeleted, 0);
        if (role == 1) {
            wrapper.eq(PaymentReminder::getUserId, uid);
        } else if (userId != null) {
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
        if (status != null) {
            wrapper.eq(PaymentReminder::getStatus, status);
        }
        wrapper.orderByDesc(PaymentReminder::getId);
        return Result.success(paymentReminderMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<PaymentReminder> create(@RequestBody PaymentReminder req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can create reminder");
        }
        if (req == null || req.getUserId() == null || (req.getFeeBillId() == null && req.getBusinessId() == null)) {
            return Result.fail(StatusCode.BAD_REQUEST, "userId and bill id are required");
        }
        LocalDateTime now = LocalDateTime.now();
        Long businessId = req.getBusinessId() == null ? req.getFeeBillId() : req.getBusinessId();
        PaymentReminder row = new PaymentReminder();
        row.setUserId(req.getUserId());
        row.setFeeBillId(req.getFeeBillId() == null ? businessId : req.getFeeBillId());
        row.setBusinessType(req.getBusinessType() == null ? 1 : req.getBusinessType());
        row.setBusinessId(businessId);
        row.setMethod(StringUtils.hasText(req.getMethod()) ? req.getMethod().trim() : "MESSAGE");
        row.setContent(StringUtils.hasText(req.getContent()) ? req.getContent().trim() : "您有一笔待缴费用，请及时处理。");
        row.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        row.setSendTime(req.getSendTime());
        row.setCreateTime(now);
        row.setUpdateTime(now);
        row.setIsDeleted(0);
        paymentReminderMapper.insert(row);
        return Result.success(row);
    }

    @PostMapping("/generate")
    public Result<List<PaymentReminder>> generateOverdue() {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can generate reminders");
        }
        return Result.success(paymentReminderService.generateRemindersNow());
    }

    @PostMapping("/{id}/send")
    public Result<PaymentReminder> markSent(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can send reminder");
        }
        PaymentReminder row = paymentReminderMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "reminder not found");
        }
        row.setStatus(1);
        row.setSendTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        paymentReminderMapper.updateById(row);
        return Result.success(row);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete reminder");
        }
        paymentReminderMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private boolean isAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == 2;
    }
}
