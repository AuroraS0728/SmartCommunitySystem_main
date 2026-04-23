package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
    private List<RepairWorkerFeeItemReq> workerFees;
}
