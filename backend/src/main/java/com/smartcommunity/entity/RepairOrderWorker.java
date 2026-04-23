package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_order_worker")
public class RepairOrderWorker {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long workerId;
    private Integer roleType;
    private Integer verifyPassed;
    private LocalDateTime verifyPassTime;
    private Integer finishConfirmed;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

