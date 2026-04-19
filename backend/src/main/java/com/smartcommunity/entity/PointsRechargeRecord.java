package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_recharge_record")
public class PointsRechargeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long operatorId;
    private Integer amount;
    private Integer beforePoints;
    private Integer afterPoints;
    private String remark;
    private LocalDateTime createTime;
}
