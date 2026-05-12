package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.Property;
import com.smartcommunity.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RealEstateArchiveService {

    private final PropertyMapper propertyMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> archive(String keyword, Integer status) {
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<Property>()
                .eq(Property::getIsDeleted, 0)
                .orderByAsc(Property::getBuilding)
                .orderByAsc(Property::getUnit)
                .orderByAsc(Property::getRoom);
        if (StringUtils.hasText(keyword)) {
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
        List<Property> properties = propertyMapper.selectList(wrapper);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", summary(properties));
        payload.put("buildingStats", buildingStats(properties));
        payload.put("properties", properties);
        return payload;
    }

    private Map<String, Object> summary(List<Property> properties) {
        int occupied = 0;
        int rented = 0;
        int vacant = 0;
        for (Property property : properties) {
            Integer status = property.getStatus();
            if (status == null) {
                continue;
            }
            if (status == 4) {
                occupied++;
            } else if (status == 5) {
                rented++;
            } else if (status == 3) {
                vacant++;
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProperties", properties.size());
        summary.put("occupiedCount", occupied);
        summary.put("rentedCount", rented);
        summary.put("vacantCount", vacant);
        summary.put("buildingCount", properties.stream().map(Property::getBuilding).filter(StringUtils::hasText).distinct().count());
        return summary;
    }

    private List<Map<String, Object>> buildingStats(List<Property> properties) {
        Map<String, List<Property>> groupMap = new LinkedHashMap<>();
        for (Property property : properties) {
            String building = StringUtils.hasText(property.getBuilding()) ? property.getBuilding().trim() : "未分配楼栋";
            groupMap.computeIfAbsent(building, key -> new ArrayList<>()).add(property);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        groupMap.forEach((building, list) -> {
            long occupied = list.stream().filter(item -> item.getStatus() != null && (item.getStatus() == 4 || item.getStatus() == 5)).count();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("building", building);
            row.put("propertyCount", list.size());
            row.put("occupiedCount", occupied);
            row.put("occupancyRate", list.isEmpty() ? 0 : Math.round(occupied * 1000D / list.size()) / 10D);
            rows.add(row);
        });
        return rows;
    }
}
