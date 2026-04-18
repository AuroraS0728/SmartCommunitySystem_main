package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class VerifyCodeReq {
    private Long orderId;
    private String code;
}
