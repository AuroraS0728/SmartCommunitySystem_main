package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long propertyId;
    private Integer serviceType;
    private String serviceMajor;
    private String serviceSubType;
    private Long facilityId;
    private String facilityName;
    private LocalDate appointmentDate;
    private String appointmentTimeSlot;
    private String category;
    private String description;
    private String images;
    private String beforeImages;
    private String afterImages;
    private BigDecimal chargeAmount;
    private String chargeRemark;
    private Integer needOutsource;
    private Integer status;
    private Long assignee;
    private Integer priority;
    private Long suggestedWorkerId;
    private LocalDateTime assignedTime;
    private String remark;
    private Integer ownerFinishConfirmed;
    private LocalDateTime ownerFinishTime;
    private Integer workerFinishConfirmed;
    private LocalDateTime workerFinishTime;
    private LocalDateTime serviceStartTime;
    private LocalDateTime serviceEndTime;
    private Integer serviceDurationMinutes;
    private LocalDateTime completionTime;
    private LocalDateTime slaDeadline;
    private Integer delayCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
