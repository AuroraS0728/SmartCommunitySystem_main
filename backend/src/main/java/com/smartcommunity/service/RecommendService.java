package com.smartcommunity.service;

import com.smartcommunity.dto.response.ServiceRecommendation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RecommendRuleService recommendRuleService;

    public List<ServiceRecommendation> recommend(Long userId) {
        return recommendRuleService.recommend(userId, 3, false);
    }
}
