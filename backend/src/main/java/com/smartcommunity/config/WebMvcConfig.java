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

    @Value("${activity.asset-dir:./activity-assets}")
    private String activityAssetDir;

    @Value("${file.upload-dir:D:/upload}")
    private String fileUploadDir;

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
                        "/api/blog/notes",
                        "/api/blog/admin/login",
                        "/api/blog/assets/**",
                        "/api/activity/assets/**",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/blog/assets/**")
                .addResourceLocations(toResourceLocation(blogAssetDir))
                .setCachePeriod(31_536_000);

        registry.addResourceHandler("/api/activity/assets/**")
                .addResourceLocations(toResourceLocation(activityAssetDir))
                .setCachePeriod(31_536_000);

        registry.addResourceHandler("/files/**")
                .addResourceLocations(toResourceLocation(fileUploadDir))
                .setCachePeriod(31_536_000);
    }

    private String toResourceLocation(String dir) {
        Path path = Paths.get(dir).toAbsolutePath().normalize();
        String location = path.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
