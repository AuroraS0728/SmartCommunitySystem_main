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
    // 历史账单快照，避免后续面积调整影响旧账单。
    private BigDecimal areaSnapshot;
    // 固定单价快照，默认 5 元/平方米/月。
    private BigDecimal unitPrice;
    private BigDecimal amount;
    // 折扣金额（例如预缴一年减免一个月）。
    private BigDecimal discountAmount;
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
