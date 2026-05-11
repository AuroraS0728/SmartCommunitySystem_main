package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.PropertyTask;
import com.smartcommunity.mapper.PropertyTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/property-task")
@RequiredArgsConstructor
public class PropertyTaskController {

    private final PropertyTaskMapper propertyTaskMapper;

    @GetMapping("/list")
    public Result<List<PropertyTask>> list(@RequestParam(required = false) Long assignedTo,
                                           @RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || uid == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        LambdaQueryWrapper<PropertyTask> wrapper = new LambdaQueryWrapper<PropertyTask>()
                .eq(PropertyTask::getIsDeleted, 0);
        if (role == 3) {
            wrapper.eq(PropertyTask::getAssignedTo, uid);
        } else if (assignedTo != null) {
            wrapper.eq(PropertyTask::getAssignedTo, assignedTo);
        }
        if (status != null) {
            wrapper.eq(PropertyTask::getStatus, status);
        }
        wrapper.orderByDesc(PropertyTask::getId);
        return Result.success(propertyTaskMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<PropertyTask> create(@RequestBody PropertyTask req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can create task");
        }
        if (req == null || !StringUtils.hasText(req.getTitle())) {
            return Result.fail(StatusCode.BAD_REQUEST, "title is empty");
        }
        LocalDateTime now = LocalDateTime.now();
        PropertyTask row = new PropertyTask();
        row.setTitle(req.getTitle().trim());
        row.setDescription(StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : null);
        row.setAssignedTo(req.getAssignedTo());
        row.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        row.setCreateTime(now);
        row.setUpdateTime(now);
        row.setIsDeleted(0);
        propertyTaskMapper.insert(row);
        return Result.success(row);
    }

    @PutMapping("/{id}")
    public Result<PropertyTask> update(@PathVariable Long id, @RequestBody PropertyTask req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can update task");
        }
        PropertyTask row = propertyTaskMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "task not found");
        }
        if (req != null && StringUtils.hasText(req.getTitle())) {
            row.setTitle(req.getTitle().trim());
        }
        if (req != null) {
            row.setDescription(req.getDescription());
            row.setAssignedTo(req.getAssignedTo());
            if (req.getStatus() != null) {
                row.setStatus(req.getStatus());
            }
        }
        row.setUpdateTime(LocalDateTime.now());
        propertyTaskMapper.updateById(row);
        return Result.success(row);
    }

    @PostMapping("/{id}/complete")
    public Result<PropertyTask> complete(@PathVariable Long id) {
        PropertyTask row = propertyTaskMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "task not found");
        }
        Integer role = AuthContext.getRole();
        Long uid = AuthContext.getUserId();
        if (role == null || (role != 2 && !(role == 3 && uid != null && uid.equals(row.getAssignedTo())))) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        row.setStatus(1);
        row.setUpdateTime(LocalDateTime.now());
        propertyTaskMapper.updateById(row);
        return Result.success(row);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete task");
        }
        propertyTaskMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private boolean isAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == 2;
    }
}
