package com.smartcommunity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    private String appId;
    private String appSecret;
    private String mchId;
    private String mchSerialNo;
    private String apiV3Key;
    private String notifyUrl;
}
