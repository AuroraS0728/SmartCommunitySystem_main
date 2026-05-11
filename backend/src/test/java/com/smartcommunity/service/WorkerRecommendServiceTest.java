package com.smartcommunity.service;

import com.smartcommunity.mapper.WorkerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerRecommendServiceTest {

    @Mock
    private WorkerMapper workerMapper;

    @InjectMocks
    private WorkerRecommendService workerRecommendService;

    @Test
    void recommendsUserWorkerFromStaffingOnly() {
        when(workerMapper.selectAvailableUserWorkerByCategory("水电")).thenReturn(3L);

        Long workerId = workerRecommendService.recommendWorker(" 水电 ");

        assertEquals(3L, workerId);
        verify(workerMapper).selectAvailableUserWorkerByCategory("水电");
    }

    @Test
    void emptyCategoryReturnsNull() {
        assertNull(workerRecommendService.recommendWorker(" "));
    }
}
