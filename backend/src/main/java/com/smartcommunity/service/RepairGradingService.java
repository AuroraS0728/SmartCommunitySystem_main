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
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepairGradingService {

    private static final int DEFAULT_PRIORITY = 2;
    private static final int DEFAULT_CREDIT_SCORE = 100;
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{P}\\p{S}\\s]+");
    private static final Map<Integer, List<String>> KEYWORD_LEVELS = new LinkedHashMap<>();

    static {
        KEYWORD_LEVELS.put(1, List.of("漏水", "断电", "冒烟", "燃气泄漏", "门锁故障", "没电", "跳闸"));
        KEYWORD_LEVELS.put(2, List.of("灯泡", "水龙头", "开关", "插座", "马桶", "堵塞"));
        KEYWORD_LEVELS.put(3, List.of("咨询", "建议", "报价"));
    }

    private final UserMapper userMapper;

    public int calculatePriority(String description, Long userId) {
        int baseLevel = matchBaseLevel(description);
        int creditScore = getCreditScore(userId);

        if (creditScore >= 150 && baseLevel > 1) {
            baseLevel--;
        }
        if (creditScore < 60 && baseLevel < 3) {
            baseLevel++;
        }
        return Math.max(1, Math.min(3, baseLevel));
    }

    private int matchBaseLevel(String description) {
        String normalized = normalizeForMatch(description);
        if (!StringUtils.hasText(normalized)) {
            return DEFAULT_PRIORITY;
        }
        for (Map.Entry<Integer, List<String>> entry : KEYWORD_LEVELS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalized.contains(normalizeForMatch(keyword))) {
                    return entry.getKey();
                }
            }
        }
        return DEFAULT_PRIORITY;
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

    private String normalizeForMatch(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String noHtml = HTML_TAG_PATTERN.matcher(text).replaceAll("");
        return PUNCTUATION_PATTERN.matcher(noHtml).replaceAll("").toLowerCase();
    }
}
