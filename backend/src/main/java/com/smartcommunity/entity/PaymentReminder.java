package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_reminder")
public class PaymentReminder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long feeBillId;
    private String method;
    private String content;
    private Integer status;
    private LocalDateTime sendTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
