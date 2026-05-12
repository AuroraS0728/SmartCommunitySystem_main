package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class SmartWorkOrderStatusReq {
    private Integer status;
    private String remark;
}
