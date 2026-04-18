package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class UpdateRepairStatusReq {
    private Long orderId;
    private Integer status;
    private String remark;
    private String completionImages;
}
