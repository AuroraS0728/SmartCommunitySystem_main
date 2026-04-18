package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class LostFoundResp {
    private Long id;
    private Integer type;
    private String title;
    private Integer status;
}
