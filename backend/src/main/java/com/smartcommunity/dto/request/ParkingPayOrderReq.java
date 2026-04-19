package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingPayOrderReq {
    @NotNull(message = "orderId不能为空")
    private Long orderId;
}
