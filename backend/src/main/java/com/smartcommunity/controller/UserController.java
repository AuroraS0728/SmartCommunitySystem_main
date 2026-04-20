package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.LocalCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;
    private final LocalCacheService localCacheService;

    @GetMapping("/me")
    public Result<User> me() {
        Long uid = AuthContext.getUserId();
        if (uid == null) {
            return Result.fail("unauthorized");
        }
        String cacheKey = "user:me:" + uid;
        User data = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(20), () -> userMapper.selectById(uid));
        return Result.success(data);
    }

    @GetMapping("/properties")
    public Result<List<Property>> myProperties() {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        String cacheKey = "user:properties:" + userId + ":" + role;
        List<Property> rows = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(20), () -> {
            if (role != null && role == 2) {
                return propertyMapper.selectList(new LambdaQueryWrapper<Property>()
                        .eq(Property::getIsDeleted, 0)
                        .orderByAsc(Property::getId));
            }
            List<Long> propertyIds = userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                            .eq(UserProperty::getUserId, userId)
                            .eq(UserProperty::getIsDeleted, 0))
                    .stream().map(UserProperty::getPropertyId).collect(Collectors.toList());
            if (propertyIds.isEmpty()) {
                return List.of();
            }
            return propertyMapper.selectList(new LambdaQueryWrapper<Property>()
                    .eq(Property::getIsDeleted, 0)
                    .in(Property::getId, propertyIds)
                    .orderByAsc(Property::getId));
        });
        return Result.success(rows);
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer role) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)
                .orderByAsc(User::getId);
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        Long total = userMapper.selectCount(wrapper);
        List<User> records = List.of();
        if (total != null && total > 0) {
            LambdaQueryWrapper<User> pageWrapper = new LambdaQueryWrapper<User>()
                    .eq(User::getIsDeleted, 0)
                    .orderByAsc(User::getId)
                    .last("LIMIT " + offset + "," + safePageSize);
            if (role != null) {
                pageWrapper.eq(User::getRole, role);
            }
            records = userMapper.selectList(pageWrapper);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("records", records);
        payload.put("total", total == null ? 0 : total);
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<User> add(@RequestBody User req) {
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        if (req.getStatus() == null) {
            req.setStatus(1);
        }
        if (req.getIsDeleted() == null) {
            req.setIsDeleted(0);
        }
        userMapper.insert(req);
        return Result.success(req);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("user not found");
        }
        user.setNickname(req.getNickname());
        user.setAvatarUrl(req.getAvatarUrl());
        user.setPhone(req.getPhone());
        user.setRole(req.getRole());
        user.setStatus(req.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success("deleted", null);
    }
}
