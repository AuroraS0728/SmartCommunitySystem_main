package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.BindPhoneReq;
import com.smartcommunity.dto.request.LoginReq;
import com.smartcommunity.dto.response.LoginResp;
import com.smartcommunity.dto.response.UserInfoResp;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.utils.JwtUtil;
import com.smartcommunity.utils.RedisUtil;
import com.smartcommunity.utils.WechatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

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

    @PostMapping("/wx-login")
    public Result<LoginResp> wxLogin(@RequestBody LoginReq req) {
        String openid = wechatUtil.exchangeCodeForOpenid(req.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)
                .last("LIMIT 1"));
        if (user == null) {
            User created = new User();
            created.setOpenid(openid);
            created.setRole(req.getRole() == null ? 1 : req.getRole());
            created.setNickname("wx_user_" + System.currentTimeMillis());
            created.setStatus(1);
            created.setCreateTime(LocalDateTime.now());
            created.setUpdateTime(LocalDateTime.now());
            created.setIsDeleted(0);
            userMapper.insert(created);
            user = created;
        }
        return Result.success(buildLoginResp(user));
    }

    @PostMapping("/account-login")
    public Result<LoginResp> accountLogin(@RequestBody LoginReq req) {
        if (!StringUtils.hasText(req.getAccount()) || !StringUtils.hasText(req.getPassword())) {
            return Result.fail("account or password is empty");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, req.getAccount().trim())
                .last("LIMIT 1"));
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
            // Allow local dev without redis.
        }
        return Result.success("ok", null);
    }

    private LoginResp buildLoginResp(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        UserInfoResp userInfo = new UserInfoResp();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        userInfo.setRole(user.getRole());
        resp.setUserInfo(userInfo);
        return resp;
    }
}
