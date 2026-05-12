package com.smartcommunity.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SmartWorkOrderDispatchReq {
    private Long assigneeId;
    private List<Long> assigneeIds;
    private String remark;
}
