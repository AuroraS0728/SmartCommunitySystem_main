package com.smartcommunity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveReq {
    private String title;
    private String description;
    private String type;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private String ageLimit;
    private Integer withChildRequired;
    private Integer withPetRequired;
    private Integer status;
}
