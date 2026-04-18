package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class PublishForumReq {
    private String board;
    private String title;
    private String content;
}
