package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParkingMonthCardReq {
    @NotBlank(message = "vehicleNo不能为空")
    private String vehicleNo;
}
