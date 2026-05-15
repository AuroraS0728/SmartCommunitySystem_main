package com.smartcommunity.service;

import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairGradingServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RepairGradingService repairGradingService;

    @Test
    void urgentKeywordReturnsPriorityOne() {
        when(userMapper.selectById(1L)).thenReturn(userWithCredit(100));

        int priority = repairGradingService.calculatePriority("<p>厨房漏水，很严重</p>", 1L);

        assertEquals(1, priority);
    }

    @Test
    void expandedUrgentKeywordReturnsPriorityOne() {
        when(userMapper.selectById(2L)).thenReturn(userWithCredit(120));

        int priority = repairGradingService.calculatePriority("卫生间疑似漏电，还有火花", 2L);

        assertEquals(1, priority);
    }

    @Test
    void similarFloodExpressionReturnsPriorityOne() {
        when(userMapper.selectById(21L)).thenReturn(userWithCredit(100));

        int priority = repairGradingService.calculatePriority("厕所一直在往外漫水，地上全是水，已经控制不住了", 21L);

        assertEquals(1, priority);
    }

    @Test
    void similarLockExpressionReturnsPriorityOne() {
        when(userMapper.selectById(22L)).thenReturn(userWithCredit(90));

        int priority = repairGradingService.calculatePriority("门锁坏了，锁打不开，人被反锁在门外", 22L);

        assertEquals(1, priority);
    }

    @Test
    void highCreditDoesNotRaiseNormalToUrgent() {
        when(userMapper.selectById(3L)).thenReturn(userWithCredit(190));

        int priority = repairGradingService.calculatePriority("灯泡坏了，需要更换", 3L);

        assertEquals(2, priority);
    }

    @Test
    void lowCreditDoesNotDowngradeUrgentOrder() {
        when(userMapper.selectById(4L)).thenReturn(userWithCredit(40));

        int priority = repairGradingService.calculatePriority("家里断电了", 4L);

        assertEquals(1, priority);
    }

    @Test
    void highCreditCanRaiseLowPriorityToNormal() {
        when(userMapper.selectById(5L)).thenReturn(userWithCredit(185));

        int priority = repairGradingService.calculatePriority("想咨询一下维修报价", 5L);

        assertEquals(2, priority);
    }

    @Test
    void lowCreditLowersNormalPriority() {
        when(userMapper.selectById(6L)).thenReturn(userWithCredit(50));

        int priority = repairGradingService.calculatePriority("水龙头堵塞，需要上门看看", 6L);

        assertEquals(3, priority);
    }

    @Test
    void punctuationIsRemovedBeforeMatching() {
        when(userMapper.selectById(7L)).thenReturn(userWithCredit(100));

        int priority = repairGradingService.calculatePriority("门锁，打不开！！", 7L);

        assertEquals(1, priority);
    }

    private User userWithCredit(int creditScore) {
        User user = new User();
        user.setCreditScore(creditScore);
        return user;
    }
}
