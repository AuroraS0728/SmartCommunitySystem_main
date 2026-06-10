package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.config.ImageAuditProperties;
import com.smartcommunity.dto.response.ImageAuditModelResp;
import com.smartcommunity.entity.ImageAuditResult;
import com.smartcommunity.mapper.ImageAuditResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ImageAuditService {

    public static final String AUDIT_PASS = "PASS";
    public static final String AUDIT_SUSPICIOUS = "SUSPICIOUS";
    public static final String AUDIT_MANUAL_REVIEW = "MANUAL_REVIEW";
    public static final String RISK_LOW = "LOW";
    public static final String RISK_MEDIUM = "MEDIUM";
    public static final String RISK_HIGH = "HIGH";

    private static final Logger log = LoggerFactory.getLogger(ImageAuditService.class);
    private static final String ACTIVITY_ASSET_PREFIX = "/api/activity/assets/";
    private static final String BLOG_ASSET_PREFIX = "/api/blog/assets/";
    private static final String FILE_ASSET_PREFIX = "/files/";
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final ImageAuditProperties properties;
    private final ImageAuditClient imageAuditClient;
    private final ImageAuditResultMapper imageAuditResultMapper;
    private final ObjectMapper objectMapper;
    private final RestTemplate downloadRestTemplate;

    @Value("${activity.asset-dir:./activity-assets}")
    private String activityAssetDir;

    @Value("${blog.asset-dir:./blog-assets}")
    private String blogAssetDir;

    @Value("${file.upload-dir:D:/upload}")
    private String fileUploadDir;

    public ImageAuditService(ImageAuditProperties properties,
                             ImageAuditClient imageAuditClient,
                             ImageAuditResultMapper imageAuditResultMapper,
                             ObjectMapper objectMapper,
                             RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.imageAuditClient = imageAuditClient;
        this.imageAuditResultMapper = imageAuditResultMapper;
        this.objectMapper = objectMapper;
        this.downloadRestTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .build();
    }

    public AuditSummary auditSecondHandImages(Long tradeId, String images) {
        if (tradeId == null) {
            return new AuditSummary(false, List.of());
        }
        List<String> imageUrls = parseImageUrls(images);
        imageAuditResultMapper.delete(new LambdaQueryWrapper<ImageAuditResult>()
                .eq(ImageAuditResult::getTradeId, tradeId));
        if (!properties.isEnabled() || imageUrls.isEmpty()) {
            return new AuditSummary(false, List.of());
        }

        boolean needReview = false;
        List<ImageAuditResult> results = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            ImageAuditResult result = detectImage(tradeId, imageUrl);
            imageAuditResultMapper.insert(result);
            results.add(result);
            if (AUDIT_MANUAL_REVIEW.equals(result.getAuditStatus())) {
                needReview = true;
            }
        }
        return new AuditSummary(needReview, results);
    }

    public List<ImageAuditResult> listByTradeIds(List<Long> tradeIds) {
        if (tradeIds == null || tradeIds.isEmpty()) {
            return List.of();
        }
        return imageAuditResultMapper.selectList(new LambdaQueryWrapper<ImageAuditResult>()
                .in(ImageAuditResult::getTradeId, tradeIds)
                .orderByAsc(ImageAuditResult::getTradeId)
                .orderByAsc(ImageAuditResult::getId));
    }

    public String buildAuditStatus(Double fakeProbability) {
        if (fakeProbability == null || !Double.isFinite(fakeProbability)) {
            return AUDIT_MANUAL_REVIEW;
        }
        // AI图片审核模型返回fake概率，后端再按阈值转成业务审核状态。
        if (fakeProbability >= properties.getReviewThreshold()) {
            return AUDIT_MANUAL_REVIEW;
        }
        if (fakeProbability >= properties.getFakeThreshold()) {
            return AUDIT_SUSPICIOUS;
        }
        return AUDIT_PASS;
    }

    public String buildRiskLevel(Double fakeProbability) {
        if (fakeProbability == null || !Double.isFinite(fakeProbability)) {
            return RISK_HIGH;
        }
        if (fakeProbability >= properties.getReviewThreshold()) {
            return RISK_HIGH;
        }
        if (fakeProbability >= properties.getFakeThreshold()) {
            return RISK_MEDIUM;
        }
        return RISK_LOW;
    }

    private ImageAuditResult detectImage(Long tradeId, String imageUrl) {
        try {
            ImagePayload payload = loadImage(imageUrl);
            // AI模型接入点：Spring Boot通过HTTP把图片传给Python FastAPI模型服务。
            ImageAuditModelResp modelResp = imageAuditClient.detect(payload.bytes(), payload.filename());
            Double fakeProbability = modelResp == null ? null : modelResp.getFakeProbability();
            ImageAuditResult result = baseResult(tradeId, imageUrl);
            result.setDetectLabel(modelResp == null ? null : modelResp.getLabel());
            result.setRealProbability(toDecimal(modelResp == null ? null : modelResp.getRealProbability()));
            result.setFakeProbability(toDecimal(fakeProbability));
            result.setThresholdValue(toDecimal(modelResp == null ? null : modelResp.getThreshold()));
            result.setRiskLevel(buildRiskLevel(fakeProbability));
            result.setAuditStatus(buildAuditStatus(fakeProbability));
            return result;
        } catch (Exception ex) {
            log.warn("second hand image audit failed, tradeId={}, imageUrl={}", tradeId, imageUrl, ex);
            ImageAuditResult result = baseResult(tradeId, imageUrl);
            result.setDetectLabel("DETECT_FAILED");
            result.setRiskLevel(RISK_HIGH);
            result.setAuditStatus(AUDIT_MANUAL_REVIEW);
            return result;
        }
    }

    private ImageAuditResult baseResult(Long tradeId, String imageUrl) {
        ImageAuditResult result = new ImageAuditResult();
        result.setTradeId(tradeId);
        result.setImageUrl(imageUrl);
        result.setCreateTime(LocalDateTime.now());
        return result;
    }

    private ImagePayload loadImage(String imageUrl) throws Exception {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IllegalArgumentException("image url is empty");
        }
        String trimmed = imageUrl.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("data:image/")) {
            int comma = trimmed.indexOf(',');
            if (comma <= 0) {
                throw new IllegalArgumentException("invalid data image url");
            }
            byte[] bytes = Base64.getDecoder().decode(trimmed.substring(comma + 1));
            if (bytes.length == 0) {
                throw new IllegalArgumentException("image bytes is empty");
            }
            return new ImagePayload(bytes, filenameFromDataUrl(lower));
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            ResponseEntity<byte[]> response = downloadRestTemplate.getForEntity(URI.create(trimmed), byte[].class);
            byte[] bytes = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || bytes == null || bytes.length == 0) {
                throw new IllegalStateException("download image failed");
            }
            return new ImagePayload(bytes, filenameFromUrl(trimmed));
        }

        Optional<Path> localPath = resolveLocalPath(trimmed);
        if (localPath.isEmpty()) {
            throw new IllegalArgumentException("image url cannot be resolved to local file");
        }
        Path path = localPath.get();
        return new ImagePayload(Files.readAllBytes(path), path.getFileName().toString());
    }

    private Optional<Path> resolveLocalPath(String imageUrl) {
        String withoutQuery = stripQuery(imageUrl).replace('\\', '/');
        if (withoutQuery.startsWith(ACTIVITY_ASSET_PREFIX)) {
            return resolveUnderRoot(activityAssetDir, withoutQuery.substring(ACTIVITY_ASSET_PREFIX.length()));
        }
        if (withoutQuery.startsWith(BLOG_ASSET_PREFIX)) {
            return resolveUnderRoot(blogAssetDir, withoutQuery.substring(BLOG_ASSET_PREFIX.length()));
        }
        if (withoutQuery.startsWith(FILE_ASSET_PREFIX)) {
            return resolveUnderRoot(fileUploadDir, withoutQuery.substring(FILE_ASSET_PREFIX.length()));
        }
        if (withoutQuery.toLowerCase(Locale.ROOT).startsWith("file:")) {
            try {
                Path path = Paths.get(URI.create(withoutQuery)).toAbsolutePath().normalize();
                return Files.isRegularFile(path) ? Optional.of(path) : Optional.empty();
            } catch (Exception ignored) {
                return Optional.empty();
            }
        }
        try {
            Path direct = Paths.get(withoutQuery).toAbsolutePath().normalize();
            if (Files.isRegularFile(direct)) {
                return Optional.of(direct);
            }
            String relativeText = withoutQuery.startsWith("/") ? withoutQuery.substring(1) : withoutQuery;
            Path relative = Paths.get(relativeText).toAbsolutePath().normalize();
            return Files.isRegularFile(relative) ? Optional.of(relative) : Optional.empty();
        } catch (InvalidPathException ex) {
            return Optional.empty();
        }
    }

    private Optional<Path> resolveUnderRoot(String rootDir, String relativePath) {
        try {
            Path root = Paths.get(rootDir).toAbsolutePath().normalize();
            String decoded = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
            Path target = root.resolve(decoded).normalize();
            if (!target.startsWith(root) || !Files.isRegularFile(target)) {
                return Optional.empty();
            }
            return Optional.of(target);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private List<String> parseImageUrls(String images) {
        if (!StringUtils.hasText(images)) {
            return List.of();
        }
        String trimmed = images.trim();
        Set<String> urls = new LinkedHashSet<>();
        if (trimmed.startsWith("[")) {
            try {
                List<String> parsed = objectMapper.readValue(trimmed, STRING_LIST_TYPE);
                for (String item : parsed) {
                    if (StringUtils.hasText(item)) {
                        urls.add(item.trim());
                    }
                }
                return List.copyOf(urls);
            } catch (Exception ignored) {
                // Fall back to simple splitting for legacy comma-separated values.
            }
        }
        for (String item : trimmed.split("[,;\\n]")) {
            if (StringUtils.hasText(item)) {
                urls.add(item.trim());
            }
        }
        return List.copyOf(urls);
    }

    private BigDecimal toDecimal(Double value) {
        if (value == null || !Double.isFinite(value)) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private String filenameFromUrl(String imageUrl) {
        String path = stripQuery(imageUrl);
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        if (!StringUtils.hasText(name)) {
            return "image.jpg";
        }
        return URLDecoder.decode(name, StandardCharsets.UTF_8);
    }

    private String filenameFromDataUrl(String dataUrl) {
        if (dataUrl.startsWith("data:image/png")) {
            return "image.png";
        }
        if (dataUrl.startsWith("data:image/webp")) {
            return "image.webp";
        }
        if (dataUrl.startsWith("data:image/gif")) {
            return "image.gif";
        }
        return "image.jpg";
    }

    private String stripQuery(String value) {
        int query = value.indexOf('?');
        return query >= 0 ? value.substring(0, query) : value;
    }

    public record AuditSummary(boolean needManualReview, List<ImageAuditResult> results) {
    }

    private record ImagePayload(byte[] bytes, String filename) {
    }
}
