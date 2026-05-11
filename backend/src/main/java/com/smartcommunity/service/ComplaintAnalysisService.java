package com.smartcommunity.service;

import com.smartcommunity.entity.Complaint;
import com.smartcommunity.utils.SentimentAnalyzer;
import com.smartcommunity.utils.SentimentResult;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ComplaintAnalysisService {

    public AnalysisResult analyze(String title, String content) {
        SentimentResult sentiment = SentimentAnalyzer.analyze(joinText(title, content));
        AnalysisResult result = new AnalysisResult();
        result.setSentimentScore(BigDecimal.valueOf(sentiment.getScore()));
        result.setSentimentLabel(sentiment.getLabel());
        result.setRiskLevel(sentiment.getRiskLevel());
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

    @Data
    public static class AnalysisResult {
        private BigDecimal sentimentScore;
        private String sentimentLabel;
        private String riskLevel;
        private LocalDateTime analyzedAt;
    }
}
