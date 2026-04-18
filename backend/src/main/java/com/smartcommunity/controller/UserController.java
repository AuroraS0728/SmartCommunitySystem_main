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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/me")
    public Result<User> me() {
        Long uid = AuthContext.getUserId();
        return Result.success(userMapper.selectById(uid));
    }

    @GetMapping("/properties")
    public Result<List<Property>> myProperties() {
        Integer role = AuthContext.getRole();
        if (role != null && role == 2) {
            return Result.success(propertyMapper.selectList(new LambdaQueryWrapper<Property>().orderByAsc(Property::getId)));
        }
        List<Long> propertyIds = userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                        .eq(UserProperty::getUserId, AuthContext.getUserId()))
                .stream().map(UserProperty::getPropertyId).collect(Collectors.toList());
        if (propertyIds.isEmpty()) {
            return Result.success(List.of());
        }
        return Result.success(propertyMapper.selectList(new LambdaQueryWrapper<Property>()
                .in(Property::getId, propertyIds)
                .orderByAsc(Property::getId)));
    }

    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByAsc(User::getId);
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        List<User> all = userMapper.selectList(wrapper);
        int from = Math.max((pageNum - 1) * pageSize, 0);
        int to = Math.min(from + pageSize, all.size());
        List<User> records = from >= all.size() ? List.of() : all.subList(from, to);
        Map<String, Object> payload = new HashMap<>();
        payload.put("records", records);
        payload.put("total", all.size());
        payload.put("pageNum", pageNum);
        payload.put("pageSize", pageSize);
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
