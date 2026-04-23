package com.smartcommunity.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class FaceLiveVerifyMultiResp {
    private Long orderId;
    private Boolean allVerified;
    private List<Long> pendingWorkerIds;
    private List<FaceLiveVerifyMultiItemResp> results;
}

