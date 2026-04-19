package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParkingEntryReq {
    @NotBlank(message = "vehicleNo不能为空")
    private String vehicleNo;
    // 1业主车, 2访客车
    private Integer sourceType = 1;
}
