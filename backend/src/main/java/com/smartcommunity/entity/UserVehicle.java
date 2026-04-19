package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_vehicle")
public class UserVehicle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String vehicleNo;
    private Integer isVisitor;
    private Long hostUserId;
    private LocalDateTime parkingDeadline;
    private LocalDateTime remindTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
