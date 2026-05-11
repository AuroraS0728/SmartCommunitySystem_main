package com.smartcommunity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserMapper userMapper;
    @Mock
    private RepairOrderMapper repairOrderMapper;
    @Mock
    private ImplicitProfileService implicitProfileService;
    @Mock
    private RecommendRuleConfigService recommendRuleConfigService;

    @InjectMocks
    private RecommendService recommendService;

    @Test
    void savedRuleConfigControlsRecommendationDisplayAndPriority() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setIsDeleted(0);
        user.setHasElderly(1);
        user.setHasPet(1);
        when(userMapper.selectById(10L)).thenReturn(user);
        when(repairOrderMapper.selectCount(any())).thenReturn(0L);
        when(implicitProfileService.getKeywords(10L)).thenReturn(List.of());
        when(recommendRuleConfigService.getRecommendRules()).thenReturn(objectMapper.readTree("""
                {
                  "maxResults": 1,
                  "rules": [
                    {
                      "serviceId": "aged-friendly-upgrade",
                      "serviceName": "银发改造",
                      "price": 1888,
                      "priority": 2,
                      "explicitReason": "后台配置的适老化理由"
                    },
                    {
                      "serviceId": "pet-mite-cleaning",
                      "serviceName": "宠物护理",
                      "price": 168,
                      "priority": 1,
                      "explicitReason": "后台配置的宠物理由"
                    }
                  ]
                }
                """));

        List<ServiceRecommendation> result = recommendService.recommend(10L);

        assertEquals(1, result.size());
        assertEquals("pet-mite-cleaning", result.get(0).getServiceId());
        assertEquals("宠物护理", result.get(0).getServiceName());
        assertEquals(168, result.get(0).getPrice());
        assertEquals("后台配置的宠物理由", result.get(0).getReason());
    }

    @Test
    void implicitKeywordsUseConfiguredServiceDisplay() throws Exception {
        User user = new User();
        user.setId(11L);
        user.setIsDeleted(0);
        user.setHasElderly(0);
        when(userMapper.selectById(11L)).thenReturn(user);
        when(repairOrderMapper.selectCount(any())).thenReturn(0L);
        when(implicitProfileService.getKeywords(11L)).thenReturn(List.of("轮椅"));
        when(recommendRuleConfigService.getRecommendRules()).thenReturn(objectMapper.readTree("""
                {
                  "maxResults": 3,
                  "rules": [
                    {
                      "serviceId": "aged-friendly-upgrade",
                      "serviceName": "适老安全包",
                      "price": 2680,
                      "priority": 1,
                      "implicitReason": "后台配置的隐式画像理由"
                    }
                  ]
                }
                """));

        List<ServiceRecommendation> result = recommendService.recommend(11L);

        assertEquals(1, result.size());
        assertEquals("适老安全包", result.get(0).getServiceName());
        assertEquals(2680, result.get(0).getPrice());
        assertEquals("后台配置的隐式画像理由", result.get(0).getReason());
    }
}
