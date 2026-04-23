package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class SubmitRepairFeeObjectionReq {
    private Integer depositPoints;
    private String reason;
}

