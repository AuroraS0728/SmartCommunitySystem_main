package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRechargeRecordVO {
    private Long id;
    private Long userId;
    private String userName;
    private Long operatorId;
    private String operatorName;
    private Integer amount;
    private Integer beforePoints;
    private Integer afterPoints;
    private String remark;
    private LocalDateTime createTime;
}
