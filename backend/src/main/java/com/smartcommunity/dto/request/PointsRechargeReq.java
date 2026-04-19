package com.smartcommunity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsRechargeReq {
    @NotNull(message = "userId不能为空")
    private Long userId;

    @NotNull(message = "amount不能为空")
    @Min(value = 1, message = "充值积分最小为1")
    @Max(value = 10000, message = "充值积分最大为10000")
    private Integer amount;

    private String remark;
}
