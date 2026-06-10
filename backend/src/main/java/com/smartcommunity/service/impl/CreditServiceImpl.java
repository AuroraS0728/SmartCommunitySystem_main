package com.smartcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.entity.CreditLog;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.CreditLogMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private static final int DEFAULT_CREDIT_SCORE = 100;
    private static final int MIN_CREDIT_SCORE = 0;
    private static final int MAX_CREDIT_SCORE = 200;

    private final UserMapper userMapper;
    private final CreditLogMapper creditLogMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void changeCredit(Long userId, int delta, String reason) {
        try {
            if (userId == null) {
                log.warn("Skip credit change: userId is null, delta={}, reason={}", delta, reason);
                return;
            }

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.executeWithoutResult(status -> {
                User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .eq(User::getIsDeleted, 0)
                        .last("FOR UPDATE"));
                if (user == null) {
                    log.warn("Skip credit change: user not found, userId={}, delta={}, reason={}", userId, delta, reason);
                    return;
                }

                int beforeScore = normalizeScore(user.getCreditScore());
                // 信用分统一限制在0到200之间，防止人工调整或自动加分导致越界。
                int afterScore = clamp(beforeScore + delta);
                LocalDateTime now = LocalDateTime.now();

                user.setCreditScore(afterScore);
                user.setCreditLastUpdate(now);
                user.setUpdateTime(now);
                userMapper.updateById(user);

                CreditLog creditLog = new CreditLog();
                creditLog.setUserId(userId);
                creditLog.setChangeValue(afterScore - beforeScore);
                creditLog.setReason(StringUtils.hasText(reason) ? reason.trim() : "信用分变更");
                creditLog.setBeforeScore(beforeScore);
                creditLog.setAfterScore(afterScore);
                creditLog.setCreateTime(now);
                creditLog.setUpdateTime(now);
                creditLog.setIsDeleted(0);
                creditLogMapper.insert(creditLog);
            });
        } catch (Exception ex) {
            log.error("Credit change failed, userId={}, delta={}, reason={}", userId, delta, reason, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CreditLog> getCreditLog(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<CreditLog> wrapper = new LambdaQueryWrapper<CreditLog>()
                .eq(CreditLog::getIsDeleted, 0)
                .orderByDesc(CreditLog::getId);
        if (userId != null) {
            wrapper.eq(CreditLog::getUserId, userId);
        }
        return creditLogMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
    }

    private int normalizeScore(Integer score) {
        return score == null ? DEFAULT_CREDIT_SCORE : clamp(score);
    }

    private int clamp(int value) {
        return Math.max(MIN_CREDIT_SCORE, Math.min(MAX_CREDIT_SCORE, value));
    }

}
