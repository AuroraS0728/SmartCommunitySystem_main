package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("image_audit_result")
public class ImageAuditResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tradeId;
    private String imageUrl;
    private String detectLabel;
    private BigDecimal realProbability;
    private BigDecimal fakeProbability;
    private BigDecimal thresholdValue;
    private String riskLevel;
    private String auditStatus;
    private LocalDateTime createTime;
}
