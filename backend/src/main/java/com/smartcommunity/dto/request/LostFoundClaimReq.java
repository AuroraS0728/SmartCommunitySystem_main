package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LostFoundClaimReq {
    @NotBlank(message = "proof不能为空")
    private String proof;
}

