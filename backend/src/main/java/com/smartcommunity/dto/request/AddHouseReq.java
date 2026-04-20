package com.smartcommunity.dto.request;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AddHouseReq {
    private String community;
    private String building;
    private String unit;
    private String room;
    private String ownerName;
    private String tenantName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime rentEndTime;
    private BigDecimal area;
    private Integer status;
}
