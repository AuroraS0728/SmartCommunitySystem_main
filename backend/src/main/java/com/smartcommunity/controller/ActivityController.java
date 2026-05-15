package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.ActivityRegistrationReq;
import com.smartcommunity.dto.request.ActivityRegistrationReviewReq;
import com.smartcommunity.dto.request.ActivitySaveReq;
import com.smartcommunity.dto.response.AssetUploadResp;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityRegistration;
import com.smartcommunity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> ownerList(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String type) {
        return Result.success(activityService.ownerList(keyword, type, AuthContext.getUserId()));
    }

    @GetMapping("/my/registrations")
    public Result<List<Map<String, Object>>> myRegistrations() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(activityService.myRegistrations(userId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(activityService.detail(id, AuthContext.getUserId()));
    }

    @PostMapping("/{id}/register")
    public Result<ActivityRegistration> register(@PathVariable Long id, @RequestBody ActivityRegistrationReq req) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return Result.success(activityService.register(id, userId, req));
    }

    @GetMapping("/admin/list")
    public Result<List<Map<String, Object>>> adminList(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) Integer status) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.adminList(keyword, type, status));
    }

    @PostMapping("/admin")
    public Result<Activity> create(@RequestBody ActivitySaveReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.createActivity(req));
    }

    @PutMapping("/admin/{id}")
    public Result<Activity> update(@PathVariable Long id, @RequestBody ActivitySaveReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.updateActivity(id, req));
    }

    @GetMapping("/admin/{id}/registrations")
    public Result<List<Map<String, Object>>> registrations(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.registrations(id));
    }

    @PostMapping("/admin/registrations/{registrationId}/review")
    public Result<ActivityRegistration> review(@PathVariable Long registrationId,
                                               @RequestBody ActivityRegistrationReviewReq req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.reviewRegistration(registrationId, req == null ? null : req.getStatus()));
    }

    @PostMapping("/admin/assets")
    public Result<AssetUploadResp> uploadAsset(@RequestParam("file") MultipartFile file) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(activityService.saveImage(file));
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
