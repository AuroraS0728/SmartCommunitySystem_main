package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalyzeComplaintReq {
    private BigDecimal sentimentScore;
    private String sentimentLabel;
    private String riskLevel;
}
