package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingBindVisitorVehicleReq {
    @NotBlank(message = "vehicleNo不能为空")
    private String vehicleNo;
    @NotNull(message = "parkingDeadline不能为空")
    private String parkingDeadline;
}
