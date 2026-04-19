package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.BindPhoneReq;
import com.smartcommunity.dto.request.ChangePasswordReq;
import com.smartcommunity.dto.request.LoginReq;
import com.smartcommunity.dto.request.OwnerRegisterReq;
import com.smartcommunity.dto.response.LoginResp;
import com.smartcommunity.dto.response.UserInfoResp;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.utils.JwtUtil;
import com.smartcommunity.utils.RedisUtil;
import com.smartcommunity.utils.WechatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final WechatUtil wechatUtil;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;
    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;

    @PostMapping("/wx-login")
    public Result<LoginResp> wxLogin(@RequestBody LoginReq req) {
        String openid = wechatUtil.exchangeCodeForOpenid(req.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)
                .last("limit 1"));
        if (user == null) {
            User created = new User();
            created.setOpenid(openid);
            created.setRole(req.getRole() == null ? 1 : req.getRole());
            created.setNickname("wx_user_" + System.currentTimeMillis());
            created.setStatus(1);
            created.setMustChangePassword(0);
            created.setCreateTime(LocalDateTime.now());
            created.setUpdateTime(LocalDateTime.now());
            created.setIsDeleted(0);
            userMapper.insert(created);
            user = created;
        }
        return Result.success(buildLoginResp(user));
    }

    @PostMapping("/owner-register")
    public Result<Map<String, Object>> ownerRegister(@RequestBody OwnerRegisterReq req) {
        if (!StringUtils.hasText(req.getPhone()) || !StringUtils.hasText(req.getPropertyCode())) {
            return Result.fail("phone and propertyCode are required");
        }
        String phone = req.getPhone().trim();
        String propertyCode = req.getPropertyCode().trim().toUpperCase();
        if (!phone.matches("^1\\d{10}$")) {
            return Result.fail("invalid phone format");
        }

        Property property = propertyMapper.selectOne(new LambdaQueryWrapper<Property>()
                .eq(Property::getPropertyCode, propertyCode)
                .eq(Property::getIsDeleted, 0)
                .last("limit 1"));
        if (property == null) {
            return Result.fail("propertyCode not found");
        }
        Long existedCount = userPropertyMapper.selectCount(new LambdaQueryWrapper<UserProperty>()
                .eq(UserProperty::getPropertyId, property.getId())
                .eq(UserProperty::getIsPrimary, 1)
                .eq(UserProperty::getIsDeleted, 0));
        if (existedCount != null && existedCount > 0) {
            return Result.fail("this property has already been registered");
        }
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getRole, 1)
                .eq(User::getIsDeleted, 0));
        if (phoneCount != null && phoneCount > 0) {
            return Result.fail("phone already registered");
        }
        Long accountCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, propertyCode)
                .eq(User::getIsDeleted, 0));
        if (accountCount != null && accountCount > 0) {
            return Result.fail("this propertyCode account already exists");
        }

        String initPassword = reverseSixDigits(propertyCode);
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setAccount(propertyCode);
        user.setPassword(initPassword);
        user.setMustChangePassword(1);
        user.setRole(1);
        user.setNickname(StringUtils.hasText(property.getOwnerName()) ? property.getOwnerName() : ("owner_" + propertyCode));
        user.setPhone(phone);
        user.setPoints(0);
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsDeleted(0);
        userMapper.insert(user);

        UserProperty bind = new UserProperty();
        bind.setUserId(user.getId());
        bind.setPropertyId(property.getId());
        bind.setRelation("self");
        bind.setIsPrimary(1);
        bind.setCreateTime(now);
        bind.setUpdateTime(now);
        bind.setIsDeleted(0);
        userPropertyMapper.insert(bind);

        return Result.success(Map.of(
                "userId", user.getId(),
                "account", user.getAccount(),
                "initialPassword", initPassword,
                "mustChangePassword", true
        ));
    }

    @PostMapping("/change-password")
    public Result<Map<String, Object>> changePassword(@RequestHeader("Authorization") String authorization,
                                                      @RequestBody ChangePasswordReq req) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.fail("unauthorized");
        }
        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return Result.fail("unauthorized");
        }
        Long userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        if (!StringUtils.hasText(req.getOldPassword()) || !StringUtils.hasText(req.getNewPassword())) {
            return Result.fail("oldPassword and newPassword are required");
        }
        if (!Objects.equals(user.getPassword(), req.getOldPassword().trim())) {
            return Result.fail("old password incorrect");
        }
        String newPassword = req.getNewPassword().trim();
        if (!newPassword.matches("^\\d{6}$")) {
            return Result.fail("new password must be 6 digits");
        }
        if (Objects.equals(req.getOldPassword().trim(), newPassword)) {
            return Result.fail("new password can not be same as old password");
        }

        user.setPassword(newPassword);
        user.setMustChangePassword(0);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(Map.of(
                "userId", userId,
                "mustChangePassword", false
        ));
    }

    @PostMapping("/account-login")
    public Result<LoginResp> accountLogin(@RequestBody LoginReq req) {
        if (!StringUtils.hasText(req.getAccount()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("account or password is empty");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, req.getAccount().trim())
                .last("limit 1"));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return Result.fail("account or password incorrect");
        }
        if (!Objects.equals(user.getPassword(), req.getPassword())) {
            return Result.fail("account or password incorrect");
        }
        if (req.getRole() != null && !Objects.equals(req.getRole(), user.getRole())) {
            return Result.fail("role mismatch");
        }
        return Result.success(buildLoginResp(user));
    }

    @PostMapping("/bind-phone")
    public Result<Map<String, Object>> bindPhone(@RequestHeader("Authorization") String authorization,
                                                 @RequestBody BindPhoneReq req) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.fail("unauthorized");
        }
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("user not found");
        }
        user.setPhone(req.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(Map.of("userId", userId, "phone", req.getPhone()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.success("ok", null);
        }
        String token = authorization.substring(7);
        try {
            redisUtil.set("jwt:blacklist:" + token, "1", jwtUtil.getExpireSeconds());
        } catch (Exception ignored) {
            // allow local dev without redis
        }
        return Result.success("ok", null);
    }

    private LoginResp buildLoginResp(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setMustChangePassword(user.getRole() != null
                && user.getRole() == 1
                && user.getMustChangePassword() != null
                && user.getMustChangePassword() == 1);

        UserInfoResp userInfo = new UserInfoResp();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(user.getRole());
        resp.setUserInfo(userInfo);
        return resp;
    }

    private String reverseSixDigits(String source) {
        String digits = source == null ? "" : source.replaceAll("\\D", "");
        String padded = "000000" + digits;
        String rightSix = padded.substring(padded.length() - 6);
        return new StringBuilder(rightSix).reverse().toString();
    }
}
