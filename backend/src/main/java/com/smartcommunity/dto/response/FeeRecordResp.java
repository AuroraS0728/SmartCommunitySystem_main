package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class FeeRecordResp {
    private Long id;
    private String billPeriod;
    private String amount;
    private String paidAmount;
    private Integer status;
}
