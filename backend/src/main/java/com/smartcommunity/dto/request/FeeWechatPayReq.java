package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeeWechatPayReq {
    @NotNull(message = "billId不能为空")
    private Long billId;
}
