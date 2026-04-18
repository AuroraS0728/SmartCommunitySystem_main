package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class VisitorBlacklistReq {
    private String phone;
    private String reason;
}
