package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitorHistoryResp {
    private String visitorName;
    private String visitorPhone;
    private LocalDateTime lastVisitTime;
    private LocalDateTime lastInviteTime;
    private Integer totalInviteCount;
}
