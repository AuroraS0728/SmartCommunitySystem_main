package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class ComplaintResp {
    private Long id;
    private Integer type;
    private String title;
    private Integer status;
    private String reply;
    private Integer satisfaction;
}
