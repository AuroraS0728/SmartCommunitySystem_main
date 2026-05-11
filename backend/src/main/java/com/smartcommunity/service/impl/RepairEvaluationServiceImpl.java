package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.mapper.RepairEvaluationMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.service.CreditService;
import com.smartcommunity.service.RepairEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairEvaluationServiceImpl implements RepairEvaluationService {

    private static final int STATUS_COMPLETED = 4;

    private final RepairEvaluationMapper repairEvaluationMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final CreditService creditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairEvaluation saveEvaluation(RepairOrder order, Integer rating, String comment, Integer anonymous) {
        RepairEvaluation current = repairEvaluationMapper.selectOne(new LambdaQueryWrapper<RepairEvaluation>()
                .eq(RepairEvaluation::getOrderId, order.getId())
                .last("LIMIT 1"));
        RepairEvaluation evaluation = current == null ? new RepairEvaluation() : current;
        LocalDateTime now = LocalDateTime.now();
        evaluation.setOrderId(order.getId());
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        evaluation.setIsAnonymous(anonymous);
        evaluation.setUpdateTime(now);
        if (current == null) {
            evaluation.setCreateTime(now);
            evaluation.setIsDeleted(0);
            repairEvaluationMapper.insert(evaluation);
        } else {
            repairEvaluationMapper.updateById(evaluation);
        }

        order.setStatus(STATUS_COMPLETED);
        if (order.getSlaDeadline() != null && now.isAfter(order.getSlaDeadline())) {
            order.setDelayCount((order.getDelayCount() == null ? 0 : order.getDelayCount()) + 1);
        }
        order.setUpdateTime(now);
        repairOrderMapper.updateById(order);
        if (current == null && Integer.valueOf(5).equals(rating)) {
            afterCommit(() -> creditService.changeCredit(order.getUserId(), 2, "报修5星好评"));
        }
        return evaluation;
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
