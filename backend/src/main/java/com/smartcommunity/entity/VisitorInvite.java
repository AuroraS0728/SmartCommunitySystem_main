package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visitor_invite")
public class VisitorInvite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hostUserId;
    private String visitorName;
    private String visitorPhone;
    private String code;
    private String validityType;
    private LocalDateTime visitTime;
    private LocalDateTime expireTime;
    private Integer maxUses;
    private Integer usedCount;
    private LocalDateTime usedTime;
    private String shareLink;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
