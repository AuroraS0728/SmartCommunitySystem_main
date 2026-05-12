package com.smartcommunity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.dto.request.RecommendRuleSaveReq;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.RecommendRule;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RecommendRuleMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendRuleServiceTest {

    @Mock
    private RecommendRuleMapper recommendRuleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RepairOrderMapper repairOrderMapper;
    @Mock
    private ImplicitProfileService implicitProfileService;

    @Test
    void popupRecommendationUsesDynamicRuleExpressionAndConditionJson() {
        RecommendRuleService service = new RecommendRuleService(
                recommendRuleMapper,
                userMapper,
                repairOrderMapper,
                implicitProfileService,
                new ObjectMapper()
        );
        User user = new User();
        user.setId(9L);
        user.setHasElderly(1);
        user.setHasPet(1);
        user.setHouseArea(130);
        user.setIsDeleted(0);

        when(recommendRuleMapper.selectCount(any())).thenReturn(1L);
        when(userMapper.selectById(9L)).thenReturn(user);
        when(repairOrderMapper.selectCount(any())).thenReturn(3L);
        when(implicitProfileService.getKeywords(9L)).thenReturn(List.of("轮椅", "宠物"));
        when(recommendRuleMapper.selectList(any())).thenReturn(List.of(
                rule(1L, "适老化改造", "aged-friendly-upgrade", "has_elderly == true",
                        "{\"popupEnabled\":true,\"reason\":\"适老化推荐\",\"actionPath\":\"/pages/service/service\"}", 1),
                rule(2L, "水电保养套餐", "water-electric-maintenance", "water_electric_repair_count >= 2",
                        "{\"popupEnabled\":true,\"reason\":\"水电推荐\"}", 2),
                rule(3L, "深度保洁", "deep-cleaning", "house_area >= 120",
                        "{\"popupEnabled\":false,\"reason\":\"保洁推荐\"}", 3)
        ));

        List<ServiceRecommendation> popup = service.recommend(9L, 2, true);

        assertEquals(2, popup.size());
        assertEquals("aged-friendly-upgrade", popup.get(0).getServiceId());
        assertEquals("水电保养套餐", popup.get(1).getServiceName());
        assertTrue(popup.stream().noneMatch(item -> "deep-cleaning".equals(item.getServiceId())));
    }

    private RecommendRule rule(Long id, String serviceName, String serviceId, String expression, String conditionJson, int priority) {
        RecommendRule rule = new RecommendRule();
        rule.setId(id);
        rule.setRuleName(serviceName);
        rule.setServiceName(serviceName);
        rule.setServiceId(serviceId);
        rule.setRuleExpression(expression);
        rule.setConditionJson(conditionJson);
        rule.setPriority(priority);
        rule.setPrice(199);
        rule.setEnabled(1);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setIsDeleted(0);
        return rule;
    }
}
