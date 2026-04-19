package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PublishLostFoundReq {
    @NotNull(message = "type不能为空")
    private Integer type;
    @NotBlank(message = "title不能为空")
    private String title;
    @NotBlank(message = "description不能为空")
    private String description;
    private String location;
    private String contact;
    private String images;
}
