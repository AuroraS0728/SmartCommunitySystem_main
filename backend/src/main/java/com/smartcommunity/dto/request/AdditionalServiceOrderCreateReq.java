package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdditionalServiceOrderCreateReq {
    @NotBlank(message = "serviceId不能为空")
    private String serviceId;

    @NotBlank(message = "appointmentDate不能为空")
    private String appointmentDate;

    @NotBlank(message = "appointmentTimeSlot不能为空")
    private String appointmentTimeSlot;

    @Size(max = 200, message = "remark长度不能超过200")
    private String remark;
}
