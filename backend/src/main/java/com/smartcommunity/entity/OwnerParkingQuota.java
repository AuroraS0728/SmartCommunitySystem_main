package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("owner_parking_quota")
public class OwnerParkingQuota {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String monthKey;
    private Integer freeHoursTotal;
    private Integer freeHoursUsed;
    private Integer ownerExtraHours;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
