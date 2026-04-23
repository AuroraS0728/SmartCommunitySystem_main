package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FaceLiveVerifyMultiReq {
    @NotNull(message = "orderId cannot be null")
    @Positive(message = "orderId must be greater than 0")
    private Long orderId;

    @NotBlank(message = "imageBase64 cannot be empty")
    private String imageBase64;
}

