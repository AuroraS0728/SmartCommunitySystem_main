package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class UserInfoResp {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer role;
}
