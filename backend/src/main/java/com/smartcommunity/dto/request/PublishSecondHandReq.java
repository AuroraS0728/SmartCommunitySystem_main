package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublishSecondHandReq {
    private String community;
    @NotBlank(message = "title不能为空")
    private String title;
    private String category;
    private String description;
    @PositiveOrZero(message = "price不能小于0")
    private BigDecimal price;
    private String images;
    private String contact;
}
