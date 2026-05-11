package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.SysMessage;
import com.smartcommunity.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageMapper sysMessageMapper;

    @GetMapping("/api/messages")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) Integer isRead,
                                            @RequestParam(required = false) Long userId) {
        Long currentUserId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (currentUserId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }

        Long targetUserId = RoleUtils.isPropertyAdmin(role) && userId != null ? userId : currentUserId;
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);

        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, targetUserId)
                .eq(SysMessage::getIsDeleted, 0)
                .orderByAsc(SysMessage::getIsRead)
                .orderByDesc(SysMessage::getId);
        if (isRead != null) {
            wrapper.eq(SysMessage::getIsRead, isRead);
        }

        Page<SysMessage> result = sysMessageMapper.selectPage(new Page<>(safePageNum, safePageSize), wrapper);
        Map<String, Object> payload = new HashMap<>();
        payload.put("records", result.getRecords());
        payload.put("total", result.getTotal());
        payload.put("pageNum", safePageNum);
        payload.put("pageSize", safePageSize);
        return Result.success(payload);
    }

    @GetMapping("/api/messages/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        Long currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        Long count = sysMessageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, currentUserId)
                .eq(SysMessage::getIsRead, 0)
                .eq(SysMessage::getIsDeleted, 0));
        return Result.success(Map.of("count", count == null ? 0 : count));
    }

    @PutMapping("/api/messages/{id}/read")
    public Result<SysMessage> markRead(@PathVariable Long id) {
        Long currentUserId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (currentUserId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }

        SysMessage message = sysMessageMapper.selectById(id);
        if (message == null || Integer.valueOf(1).equals(message.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "message not found");
        }
        if (!currentUserId.equals(message.getUserId()) && !RoleUtils.isPropertyAdmin(role)) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        message.setIsRead(1);
        message.setUpdateTime(LocalDateTime.now());
        sysMessageMapper.updateById(message);
        return Result.success(message);
    }

    @PutMapping("/api/messages/read-all")
    public Result<Map<String, Object>> markAllRead() {
        Long currentUserId = AuthContext.getUserId();
        if (currentUserId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        var wrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, currentUserId)
                .eq(SysMessage::getIsRead, 0)
                .eq(SysMessage::getIsDeleted, 0);
        SysMessage update = new SysMessage();
        update.setIsRead(1);
        update.setUpdateTime(LocalDateTime.now());
        int affected = sysMessageMapper.update(update, wrapper);
        return Result.success(Map.of("updated", affected));
    }
}
