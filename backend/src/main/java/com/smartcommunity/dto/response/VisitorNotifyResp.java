package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitorNotifyResp {
    private Long id;
    private Long inviteId;
    private String content;
    private Integer readFlag;
    private LocalDateTime createTime;
}
