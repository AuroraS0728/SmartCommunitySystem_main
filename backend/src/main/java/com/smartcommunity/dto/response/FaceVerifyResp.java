package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class FaceVerifyResp {
    private Boolean match;
    private Double score;
    private Double threshold;
}
