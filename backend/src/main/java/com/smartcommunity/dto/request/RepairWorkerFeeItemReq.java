package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepairWorkerFeeItemReq {
    private Long workerId;
    private BigDecimal techFee;
    private BigDecimal materialFee;
    private BigDecimal highAltitudeFee;
    private BigDecimal otherFee;
    private String remark;
}

