package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommend_rule")
public class RecommendRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String serviceId;
    private String serviceName;
    private String ruleExpression;
    private String conditionJson;
    private String imageUrl;
    private Integer price;
    private Integer priority;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
