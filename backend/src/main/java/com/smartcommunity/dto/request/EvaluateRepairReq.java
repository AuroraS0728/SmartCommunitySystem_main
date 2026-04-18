package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class EvaluateRepairReq {
    private Integer rating;
    private String comment;
    private Integer anonymous;
}
