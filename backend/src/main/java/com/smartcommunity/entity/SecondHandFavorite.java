package com.smartcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("second_hand_favorite")
public class SecondHandFavorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long secondHandId;
    private LocalDateTime createTime;
}

