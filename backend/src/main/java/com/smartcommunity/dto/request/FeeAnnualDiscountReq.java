package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeeAnnualDiscountReq {
    @NotNull(message = "propertyId不能为空")
    private Long propertyId;
    @NotBlank(message = "startPeriod不能为空")
    private String startPeriod;
}
