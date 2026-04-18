package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class WorkerScheduleReq {
    private Long workerId;
    private String scheduleDate;
    private String shift;
}
