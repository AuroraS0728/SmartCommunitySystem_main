package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.dto.response.OverdueFeeBillReminderRow;
import com.smartcommunity.entity.PaymentReminder;
import com.smartcommunity.entity.PropertyTask;
import com.smartcommunity.entity.SysMessage;
import com.smartcommunity.mapper.PaymentReminderMapper;
import com.smartcommunity.mapper.PropertyTaskMapper;
import com.smartcommunity.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    private static final String METHOD_MESSAGE = "MESSAGE";
    private static final String METHOD_TASK = "TASK";
    private static final String TITLE = "费用催缴";
    private static final BigDecimal TASK_AMOUNT_THRESHOLD = new BigDecimal("1000");

    private final PaymentReminderMapper paymentReminderMapper;
    private final SysMessageMapper sysMessageMapper;
    private final PropertyTaskMapper propertyTaskMapper;
    private final PlatformTransactionManager transactionManager;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Shanghai")
    public void generateReminders() {
        generateRemindersNow();
    }

    public List<PaymentReminder> generateRemindersNow() {
        List<OverdueFeeBillReminderRow> bills = paymentReminderMapper.selectOverdueFeeBillsForReminder();
        List<PaymentReminder> created = new ArrayList<>();
        for (OverdueFeeBillReminderRow bill : bills) {
            try {
                PaymentReminder reminder = processBillInTransaction(bill);
                if (reminder != null) {
                    created.add(reminder);
                }
            } catch (Exception ex) {
                log.warn("Generate payment reminder failed, businessType={}, businessId={}",
                        bill == null ? null : bill.getBusinessType(),
                        bill == null ? null : bill.getBusinessId(),
                        ex);
            }
        }
        return created;
    }

    private PaymentReminder processBillInTransaction(OverdueFeeBillReminderRow bill) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> processBill(bill));
    }

    private PaymentReminder processBill(OverdueFeeBillReminderRow bill) {
        if (bill == null || bill.getBusinessId() == null || bill.getUserId() == null || bill.getDueDate() == null) {
            return null;
        }

        // 欠费金额是催缴规则的核心输入，停车费和维修费没有部分缴费时paidAmount按0处理。
        BigDecimal unpaid = unpaidAmount(bill);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // 逾期天数只按日期差计算，避免小时分钟导致当天重复波动。
        long overdueDays = Math.max(0L, ChronoUnit.DAYS.between(bill.getDueDate().toLocalDate(), LocalDate.now()));
        String method = decideMethod(unpaid, overdueDays);
        if (method == null || shouldSkipDuplicate(bill.getBusinessType(), bill.getBusinessId(), method)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        String content = METHOD_TASK.equals(method)
                ? taskDescription(bill, unpaid, overdueDays)
                : messageContent(bill);

        PaymentReminder reminder = new PaymentReminder();
        reminder.setUserId(bill.getUserId());
        reminder.setFeeBillId(bill.getBusinessId());
        reminder.setBusinessType(bill.getBusinessType());
        reminder.setBusinessId(bill.getBusinessId());
        reminder.setMethod(method);
        reminder.setContent(content);
        reminder.setStatus(METHOD_MESSAGE.equals(method) ? 1 : 0);
        reminder.setSendTime(METHOD_MESSAGE.equals(method) ? now : null);
        reminder.setCreateTime(now);
        reminder.setUpdateTime(now);
        reminder.setIsDeleted(0);
        paymentReminderMapper.insert(reminder);

        if (METHOD_MESSAGE.equals(method)) {
            createMessage(bill.getUserId(), content, now);
        } else {
            createPropertyTask(content, now);
        }
        return reminder;
    }

    private BigDecimal unpaidAmount(OverdueFeeBillReminderRow bill) {
        BigDecimal amount = bill.getAmount() == null ? BigDecimal.ZERO : bill.getAmount();
        BigDecimal paidAmount = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
        return amount.subtract(paidAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private String decideMethod(BigDecimal unpaid, long overdueDays) {
        // 金额较大或逾期太久时，单纯消息提醒不够，所以升级为物业待办任务。
        if (unpaid.compareTo(TASK_AMOUNT_THRESHOLD) > 0 || overdueDays > 90) {
            return METHOD_TASK;
        }
        if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
            return METHOD_MESSAGE;
        }
        return null;
    }

    private boolean shouldSkipDuplicate(Integer businessType, Long businessId, String method) {
        LambdaQueryWrapper<PaymentReminder> wrapper = new LambdaQueryWrapper<PaymentReminder>()
                .eq(PaymentReminder::getMethod, method)
                .eq(PaymentReminder::getIsDeleted, 0)
                .and(w -> w.eq(PaymentReminder::getBusinessType, businessType)
                        .eq(PaymentReminder::getBusinessId, businessId)
                        .or()
                        .isNull(PaymentReminder::getBusinessType)
                        .eq(PaymentReminder::getFeeBillId, businessId));
        if (METHOD_TASK.equals(method)) {
            wrapper.eq(PaymentReminder::getStatus, 0);
        } else {
            wrapper.ge(PaymentReminder::getCreateTime, LocalDate.now().atStartOfDay());
        }
        Long count = paymentReminderMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private void createMessage(Long userId, String content, LocalDateTime now) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(TITLE);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(now);
        sysMessageMapper.insert(message);
    }

    private void createPropertyTask(String description, LocalDateTime now) {
        PropertyTask task = new PropertyTask();
        task.setTitle(TITLE);
        task.setDescription(description);
        task.setAssignedTo(null);
        task.setStatus(0);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        task.setIsDeleted(0);
        propertyTaskMapper.insert(task);
    }

    private String taskDescription(OverdueFeeBillReminderRow bill, BigDecimal unpaid, long overdueDays) {
        String ownerName = bill.getOwnerName() == null || bill.getOwnerName().isBlank()
                ? "未知业主"
                : bill.getOwnerName().trim();
        return "业主 " + ownerName + " 的" + feeLabel(bill) + "欠费 "
                + unpaid.toPlainString() + " 元，逾期 " + overdueDays + " 天，请电话联系。";
    }

    private String messageContent(OverdueFeeBillReminderRow bill) {
        return "您的" + feeLabel(bill) + "账单已逾期，请及时缴纳。";
    }

    private String feeLabel(OverdueFeeBillReminderRow bill) {
        if (bill.getBusinessTypeText() != null && !bill.getBusinessTypeText().isBlank()) {
            return bill.getBusinessTypeText().trim();
        }
        if (bill.getBusinessType() != null && bill.getBusinessType() == 2) {
            return "停车费";
        }
        if (bill.getBusinessType() != null && bill.getBusinessType() == 3) {
            return "维修费";
        }
        return "物业费";
    }
}
