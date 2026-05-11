package com.smartcommunity.controller;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.UserProfileDTO;
import com.smartcommunity.dto.response.UserProfileVO;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.service.LocalCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserMapper userMapper;
    private final LocalCacheService localCacheService;

    @GetMapping("/api/user/info")
    public Result<UserProfileVO> info() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "user not found");
        }
        return Result.success(UserProfileVO.from(user));
    }

    @PutMapping("/api/user/profile")
    @Transactional
    public Result<UserProfileVO> updateOwnProfile(@RequestBody(required = false) UserProfileDTO req) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        return updateProfile(userId, req);
    }

    @PutMapping("/api/admin/user/{userId}/profile")
    @Transactional
    public Result<UserProfileVO> updateProfileByAdmin(@PathVariable Long userId,
                                                      @RequestBody(required = false) UserProfileDTO req) {
        Integer role = AuthContext.getRole();
        if (!RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can update user profile");
        }
        if (userId == null || userId <= 0) {
            return Result.fail(StatusCode.BAD_REQUEST, "userId is invalid");
        }
        return updateProfile(userId, req);
    }

    private Result<UserProfileVO> updateProfile(Long userId, UserProfileDTO req) {
        UserProfileDTO safeReq = req == null ? new UserProfileDTO() : req;
        String error = validate(safeReq);
        if (error != null) {
            return Result.fail(StatusCode.BAD_REQUEST, error);
        }

        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "user not found");
        }
        if (safeReq.getHasElderly() != null) {
            user.setHasElderly(safeReq.getHasElderly());
        }
        if (safeReq.getHasChild() != null) {
            user.setHasChild(safeReq.getHasChild());
        }
        if (safeReq.getHasPet() != null) {
            user.setHasPet(safeReq.getHasPet());
        }
        if (safeReq.getHouseArea() != null) {
            user.setHouseArea(safeReq.getHouseArea());
        }
        if (safeReq.getRoomCount() != null) {
            user.setRoomCount(safeReq.getRoomCount());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        localCacheService.evict("user:me:" + userId);
        return Result.success(UserProfileVO.from(user));
    }

    private String validate(UserProfileDTO req) {
        if (!isSwitchValue(req.getHasElderly())) {
            return "hasElderly must be 0 or 1";
        }
        if (!isSwitchValue(req.getHasChild())) {
            return "hasChild must be 0 or 1";
        }
        if (!isSwitchValue(req.getHasPet())) {
            return "hasPet must be 0 or 1";
        }
        if (req.getHouseArea() != null && req.getHouseArea() < 0) {
            return "houseArea must be greater than or equal to 0";
        }
        if (req.getRoomCount() != null && (req.getRoomCount() < 0 || req.getRoomCount() > 127)) {
            return "roomCount must be between 0 and 127";
        }
        return null;
    }

    private boolean isSwitchValue(Integer value) {
        return value == null || value == 0 || value == 1;
    }
}
