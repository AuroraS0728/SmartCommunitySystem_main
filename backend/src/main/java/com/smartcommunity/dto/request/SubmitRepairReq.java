package com.smartcommunity.dto.request;

import lombok.Data;

@Data
public class SubmitRepairReq {
    private Long propertyId;
    private Integer serviceType;
    private String serviceMajor;
    private String serviceSubType;
    private String category;
    private String description;
    private String images;
}
