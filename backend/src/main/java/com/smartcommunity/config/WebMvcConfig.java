package com.smartcommunity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Value("${blog.asset-dir:./blog-assets}")
    private String blogAssetDir;

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
                        "/api/blog/profile",
                        "/api/blog/admin/login",
                        "/api/blog/assets/**",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path assetPath = Paths.get(blogAssetDir).toAbsolutePath().normalize();
        String location = assetPath.toUri().toString();
        registry.addResourceHandler("/api/blog/assets/**")
                .addResourceLocations(location)
                .setCachePeriod(31_536_000);
    }
}
