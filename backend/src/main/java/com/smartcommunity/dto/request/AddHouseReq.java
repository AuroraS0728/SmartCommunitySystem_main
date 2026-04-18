package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddHouseReq {
    private String community;
    private String building;
    private String unit;
    private String room;
    private String ownerName;
    private BigDecimal area;
    private Integer status;
}
