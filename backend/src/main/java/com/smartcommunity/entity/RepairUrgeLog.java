package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_urge_log")
public class RepairUrgeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String targetType;
    private Long targetId;
    private LocalDateTime urgeTime;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
