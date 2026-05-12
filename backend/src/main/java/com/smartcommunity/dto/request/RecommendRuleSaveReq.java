package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class RecommendRuleSaveReq {
    private String ruleName;
    private String serviceId;
    private String serviceName;
    private String ruleExpression;
    private String conditionJson;
    private String imageUrl;
    private Integer price;
    private Integer priority;
    private Integer enabled;
}
