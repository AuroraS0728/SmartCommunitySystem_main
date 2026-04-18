package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitorInviteResp {
    private Long id;
    private Long hostUserId;
    private String visitorName;
    private String visitorPhone;
    private String code;
    private String validityType;
    private LocalDateTime visitTime;
    private LocalDateTime expireTime;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime usedTime;
    private String status;
    private String statusText;
    private Long countdownSeconds;
    private Boolean canRenew;
    private String qrContent;
    private String shareLink;
    private LocalDateTime createTime;
}
