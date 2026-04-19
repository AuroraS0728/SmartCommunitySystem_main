package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class WorkerStaffingReq {
    private Long workerId;
    private Integer staffType;
    private String position;
    private String shiftGroup;
    private String certificates;
    private String specialties;
    private Integer maxDailyOrders;
    private Integer currentStatus;
}

