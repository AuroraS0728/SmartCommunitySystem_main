package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private Boolean mustChangePassword;
    private UserInfoResp userInfo;
}
