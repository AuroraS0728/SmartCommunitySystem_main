package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_consumption_record")
public class PointsConsumptionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer businessType;
    private Long businessId;
    private Integer points;
    private Integer beforePoints;
    private Integer afterPoints;
    private LocalDateTime createTime;
}
