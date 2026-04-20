package com.smartcommunity.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentSubjectVO {
    /**
     * 1-物业费，2-停车费，3-维修费
     */
    private Integer businessType;
    private String businessTypeText;
    private Long businessId;

    /**
     * 缴费主体展示文本（如房产号、车牌、工单号）
     */
    private String subjectName;

    /**
     * 业务标识（物业费账期、停车订单类型、维修工单号）
     */
    private String businessRef;

    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Integer needPoints;
    private Integer status;
    private String statusText;
    private LocalDateTime dueDate;
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
}
