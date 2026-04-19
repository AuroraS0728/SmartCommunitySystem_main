package com.smartcommunity.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForumAdminFlagReq {
    @NotNull(message = "enabled不能为空")
    private Boolean enabled;
}

