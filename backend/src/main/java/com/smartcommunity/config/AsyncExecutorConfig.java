package com.smartcommunity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncExecutorConfig {

    @Bean("queryExecutor")
    public Executor queryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(300);
        executor.setThreadNamePrefix("query-exec-");
        executor.initialize();
        return executor;
    }
}
