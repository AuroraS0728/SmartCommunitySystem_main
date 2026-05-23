package com.smartcommunity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpressPackageSaveReq {
    private Long propertyId;
    private String courierCompany;
    private String trackingNo;
    private String pickupCode;
    private String recipientName;
    private String recipientPhone;
    private String shelfLocation;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime arrivedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime pickupTime;
    private String remark;
}
