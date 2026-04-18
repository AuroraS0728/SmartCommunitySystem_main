package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class RenewInviteReq {
    private String visitTime;
    private String validityType;
    private String customExpireTime;
    private Integer maxUses;
}
