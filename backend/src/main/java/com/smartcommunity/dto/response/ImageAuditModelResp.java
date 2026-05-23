package com.smartcommunity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImageAuditModelResp {
    private String filename;
    private String label;

    @JsonProperty("fake_probability")
    private Double fakeProbability;

    @JsonProperty("real_probability")
    private Double realProbability;

    private Double threshold;
}
