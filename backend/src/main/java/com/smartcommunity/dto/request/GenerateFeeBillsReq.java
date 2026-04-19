package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateFeeBillsReq {
    @NotBlank(message = "billPeriod不能为空")
    private String billPeriod;
    private Long propertyId;
}
