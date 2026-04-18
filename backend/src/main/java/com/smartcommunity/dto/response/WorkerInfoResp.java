package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class WorkerInfoResp {
    private Long id;
    private String nickname;
    private String phone;
    private String skills;
    private Integer completedCount;
    private String rating;
}
