package com.smartcommunity.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class StatisticsResp {
    private Map<String, Object> overview;
    private Map<String, Object> fee;
    private Map<String, Object> repair;
}
