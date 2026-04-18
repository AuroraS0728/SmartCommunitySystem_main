package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class SubmitComplaintReq {
    private Integer type;
    private String title;
    private String content;
    private String images;
}
