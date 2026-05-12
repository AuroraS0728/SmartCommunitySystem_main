package com.smartcommunity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogAdminLoginResp {
    private String token;
    private Long expireSeconds;
}
