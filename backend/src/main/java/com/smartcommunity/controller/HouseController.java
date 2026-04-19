package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.AddHouseReq;
import com.smartcommunity.entity.Property;
import com.smartcommunity.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/house")
@RequiredArgsConstructor
public class HouseController {

    private final PropertyMapper propertyMapper;

    @GetMapping("/list")
    public Result<List<Property>> list(@RequestParam(required = false) String building) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<Property>()
                .orderByAsc(Property::getId);
        if (building != null && !building.isBlank()) {
            wrapper.eq(Property::getBuilding, building);
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
        LocalDateTime now = LocalDateTime.now();
        Property p = new Property();
        p.setCommunity(req.getCommunity());
        p.setBuilding(req.getBuilding());
        p.setUnit(req.getUnit());
        p.setRoom(req.getRoom());
        p.setPropertyCode(buildPropertyCode(req.getBuilding(), req.getUnit(), req.getRoom(), now));
        p.setOwnerName(req.getOwnerName());
        p.setArea(req.getArea());
        p.setStatus(req.getStatus());
        p.setCreateTime(now);
        p.setUpdateTime(now);
        p.setIsDeleted(0);
        propertyMapper.insert(p);
        return Result.success(p);
    }

    @PutMapping("/{id}")
    public Result<Property> update(@PathVariable Long id, @RequestBody AddHouseReq req) {
        Property p = propertyMapper.selectById(id);
        if (p == null) {
            return Result.fail("property not found");
        }
        p.setCommunity(req.getCommunity());
        p.setBuilding(req.getBuilding());
        p.setUnit(req.getUnit());
        p.setRoom(req.getRoom());
        LocalDateTime registerTime = p.getCreateTime() == null ? LocalDateTime.now() : p.getCreateTime();
        p.setPropertyCode(buildPropertyCode(req.getBuilding(), req.getUnit(), req.getRoom(), registerTime));
        p.setOwnerName(req.getOwnerName());
        p.setArea(req.getArea());
        p.setStatus(req.getStatus());
        p.setUpdateTime(LocalDateTime.now());
        propertyMapper.updateById(p);
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
        int buildingNo = normalize2Digits(building);
        int unitNo = normalize2Digits(unit);
        int roomNo = normalize2Digits(room);
        int year = registerTime == null ? LocalDateTime.now().getYear() : registerTime.getYear();
        return String.format("YZ%02d%02d%02d%02d", buildingNo, unitNo, roomNo, year % 100);
    }

    private int normalize2Digits(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits) % 100;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
