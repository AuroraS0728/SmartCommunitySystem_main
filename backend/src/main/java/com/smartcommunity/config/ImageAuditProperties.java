package com.smartcommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "image-audit")
public class ImageAuditProperties {
    private boolean enabled = true;
    private String modelUrl = "http://127.0.0.1:8000/predict";
    private double fakeThreshold = 0.5D;
    private double reviewThreshold = 0.7D;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 10000;
}
