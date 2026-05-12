package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.dto.request.BlogProfileReq;
import com.smartcommunity.dto.response.BlogAssetUploadResp;
import com.smartcommunity.entity.SystemConfig;
import com.smartcommunity.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogProfileService {

    private static final String CONFIG_KEY = "blog.profile";
    private static final int MAX_TEXT = 500;
    private static final int MAX_LINE = 180;
    private static final int MAX_LIST_ITEMS = 30;
    private static final int MAX_PROJECTS = 24;
    private static final long MAX_IMAGE_BYTES = 8L * 1024L * 1024L;
    private static final DateTimeFormatter ASSET_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final SystemConfigMapper systemConfigMapper;
    private final ObjectMapper objectMapper;

    @Value("${blog.asset-dir:./blog-assets}")
    private String assetDir;

    public BlogProfileReq getProfile() {
        SystemConfig config = selectConfig();
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultProfile();
        }
        try {
            return sanitize(objectMapper.readValue(config.getConfigValue(), BlogProfileReq.class));
        } catch (Exception ignored) {
            return defaultProfile();
        }
    }

    @Transactional
    public BlogProfileReq saveProfile(BlogProfileReq req) {
        BlogProfileReq profile = sanitize(req == null ? defaultProfile() : req);
        String json;
        try {
            json = objectMapper.writeValueAsString(profile);
        } catch (Exception ex) {
            throw new IllegalArgumentException("profile json is invalid", ex);
        }

        LocalDateTime now = LocalDateTime.now();
        SystemConfig config = selectConfig();
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(CONFIG_KEY);
            config.setDescription("Blog profile content");
            config.setConfigValue(json);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setIsDeleted(0);
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(json);
            config.setUpdateTime(now);
            config.setIsDeleted(0);
            systemConfigMapper.updateById(config);
        }
        return profile;
    }

    public BlogAssetUploadResp saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file is empty");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("image file is too large");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String ext = switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("only jpg, png, webp and gif images are supported");
        };

        String dateDir = LocalDate.now().format(ASSET_DATE);
        String filename = UUID.randomUUID() + ext;
        Path root = Paths.get(assetDir).toAbsolutePath().normalize();
        Path dir = root.resolve(dateDir).normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("invalid asset path");
        }
        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("save image failed", ex);
        }
        String url = "/api/blog/assets/" + dateDir + "/" + filename;
        return new BlogAssetUploadResp(url, filename);
    }

    public boolean passwordMatches(String password, String configuredHash) {
        if (!StringUtils.hasText(password) || !StringUtils.hasText(configuredHash)) {
            return false;
        }
        String normalized = configuredHash.trim().toLowerCase(Locale.ROOT);
        return MessageDigest.isEqual(
                sha256(password).getBytes(StandardCharsets.US_ASCII),
                normalized.getBytes(StandardCharsets.US_ASCII)
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("sha256 unavailable", ex);
        }
    }

    private BlogProfileReq sanitize(BlogProfileReq source) {
        if (source == null) {
            return defaultProfile();
        }
        BlogProfileReq profile = new BlogProfileReq();
        profile.setSiteName(clean(source.getSiteName(), 80, "Sqh的灵感仓库"));
        profile.setBrand(clean(source.getBrand(), 80, "Sqh's Pixel Lab"));
        profile.setHeroTitle(clean(source.getHeroTitle(), 80, "Sqh的灵感仓库"));
        profile.setHeroTagline(clean(source.getHeroTagline(), MAX_LINE, "把毕设、灵感和正在生长的代码收进一个阳光像素世界。"));
        profile.setAvatar(clean(source.getAvatar(), 16, "SQH"));
        profile.setProfileName(clean(source.getProfileName(), 80, "宋庆涵 · 智慧社区探险家"));
        profile.setProfileLine1(clean(source.getProfileLine1(), MAX_TEXT, "辽宁工程技术大学 软件学院 22级，正在把物业管理场景做成可运行、可演示、可部署的毕业设计。"));
        profile.setProfileLine2(clean(source.getProfileLine2(), MAX_TEXT, "热衷构建“代码 + 玩法”的实用工具，擅长 Spring Boot、Vue、微信小程序和 MyBatis-Plus 生态。"));
        profile.setProfileLine3(clean(source.getProfileLine3(), MAX_TEXT, "正在探索智能派单、信用积分、投诉情感分析和隐式画像推荐在智慧社区里的落地方式。"));
        profile.setSkills(cleanList(source.getSkills(), MAX_LIST_ITEMS, 40));
        profile.setUpdates(cleanList(source.getUpdates(), MAX_LIST_ITEMS, MAX_LINE));
        profile.setContacts(cleanList(source.getContacts(), MAX_LIST_ITEMS, MAX_LINE));
        profile.setProjects(cleanProjects(source.getProjects()));
        profile.setFooterText(clean(source.getFooterText(), MAX_LINE, "像素风格个人工坊 · 智慧社区毕设项目 © 2026"));
        return profile;
    }

    private List<BlogProfileReq.BlogProjectReq> cleanProjects(List<BlogProfileReq.BlogProjectReq> projects) {
        List<BlogProfileReq.BlogProjectReq> result = new ArrayList<>();
        if (projects == null) {
            return result;
        }
        for (BlogProfileReq.BlogProjectReq item : projects) {
            if (item == null || result.size() >= MAX_PROJECTS) {
                continue;
            }
            BlogProfileReq.BlogProjectReq project = new BlogProfileReq.BlogProjectReq();
            project.setTitle(clean(item.getTitle(), 100, "未命名项目"));
            project.setDescription(clean(item.getDescription(), 700, ""));
            project.setImageUrl(clean(item.getImageUrl(), 300, ""));
            project.setHref(clean(item.getHref(), 300, ""));
            project.setTags(cleanList(item.getTags(), 12, 40));
            result.add(project);
        }
        return result;
    }

    private List<String> cleanList(List<String> items, int maxItems, int maxLength) {
        List<String> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (String item : items) {
            if (result.size() >= maxItems) {
                break;
            }
            String text = clean(item, maxLength, "");
            if (StringUtils.hasText(text)) {
                result.add(text);
            }
        }
        return result;
    }

    private String clean(String value, int maxLength, String fallback) {
        String text = value == null ? "" : value.trim();
        if (!StringUtils.hasText(text)) {
            return fallback;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private BlogProfileReq defaultProfile() {
        BlogProfileReq profile = new BlogProfileReq();
        profile.setSiteName("Sqh的灵感仓库");
        profile.setBrand("Sqh's Pixel Lab");
        profile.setHeroTitle("Sqh的灵感仓库");
        profile.setHeroTagline("把毕设、灵感和正在生长的代码收进一个阳光像素世界。");
        profile.setAvatar("SQH");
        profile.setProfileName("宋庆涵 · 智慧社区探险家");
        profile.setProfileLine1("辽宁工程技术大学 软件学院 22级，正在把物业管理场景做成可运行、可演示、可部署的毕业设计。");
        profile.setProfileLine2("热衷构建“代码 + 玩法”的实用工具，擅长 Spring Boot、Vue、微信小程序和 MyBatis-Plus 生态。");
        profile.setProfileLine3("正在探索智能派单、信用积分、投诉情感分析和隐式画像推荐在智慧社区里的落地方式。");
        profile.setSkills(List.of("Spring Boot", "Vue 3", "微信小程序", "MyBatis-Plus", "Redis", "规则引擎"));
        profile.setUpdates(List.of(
                "完成阿里云轻量部署与域名备案接入",
                "完成智慧社区三端联调与部署路径整理",
                "投诉情感分析、信用积分、SLA 监控模块持续完善",
                "隐式画像融合推荐进行中"
        ));
        profile.setContacts(List.of("邮箱：shan.han@edu.cn", "GitHub：github.com/auroras0728", "站点：wiseprop.online"));
        BlogProfileReq.BlogProjectReq project = new BlogProfileReq.BlogProjectReq();
        project.setTitle("智慧社区物业管理系统 ｜ 毕设核心作品");
        project.setDescription("业主、物业、维修三端闭环，包含智能派单、信用积分、投诉情感分析、SLA 监控、催缴提醒和实时消息。");
        project.setHref("/bs/");
        project.setTags(List.of("Spring Boot", "Vue 3", "微信小程序", "MyBatis-Plus", "Redis"));
        profile.setProjects(List.of(project));
        profile.setFooterText("像素风格个人工坊 · 智慧社区毕设项目 © 2026");
        return profile;
    }

    private SystemConfig selectConfig() {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, CONFIG_KEY)
                .eq(SystemConfig::getIsDeleted, 0)
                .last("LIMIT 1"));
    }
}
