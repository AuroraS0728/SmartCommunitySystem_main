package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class VerifyVisitorInviteReq {
    private String code;
    private String visitorName;
    private String visitorPhone;
}
