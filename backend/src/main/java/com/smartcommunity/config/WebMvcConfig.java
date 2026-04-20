package com.smartcommunity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/access/verify-invite",
                        "/api/access/invite/dynamic-token",
                        "/api/access/verify-token",
                        "/api/fee/pay/callback",
                        "/api/parking/pay/callback",
                        "/api/realtime/stream",
                        "/error"
                );
    }
}
