package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class InviteVisitorReq {
    private String visitorName;
    private String visitorPhone;
    private String visitReason;
    private Integer maxUses;
    private String visitTime;
    private String validityType;
    private String customExpireTime;
}
