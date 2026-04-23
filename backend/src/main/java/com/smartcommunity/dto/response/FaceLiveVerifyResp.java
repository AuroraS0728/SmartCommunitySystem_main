package com.smartcommunity.dto.response;

import lombok.Data;

@Data
public class FaceLiveVerifyResp {
    private Boolean match;
    private Double score;
    private Double threshold;
    private Boolean livenessPassed;
    private Integer livenessStatus;
    private String livenessLabel;
    private Boolean maskCheckSupported;
    private Boolean maskPassed;
    private Integer maskStatus;
    private String maskLabel;
    private Boolean eyeStateCheckSupported;
    private Boolean eyesOpenPassed;
    private Integer leftEyeState;
    private String leftEyeLabel;
    private Integer rightEyeState;
    private String rightEyeLabel;
    private Boolean qualityCheckSupported;
    private Boolean qualityPassed;
    private Double qualityScore;
    private Integer qualityLevel;
    private String qualityLevelLabel;
}
