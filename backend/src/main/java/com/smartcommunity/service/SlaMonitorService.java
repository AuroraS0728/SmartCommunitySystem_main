package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairUrgeLog;
import com.smartcommunity.entity.SysMessage;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairUrgeLogMapper;
import com.smartcommunity.mapper.SysMessageMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaMonitorService {

    private static final int STATUS_WAIT_DISPATCH = 1;
    private static final int STATUS_IN_SERVICE = 2;
    private static final int ROLE_ADMIN = 2;
    private static final int ROLE_WORKER = 3;
    private static final String TARGET_ADMIN = "ADMIN";
    private static final String TARGET_WORKER = "WORKER";
    private static final String TITLE = "工单SLA催办";
    private static final long URGE_INTERVAL_MINUTES = 30;

    private final RepairOrderMapper repairOrderMapper;
    private final UserMapper userMapper;
    private final SysMessageMapper sysMessageMapper;
    private final RepairUrgeLogMapper repairUrgeLogMapper;
    private final PlatformTransactionManager transactionManager;

    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Shanghai")
    public void monitorTimeoutOrders() {
        scanAndUrge();
    }

    public int scanAndUrge() {
        LocalDateTime now = LocalDateTime.now();
        List<RepairOrder> overdueOrders = repairOrderMapper.selectList(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getIsDeleted, 0)
                .in(RepairOrder::getStatus, STATUS_WAIT_DISPATCH, STATUS_IN_SERVICE)
                .isNotNull(RepairOrder::getSlaDeadline)
                .lt(RepairOrder::getSlaDeadline, now));

        int handled = 0;
        for (RepairOrder order : overdueOrders) {
            try {
                processOrderInTransaction(order, now);
                handled++;
            } catch (Exception ex) {
                log.warn("SLA timeout urge failed, orderId={}", order == null ? null : order.getId(), ex);
            }
        }
        return handled;
    }

    private void processOrderInTransaction(RepairOrder order, LocalDateTime now) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> processOrder(order, now));
    }

    private void processOrder(RepairOrder order, LocalDateTime now) {
        RepairOrder current = repairOrderMapper.selectOne(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getId, order.getId())
                .eq(RepairOrder::getIsDeleted, 0)
                .last("FOR UPDATE"));
        if (current == null
                || current.getSlaDeadline() == null
                || !current.getSlaDeadline().isBefore(now)
                || (current.getStatus() == null
                || (current.getStatus() != STATUS_WAIT_DISPATCH && current.getStatus() != STATUS_IN_SERVICE))) {
            return;
        }
        if (recentlyUrged(current.getId(), now)) {
            return;
        }

        if (current.getStatus() == STATUS_WAIT_DISPATCH) {
            urgeAdmins(current, now, "工单ID " + current.getId() + " 派单超时，请立即处理", "DISPATCH_TIMEOUT");
        } else {
            urgeWorker(current, now);
            urgeAdmins(current, now, "工单ID " + current.getId() + " 处理超时，请立即跟进", "COMPLETE_TIMEOUT");
        }

        current.setDelayCount((current.getDelayCount() == null ? 0 : current.getDelayCount()) + 1);
        current.setUpdateTime(now);
        repairOrderMapper.updateById(current);
    }

    private boolean recentlyUrged(Long orderId, LocalDateTime now) {
        if (orderId == null) {
            return false;
        }
        Long count = repairUrgeLogMapper.selectCount(new LambdaQueryWrapper<RepairUrgeLog>()
                .eq(RepairUrgeLog::getOrderId, orderId)
                .eq(RepairUrgeLog::getIsDeleted, 0)
                .ge(RepairUrgeLog::getUrgeTime, now.minusMinutes(URGE_INTERVAL_MINUTES)));
        return count != null && count > 0;
    }

    private void urgeAdmins(RepairOrder order, LocalDateTime now, String content, String reason) {
        List<User> admins = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, ROLE_ADMIN)
                .eq(User::getStatus, 1)
                .eq(User::getIsDeleted, 0));
        for (User admin : admins) {
            if (admin.getId() == null) {
                continue;
            }
            sendMessage(admin.getId(), content, now);
            insertUrgeLog(order.getId(), TARGET_ADMIN, admin.getId(), now, reason);
        }
        if (admins.isEmpty()) {
            insertUrgeLog(order.getId(), TARGET_ADMIN, null, now, reason);
        }
    }

    private void urgeWorker(RepairOrder order, LocalDateTime now) {
        if (order.getAssignee() == null) {
            insertUrgeLog(order.getId(), TARGET_WORKER, null, now, "COMPLETE_TIMEOUT_NO_ASSIGNEE");
            return;
        }
        User worker = userMapper.selectById(order.getAssignee());
        if (worker == null
                || worker.getRole() == null
                || worker.getRole() != ROLE_WORKER
                || worker.getStatus() == null
                || worker.getStatus() != 1
                || Integer.valueOf(1).equals(worker.getIsDeleted())) {
            insertUrgeLog(order.getId(), TARGET_WORKER, order.getAssignee(), now, "COMPLETE_TIMEOUT_WORKER_UNAVAILABLE");
            return;
        }
        sendMessage(worker.getId(), "工单ID " + order.getId() + " 处理超时，请尽快完成", now);
        insertUrgeLog(order.getId(), TARGET_WORKER, worker.getId(), now, "COMPLETE_TIMEOUT");
    }

    private void sendMessage(Long userId, String content, LocalDateTime now) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(TITLE);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreateTime(now);
        sysMessageMapper.insert(message);
    }

    private void insertUrgeLog(Long orderId, String targetType, Long targetId, LocalDateTime now, String reason) {
        RepairUrgeLog log = new RepairUrgeLog();
        log.setOrderId(orderId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setUrgeTime(now);
        log.setReason(reason);
        log.setCreateTime(now);
        log.setUpdateTime(now);
        log.setIsDeleted(0);
        repairUrgeLogMapper.insert(log);
    }
}
