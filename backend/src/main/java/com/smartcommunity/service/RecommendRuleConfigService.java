package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.entity.SystemConfig;
import com.smartcommunity.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecommendRuleConfigService {

    private static final String CONFIG_KEY = "recommend.rules";
    private static final String DEFAULT_RULES = """
            {
              "maxResults": 3,
              "rules": [
                {
                  "serviceId": "aged-friendly-upgrade",
                  "serviceName": "适老化改造",
                  "price": 2999,
                  "priority": 1,
                  "condition": "hasElderly == true 或隐式关键词包含 老人/轮椅/防滑/年迈/高龄"
                },
                {
                  "serviceId": "water-electric-maintenance",
                  "serviceName": "水电保养套餐",
                  "price": 399,
                  "priority": 2,
                  "condition": "近6个月水电/家电报修次数 >= 2"
                },
                {
                  "serviceId": "deep-cleaning",
                  "serviceName": "深度保洁",
                  "price": 599,
                  "priority": 3,
                  "condition": "houseArea > 120"
                },
                {
                  "serviceId": "pet-mite-cleaning",
                  "serviceName": "宠物除螨服务",
                  "price": 199,
                  "priority": 4,
                  "condition": "hasPet == true 或隐式关键词包含 宠物/狗/猫"
                }
              ]
            }
            """;

    private final SystemConfigMapper systemConfigMapper;
    private final ObjectMapper objectMapper;

    public JsonNode getRecommendRules() {
        SystemConfig config = selectConfig();
        String json = config == null || config.getConfigValue() == null ? DEFAULT_RULES : config.getConfigValue();
        try {
            return objectMapper.readTree(json);
        } catch (Exception ignored) {
            try {
                return objectMapper.readTree(DEFAULT_RULES);
            } catch (Exception ex) {
                throw new IllegalStateException("default recommend rules json invalid", ex);
            }
        }
    }

    @Transactional
    public JsonNode saveRecommendRules(JsonNode rules) {
        if (rules == null || rules.isNull()) {
            throw new IllegalArgumentException("rules json is empty");
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(rules);
        } catch (Exception ex) {
            throw new IllegalArgumentException("rules json is invalid", ex);
        }

        LocalDateTime now = LocalDateTime.now();
        SystemConfig config = selectConfig();
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(CONFIG_KEY);
            config.setDescription("推荐规则配置");
            config.setConfigValue(json);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setIsDeleted(0);
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(json);
            config.setUpdateTime(now);
            config.setIsDeleted(0);
            systemConfigMapper.updateById(config);
        }
        return rules;
    }

    private SystemConfig selectConfig() {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, CONFIG_KEY)
                .eq(SystemConfig::getIsDeleted, 0)
                .last("LIMIT 1"));
    }
}
