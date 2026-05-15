package com.smartcommunity.service;

import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairGradingService {

    private static final int PRIORITY_URGENT = 1;
    private static final int PRIORITY_NORMAL = 2;
    private static final int PRIORITY_LOW = 3;
    private static final int DEFAULT_CREDIT_SCORE = 100;
    private static final int HIGH_CREDIT_THRESHOLD = 180;
    private static final int LOW_CREDIT_THRESHOLD = 60;

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{P}\\p{S}\\s]+");
    private static final Map<Integer, Map<String, List<String>>> KEYWORD_LEVELS = new LinkedHashMap<>();
    private static final Map<String, String> SYNONYM_TO_CANONICAL = new LinkedHashMap<>();
    private static final List<Map.Entry<String, String>> SORTED_SYNONYM_ENTRIES;

    static {
        registerKeyword(PRIORITY_URGENT, "漏水", "跑水", "渗水", "滴水", "冒水", "漏个不停", "一直流水", "止不住流水");
        registerKeyword(PRIORITY_URGENT, "爆管", "水管爆裂", "管道爆了", "水管裂开", "水管炸裂");
        registerKeyword(PRIORITY_URGENT, "漫水", "厕所一直在往外漫水", "卫生间漫水", "水漫出来", "地上全是水", "水流控制不住");
        registerKeyword(PRIORITY_URGENT, "断电", "停电", "没电", "家里没电", "整个房间没电");
        registerKeyword(PRIORITY_URGENT, "跳闸", "断闸", "总闸跳了", "电闸跳了", "老是跳闸");
        registerKeyword(PRIORITY_URGENT, "漏电", "电线短路", "短路", "打火", "火花", "插座冒火");
        registerKeyword(PRIORITY_URGENT, "冒烟", "着火", "起火", "烧焦味", "焦味很重");
        registerKeyword(PRIORITY_URGENT, "燃气泄漏", "煤气泄漏", "天然气泄漏", "煤气味很重", "燃气味很重");
        registerKeyword(PRIORITY_URGENT, "门锁故障", "门锁打不开", "锁打不开", "门打不开", "反锁在屋里", "反锁在门外");
        registerKeyword(PRIORITY_URGENT, "门禁故障", "门禁失灵", "门禁打不开", "刷卡进不去");
        registerKeyword(PRIORITY_URGENT, "下水倒灌", "马桶反水", "污水倒灌", "地漏返水", "厕所返水");

        registerKeyword(PRIORITY_NORMAL, "灯泡", "灯不亮", "照明坏了");
        registerKeyword(PRIORITY_NORMAL, "水龙头", "龙头");
        registerKeyword(PRIORITY_NORMAL, "开关", "面板开关");
        registerKeyword(PRIORITY_NORMAL, "插座", "插口");
        registerKeyword(PRIORITY_NORMAL, "马桶", "坐便器");
        registerKeyword(PRIORITY_NORMAL, "堵塞", "堵住", "不通");
        registerKeyword(PRIORITY_NORMAL, "窗帘电机", "智能窗帘", "窗帘电动机");
        registerKeyword(PRIORITY_NORMAL, "热水器", "热水器不热");
        registerKeyword(PRIORITY_NORMAL, "空调", "空调不制冷", "空调不出风");
        registerKeyword(PRIORITY_NORMAL, "玻璃", "窗玻璃");
        registerKeyword(PRIORITY_NORMAL, "柜门", "橱柜门", "柜子门");
        registerKeyword(PRIORITY_NORMAL, "地漏", "下水口");

        registerKeyword(PRIORITY_LOW, "咨询", "想问问", "咨询一下", "了解一下");
        registerKeyword(PRIORITY_LOW, "建议", "反馈建议");
        registerKeyword(PRIORITY_LOW, "报价", "多少钱", "怎么收费", "维修费用");
        registerKeyword(PRIORITY_LOW, "预约", "提前预约");
        registerKeyword(PRIORITY_LOW, "上门时间", "什么时候来", "几时上门");

        SORTED_SYNONYM_ENTRIES = SYNONYM_TO_CANONICAL.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()))
                .collect(Collectors.toList());
    }

    private final UserMapper userMapper;

    public int calculatePriority(String description, Long userId) {
        int baseLevel = matchBaseLevel(description);
        int creditScore = getCreditScore(userId);

        if (baseLevel == PRIORITY_URGENT) {
            return PRIORITY_URGENT;
        }
        if (baseLevel == PRIORITY_LOW && creditScore >= HIGH_CREDIT_THRESHOLD) {
            return PRIORITY_NORMAL;
        }
        if (baseLevel == PRIORITY_NORMAL && creditScore < LOW_CREDIT_THRESHOLD) {
            return PRIORITY_LOW;
        }
        return clampPriority(baseLevel);
    }

    private int matchBaseLevel(String description) {
        String normalized = normalizeForMatch(description);
        if (!StringUtils.hasText(normalized)) {
            return PRIORITY_NORMAL;
        }
        String canonicalText = canonicalizeText(normalized);
        for (Map.Entry<Integer, Map<String, List<String>>> entry : KEYWORD_LEVELS.entrySet()) {
            for (String keyword : entry.getValue().keySet()) {
                if (canonicalText.contains(normalizeForMatch(keyword))) {
                    return entry.getKey();
                }
            }
        }
        return PRIORITY_NORMAL;
    }

    private int getCreditScore(Long userId) {
        if (userId == null) {
            return DEFAULT_CREDIT_SCORE;
        }
        try {
            User user = userMapper.selectById(userId);
            if (user == null || user.getCreditScore() == null) {
                return DEFAULT_CREDIT_SCORE;
            }
            return user.getCreditScore();
        } catch (Exception ex) {
            log.warn("Failed to load user credit score, userId={}", userId, ex);
            return DEFAULT_CREDIT_SCORE;
        }
    }

    private int clampPriority(int priority) {
        return Math.max(PRIORITY_URGENT, Math.min(PRIORITY_LOW, priority));
    }

    private String canonicalizeText(String normalizedText) {
        String canonicalText = normalizedText;
        for (Map.Entry<String, String> entry : SORTED_SYNONYM_ENTRIES) {
            if (canonicalText.contains(entry.getKey())) {
                canonicalText = canonicalText.replace(entry.getKey(), entry.getValue());
            }
        }
        return canonicalText;
    }

    private static void registerKeyword(int priority, String canonical, String... synonyms) {
        Map<String, List<String>> levelMap = KEYWORD_LEVELS.computeIfAbsent(priority, key -> new LinkedHashMap<>());
        List<String> aliasList = List.of(synonyms);
        levelMap.put(canonical, aliasList);

        String normalizedCanonical = normalizeStatic(canonical);
        SYNONYM_TO_CANONICAL.put(normalizedCanonical, normalizedCanonical);
        for (String synonym : aliasList) {
            SYNONYM_TO_CANONICAL.put(normalizeStatic(synonym), normalizedCanonical);
        }
    }

    private String normalizeForMatch(String text) {
        return normalizeStatic(text);
    }

    private static String normalizeStatic(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String noHtml = HTML_TAG_PATTERN.matcher(text).replaceAll("");
        return PUNCTUATION_PATTERN.matcher(noHtml).replaceAll("").toLowerCase();
    }
}
