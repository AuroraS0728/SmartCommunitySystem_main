package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserMapper userMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ImplicitProfileService implicitProfileService;
    private final RecommendRuleConfigService recommendRuleConfigService;

    public List<ServiceRecommendation> recommend(Long userId) {
        if (userId == null) {
            return List.of();
        }
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            return List.of();
        }

        long waterElectricRepairCount = countWaterElectricRepairs(userId);
        RuleConfigBundle ruleConfig = loadRuleConfig();
        Map<String, ServiceRecommendation> result = new LinkedHashMap<>();

        for (RecommendationRule rule : ruleConfig.rules()) {
            if (matchesExplicitRule(rule, user, waterElectricRepairCount)) {
                add(result, rule.toRecommendation(explicitReason(rule)));
            }
        }

        applyImplicitRules(result, implicitProfileService.getKeywords(userId), ruleConfig.rules());

        return new ArrayList<>(result.values()).stream().limit(ruleConfig.maxResults()).toList();
    }

    private long countWaterElectricRepairs(Long userId) {
        Long count = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getUserId, userId)
                .eq(RepairOrder::getIsDeleted, 0)
                .gt(RepairOrder::getCreateTime, LocalDateTime.now().minusMonths(6))
                .and(wrapper -> wrapper
                        .in(RepairOrder::getCategory, List.of("水电", "家电"))
                        .or()
                        .in(RepairOrder::getServiceMajor, List.of("水电维修", "家电维修"))));
        return count == null ? 0 : count;
    }

    private boolean matchesExplicitRule(RecommendationRule rule, User user, long waterElectricRepairCount) {
        return switch (rule.canonicalId()) {
            case "aged-friendly-upgrade" -> Integer.valueOf(1).equals(user.getHasElderly());
            case "water-electric-maintenance" -> waterElectricRepairCount >= rule.minRepairCount();
            case "deep-cleaning" -> user.getHouseArea() != null && user.getHouseArea() > rule.minHouseArea();
            case "pet-mite-cleaning" -> Integer.valueOf(1).equals(user.getHasPet());
            default -> false;
        };
    }

    private void add(Map<String, ServiceRecommendation> result, ServiceRecommendation item) {
        String key = StringUtils.hasText(item.getServiceId()) ? item.getServiceId() : item.getServiceName();
        result.putIfAbsent(key, item);
    }

    private void applyImplicitRules(Map<String, ServiceRecommendation> result,
                                    List<String> keywords,
                                    List<RecommendationRule> rules) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        if (containsAnyKeyword(keywords, Set.of("老人", "轮椅", "防滑", "年迈", "高龄"))) {
            findRule(rules, "aged-friendly-upgrade")
                    .ifPresent(rule -> add(result, rule.toRecommendation(implicitReason(rule))));
        }
        if (containsAnyKeyword(keywords, Set.of("宠物", "狗", "猫"))) {
            findRule(rules, "pet-mite-cleaning")
                    .ifPresent(rule -> add(result, rule.toRecommendation(implicitReason(rule))));
        }
    }

    private java.util.Optional<RecommendationRule> findRule(List<RecommendationRule> rules, String canonicalId) {
        return rules.stream()
                .filter(rule -> Objects.equals(rule.canonicalId(), canonicalId))
                .findFirst();
    }

    private boolean containsAnyKeyword(List<String> keywords, Set<String> targets) {
        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            String normalized = keyword.trim();
            for (String target : targets) {
                if (normalized.contains(target) || target.contains(normalized)) {
                    return true;
                }
            }
        }
        return false;
    }

    private RuleConfigBundle loadRuleConfig() {
        JsonNode root = recommendRuleConfigService.getRecommendRules();
        int maxResults = clamp(intValue(root, "maxResults", 3), 1, 10);
        List<RecommendationRule> rules = new ArrayList<>();
        JsonNode ruleNodes = root == null ? null : root.get("rules");
        if (ruleNodes != null && ruleNodes.isArray()) {
            for (JsonNode node : ruleNodes) {
                RecommendationRule rule = parseRule(node);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }
        if (rules.isEmpty()) {
            rules.add(defaultRule("aged-friendly-upgrade", "适老化改造", 2999, 1));
            rules.add(defaultRule("water-electric-maintenance", "水电保养套餐", 399, 2));
            rules.add(defaultRule("deep-cleaning", "深度保洁", 599, 3));
            rules.add(defaultRule("pet-mite-cleaning", "宠物除螨服务", 199, 4));
        }
        rules.sort(Comparator.comparingInt(RecommendationRule::priority));
        return new RuleConfigBundle(maxResults, rules);
    }

    private RecommendationRule parseRule(JsonNode node) {
        if (node == null || node.isNull() || !booleanValue(node, "enabled", true)) {
            return null;
        }
        String serviceName = textValue(node, "serviceName", "");
        String serviceId = textValue(node, "serviceId", inferServiceId(serviceName));
        String canonicalId = canonicalServiceId(serviceId, serviceName);
        if (!StringUtils.hasText(canonicalId)) {
            return null;
        }
        RecommendationRule defaults = defaultRule(canonicalId, defaultName(canonicalId), defaultPrice(canonicalId), defaultPriority(canonicalId));
        return new RecommendationRule(
                canonicalId,
                StringUtils.hasText(serviceId) ? serviceId.trim() : canonicalId,
                StringUtils.hasText(serviceName) ? serviceName.trim() : defaults.serviceName(),
                intValue(node, "price", defaults.price()),
                intValue(node, "priority", defaults.priority()),
                intValue(node, "minRepairCount", defaults.minRepairCount()),
                intValue(node, "minHouseArea", defaults.minHouseArea()),
                textValue(node, "reason", null),
                textValue(node, "explicitReason", null),
                textValue(node, "implicitReason", null)
        );
    }

    private RecommendationRule defaultRule(String canonicalId, String name, int price, int priority) {
        return new RecommendationRule(canonicalId, canonicalId, name, price, priority, 2, 120, null, null, null);
    }

    private String explicitReason(RecommendationRule rule) {
        if (StringUtils.hasText(rule.explicitReason())) {
            return rule.explicitReason().trim();
        }
        if (StringUtils.hasText(rule.reason())) {
            return rule.reason().trim();
        }
        return switch (rule.canonicalId()) {
            case "aged-friendly-upgrade" -> "您家中有老人，推荐安装防滑扶手/紧急呼叫设备";
            case "water-electric-maintenance" -> "您近期报修水电频繁，套餐更划算";
            case "deep-cleaning" -> "大户型房屋，推荐深度保洁服务";
            case "pet-mite-cleaning" -> "家有宠物，定期除螨更健康";
            default -> "根据您的家庭信息推荐";
        };
    }

    private String implicitReason(RecommendationRule rule) {
        if (StringUtils.hasText(rule.implicitReason())) {
            return rule.implicitReason().trim();
        }
        return switch (rule.canonicalId()) {
            case "aged-friendly-upgrade" -> "根据您发布的内容分析，您可能对适老化服务感兴趣";
            case "pet-mite-cleaning" -> "根据您发布的内容分析，您可能对宠物除螨服务感兴趣";
            default -> explicitReason(rule);
        };
    }

    private String inferServiceId(String serviceName) {
        return canonicalServiceId(null, serviceName);
    }

    private String canonicalServiceId(String serviceId, String serviceName) {
        String id = serviceId == null ? "" : serviceId.trim();
        String name = serviceName == null ? "" : serviceName.trim();
        if ("aged-friendly-upgrade".equals(id) || name.contains("适老")) {
            return "aged-friendly-upgrade";
        }
        if ("water-electric-maintenance".equals(id) || name.contains("水电")) {
            return "water-electric-maintenance";
        }
        if ("deep-cleaning".equals(id) || name.contains("深度保洁")) {
            return "deep-cleaning";
        }
        if ("pet-mite-cleaning".equals(id) || name.contains("宠物")) {
            return "pet-mite-cleaning";
        }
        return id;
    }

    private String defaultName(String canonicalId) {
        return switch (canonicalId) {
            case "aged-friendly-upgrade" -> "适老化改造";
            case "water-electric-maintenance" -> "水电保养套餐";
            case "deep-cleaning" -> "深度保洁";
            case "pet-mite-cleaning" -> "宠物除螨服务";
            default -> canonicalId;
        };
    }

    private int defaultPrice(String canonicalId) {
        return switch (canonicalId) {
            case "aged-friendly-upgrade" -> 2999;
            case "water-electric-maintenance" -> 399;
            case "deep-cleaning" -> 599;
            case "pet-mite-cleaning" -> 199;
            default -> 0;
        };
    }

    private int defaultPriority(String canonicalId) {
        return switch (canonicalId) {
            case "aged-friendly-upgrade" -> 1;
            case "water-electric-maintenance" -> 2;
            case "deep-cleaning" -> 3;
            case "pet-mite-cleaning" -> 4;
            default -> 100;
        };
    }

    private int intValue(JsonNode node, String field, int defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.canConvertToInt() ? value.asInt() : defaultValue;
    }

    private String textValue(JsonNode node, String field, String defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && !value.isNull() ? value.asText(defaultValue) : defaultValue;
    }

    private boolean booleanValue(JsonNode node, String field, boolean defaultValue) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isBoolean() ? value.asBoolean() : defaultValue;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record RuleConfigBundle(int maxResults, List<RecommendationRule> rules) {
    }

    private record RecommendationRule(String canonicalId,
                                      String serviceId,
                                      String serviceName,
                                      int price,
                                      int priority,
                                      int minRepairCount,
                                      int minHouseArea,
                                      String reason,
                                      String explicitReason,
                                      String implicitReason) {
        ServiceRecommendation toRecommendation(String resolvedReason) {
            return ServiceRecommendation.of(serviceId, serviceName, price, resolvedReason);
        }
    }
}
