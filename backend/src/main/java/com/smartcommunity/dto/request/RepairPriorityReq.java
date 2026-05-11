package com.smartcommunity.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairPriorityReq {
    private Integer priority;
    private Long suggestedWorkerId;
    private LocalDateTime slaDeadline;
}
