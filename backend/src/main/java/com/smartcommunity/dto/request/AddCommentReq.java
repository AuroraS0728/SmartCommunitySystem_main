package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCommentReq {
    @NotNull(message = "postId不能为空")
    private Long postId;
    private Long parentId;
    @NotBlank(message = "content不能为空")
    private String content;
}
