package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("additional_service_order")
public class AdditionalServiceOrder {
    @TableId(type = IdType.AUTO)
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
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
