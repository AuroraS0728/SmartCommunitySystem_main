package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_fee_bill")
public class RepairFeeBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long propertyId;
    private BigDecimal amount;
    private Integer needPoints;
    private Integer paidPoints;
    private Integer status;
    private LocalDateTime dueDate;
    private LocalDateTime paymentTime;
    private String transactionId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
