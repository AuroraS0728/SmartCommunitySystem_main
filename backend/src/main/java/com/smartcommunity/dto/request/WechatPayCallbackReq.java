package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatPayCallbackReq {
    @NotBlank(message = "outTradeNo不能为空")
    private String outTradeNo;
    private Boolean success = true;
}
