package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class PublishLostFoundReq {
    private Integer type;
    private String title;
    private String description;
    private String contact;
    private String images;
}
