package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class RepairDetailResp {
    private Long id;
    private String category;
    private String description;
    private String images;
    private Integer status;
    private Long assignee;
    private String remark;
    private String createTime;
}
