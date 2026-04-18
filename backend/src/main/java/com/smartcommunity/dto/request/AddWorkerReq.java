package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class AddWorkerReq {
    private String nickname;
    private String phone;
    private String skills;
}
