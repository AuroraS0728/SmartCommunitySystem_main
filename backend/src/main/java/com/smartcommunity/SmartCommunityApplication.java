package com.smartcommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smartcommunity.mapper")
public class SmartCommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartCommunityApplication.class, args);
    }
}
