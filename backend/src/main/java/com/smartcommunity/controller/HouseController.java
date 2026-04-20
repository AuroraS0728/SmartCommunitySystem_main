package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.AddHouseReq;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/house")
@RequiredArgsConstructor
public class HouseController {

    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;
    private final UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<Property>> list(@RequestParam(value = "building", required = false) String building,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<Property>()
                .eq(Property::getIsDeleted, 0)
                .orderByAsc(Property::getId);
        if (building != null && !building.isBlank()) {
            wrapper.eq(Property::getBuilding, building);
        }
        if (keyword != null && !keyword.isBlank()) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(Property::getPropertyCode, text)
                    .or().like(Property::getBuilding, text)
                    .or().like(Property::getUnit, text)
                    .or().like(Property::getRoom, text)
                    .or().like(Property::getOwnerName, text)
                    .or().like(Property::getTenantName, text));
        }
        if (status != null) {
            wrapper.eq(Property::getStatus, status);
        }
        return Result.success(propertyMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Property> detail(@PathVariable Long id) {
        return Result.success(propertyMapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<Property> add(@RequestBody AddHouseReq req) {
        if (!StringUtils.hasText(req.getBuilding()) || !StringUtils.hasText(req.getUnit()) || !StringUtils.hasText(req.getRoom())) {
            return Result.fail("building/unit/room is required");
        }
        Integer targetStatus = req.getStatus() == null ? 3 : req.getStatus();
        if (Integer.valueOf(5).equals(targetStatus) && !hasValidRentalInfo(req)) {
            return Result.fail("tenantName and rentEndTime are required when status=已出租");
        }
        LocalDateTime now = LocalDateTime.now();
        Property p = new Property();
        p.setCommunity(req.getCommunity());
        p.setBuilding(req.getBuilding());
        p.setUnit(req.getUnit());
        p.setRoom(req.getRoom());
        p.setPropertyCode(buildPropertyCode(req.getBuilding(), req.getUnit(), req.getRoom(), now));
        p.setOwnerName(normalizeOwnerNameByStatus(targetStatus, req.getOwnerName()));
        p.setTenantName(normalizeTenantNameByStatus(targetStatus, req.getTenantName()));
        p.setRentEndTime(normalizeRentEndByStatus(targetStatus, req.getRentEndTime()));
        p.setArea(req.getArea());
        p.setStatus(targetStatus);
        p.setCreateTime(now);
        p.setUpdateTime(now);
        p.setIsDeleted(0);
        propertyMapper.insert(p);
        syncOwnerNicknameByProperty(p);
        return Result.success(p);
    }

    @PutMapping("/{id}")
    public Result<Property> update(@PathVariable Long id, @RequestBody AddHouseReq req) {
        Property p = propertyMapper.selectById(id);
        if (p == null) {
            return Result.fail("property not found");
        }
        Integer targetStatus = req.getStatus() == null ? p.getStatus() : req.getStatus();
        if (Integer.valueOf(5).equals(targetStatus) && !hasValidRentalInfo(req)) {
            return Result.fail("tenantName and rentEndTime are required when status=已出租");
        }
        p.setCommunity(req.getCommunity());
        p.setBuilding(req.getBuilding());
        p.setUnit(req.getUnit());
        p.setRoom(req.getRoom());
        LocalDateTime registerTime = p.getCreateTime() == null ? LocalDateTime.now() : p.getCreateTime();
        p.setPropertyCode(buildPropertyCode(req.getBuilding(), req.getUnit(), req.getRoom(), registerTime));
        p.setOwnerName(normalizeOwnerNameByStatus(targetStatus, req.getOwnerName()));
        p.setTenantName(normalizeTenantNameByStatus(targetStatus, req.getTenantName()));
        p.setRentEndTime(normalizeRentEndByStatus(targetStatus, req.getRentEndTime()));
        p.setArea(req.getArea());
        p.setStatus(targetStatus);
        p.setUpdateTime(LocalDateTime.now());
        propertyMapper.updateById(p);
        clearOwnerBindingWhenUnsold(p);
        syncOwnerNicknameByProperty(p);
        return Result.success(p);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        propertyMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success("import accepted: " + file.getOriginalFilename(), null);
    }

    @GetMapping("/export")
    public Result<String> exportExcel() {
        return Result.success("export placeholder: /download/house.xlsx", null);
    }

    private String buildPropertyCode(String building, String unit, String room, LocalDateTime registerTime) {
        int buildingNo = normalizeDigits(building, 2);
        int unitNo = normalizeDigits(unit, 2);
        int roomNo = normalizeDigits(room, 3);
        int year = registerTime == null ? LocalDateTime.now().getYear() : registerTime.getYear();
        return String.format("YZ%02d%02d%03d%02d", buildingNo, unitNo, roomNo, year % 100);
    }

    private int normalizeDigits(String value, int width) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        String normalizedDigits = value.replaceAll("[^0-9]", "");
        if (normalizedDigits.isEmpty()) {
            return 0;
        }
        try {
            int modulo = (int) Math.pow(10, Math.max(width, 1));
            return Integer.parseInt(normalizedDigits) % modulo;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String normalizeOwnerNameByStatus(Integer status, String ownerName) {
        if (Integer.valueOf(1).equals(status)) {
            return "";
        }
        return ownerName;
    }

    private String normalizeTenantNameByStatus(Integer status, String tenantName) {
        if (!Integer.valueOf(5).equals(status)) {
            return null;
        }
        return StringUtils.hasText(tenantName) ? tenantName.trim() : null;
    }

    private LocalDateTime normalizeRentEndByStatus(Integer status, LocalDateTime rentEndTime) {
        if (!Integer.valueOf(5).equals(status)) {
            return null;
        }
        return rentEndTime;
    }

    private boolean hasValidRentalInfo(AddHouseReq req) {
        return req != null && StringUtils.hasText(req.getTenantName()) && req.getRentEndTime() != null;
    }

    private void clearOwnerBindingWhenUnsold(Property property) {
        if (property == null || property.getId() == null) {
            return;
        }
        if (!Integer.valueOf(1).equals(property.getStatus())) {
            return;
        }
        userPropertyMapper.update(null, new LambdaUpdateWrapper<UserProperty>()
                .set(UserProperty::getIsDeleted, 1)
                .set(UserProperty::getUpdateTime, LocalDateTime.now())
                .eq(UserProperty::getPropertyId, property.getId())
                .eq(UserProperty::getIsDeleted, 0));
    }

    private void syncOwnerNicknameByProperty(Property property) {
        if (property == null || property.getId() == null) {
            return;
        }
        if (Integer.valueOf(1).equals(property.getStatus())) {
            return;
        }
        if (!StringUtils.hasText(property.getOwnerName())) {
            return;
        }
        List<Long> ownerUserIds = userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                        .eq(UserProperty::getPropertyId, property.getId())
                        .eq(UserProperty::getIsDeleted, 0)
                        .eq(UserProperty::getIsPrimary, 1))
                .stream()
                .map(UserProperty::getUserId)
                .filter(Objects::nonNull)
                .toList();
        if (ownerUserIds.isEmpty()) {
            return;
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .set(User::getNickname, property.getOwnerName().trim())
                .set(User::getUpdateTime, LocalDateTime.now())
                .in(User::getId, ownerUserIds)
                .eq(User::getRole, 1)
                .eq(User::getIsDeleted, 0));
    }
}
