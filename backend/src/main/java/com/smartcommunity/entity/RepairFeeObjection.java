package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_fee_objection")
public class RepairFeeObjection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long billId;
    private Long userId;
    private Integer depositPoints;
    private String reason;
    private Integer status;
    private String resolutionRemark;
    private Long resolverId;
    private Integer refundPoints;
    private LocalDateTime resolveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

