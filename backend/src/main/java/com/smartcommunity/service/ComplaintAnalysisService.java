package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.Complaint;
import com.smartcommunity.entity.EmergencyKeyword;
import com.smartcommunity.mapper.EmergencyKeywordMapper;
import com.smartcommunity.utils.SentimentAnalyzer;
import com.smartcommunity.utils.SentimentResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintAnalysisService {

    private static final String LABEL_NEGATIVE = "NEGATIVE";
    private static final String RISK_HIGH = "HIGH";
    private static final double EMERGENCY_SCORE = -4D;

    private final EmergencyKeywordMapper emergencyKeywordMapper;

    public AnalysisResult analyze(String title, String content) {
        String text = joinText(title, content);
        SentimentResult sentiment = SentimentAnalyzer.analyze(text);
        AnalysisResult result = new AnalysisResult();
        double score = sentiment.getScore();
        String label = sentiment.getLabel();
        String riskLevel = sentiment.getRiskLevel();
        if (matchesEmergencyKeyword(text)) {
            score = Math.min(score, EMERGENCY_SCORE);
            label = LABEL_NEGATIVE;
            riskLevel = RISK_HIGH;
        }
        result.setSentimentScore(BigDecimal.valueOf(score));
        result.setSentimentLabel(label);
        result.setRiskLevel(riskLevel);
        result.setAnalyzedAt(LocalDateTime.now());
        return result;
    }

    public void apply(Complaint complaint) {
        AnalysisResult result = analyze(complaint.getTitle(), complaint.getContent());
        complaint.setSentimentScore(result.getSentimentScore());
        complaint.setSentimentLabel(result.getSentimentLabel());
        complaint.setRiskLevel(result.getRiskLevel());
        complaint.setAnalyzedAt(result.getAnalyzedAt());
    }

    private String joinText(String title, String content) {
        String safeTitle = title == null ? "" : title;
        String safeContent = content == null ? "" : content;
        return safeTitle + " " + safeContent;
    }

    private boolean matchesEmergencyKeyword(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        List<EmergencyKeyword> keywords = emergencyKeywordMapper.selectList(new LambdaQueryWrapper<EmergencyKeyword>()
                .eq(EmergencyKeyword::getIsDeleted, 0)
                .eq(EmergencyKeyword::getEnabled, 1));
        String normalizedText = text.trim();
        for (EmergencyKeyword keyword : keywords) {
            if (keyword == null || !StringUtils.hasText(keyword.getKeyword())) {
                continue;
            }
            if (normalizedText.contains(keyword.getKeyword().trim())) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class AnalysisResult {
        private BigDecimal sentimentScore;
        private String sentimentLabel;
        private String riskLevel;
        private LocalDateTime analyzedAt;
    }
}
