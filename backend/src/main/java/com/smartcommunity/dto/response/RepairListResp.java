package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class RepairListResp {
    private Long id;
    private String category;
    private Integer status;
    private String createTime;
}
