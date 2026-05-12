package com.smartcommunity.service;

import com.smartcommunity.entity.EmergencyKeyword;
import com.smartcommunity.mapper.EmergencyKeywordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintAnalysisServiceTest {

    @Mock
    private EmergencyKeywordMapper emergencyKeywordMapper;

    @Test
    void enabledEmergencyKeywordForcesHighRiskNegativeResult() {
        ComplaintAnalysisService service = new ComplaintAnalysisService(emergencyKeywordMapper);
        EmergencyKeyword keyword = new EmergencyKeyword();
        keyword.setKeyword("异响");
        keyword.setEnabled(1);
        keyword.setIsDeleted(0);
        when(emergencyKeywordMapper.selectList(any())).thenReturn(List.of(keyword));

        ComplaintAnalysisService.AnalysisResult result = service.analyze(
                "2号楼电梯异响希望尽快排查",
                "中午电梯上行时持续异响，家里老人乘坐很紧张"
        );

        assertEquals("NEGATIVE", result.getSentimentLabel());
        assertEquals("HIGH", result.getRiskLevel());
        assertEquals("-4.0", result.getSentimentScore().toPlainString());
    }
}
