package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.AddHouseReq;
import com.smartcommunity.entity.Property;
import com.smartcommunity.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        Property p = new Property();
        p.setCommunity(req.getCommunity());
        p.setBuilding(req.getBuilding());
        p.setUnit(req.getUnit());
        p.setRoom(req.getRoom());
        p.setOwnerName(req.getOwnerName());
        p.setArea(req.getArea());
        p.setStatus(req.getStatus());
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
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
}
