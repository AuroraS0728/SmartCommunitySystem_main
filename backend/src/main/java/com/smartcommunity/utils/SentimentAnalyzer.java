package com.smartcommunity.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public final class SentimentAnalyzer {

    private static final String LABEL_POSITIVE = "POSITIVE";
    private static final String LABEL_NEGATIVE = "NEGATIVE";
    private static final String LABEL_NEUTRAL = "NEUTRAL";
    private static final String RISK_HIGH = "HIGH";
    private static final String RISK_MID = "MID";
    private static final String RISK_LOW = "LOW";

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "满意", "及时", "高效", "专业", "负责", "耐心", "好评", "点赞", "快速", "认真",
            "友善", "靠谱", "敬业", "细心", "安全", "舒适", "方便", "周到", "热情", "感动"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "差评", "慢", "敷衍", "不负责", "态度差", "投诉", "愤怒", "失望", "恶心", "糟糕",
            "烂", "坑", "无语", "崩溃", "气人", "恼火", "威胁", "辱骂", "推诿", "拖延"
    );

    private static final Map<String, Double> DEGREE_ADVERBS = Map.of(
            "非常", 1.5,
            "很", 1.3,
            "比较", 1.1,
            "稍微", 0.8,
            "有点", 0.6
    );

    private static final Set<String> NEGATION_WORDS = Set.of("不", "没", "无", "未", "别", "莫");

    private static final Set<String> EMERGENCY_KEYWORDS = Set.of(
            "报警", "砍人", "杀人", "威胁", "砸门", "火灾", "漏电", "救护车"
    );

    private static final Set<String> RESET_PUNCTUATION = Set.of(",", "，", ".", "。", "?", "？", "!", "！");

    private SentimentAnalyzer() {
    }

    public static SentimentResult analyze(String text) {
        if (!StringUtils.hasText(text)) {
            return neutral();
        }
        try {
            String normalized = text.trim();
            if (containsAny(normalized, EMERGENCY_KEYWORDS)) {
                return new SentimentResult(-5.0D, LABEL_NEGATIVE, RISK_HIGH);
            }

            List<String> words = segment(normalized);
            if (words.isEmpty()) {
                return neutral();
            }

            double score = 0D;
            int polarity = 1;
            double degreeWeight = 1D;

            for (String word : words) {
                if (!StringUtils.hasText(word)) {
                    continue;
                }
                String token = word.trim();
                if (RESET_PUNCTUATION.contains(token)) {
                    polarity = 1;
                    degreeWeight = 1D;
                    continue;
                }
                if (isNegationToken(token)) {
                    polarity *= -1;
                    continue;
                }
                Double weight = DEGREE_ADVERBS.get(token);
                if (weight != null) {
                    degreeWeight *= weight;
                    continue;
                }

                Integer baseScore = matchSentiment(token);
                if (baseScore != null) {
                    score += baseScore * polarity * degreeWeight;
                    polarity = 1;
                    degreeWeight = 1D;
                }
            }

            String label = score >= 2D ? LABEL_POSITIVE : score <= -2D ? LABEL_NEGATIVE : LABEL_NEUTRAL;
            String riskLevel = Math.abs(score) >= 4D ? RISK_MID : RISK_LOW;
            return new SentimentResult(round2(score), label, riskLevel);
        } catch (Exception ex) {
            log.warn("Sentiment analysis failed, text={}", text, ex);
            return neutral();
        }
    }

    private static List<String> segment(String text) {
        List<String> words = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String current = String.valueOf(text.charAt(i));
            if (RESET_PUNCTUATION.contains(current)) {
                if (buffer.length() > 0) {
                    addHanlpTerms(words, buffer.toString());
                    buffer.setLength(0);
                }
                words.add(current);
            } else {
                buffer.append(current);
            }
        }
        if (buffer.length() > 0) {
            addHanlpTerms(words, buffer.toString());
        }
        mergeDictionaryTerms(words);
        return words;
    }

    private static void addHanlpTerms(List<String> words, String sentence) {
        List<Term> terms = HanLP.segment(sentence);
        for (Term term : terms) {
            if (term != null && StringUtils.hasText(term.word)) {
                words.add(term.word.trim());
            }
        }
    }

    private static void mergeDictionaryTerms(List<String> words) {
        if (words.size() < 2) {
            return;
        }
        Set<String> mergeTargets = new HashSet<>();
        mergeTargets.addAll(POSITIVE_WORDS);
        mergeTargets.addAll(NEGATIVE_WORDS);
        mergeTargets.addAll(DEGREE_ADVERBS.keySet());

        for (int i = 0; i < words.size() - 1; i++) {
            String two = words.get(i) + words.get(i + 1);
            if (mergeTargets.contains(two)) {
                words.set(i, two);
                words.remove(i + 1);
                i--;
            }
        }
    }

    private static Integer matchSentiment(String token) {
        if (POSITIVE_WORDS.contains(token)) {
            return 1;
        }
        if (NEGATIVE_WORDS.contains(token)) {
            return -1;
        }
        for (String word : POSITIVE_WORDS) {
            if (token.contains(word)) {
                return 1;
            }
        }
        for (String word : NEGATIVE_WORDS) {
            if (token.contains(word)) {
                return -1;
            }
        }
        return null;
    }

    private static boolean isNegationToken(String token) {
        if (NEGATION_WORDS.contains(token)) {
            return true;
        }
        return "没有".equals(token) || "不是".equals(token) || "不能".equals(token) || "不会".equals(token);
    }

    private static boolean containsAny(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static SentimentResult neutral() {
        return new SentimentResult(0D, LABEL_NEUTRAL, RISK_LOW);
    }

    private static double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
