package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class HouseInfoResp {
    private Long id;
    private String community;
    private String building;
    private String unit;
    private String room;
    private String ownerName;
    private String area;
    private Integer status;
}
