package com.smartcommunity.service;

import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.RepairUrgeLog;
import com.smartcommunity.entity.SysMessage;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.RepairUrgeLogMapper;
import com.smartcommunity.mapper.SysMessageMapper;
import com.smartcommunity.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaMonitorServiceTest {

    @Mock
    private RepairOrderMapper repairOrderMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SysMessageMapper sysMessageMapper;
    @Mock
    private RepairUrgeLogMapper repairUrgeLogMapper;
    @Mock
    private PlatformTransactionManager transactionManager;

    private SlaMonitorService slaMonitorService;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
        slaMonitorService = new SlaMonitorService(
                repairOrderMapper,
                userMapper,
                sysMessageMapper,
                repairUrgeLogMapper,
                transactionManager
        );
    }

    @Test
    void recentlyUrgedOrderDoesNotSendRepeatedMessagesOrIncrementDelay() {
        RepairOrder order = new RepairOrder();
        order.setId(100L);
        order.setStatus(1);
        order.setSlaDeadline(LocalDateTime.now().minusMinutes(5));
        order.setIsDeleted(0);
        when(repairOrderMapper.selectList(any())).thenReturn(List.of(order));
        when(repairOrderMapper.selectOne(any())).thenReturn(order);
        when(repairUrgeLogMapper.selectCount(any())).thenReturn(1L);

        int handled = slaMonitorService.scanAndUrge();

        assertEquals(1, handled);
        verify(sysMessageMapper, never()).insert(any(SysMessage.class));
        verify(repairUrgeLogMapper, never()).insert(any(RepairUrgeLog.class));
        verify(repairOrderMapper, never()).updateById(any(RepairOrder.class));
    }
}
