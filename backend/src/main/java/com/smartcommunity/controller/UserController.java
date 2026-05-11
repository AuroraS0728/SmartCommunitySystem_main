package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.LocalCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                                            @RequestParam(required = false) Integer role,
                                            @RequestParam(required = false) String keyword) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        LambdaQueryWrapper<User> wrapper = buildUserPageWrapper(role, keyword);
        Long total = userMapper.selectCount(wrapper);
        List<User> records = List.of();
        if (total != null && total > 0) {
            LambdaQueryWrapper<User> pageWrapper = buildUserPageWrapper(role, keyword)
                    .last("LIMIT " + offset + "," + safePageSize);
            records = userMapper.selectList(pageWrapper);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("records", records);
        payload.put("total", total == null ? 0 : total);
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    private LambdaQueryWrapper<User> buildUserPageWrapper(Integer role, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, 0)
                .orderByAsc(User::getId);
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(User::getNickname, text)
                    .or()
                    .like(User::getPhone, text)
                    .or()
                    .like(User::getAccount, text));
        }
        return wrapper;
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
        if (req.getCreditScore() == null) {
            req.setCreditScore(100);
        }
        if (req.getHasElderly() == null) {
            req.setHasElderly(0);
        }
        if (req.getHasChild() == null) {
            req.setHasChild(0);
        }
        if (req.getHasPet() == null) {
            req.setHasPet(0);
        }
        if (req.getIsDeleted() == null) {
            req.setIsDeleted(0);
        }
        userMapper.insert(req);
        return Result.success(req);
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User req) {
        Integer role = AuthContext.getRole();
        Long currentUserId = AuthContext.getUserId();
        if (role == null || currentUserId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        boolean admin = role == 2;
        if (!admin && !Objects.equals(currentUserId, id)) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        if (req == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "request body is empty");
        }
        String profileError = validateProfileFields(req);
        if (profileError != null) {
            return Result.fail(StatusCode.BAD_REQUEST, profileError);
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("user not found");
        }
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(req.getAvatarUrl());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        if (admin) {
            user.setRole(req.getRole());
            user.setStatus(req.getStatus());
        }
        if (req.getHasElderly() != null) {
            user.setHasElderly(req.getHasElderly());
        }
        if (req.getHasChild() != null) {
            user.setHasChild(req.getHasChild());
        }
        if (req.getHasPet() != null) {
            user.setHasPet(req.getHasPet());
        }
        if (req.getHouseArea() != null) {
            user.setHouseArea(req.getHouseArea());
        }
        if (req.getRoomCount() != null) {
            user.setRoomCount(req.getRoomCount());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        localCacheService.evict("user:me:" + id);
        return Result.success(user);
    }

    private String validateProfileFields(User req) {
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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success("deleted", null);
    }
}
