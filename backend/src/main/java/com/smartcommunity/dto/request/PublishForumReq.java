package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublishForumReq {
    private String board;
    @NotBlank(message = "title不能为空")
    private String title;
    @NotBlank(message = "content不能为空")
    private String content;
}
