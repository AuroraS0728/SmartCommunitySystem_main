package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class BindPhoneReq {
    private String phone;
    private String smsCode;
}
