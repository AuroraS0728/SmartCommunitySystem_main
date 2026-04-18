package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class LoginReq {
    private String code;
    private String account;
    private String password;
    private Integer role;
}
