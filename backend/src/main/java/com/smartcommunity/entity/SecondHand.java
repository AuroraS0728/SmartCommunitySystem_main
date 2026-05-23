package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("second_hand")
public class SecondHand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String community;
    private String title;
    private String category;
    private String description;
    private BigDecimal price;
    private String images;
    private Integer status;
    private Integer viewCount;
    private Integer reportCount;
    private String contact;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

    @TableField(exist = false)
    private List<ImageAuditResult> imageAuditResults;

    @TableField(exist = false)
    private BigDecimal maxFakeProbability;

    @TableField(exist = false)
    private String imageAuditStatus;

    @TableField(exist = false)
    private String imageRiskLevel;

    @TableField(exist = false)
    private String statusName;
}
