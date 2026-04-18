package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class VisitorRecordResp {
    private Long id;
    private String visitorName;
    private String visitorPhone;
    private String code;
    private String expireTime;
    private Integer usedCount;
}
