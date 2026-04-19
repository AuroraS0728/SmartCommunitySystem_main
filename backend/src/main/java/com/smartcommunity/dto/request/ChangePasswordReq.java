package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class ChangePasswordReq {
    private String oldPassword;
    private String newPassword;
}
