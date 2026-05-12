package com.smartcommunity.service;

import com.smartcommunity.dto.request.ActivityRegistrationReq;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityRegistration;
import com.smartcommunity.mapper.ActivityMapper;
import com.smartcommunity.mapper.ActivityRegistrationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Test
    void registerRejectsWhenAgeLimitDoesNotMatch() {
        ActivityService service = new ActivityService(activityMapper, activityRegistrationMapper);
        Activity activity = activity(11L);
        activity.setAgeLimit(">=18");
        when(activityMapper.selectById(11L)).thenReturn(activity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(null);

        ActivityRegistrationReq req = new ActivityRegistrationReq();
        req.setNickname("小王");
        req.setPhone("13800000000");
        req.setAge(16);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.register(11L, 5L, req));
        assertEquals("年龄不符合当前活动限制", error.getMessage());
    }

    @Test
    void registerIncrementsParticipantsForPendingSignup() {
        ActivityService service = new ActivityService(activityMapper, activityRegistrationMapper);
        Activity activity = activity(12L);
        activity.setAgeLimit(">=18");
        activity.setCurrentParticipants(1);
        activity.setMaxParticipants(3);
        when(activityMapper.selectById(12L)).thenReturn(activity);
        when(activityRegistrationMapper.selectOne(any())).thenReturn(null);

        ActivityRegistrationReq req = new ActivityRegistrationReq();
        req.setNickname("老李");
        req.setPhone("13900000000");
        req.setAge(28);
        req.setHasChild(1);
        req.setHasPet(0);

        ActivityRegistration registration = service.register(12L, 8L, req);

        assertEquals(0, registration.getStatus());
        assertEquals(2, activity.getCurrentParticipants());
        ArgumentCaptor<ActivityRegistration> captor = ArgumentCaptor.forClass(ActivityRegistration.class);
        verify(activityRegistrationMapper).insert(captor.capture());
        assertEquals("老李", captor.getValue().getNickname());
        verify(activityMapper).updateById(activity);
    }

    private Activity activity(Long id) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle("周末露营");
        activity.setType("露营");
        activity.setStatus(0);
        activity.setLocation("社区北门");
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(2));
        activity.setMaxParticipants(10);
        activity.setCurrentParticipants(0);
        activity.setWithChildRequired(0);
        activity.setWithPetRequired(0);
        activity.setIsDeleted(0);
        return activity;
    }
}
