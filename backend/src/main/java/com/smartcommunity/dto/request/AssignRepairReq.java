package com.smartcommunity.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignRepairReq {
    private Long orderId;
    private Long assignee;
    private List<Long> assignees;
    private String remark;
}
