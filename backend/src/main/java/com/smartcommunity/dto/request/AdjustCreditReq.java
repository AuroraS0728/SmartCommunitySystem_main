package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class AdjustCreditReq {
    private Long userId;
    private Integer changeValue;
    private String reason;
}
