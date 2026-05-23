package com.smartcommunity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacilitySaveReq {
    private String name;
    private String category;
    private String location;
    private String openHours;
    private String contactPhone;
    private Integer status;
    private Integer sortOrder;
    private String description;
    private String imageUrls;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastInspectionTime;
}
