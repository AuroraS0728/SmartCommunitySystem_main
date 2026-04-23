package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewRepairFeeObjectionReq {
    /**
     * 1 = reject objection (fee reasonable), 2 = accept objection (refund).
     */
    private Integer status;
    private Integer refundPoints;
    private BigDecimal adjustedAmount;
    private String remark;
}

