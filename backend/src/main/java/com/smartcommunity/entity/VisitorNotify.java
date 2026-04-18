package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visitor_notify")
public class VisitorNotify {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hostUserId;
    private Long inviteId;
    private String visitorName;
    private String content;
    private Integer readFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
