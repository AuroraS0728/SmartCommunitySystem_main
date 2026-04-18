package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class PublishNoticeReq {
    private String title;
    private String content;
    private String attachmentUrls;
    private Integer top;
}
