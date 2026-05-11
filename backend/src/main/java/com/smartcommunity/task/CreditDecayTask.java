package com.smartcommunity.task;

import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditDecayTask {

    private final UserMapper userMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    public void decayCreditScores() {
        try {
            int affected = userMapper.decayCreditScores();
            log.info("Credit score decay completed, affected={}", affected);
        } catch (Exception ex) {
            log.error("Credit score decay failed", ex);
        }
    }
}
