package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SecondHandReportReq {
    @NotBlank(message = "reason不能为空")
    private String reason;
}

