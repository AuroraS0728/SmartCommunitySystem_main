package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class AssignRepairReq {
    private Long orderId;
    private Long assignee;
    private String remark;
}
