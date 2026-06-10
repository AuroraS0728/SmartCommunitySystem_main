package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.AddHouseReq;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.LocalCacheService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/house")
@RequiredArgsConstructor
public class HouseController {

    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;
    private final UserMapper userMapper;
    private final LocalCacheService localCacheService;

    @GetMapping("/list")
    public Result<List<Property>> list(@RequestParam(value = "building", required = false) String building,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "status", required = false) Integer status,
                                       @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Integer safePageNum = pageNum == null ? null : Math.max(pageNum, 1);
        Integer safePageSize = pageSize == null ? null : Math.min(Math.max(pageSize, 1), 200);
        Integer offset = (safePageNum == null || safePageSize == null) ? null : (safePageNum - 1) * safePageSize;
        String pagePart = (safePageNum == null || safePageSize == null) ? "all" : (safePageNum + ":" + safePageSize);
        String cacheKey = "house:list:" + normalize(building) + ":" + normalize(keyword) + ":" + status + ":" + pagePart;

        List<Property> rows = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(20), () -> {
            LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<Property>()
                    .eq(Property::getIsDeleted, 0)
                    .orderByAsc(Property::getId);
            if (offset != null && safePageSize != null) {
                wrapper.last("LIMIT " + offset + "," + safePageSize);
            }
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
            return propertyMapper.selectList(wrapper);
        });
        return Result.success(rows);
    }

    @GetMapping("/{id}")
    public Result<Property> detail(@PathVariable Long id) {
        return Result.success(propertyMapper.selectById(id));
    }

    @PostMapping("/add")
    public Result<Property> add(@RequestBody AddHouseReq req) {
        Result<Void> permission = requirePropertyAdmin();
        if (permission != null) {
            return Result.fail(permission.getCode(), permission.getMessage());
        }
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
        localCacheService.evictByPrefix("house:");
        localCacheService.evictByPrefix("real-estate:");
        syncOwnerNicknameByProperty(p);
        syncOwnerAccountByProperty(p);
        return Result.success(p);
    }

    @PutMapping("/{id}")
    public Result<Property> update(@PathVariable Long id, @RequestBody AddHouseReq req) {
        Result<Void> permission = requirePropertyAdmin();
        if (permission != null) {
            return Result.fail(permission.getCode(), permission.getMessage());
        }
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
        localCacheService.evictByPrefix("house:");
        localCacheService.evictByPrefix("real-estate:");
        clearOwnerBindingWhenUnsold(p);
        syncOwnerNicknameByProperty(p);
        syncOwnerAccountByProperty(p);
        return Result.success(p);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Result<Void> permission = requirePropertyAdmin();
        if (permission != null) {
            return permission;
        }
        propertyMapper.deleteById(id);
        localCacheService.evictByPrefix("house:");
        localCacheService.evictByPrefix("real-estate:");
        return Result.success("deleted", null);
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        Result<Void> permission = requirePropertyAdmin();
        if (permission != null) {
            return Result.fail(permission.getCode(), permission.getMessage());
        }
        if (file == null || file.isEmpty()) {
            return Result.fail(StatusCode.BAD_REQUEST, "file is empty");
        }

        List<String> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return Result.fail(StatusCode.BAD_REQUEST, "csv is empty");
            }
            List<String> headers = parseCsvLine(stripBom(headerLine));
            String line;
            int rowNo = 1;
            while ((line = reader.readLine()) != null) {
                rowNo++;
                if (!StringUtils.hasText(line)) {
                    skipped++;
                    continue;
                }
                try {
                    Map<String, String> row = toRow(headers, parseCsvLine(line));
                    ImportResult result = upsertImportedProperty(row);
                    if (result.created()) {
                        created++;
                    } else if (result.updated()) {
                        updated++;
                    } else {
                        skipped++;
                    }
                } catch (Exception ex) {
                    errors.add("row " + rowNo + ": " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            return Result.fail(StatusCode.BAD_REQUEST, "import failed: " + ex.getMessage());
        }
        localCacheService.evictByPrefix("house:");
        localCacheService.evictByPrefix("real-estate:");
        return Result.success(Map.of(
                "created", created,
                "updated", updated,
                "skipped", skipped,
                "errors", errors
        ));
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "status", required = false) Integer status) throws Exception {
        Result<Void> permission = requirePropertyAdmin();
        if (permission != null) {
            response.setStatus(permission.getCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":" + permission.getCode() + ",\"message\":\"" + permission.getMessage() + "\"}");
            return;
        }
        String filename = URLEncoder.encode("house_export.csv", StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<Property>()
                .eq(Property::getIsDeleted, 0)
                .eq(status != null, Property::getStatus, status)
                .orderByAsc(Property::getId);
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(Property::getPropertyCode, text)
                    .or().like(Property::getBuilding, text)
                    .or().like(Property::getUnit, text)
                    .or().like(Property::getRoom, text)
                    .or().like(Property::getOwnerName, text)
                    .or().like(Property::getTenantName, text));
        }
        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,community,building,unit,room,propertyCode,ownerName,tenantName,rentEndTime,area,status");
            for (Property row : propertyMapper.selectList(wrapper)) {
                writer.println(String.join(",",
                        csv(row.getId()),
                        csv(row.getCommunity()),
                        csv(row.getBuilding()),
                        csv(row.getUnit()),
                        csv(row.getRoom()),
                        csv(row.getPropertyCode()),
                        csv(row.getOwnerName()),
                        csv(row.getTenantName()),
                        csv(formatDateTime(row.getRentEndTime())),
                        csv(row.getArea()),
                        csv(row.getStatus())
                ));
            }
        }
    }

    private ImportResult upsertImportedProperty(Map<String, String> row) {
        String building = pick(row, "building", "楼栋");
        String unit = pick(row, "unit", "单元");
        String room = pick(row, "room", "房号");
        if (!StringUtils.hasText(building) || !StringUtils.hasText(unit) || !StringUtils.hasText(room)) {
            throw new IllegalArgumentException("building/unit/room is required");
        }
        Integer status = parseStatus(pick(row, "status", "状态"), 3);
        String tenantName = pick(row, "tenantName", "tenant", "租户");
        LocalDateTime rentEndTime = parseDateTime(pick(row, "rentEndTime", "租约到期"));
        if (Integer.valueOf(5).equals(status) && (!StringUtils.hasText(tenantName) || rentEndTime == null)) {
            throw new IllegalArgumentException("tenantName and rentEndTime are required when status=已出租");
        }

        Long id = parseLong(pick(row, "id", "ID"));
        String propertyCode = pick(row, "propertyCode", "房产号");
        Property property = id == null ? null : propertyMapper.selectById(id);
        if (property == null && StringUtils.hasText(propertyCode)) {
            property = propertyMapper.selectOne(new LambdaQueryWrapper<Property>()
                    .eq(Property::getPropertyCode, propertyCode.trim())
                    .last("limit 1"));
        }
        boolean created = property == null;
        LocalDateTime now = LocalDateTime.now();
        if (property == null) {
            property = new Property();
            property.setCreateTime(now);
            property.setIsDeleted(0);
        }
        property.setCommunity(pick(row, "community", "小区"));
        property.setBuilding(building.trim());
        property.setUnit(unit.trim());
        property.setRoom(room.trim());
        LocalDateTime registerTime = property.getCreateTime() == null ? now : property.getCreateTime();
        property.setPropertyCode(StringUtils.hasText(propertyCode) ? propertyCode.trim() : buildPropertyCode(building, unit, room, registerTime));
        property.setOwnerName(normalizeOwnerNameByStatus(status, pick(row, "ownerName", "owner", "业主")));
        property.setTenantName(normalizeTenantNameByStatus(status, tenantName));
        property.setRentEndTime(normalizeRentEndByStatus(status, rentEndTime));
        property.setArea(parseDecimal(pick(row, "area", "面积")));
        property.setStatus(status);
        property.setUpdateTime(now);
        if (created) {
            propertyMapper.insert(property);
        } else {
            propertyMapper.updateById(property);
        }
        clearOwnerBindingWhenUnsold(property);
        syncOwnerNicknameByProperty(property);
        syncOwnerAccountByProperty(property);
        return new ImportResult(created, !created);
    }

    private Result<Void> requirePropertyAdmin() {
        if (!RoleUtils.isPropertyAdmin(AuthContext.getRole())) {
            return Result.fail(StatusCode.FORBIDDEN, "forbidden");
        }
        return null;
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

    private void syncOwnerAccountByProperty(Property property) {
        if (property == null || property.getId() == null || !StringUtils.hasText(property.getPropertyCode())) {
            return;
        }
        if (Integer.valueOf(1).equals(property.getStatus())) {
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
                .set(User::getAccount, property.getPropertyCode().trim())
                .set(User::getUpdateTime, LocalDateTime.now())
                .in(User::getId, ownerUserIds)
                .eq(User::getRole, 1)
                .eq(User::getIsDeleted, 0));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        return values;
    }

    private Map<String, String> toRow(List<String> headers, List<String> values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i).trim(), i < values.size() ? values.get(i) : "");
        }
        return row;
    }

    private String pick(Map<String, String> row, String... keys) {
        for (String key : keys) {
            String value = row.get(key);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String stripBom(String value) {
        return value != null && value.startsWith("\uFEFF") ? value.substring(1) : value;
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("id is invalid");
        }
    }

    private Integer parseStatus(String value, int fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String text = value.trim();
        return switch (text) {
            case "未售" -> 1;
            case "已售" -> 2;
            case "空置" -> 3;
            case "已入住" -> 4;
            case "已出租" -> 5;
            default -> {
                try {
                    int parsed = Integer.parseInt(text);
                    if (parsed < 1 || parsed > 5) {
                        throw new IllegalArgumentException("status must be 1-5");
                    }
                    yield parsed;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("status is invalid");
                }
            }
        };
    }

    private BigDecimal parseDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("area is invalid");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim().replace('T', ' ');
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return java.time.LocalDate.parse(text).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("rentEndTime is invalid");
        }
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private record ImportResult(boolean created, boolean updated) {
    }
}
