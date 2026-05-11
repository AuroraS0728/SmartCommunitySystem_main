package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.entity.EmergencyKeyword;
import com.smartcommunity.mapper.EmergencyKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/emergency-keyword")
@RequiredArgsConstructor
public class EmergencyKeywordController {

    private final EmergencyKeywordMapper emergencyKeywordMapper;

    @GetMapping("/list")
    public Result<List<EmergencyKeyword>> list(@RequestParam(required = false) Integer enabled) {
        LambdaQueryWrapper<EmergencyKeyword> wrapper = new LambdaQueryWrapper<EmergencyKeyword>()
                .eq(EmergencyKeyword::getIsDeleted, 0);
        if (enabled != null) {
            wrapper.eq(EmergencyKeyword::getEnabled, enabled);
        }
        wrapper.orderByDesc(EmergencyKeyword::getId);
        return Result.success(emergencyKeywordMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<EmergencyKeyword> add(@RequestBody EmergencyKeyword req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can manage keywords");
        }
        if (req == null || !StringUtils.hasText(req.getKeyword())) {
            return Result.fail(StatusCode.BAD_REQUEST, "keyword is empty");
        }
        LocalDateTime now = LocalDateTime.now();
        EmergencyKeyword row = new EmergencyKeyword();
        row.setKeyword(req.getKeyword().trim());
        row.setEnabled(req.getEnabled() == null ? 1 : req.getEnabled());
        row.setCreateTime(now);
        row.setUpdateTime(now);
        row.setIsDeleted(0);
        emergencyKeywordMapper.insert(row);
        return Result.success(row);
    }

    @PutMapping("/{id}")
    public Result<EmergencyKeyword> update(@PathVariable Long id, @RequestBody EmergencyKeyword req) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can manage keywords");
        }
        EmergencyKeyword row = emergencyKeywordMapper.selectById(id);
        if (row == null || Integer.valueOf(1).equals(row.getIsDeleted())) {
            return Result.fail(StatusCode.NOT_FOUND, "keyword not found");
        }
        if (req != null && StringUtils.hasText(req.getKeyword())) {
            row.setKeyword(req.getKeyword().trim());
        }
        if (req != null && req.getEnabled() != null) {
            row.setEnabled(req.getEnabled());
        }
        row.setUpdateTime(LocalDateTime.now());
        emergencyKeywordMapper.updateById(row);
        return Result.success(row);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!isAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can manage keywords");
        }
        emergencyKeywordMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private boolean isAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == 2;
    }
}
