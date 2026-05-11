package com.smartcommunity.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SentimentResult {
    private double score;
    private String label;
    private String riskLevel;
}
