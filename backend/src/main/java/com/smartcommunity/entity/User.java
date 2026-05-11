package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String unionid;
    private String account;
    @JsonIgnore
    private String password;
    private Integer mustChangePassword;
    private Integer role;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer points;
    private Integer creditScore;
    private LocalDateTime creditLastUpdate;
    private Integer hasElderly;
    private Integer hasChild;
    private Integer hasPet;
    private Integer houseArea;
    private Integer roomCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
