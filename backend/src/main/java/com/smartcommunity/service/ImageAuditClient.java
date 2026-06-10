package com.smartcommunity.service;

import com.smartcommunity.config.ImageAuditProperties;
import com.smartcommunity.dto.response.ImageAuditModelResp;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class ImageAuditClient {

    private final ImageAuditProperties properties;
    private final RestTemplate restTemplate;

    public ImageAuditClient(ImageAuditProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    public ImageAuditModelResp detect(byte[] imageBytes, String filename) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("image bytes is empty");
        }
        // 使用multipart/form-data上传图片，和普通浏览器上传文件的格式一致。
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename == null || filename.isBlank() ? "image.jpg" : filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        // modelUrl默认是http://127.0.0.1:8000/predict，表示服务器本机的图片审核服务。
        return restTemplate.postForObject(properties.getModelUrl(), request, ImageAuditModelResp.class);
    }
}
