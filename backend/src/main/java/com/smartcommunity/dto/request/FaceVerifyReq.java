package com.smartcommunity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class FaceVerifyReq {
    @NotNull(message = "workerId不能为空")
    @Positive(message = "workerId必须大于0")
    private Long workerId;

    @NotBlank(message = "imageBase64不能为空")
    private String imageBase64;
}
