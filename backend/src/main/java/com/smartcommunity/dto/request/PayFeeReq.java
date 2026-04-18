package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayFeeReq {
    private Long billId;
    private BigDecimal amount;
}
