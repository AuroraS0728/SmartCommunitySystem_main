package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class AddCommentReq {
    private Long postId;
    private Long parentId;
    private String content;
}
