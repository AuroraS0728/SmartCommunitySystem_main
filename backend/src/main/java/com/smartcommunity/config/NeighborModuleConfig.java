package com.smartcommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "neighbor")
public class NeighborModuleConfig {
    private int defaultPageSize = 10;
    private int maxPageSize = 50;
    private List<String> sensitiveWords = new ArrayList<>(List.of(
            "赌博",
            "诈骗",
            "涉黄",
            "毒品",
            "暴恐"
    ));
}

