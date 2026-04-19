package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("worker_staffing")
public class WorkerStaffing {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workerId;
    private Integer staffType;
    private String position;
    private String shiftGroup;
    private String certificates;
    private String specialties;
    private Integer maxDailyOrders;
    private Integer currentStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

