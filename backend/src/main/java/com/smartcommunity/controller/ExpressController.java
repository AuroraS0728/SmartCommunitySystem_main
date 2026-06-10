package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.ExpressPackageSaveReq;
import com.smartcommunity.entity.ExpressPackage;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.ExpressPackageMapper;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/express")
@RequiredArgsConstructor
public class ExpressController {

    private final ExpressPackageMapper expressPackageMapper;
    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;

    @GetMapping("/admin/list")
    public Result<List<Map<String, Object>>> adminList(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String company,
                                                       @RequestParam(required = false) Integer status) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        List<ExpressPackage> rows = expressPackageMapper.selectList(new LambdaQueryWrapper<ExpressPackage>()
                .eq(ExpressPackage::getIsDeleted, 0)
                .eq(status != null, ExpressPackage::getStatus, status)
                .eq(hasText(company), ExpressPackage::getCourierCompany, clean(company))
                .and(hasText(keyword), wrapper -> wrapper
                        .like(ExpressPackage::getCourierCompany, keyword)
                        .or()
                        .like(ExpressPackage::getTrackingNo, keyword)
                        .or()
                        .like(ExpressPackage::getRecipientName, keyword)
                        .or()
                        .like(ExpressPackage::getRecipientPhone, keyword)
                        .or()
                        .like(ExpressPackage::getShelfLocation, keyword))
                .orderByAsc(ExpressPackage::getStatus)
                .orderByDesc(ExpressPackage::getArrivedTime));
        return Result.success(toExpressRows(rows));
    }

    @PostMapping("/admin")
    public Result<ExpressPackage> create(@RequestBody ExpressPackageSaveReq req) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        ExpressPackage row = new ExpressPackage();
        applyReq(row, req);
        row.setStatus(req == null || req.getStatus() == null ? 0 : req.getStatus());
        row.setArrivedTime(req == null || req.getArrivedTime() == null ? LocalDateTime.now() : req.getArrivedTime());
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        row.setIsDeleted(0);
        expressPackageMapper.insert(row);
        return Result.success(row);
    }

    @PutMapping("/admin/{id}")
    public Result<ExpressPackage> update(@PathVariable Long id, @RequestBody ExpressPackageSaveReq req) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        ExpressPackage row = expressPackageMapper.selectById(id);
        if (row == null || safeInt(row.getIsDeleted()) == 1) {
            return Result.fail("express package not found");
        }
        applyReq(row, req);
        row.setUpdateTime(LocalDateTime.now());
        expressPackageMapper.updateById(row);
        return Result.success(row);
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        expressPackageMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myPackages() {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        Set<Long> propertyIds = myPropertyIds(userId);
        if (propertyIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<ExpressPackage> rows = expressPackageMapper.selectList(new LambdaQueryWrapper<ExpressPackage>()
                .eq(ExpressPackage::getIsDeleted, 0)
                .in(ExpressPackage::getPropertyId, propertyIds)
                .orderByAsc(ExpressPackage::getStatus)
                .orderByDesc(ExpressPackage::getArrivedTime));
        return Result.success(toExpressRows(rows));
    }

    @PostMapping("/{id}/pickup")
    public Result<ExpressPackage> pickup(@PathVariable Long id) {
        Long userId = AuthContext.getUserId();
        if (userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        ExpressPackage row = expressPackageMapper.selectById(id);
        if (row == null || safeInt(row.getIsDeleted()) == 1) {
            return Result.fail("express package not found");
        }
        if (!myPropertyIds(userId).contains(row.getPropertyId())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        row.setStatus(1);
        row.setPickupTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        expressPackageMapper.updateById(row);
        return Result.success(row);
    }

    private void applyReq(ExpressPackage row, ExpressPackageSaveReq req) {
        row.setPropertyId(req == null ? null : req.getPropertyId());
        row.setCourierCompany(clean(req == null ? null : req.getCourierCompany()));
        row.setTrackingNo(clean(req == null ? null : req.getTrackingNo()));
        row.setPickupCode(clean(req == null ? null : req.getPickupCode()));
        row.setRecipientName(clean(req == null ? null : req.getRecipientName()));
        row.setRecipientPhone(clean(req == null ? null : req.getRecipientPhone()));
        row.setShelfLocation(clean(req == null ? null : req.getShelfLocation()));
        row.setStatus(req == null || req.getStatus() == null ? safeInt(row.getStatus()) : req.getStatus());
        row.setArrivedTime(req == null || req.getArrivedTime() == null ? row.getArrivedTime() : req.getArrivedTime());
        row.setPickupTime(req == null ? row.getPickupTime() : req.getPickupTime());
        row.setRemark(clean(req == null ? null : req.getRemark()));
    }

    private List<Map<String, Object>> toExpressRows(List<ExpressPackage> rows) {
        Map<Long, Property> propertyMap = loadPropertyMap(rows.stream().map(ExpressPackage::getPropertyId).collect(Collectors.toSet()));
        return rows.stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("propertyId", item.getPropertyId());
            Property property = propertyMap.get(item.getPropertyId());
            row.put("propertyCode", property == null ? "--" : property.getPropertyCode());
            row.put("ownerName", property == null ? "--" : property.getOwnerName());
            row.put("courierCompany", item.getCourierCompany());
            row.put("trackingNo", item.getTrackingNo());
            row.put("pickupCode", item.getPickupCode());
            row.put("recipientName", item.getRecipientName());
            row.put("recipientPhone", item.getRecipientPhone());
            row.put("shelfLocation", item.getShelfLocation());
            row.put("status", item.getStatus());
            row.put("statusText", expressStatusText(item.getStatus()));
            row.put("arrivedTime", item.getArrivedTime());
            row.put("pickupTime", item.getPickupTime());
            row.put("remark", item.getRemark());
            row.put("createTime", item.getCreateTime());
            row.put("updateTime", item.getUpdateTime());
            return row;
        }).toList();
    }

    private Map<Long, Property> loadPropertyMap(Set<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return propertyMapper.selectList(new LambdaQueryWrapper<Property>()
                        .in(Property::getId, propertyIds)
                        .eq(Property::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(Property::getId, item -> item, (a, b) -> a));
    }

    private Set<Long> myPropertyIds(Long userId) {
        return userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                        .eq(UserProperty::getUserId, userId)
                        .eq(UserProperty::getIsDeleted, 0))
                .stream()
                .map(UserProperty::getPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String expressStatusText(Integer status) {
        return safeInt(status) == 1 ? "已取件" : "待取件";
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
