package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.BlogAdminLoginReq;
import com.smartcommunity.dto.request.BlogProfileReq;
import com.smartcommunity.dto.response.BlogAdminLoginResp;
import com.smartcommunity.dto.response.BlogAssetUploadResp;
import com.smartcommunity.service.BlogProfileService;
import com.smartcommunity.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/blog", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
@RequiredArgsConstructor
public class BlogController {

    private final BlogProfileService blogProfileService;
    private final JwtUtil jwtUtil;

    @Value("${blog.admin-password-sha256:}")
    private String adminPasswordSha256;

    @GetMapping("/profile")
    public Result<BlogProfileReq> profile() {
        return Result.success(blogProfileService.getProfile());
    }

    @PostMapping("/admin/login")
    public Result<BlogAdminLoginResp> adminLogin(@RequestBody BlogAdminLoginReq req) {
        if (!StringUtils.hasText(adminPasswordSha256)) {
            return Result.fail(StatusCode.ERROR, "blog admin password is not configured");
        }
        if (req == null || !blogProfileService.passwordMatches(req.getPassword(), adminPasswordSha256)) {
            return Result.fail(StatusCode.UNAUTHORIZED, "password incorrect");
        }
        String token = jwtUtil.generateToken(0L, RoleUtils.ROLE_ADMIN);
        return Result.success(new BlogAdminLoginResp(token, jwtUtil.getExpireSeconds()));
    }

    @PutMapping("/admin/profile")
    public Result<BlogProfileReq> saveProfile(@RequestBody BlogProfileReq req) {
        if (!isBlogAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(blogProfileService.saveProfile(req));
    }

    @PostMapping("/admin/assets")
    public Result<BlogAssetUploadResp> uploadAsset(@RequestParam("file") MultipartFile file) {
        if (!isBlogAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(blogProfileService.saveImage(file));
    }

    private boolean isBlogAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == RoleUtils.ROLE_ADMIN;
    }
}
