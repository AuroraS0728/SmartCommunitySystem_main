package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.FacilitySaveReq;
import com.smartcommunity.entity.FacilityInfo;
import com.smartcommunity.mapper.FacilityInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityInfoMapper facilityInfoMapper;

    @GetMapping("/list")
    public Result<List<FacilityInfo>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) Integer status) {
        return Result.success(queryList(keyword, category, status));
    }

    @GetMapping("/{id}")
    public Result<FacilityInfo> detail(@PathVariable Long id) {
        FacilityInfo row = facilityInfoMapper.selectById(id);
        if (row == null || safeInt(row.getIsDeleted()) == 1) {
            return Result.fail("facility not found");
        }
        return Result.success(row);
    }

    @GetMapping("/admin/list")
    public Result<List<FacilityInfo>> adminList(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) Integer status) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return Result.success(queryList(keyword, category, status));
    }

    @PostMapping("/admin")
    public Result<FacilityInfo> create(@RequestBody FacilitySaveReq req) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        FacilityInfo row = new FacilityInfo();
        applyReq(row, req);
        row.setStatus(req == null || req.getStatus() == null ? 1 : req.getStatus());
        row.setSortOrder(req == null || req.getSortOrder() == null ? 0 : req.getSortOrder());
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        row.setIsDeleted(0);
        facilityInfoMapper.insert(row);
        return Result.success(row);
    }

    @PutMapping("/admin/{id}")
    public Result<FacilityInfo> update(@PathVariable Long id, @RequestBody FacilitySaveReq req) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        FacilityInfo row = facilityInfoMapper.selectById(id);
        if (row == null || safeInt(row.getIsDeleted()) == 1) {
            return Result.fail("facility not found");
        }
        applyReq(row, req);
        row.setUpdateTime(LocalDateTime.now());
        facilityInfoMapper.updateById(row);
        return Result.success(row);
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        facilityInfoMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private List<FacilityInfo> queryList(String keyword, String category, Integer status) {
        return facilityInfoMapper.selectList(new LambdaQueryWrapper<FacilityInfo>()
                .eq(FacilityInfo::getIsDeleted, 0)
                .eq(status != null, FacilityInfo::getStatus, status)
                .eq(hasText(category), FacilityInfo::getCategory, clean(category))
                .and(hasText(keyword), wrapper -> wrapper
                        .like(FacilityInfo::getName, keyword)
                        .or()
                        .like(FacilityInfo::getCategory, keyword)
                        .or()
                        .like(FacilityInfo::getLocation, keyword))
                .orderByAsc(FacilityInfo::getStatus)
                .orderByAsc(FacilityInfo::getSortOrder)
                .orderByDesc(FacilityInfo::getUpdateTime));
    }

    private void applyReq(FacilityInfo row, FacilitySaveReq req) {
        row.setName(clean(req == null ? null : req.getName()));
        row.setCategory(clean(req == null ? null : req.getCategory()));
        row.setLocation(clean(req == null ? null : req.getLocation()));
        row.setOpenHours(clean(req == null ? null : req.getOpenHours()));
        row.setContactPhone(clean(req == null ? null : req.getContactPhone()));
        row.setStatus(req == null || req.getStatus() == null ? safeInt(row.getStatus()) : req.getStatus());
        row.setSortOrder(req == null || req.getSortOrder() == null ? safeInt(row.getSortOrder()) : req.getSortOrder());
        row.setDescription(clean(req == null ? null : req.getDescription()));
        row.setImageUrls(clean(req == null ? null : req.getImageUrls()));
        row.setLastInspectionTime(req == null || req.getLastInspectionTime() == null ? row.getLastInspectionTime() : req.getLastInspectionTime());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
