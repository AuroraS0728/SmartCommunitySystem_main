package com.smartcommunity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OwnerParkingExtraReq {
    // yyyy-MM，可空，默认当前月
    private String monthKey;
    @NotNull(message = "extraHours不能为空")
    @Min(value = 0, message = "extraHours不能小于0")
    private Integer extraHours;
}
