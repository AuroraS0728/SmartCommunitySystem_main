package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class AccessQrCodeResp {
    private String token;
    private String qrContent;
    private String expireTime;
}
