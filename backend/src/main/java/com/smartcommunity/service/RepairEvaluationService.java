package com.smartcommunity.service;

import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.RepairOrder;

public interface RepairEvaluationService {
    RepairEvaluation saveEvaluation(RepairOrder order, Integer rating, String comment, Integer anonymous);
}
