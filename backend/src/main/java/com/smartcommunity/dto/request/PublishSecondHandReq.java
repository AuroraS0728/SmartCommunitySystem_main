package com.smartcommunity.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublishSecondHandReq {
    private String title;
    private String category;
    private BigDecimal price;
    private String images;
    private String contact;
}
