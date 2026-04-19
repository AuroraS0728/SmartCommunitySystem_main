package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRepairStatusReq {
    private Long orderId;
    private Integer status;
    private String remark;
    private String completionImages;
    private String beforeImages;
    private String afterImages;
    private BigDecimal chargeAmount;
    private String chargeRemark;
}
