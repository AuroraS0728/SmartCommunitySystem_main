package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_bill")
public class FeeBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long propertyId;
    private String billPeriod;
    private BigDecimal amount;
    private Integer needPoints;
    private BigDecimal paidAmount;
    private Integer status;
    private LocalDateTime dueDate;
    private LocalDateTime paymentTime;
    private String transactionId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
