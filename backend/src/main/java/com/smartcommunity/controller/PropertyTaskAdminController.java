package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.PropertyTask;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.PropertyTaskMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PropertyTaskAdminController {

    private final PropertyTaskMapper propertyTaskMapper;
    private final UserMapper userMapper;

    @GetMapping("/api/tasks")
    public Result<Map<String, Object>> tasks(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) Long assignedTo,
                                             @RequestParam(required = false) String keyword) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can query tasks");
        }
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (safePageNum - 1) * safePageSize;

        LambdaQueryWrapper<PropertyTask> base = buildWrapper(status, assignedTo, keyword);
        Long total = propertyTaskMapper.selectCount(base);
        List<PropertyTask> records = List.of();
        if (total != null && total > 0) {
            records = propertyTaskMapper.selectList(buildWrapper(status, assignedTo, keyword)
                    .orderByAsc(PropertyTask::getStatus)
                    .orderByDesc(PropertyTask::getId)
                    .last("LIMIT " + offset + "," + safePageSize));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("records", taskRows(records));
        payload.put("total", total == null ? 0 : total);
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    @PutMapping("/api/tasks/{id}/complete")
    public Result<PropertyTask> complete(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can complete tasks");
        }
        PropertyTask row = propertyTaskMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "task not found");
        }
        row.setStatus(1);
        row.setUpdateTime(LocalDateTime.now());
        propertyTaskMapper.updateById(row);
        return Result.success(row);
    }

    private LambdaQueryWrapper<PropertyTask> buildWrapper(Integer status, Long assignedTo, String keyword) {
        LambdaQueryWrapper<PropertyTask> wrapper = new LambdaQueryWrapper<PropertyTask>()
                .eq(PropertyTask::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(PropertyTask::getStatus, status);
        }
        if (assignedTo != null) {
            wrapper.eq(PropertyTask::getAssignedTo, assignedTo);
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(PropertyTask::getTitle, text)
                    .or()
                    .like(PropertyTask::getDescription, text));
        }
        return wrapper;
    }

    private List<Map<String, Object>> taskRows(List<PropertyTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = tasks.stream()
                .map(PropertyTask::getAssignedTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> users = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
                        .eq(User::getIsDeleted, 0))
                .stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
        return tasks.stream().map(task -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", task.getId());
            row.put("title", task.getTitle());
            row.put("description", task.getDescription());
            row.put("assignedTo", task.getAssignedTo());
            row.put("assignedToName", displayName(users.get(task.getAssignedTo()), task.getAssignedTo()));
            row.put("status", task.getStatus());
            row.put("createTime", task.getCreateTime());
            row.put("updateTime", task.getUpdateTime());
            row.put("isDeleted", task.getIsDeleted());
            return row;
        }).toList();
    }

    private String displayName(User user, Long id) {
        if (user != null) {
            if (StringUtils.hasText(user.getNickname())) {
                return user.getNickname();
            }
            if (StringUtils.hasText(user.getPhone())) {
                return user.getPhone();
            }
        }
        return id == null ? "未指派" : "用户" + id;
    }

    private boolean isAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }
}
