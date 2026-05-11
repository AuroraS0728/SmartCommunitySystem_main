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

        int priority = repairGradingService.calculatePriority("<p>厨房，漏水！很严重</p>", 1L);

        assertEquals(1, priority);
    }

    @Test
    void highCreditRaisesNormalPriority() {
        when(userMapper.selectById(2L)).thenReturn(userWithCredit(160));

        int priority = repairGradingService.calculatePriority("灯泡坏了，需要更换", 2L);

        assertEquals(1, priority);
    }

    @Test
    void lowCreditLowersUrgentPriority() {
        when(userMapper.selectById(3L)).thenReturn(userWithCredit(50));

        int priority = repairGradingService.calculatePriority("家里断电", 3L);

        assertEquals(2, priority);
    }

    @Test
    void lowPriorityKeywordCanBeRaisedByHighCredit() {
        when(userMapper.selectById(4L)).thenReturn(userWithCredit(180));

        int priority = repairGradingService.calculatePriority("想咨询一下维修报价", 4L);

        assertEquals(2, priority);
    }

    @Test
    void unknownDescriptionDefaultsToNormalThenAppliesLowCredit() {
        when(userMapper.selectById(5L)).thenReturn(userWithCredit(40));

        int priority = repairGradingService.calculatePriority("帮忙看一下这个问题", 5L);

        assertEquals(3, priority);
    }

    @Test
    void punctuationIsRemovedBeforeMatching() {
        when(userMapper.selectById(6L)).thenReturn(userWithCredit(100));

        int priority = repairGradingService.calculatePriority("水龙头（轻微）堵塞。", 6L);

        assertEquals(2, priority);
    }

    private User userWithCredit(int creditScore) {
        User user = new User();
        user.setCreditScore(creditScore);
        return user;
    }
}
