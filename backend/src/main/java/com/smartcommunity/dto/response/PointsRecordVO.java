package com.smartcommunity.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsRecordVO {
    private Long id;
    // 1=收入(充值), 2=支出(消费)
    private Integer recordType;
    private Integer points;
    private Integer beforePoints;
    private Integer afterPoints;
    private Integer businessType;
    private Long businessId;
    private String remark;
    private LocalDateTime createTime;
}
