package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.dto.request.RecommendRuleSaveReq;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.RecommendRule;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.RecommendRuleMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendRuleService {

    private final RecommendRuleMapper recommendRuleMapper;
    private final UserMapper userMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ImplicitProfileService implicitProfileService;
    private final ObjectMapper objectMapper;

    @Transactional
    public RecommendRule createRule(RecommendRuleSaveReq req) {
        RecommendRule rule = new RecommendRule();
        fillRule(rule, req);
        rule.setCreateTime(java.time.LocalDateTime.now());
        rule.setUpdateTime(rule.getCreateTime());
        rule.setIsDeleted(0);
        recommendRuleMapper.insert(rule);
        return rule;
    }

    @Transactional
    public RecommendRule updateRule(Long id, RecommendRuleSaveReq req) {
        RecommendRule rule = requireRule(id);
        fillRule(rule, req);
        rule.setUpdateTime(java.time.LocalDateTime.now());
        recommendRuleMapper.updateById(rule);
        return rule;
    }

    @Transactional
    public void deleteRule(Long id) {
        RecommendRule rule = requireRule(id);
        rule.setIsDeleted(1);
        rule.setUpdateTime(java.time.LocalDateTime.now());
        recommendRuleMapper.updateById(rule);
    }

    public List<RecommendRule> listRules() {
        bootstrapDefaultRulesIfAbsent();
        return recommendRuleMapper.selectList(new LambdaQueryWrapper<RecommendRule>()
                        .eq(RecommendRule::getIsDeleted, 0)
                        .orderByAsc(RecommendRule::getPriority)
                        .orderByAsc(RecommendRule::getId))
                .stream()
                .sorted(Comparator.comparing(rule -> rule.getPriority() == null ? 99 : rule.getPriority()))
                .toList();
    }

    public List<ServiceRecommendation> recommend(Long userId, int limit, boolean popupOnly) {
        bootstrapDefaultRulesIfAbsent();
        if (userId == null) {
            return List.of();
        }
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            return List.of();
        }
        long waterElectricRepairCount = countWaterElectricRepairs(userId);
        List<String> keywords = implicitProfileService.getKeywords(userId);
        // 把用户画像、历史报修次数、隐式关键词统一整理成规则上下文，后面表达式都从这里取值。
        RuleContext context = buildContext(user, waterElectricRepairCount, keywords);
        Map<String, ServiceRecommendation> matched = new LinkedHashMap<>();

        // 后续如果要新增推荐规则，优先改数据库中的recommend_rule记录，
        // 不建议在这里写死具体服务，这样后台规则管理才能继续生效。
        for (RecommendRule rule : activeRules()) {
            JsonNode condition = parseCondition(rule.getConditionJson());
            if (popupOnly && !booleanCondition(condition, "popupEnabled", true)) {
                continue;
            }
            if (!matchesRule(rule, condition, context)) {
                continue;
            }
            String serviceKey = StringUtils.hasText(rule.getServiceId()) ? rule.getServiceId() : String.valueOf(rule.getId());
            matched.putIfAbsent(serviceKey, ServiceRecommendation.of(
                    serviceKey,
                    clean(rule.getServiceName(), "未命名推荐"),
                    rule.getPrice() == null ? 0 : rule.getPrice(),
                    resolveReason(rule, condition, context),
                    clean(rule.getImageUrl(), ""),
                    textCondition(condition, "actionPath", "/pages/service/service")
            ));
        }

        return matched.values().stream().limit(Math.max(limit, 1)).toList();
    }

    private void fillRule(RecommendRule rule, RecommendRuleSaveReq req) {
        if (req == null) {
            throw new IllegalArgumentException("recommend rule payload is empty");
        }
        String ruleName = clean(req.getRuleName(), "");
        String serviceId = clean(req.getServiceId(), "");
        String serviceName = clean(req.getServiceName(), "");
        String ruleExpression = clean(req.getRuleExpression(), "");
        if (!StringUtils.hasText(ruleName)) {
            throw new IllegalArgumentException("ruleName is empty");
        }
        if (!StringUtils.hasText(serviceName)) {
            throw new IllegalArgumentException("serviceName is empty");
        }
        if (!StringUtils.hasText(ruleExpression)) {
            throw new IllegalArgumentException("ruleExpression is empty");
        }
        if (StringUtils.hasText(req.getConditionJson())) {
            parseCondition(req.getConditionJson());
        }
        rule.setRuleName(ruleName);
        rule.setServiceId(StringUtils.hasText(serviceId) ? serviceId : normalizeKey(serviceName));
        rule.setServiceName(serviceName);
        rule.setRuleExpression(ruleExpression);
        rule.setConditionJson(clean(req.getConditionJson(), ""));
        rule.setImageUrl(clean(req.getImageUrl(), ""));
        rule.setPrice(req.getPrice() == null ? 0 : req.getPrice());
        rule.setPriority(req.getPriority() == null ? 99 : req.getPriority());
        rule.setEnabled(req.getEnabled() == null ? 1 : (req.getEnabled() == 0 ? 0 : 1));
    }

    private RecommendRule requireRule(Long id) {
        RecommendRule rule = recommendRuleMapper.selectById(id);
        if (rule == null || Integer.valueOf(1).equals(rule.getIsDeleted())) {
            throw new IllegalArgumentException("recommend rule not found");
        }
        return rule;
    }

    private List<RecommendRule> activeRules() {
        return recommendRuleMapper.selectList(new LambdaQueryWrapper<RecommendRule>()
                        .eq(RecommendRule::getIsDeleted, 0)
                        .eq(RecommendRule::getEnabled, 1)
                        .orderByAsc(RecommendRule::getPriority)
                        .orderByAsc(RecommendRule::getId))
                .stream()
                .sorted(Comparator.comparing(rule -> rule.getPriority() == null ? 99 : rule.getPriority()))
                .toList();
    }

    private long countWaterElectricRepairs(Long userId) {
        // 近似统计用户对水电/家电维修的需求频率，用于推荐水电保养套餐。
        Long count = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getUserId, userId)
                .eq(RepairOrder::getIsDeleted, 0)
                .and(wrapper -> wrapper
                        .in(RepairOrder::getCategory, List.of("水电", "家电", "水电维修", "家电维修"))
                        .or()
                        .in(RepairOrder::getServiceMajor, List.of("水电维修", "家电维修"))));
        return count == null ? 0 : count;
    }

    private RuleContext buildContext(User user, long waterElectricRepairCount, List<String> keywords) {
        Map<String, Object> values = new HashMap<>();
        // 同时放驼峰和下划线两种字段名，方便后台规则表达式用不同写法。
        values.put(normalizeKey("hasElderly"), truthy(user.getHasElderly()));
        values.put(normalizeKey("has_elderly"), truthy(user.getHasElderly()));
        values.put(normalizeKey("hasChild"), truthy(user.getHasChild()));
        values.put(normalizeKey("has_child"), truthy(user.getHasChild()));
        values.put(normalizeKey("hasPet"), truthy(user.getHasPet()));
        values.put(normalizeKey("has_pet"), truthy(user.getHasPet()));
        values.put(normalizeKey("houseArea"), numberValue(user.getHouseArea()));
        values.put(normalizeKey("house_area"), numberValue(user.getHouseArea()));
        values.put(normalizeKey("roomCount"), numberValue(user.getRoomCount()));
        values.put(normalizeKey("room_count"), numberValue(user.getRoomCount()));
        values.put(normalizeKey("waterElectricRepairCount"), waterElectricRepairCount);
        values.put(normalizeKey("water_electric_repair_count"), waterElectricRepairCount);
        return new RuleContext(values, keywords == null ? List.of() : keywords);
    }

    private boolean matchesRule(RecommendRule rule, JsonNode condition, RuleContext context) {
        boolean expressionMatch = evaluateExpression(rule.getRuleExpression(), context.values());
        if (!expressionMatch) {
            return false;
        }
        List<String> keywordsAny = textArrayCondition(condition, "keywordsAny");
        if (!keywordsAny.isEmpty() && !containsAnyKeyword(context.keywords(), keywordsAny)) {
            return false;
        }
        Integer minRepairCount = integerCondition(condition, "minRepairCount");
        if (minRepairCount != null) {
            long count = numberAsLong(context.values().get(normalizeKey("waterElectricRepairCount")));
            if (count < minRepairCount) {
                return false;
            }
        }
        Integer minHouseArea = integerCondition(condition, "minHouseArea");
        if (minHouseArea != null) {
            long area = numberAsLong(context.values().get(normalizeKey("houseArea")));
            if (area < minHouseArea) {
                return false;
            }
        }
        return true;
    }

    boolean evaluateExpression(String expression, Map<String, Object> rawContext) {
        if (!StringUtils.hasText(expression)) {
            return true;
        }
        Map<String, Object> context = new HashMap<>();
        rawContext.forEach((key, value) -> context.put(normalizeKey(key), value));
        String normalized = expression
                .replace("AND", "&&")
                .replace("and", "&&")
                .replace("或", "||")
                .replace("OR", "||")
                .replace("or", "||")
                .replace("且", "&&")
                .replace("并且", "&&");
        // 简单表达式解析器：先按“或”拆分，再按“且”逐个判断条件。
        for (String orPart : normalized.split("\\|\\|")) {
            boolean andMatch = true;
            for (String andPart : orPart.split("&&")) {
                if (!evaluateClause(andPart.trim(), context)) {
                    andMatch = false;
                    break;
                }
            }
            if (andMatch) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateClause(String clause, Map<String, Object> context) {
        if (!StringUtils.hasText(clause)) {
            return true;
        }
        for (String operator : List.of(">=", "<=", "==", "!=", ">", "<")) {
            int index = clause.indexOf(operator);
            if (index > 0) {
                String left = clause.substring(0, index).trim();
                String right = clause.substring(index + operator.length()).trim();
                Object leftValue = context.get(normalizeKey(left));
                Object rightValue = parseLiteral(right);
                return compare(leftValue, rightValue, operator);
            }
        }
        Object value = context.get(normalizeKey(clause));
        return truthy(value);
    }

    private boolean compare(Object left, Object right, String operator) {
        if (left instanceof Number || right instanceof Number) {
            double a = numberAsDouble(left);
            double b = numberAsDouble(right);
            return switch (operator) {
                case ">=" -> a >= b;
                case "<=" -> a <= b;
                case "==" -> Double.compare(a, b) == 0;
                case "!=" -> Double.compare(a, b) != 0;
                case ">" -> a > b;
                case "<" -> a < b;
                default -> false;
            };
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            boolean a = truthy(left);
            boolean b = truthy(right);
            return switch (operator) {
                case "==" -> a == b;
                case "!=" -> a != b;
                default -> false;
            };
        }
        String a = left == null ? "" : String.valueOf(left).trim();
        String b = right == null ? "" : String.valueOf(right).trim();
        return switch (operator) {
            case "==" -> Objects.equals(a, b);
            case "!=" -> !Objects.equals(a, b);
            default -> false;
        };
    }

    private Object parseLiteral(String raw) {
        String value = raw.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private String resolveReason(RecommendRule rule, JsonNode condition, RuleContext context) {
        String popupReason = textCondition(condition, "popupReason", "");
        if (StringUtils.hasText(popupReason)) {
            return popupReason;
        }
        String reason = textCondition(condition, "reason", "");
        if (StringUtils.hasText(reason)) {
            return reason;
        }
        if (!context.keywords().isEmpty()) {
            return "根据业主画像和近期服务记录推荐";
        }
        return "命中规则：" + clean(rule.getRuleName(), "推荐规则");
    }

    private JsonNode parseCondition(String json) {
        if (!StringUtils.hasText(json)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("conditionJson is invalid");
        }
    }

    private boolean booleanCondition(JsonNode node, String field, boolean defaultValue) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return defaultValue;
        }
        JsonNode value = node.get(field);
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        return Boolean.parseBoolean(value.asText());
    }

    private Integer integerCondition(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value.canConvertToInt()) {
            return value.asInt();
        }
        try {
            return Integer.parseInt(value.asText());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String textCondition(JsonNode node, String field, String defaultValue) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return defaultValue;
        }
        return node.get(field).asText(defaultValue);
    }

    private List<String> textArrayCondition(JsonNode node, String field) {
        if (node == null || node.get(field) == null || !node.get(field).isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.get(field).forEach(item -> {
            String text = item == null || item.isNull() ? "" : item.asText("");
            if (StringUtils.hasText(text)) {
                values.add(text.trim());
            }
        });
        return values;
    }

    private boolean containsAnyKeyword(List<String> keywords, List<String> targets) {
        for (String keyword : keywords) {
            if (!StringUtils.hasText(keyword)) {
                continue;
            }
            for (String target : targets) {
                if (!StringUtils.hasText(target)) {
                    continue;
                }
                if (keyword.contains(target) || target.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean truthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Number numberValue(Integer value) {
        return value == null ? 0 : value;
    }

    private double numberAsDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0D;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }

    private long numberAsLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private String normalizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private String clean(String value, String fallback) {
        String text = value == null ? "" : value.trim();
        return StringUtils.hasText(text) ? text : fallback;
    }

    private void bootstrapDefaultRulesIfAbsent() {
        Long count = recommendRuleMapper.selectCount(new LambdaQueryWrapper<RecommendRule>()
                .eq(RecommendRule::getIsDeleted, 0));
        if (count != null && count > 0) {
            return;
        }
        List<RecommendRuleSaveReq> defaults = List.of(
                defaultRule("适老化改造", "aged-friendly-upgrade", "has_elderly == true", """
                        {"reason":"家中有老人，优先推荐适老化改造服务","popupEnabled":true,"keywordsAny":["老人","轮椅","高龄"],"actionPath":"/pages/service/service"}
                        """, "https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b?auto=format&fit=crop&w=900&q=80", 2999, 1),
                defaultRule("水电保养套餐", "water-electric-maintenance", "water_electric_repair_count >= 2", """
                        {"reason":"近期水电报修较多，建议集中保养","popupEnabled":true,"minRepairCount":2,"actionPath":"/pages/repair/list"}
                        """, "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=900&q=80", 399, 2),
                defaultRule("深度保洁", "deep-cleaning", "house_area >= 120", """
                        {"reason":"户型面积较大，推荐深度保洁","popupEnabled":false,"minHouseArea":120,"actionPath":"/pages/service/service"}
                        """, "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=900&q=80", 599, 3),
                defaultRule("宠物除螨服务", "pet-mite-cleaning", "has_pet == true", """
                        {"reason":"家有宠物，建议定期除螨","popupEnabled":true,"keywordsAny":["宠物","狗","猫"],"actionPath":"/pages/service/service"}
                        """, "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=900&q=80", 199, 4)
        );
        defaults.forEach(this::insertDefaultRule);
    }

    private void insertDefaultRule(RecommendRuleSaveReq req) {
        RecommendRule rule = new RecommendRule();
        fillRule(rule, req);
        rule.setCreateTime(java.time.LocalDateTime.now());
        rule.setUpdateTime(rule.getCreateTime());
        rule.setIsDeleted(0);
        recommendRuleMapper.insert(rule);
    }

    private RecommendRuleSaveReq defaultRule(String ruleName,
                                             String serviceId,
                                             String expression,
                                             String conditionJson,
                                             String imageUrl,
                                             Integer price,
                                             Integer priority) {
        RecommendRuleSaveReq req = new RecommendRuleSaveReq();
        req.setRuleName(ruleName);
        req.setServiceId(serviceId);
        req.setServiceName(ruleName);
        req.setRuleExpression(expression);
        req.setConditionJson(conditionJson);
        req.setImageUrl(imageUrl);
        req.setPrice(price);
        req.setPriority(priority);
        req.setEnabled(1);
        return req;
    }

    private record RuleContext(Map<String, Object> values, List<String> keywords) {
    }
}
