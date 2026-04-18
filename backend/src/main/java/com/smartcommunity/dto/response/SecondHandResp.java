package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class SecondHandResp {
    private Long id;
    private String title;
    private String category;
    private String price;
    private Integer status;
}
