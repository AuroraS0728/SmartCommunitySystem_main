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
    private static final String TITLE = "物业费催缴";
    private static final String MESSAGE_CONTENT = "您的物业费账单已逾期，请及时缴纳。";
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
                log.warn("Generate payment reminder failed, feeBillId={}", bill == null ? null : bill.getFeeBillId(), ex);
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
        if (bill == null || bill.getFeeBillId() == null || bill.getUserId() == null || bill.getDueDate() == null) {
            return null;
        }

        BigDecimal unpaid = unpaidAmount(bill);
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        long overdueDays = Math.max(0L, ChronoUnit.DAYS.between(bill.getDueDate().toLocalDate(), LocalDate.now()));
        String method = decideMethod(unpaid, overdueDays);
        if (method == null || shouldSkipDuplicate(bill.getFeeBillId(), method)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        String content = METHOD_TASK.equals(method)
                ? taskDescription(bill, unpaid, overdueDays)
                : MESSAGE_CONTENT;

        PaymentReminder reminder = new PaymentReminder();
        reminder.setUserId(bill.getUserId());
        reminder.setFeeBillId(bill.getFeeBillId());
        reminder.setMethod(method);
        reminder.setContent(content);
        reminder.setStatus(METHOD_MESSAGE.equals(method) ? 1 : 0);
        reminder.setSendTime(METHOD_MESSAGE.equals(method) ? now : null);
        reminder.setCreateTime(now);
        reminder.setUpdateTime(now);
        reminder.setIsDeleted(0);
        paymentReminderMapper.insert(reminder);

        if (METHOD_MESSAGE.equals(method)) {
            createMessage(bill.getUserId(), now);
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
        if (unpaid.compareTo(TASK_AMOUNT_THRESHOLD) > 0 || overdueDays > 90) {
            return METHOD_TASK;
        }
        if (unpaid.compareTo(BigDecimal.ZERO) > 0) {
            return METHOD_MESSAGE;
        }
        return null;
    }

    private boolean shouldSkipDuplicate(Long feeBillId, String method) {
        LambdaQueryWrapper<PaymentReminder> wrapper = new LambdaQueryWrapper<PaymentReminder>()
                .eq(PaymentReminder::getFeeBillId, feeBillId)
                .eq(PaymentReminder::getMethod, method)
                .eq(PaymentReminder::getIsDeleted, 0);
        if (METHOD_TASK.equals(method)) {
            wrapper.eq(PaymentReminder::getStatus, 0);
        } else {
            wrapper.ge(PaymentReminder::getCreateTime, LocalDate.now().atStartOfDay());
        }
        Long count = paymentReminderMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private void createMessage(Long userId, LocalDateTime now) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(TITLE);
        message.setContent(MESSAGE_CONTENT);
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
        String ownerName = bill.getOwnerName() == null || bill.getOwnerName().isBlank() ? "未知业主" : bill.getOwnerName().trim();
        return "业主 " + ownerName + " 欠费 " + unpaid.toPlainString() + " 元，逾期 " + overdueDays + " 天，请电话联系。";
    }
}
