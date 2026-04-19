package com.smartcommunity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LostFoundClaimReviewReq {
    @NotNull(message = "status不能为空")
    @Min(value = 1, message = "status只能为1或2")
    @Max(value = 2, message = "status只能为1或2")
    private Integer status;
}

