package com.smartcommunity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsConsumeReq {
    @NotNull(message = "businessType不能为空")
    @Min(value = 1, message = "businessType取值范围为1~3")
    @Max(value = 3, message = "businessType取值范围为1~3")
    private Integer businessType;

    @NotNull(message = "businessId不能为空")
    private Long businessId;
}
