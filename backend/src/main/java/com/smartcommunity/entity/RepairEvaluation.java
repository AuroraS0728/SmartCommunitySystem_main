package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repair_evaluation")
public class RepairEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Integer rating;
    private String comment;
    private Integer isAnonymous;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
