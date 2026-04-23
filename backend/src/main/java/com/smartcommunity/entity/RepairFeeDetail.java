package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_fee_detail")
public class RepairFeeDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long workerId;
    private BigDecimal techFee;
    private BigDecimal materialFee;
    private BigDecimal highAltitudeFee;
    private BigDecimal otherFee;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}

