package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class NoticeResp {
    private Long id;
    private String title;
    private String content;
    private String publishTime;
    private Integer top;
}
