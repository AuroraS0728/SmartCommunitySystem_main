package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdditionalServiceOrderVO {
    private Long id;
    private Long userId;
    private String serviceId;
    private String serviceName;
    private Integer price;
    private Integer pointsCost;
    private LocalDate appointmentDate;
    private String appointmentTimeSlot;
    private String contactName;
    private String contactPhone;
    private String remark;
    private Integer status;
    private String statusText;
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
}
