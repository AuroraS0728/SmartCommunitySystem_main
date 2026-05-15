package com.smartcommunity.service;

import com.smartcommunity.dto.response.ServiceRecommendation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock
    private RecommendRuleService recommendRuleService;

    @InjectMocks
    private RecommendService recommendService;

    @Test
    void recommendDelegatesToRuleServiceWithHomepageDefaults() {
        ServiceRecommendation item = ServiceRecommendation.of(
                "pet-mite-cleaning",
                "宠物除螨服务",
                199,
                "家有宠物，定期除螨更健康"
        );
        when(recommendRuleService.recommend(10L, 3, false)).thenReturn(List.of(item));

        List<ServiceRecommendation> result = recommendService.recommend(10L);

        assertEquals(1, result.size());
        assertEquals("pet-mite-cleaning", result.get(0).getServiceId());
        verify(recommendRuleService).recommend(10L, 3, false);
    }
}
