package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.dto.request.ActivityRegistrationReq;
import com.smartcommunity.dto.request.ActivitySaveReq;
import com.smartcommunity.dto.response.AssetUploadResp;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityRegistration;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.ActivityMapper;
import com.smartcommunity.mapper.ActivityRegistrationMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final long MAX_IMAGE_BYTES = 8L * 1024L * 1024L;
    private static final DateTimeFormatter ASSET_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int STATUS_SIGNING = 0;
    private static final int STATUS_FINISHED = 1;
    private static final int REG_STATUS_PENDING = 0;
    private static final int REG_STATUS_CONFIRMED = 1;
    private static final int REG_STATUS_CANCELED = 2;

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper activityRegistrationMapper;
    private final UserMapper userMapper;

    @Value("${activity.asset-dir:./activity-assets}")
    private String activityAssetDir;

    public List<Map<String, Object>> adminList(String keyword, String type, Integer status) {
        return activityMapper.selectList(buildActivityWrapper(keyword, type, status)).stream()
                .sorted(Comparator.comparing(Activity::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(activity -> toActivityRow(activity, ActivityRecommendation.NONE))
                .toList();
    }

    public List<Map<String, Object>> ownerList(String keyword, String type, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = buildActivityWrapper(keyword, type, null)
                .orderByAsc(Activity::getStatus)
                .orderByAsc(Activity::getStartTime);
        boolean personalize = !StringUtils.hasText(type);
        User user = loadOwnerProfile(userId);

        return activityMapper.selectList(wrapper).stream()
                .map(activity -> new OwnerActivityView(activity, personalize ? buildRecommendation(activity, user) : ActivityRecommendation.NONE))
                .sorted((left, right) -> compareOwnerActivity(left, right, personalize))
                .map(view -> toActivityRow(view.activity(), view.recommendation()))
                .toList();
    }

    public Map<String, Object> detail(Long activityId, Long userId) {
        Activity activity = requireActivity(activityId);
        Map<String, Object> payload = toActivityRow(activity, ActivityRecommendation.NONE);
        ActivityRegistration myRegistration = findUserRegistration(activityId, userId);
        payload.put("myRegistration", myRegistration);
        payload.put("canRegister", canRegister(activity));
        payload.put("remainingSlots", remainingSlots(activity));
        return payload;
    }

    public List<Map<String, Object>> myRegistrations(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<ActivityRegistration> registrations = activityRegistrationMapper.selectList(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getIsDeleted, 0)
                .orderByDesc(ActivityRegistration::getCreateTime));
        if (registrations.isEmpty()) {
            return List.of();
        }

        List<Long> activityIds = registrations.stream().map(ActivityRegistration::getActivityId).distinct().toList();
        Map<Long, Activity> activityMap = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                        .in(Activity::getId, activityIds)
                        .eq(Activity::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(Activity::getId, item -> item, (a, b) -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (ActivityRegistration registration : registrations) {
            Activity activity = activityMap.get(registration.getActivityId());
            if (activity == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("registration", registration);
            row.put("activity", toActivityRow(activity, ActivityRecommendation.NONE));
            rows.add(row);
        }
        return rows;
    }

    @Transactional
    public Activity createActivity(ActivitySaveReq req) {
        Activity activity = new Activity();
        fillActivity(activity, req);
        LocalDateTime now = LocalDateTime.now();
        activity.setCurrentParticipants(0);
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        activity.setIsDeleted(0);
        activityMapper.insert(activity);
        return activity;
    }

    @Transactional
    public Activity updateActivity(Long activityId, ActivitySaveReq req) {
        Activity activity = requireActivity(activityId);
        fillActivity(activity, req);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        return activity;
    }

    @Transactional
    public ActivityRegistration register(Long activityId, Long userId, ActivityRegistrationReq req) {
        Activity activity = requireActivity(activityId);
        if (userId == null) {
            throw new IllegalArgumentException("userId is empty");
        }
        if (!canRegister(activity)) {
            throw new IllegalArgumentException("当前活动不可报名");
        }
        if (findUserRegistration(activityId, userId) != null) {
            throw new IllegalArgumentException("您已报名该活动");
        }

        validateRegistration(activity, req);

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivityId(activityId);
        registration.setUserId(userId);
        registration.setNickname(clean(req == null ? null : req.getNickname(), "未命名业主"));
        registration.setPhone(clean(req == null ? null : req.getPhone(), ""));
        registration.setAge(req == null || req.getAge() == null ? 0 : req.getAge());
        registration.setHasChild(req == null || req.getHasChild() == null ? 0 : req.getHasChild());
        registration.setHasPet(req == null || req.getHasPet() == null ? 0 : req.getHasPet());
        registration.setRemark(clean(req == null ? null : req.getRemark(), ""));
        registration.setStatus(REG_STATUS_PENDING);
        registration.setCreateTime(LocalDateTime.now());
        registration.setUpdateTime(registration.getCreateTime());
        registration.setIsDeleted(0);
        activityRegistrationMapper.insert(registration);

        activity.setCurrentParticipants((activity.getCurrentParticipants() == null ? 0 : activity.getCurrentParticipants()) + 1);
        if (remainingSlots(activity) <= 0) {
            activity.setStatus(STATUS_FINISHED);
        }
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        return registration;
    }

    public List<Map<String, Object>> registrations(Long activityId) {
        requireActivity(activityId);
        return activityRegistrationMapper.selectList(new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, activityId)
                        .eq(ActivityRegistration::getIsDeleted, 0)
                        .orderByDesc(ActivityRegistration::getCreateTime))
                .stream()
                .map(registration -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", registration.getId());
                    row.put("activityId", registration.getActivityId());
                    row.put("userId", registration.getUserId());
                    row.put("nickname", registration.getNickname());
                    row.put("phone", registration.getPhone());
                    row.put("age", registration.getAge());
                    row.put("hasChild", registration.getHasChild());
                    row.put("hasPet", registration.getHasPet());
                    row.put("remark", registration.getRemark());
                    row.put("status", registration.getStatus());
                    row.put("createTime", registration.getCreateTime());
                    return row;
                })
                .toList();
    }

    @Transactional
    public ActivityRegistration reviewRegistration(Long registrationId, Integer status) {
        if (status == null || (status != REG_STATUS_CONFIRMED && status != REG_STATUS_CANCELED)) {
            throw new IllegalArgumentException("status is invalid");
        }
        ActivityRegistration registration = activityRegistrationMapper.selectById(registrationId);
        if (registration == null || Integer.valueOf(1).equals(registration.getIsDeleted())) {
            throw new IllegalArgumentException("registration not found");
        }

        Integer oldStatus = registration.getStatus();
        registration.setStatus(status);
        registration.setUpdateTime(LocalDateTime.now());
        activityRegistrationMapper.updateById(registration);

        if (!Integer.valueOf(REG_STATUS_CANCELED).equals(oldStatus) && Integer.valueOf(REG_STATUS_CANCELED).equals(status)) {
            Activity activity = requireActivity(registration.getActivityId());
            int current = activity.getCurrentParticipants() == null ? 0 : activity.getCurrentParticipants();
            activity.setCurrentParticipants(Math.max(current - 1, 0));
            if (Integer.valueOf(STATUS_FINISHED).equals(activity.getStatus())
                    && activity.getEndTime() != null
                    && LocalDateTime.now().isBefore(activity.getEndTime())) {
                activity.setStatus(STATUS_SIGNING);
            }
            activity.setUpdateTime(LocalDateTime.now());
            activityMapper.updateById(activity);
        }
        return registration;
    }

    public AssetUploadResp saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file is empty");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("image file is too large");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String ext = switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new IllegalArgumentException("only jpg, png, webp and gif images are supported");
        };

        String dateDir = LocalDate.now().format(ASSET_DATE);
        String filename = UUID.randomUUID() + ext;
        Path root = Paths.get(activityAssetDir).toAbsolutePath().normalize();
        Path dir = root.resolve(dateDir).normalize();
        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("invalid activity asset path");
        }

        try {
            Files.createDirectories(dir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("save activity image failed", ex);
        }

        return new AssetUploadResp("/api/activity/assets/" + dateDir + "/" + filename, filename);
    }

    private LambdaQueryWrapper<Activity> buildActivityWrapper(String keyword, String type, Integer status) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(w -> w.like(Activity::getTitle, text)
                    .or().like(Activity::getDescription, text)
                    .or().like(Activity::getLocation, text));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Activity::getType, type.trim());
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        return wrapper;
    }

    private void fillActivity(Activity activity, ActivitySaveReq req) {
        if (req == null) {
            throw new IllegalArgumentException("activity payload is empty");
        }
        if (!StringUtils.hasText(req.getTitle())) {
            throw new IllegalArgumentException("title is empty");
        }
        if (req.getStartTime() == null || req.getEndTime() == null || !req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("activity time range is invalid");
        }
        if (req.getMaxParticipants() == null || req.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("maxParticipants is invalid");
        }

        activity.setTitle(req.getTitle().trim());
        activity.setDescription(clean(req.getDescription(), ""));
        activity.setType(clean(req.getType(), "公益"));
        activity.setImageUrl(clean(req.getImageUrl(), ""));
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setLocation(clean(req.getLocation(), ""));
        activity.setMaxParticipants(req.getMaxParticipants());
        activity.setCurrentParticipants(activity.getCurrentParticipants() == null ? 0 : activity.getCurrentParticipants());
        activity.setAgeLimit(clean(req.getAgeLimit(), ""));
        activity.setWithChildRequired(req.getWithChildRequired() == null ? 0 : req.getWithChildRequired());
        activity.setWithPetRequired(req.getWithPetRequired() == null ? 0 : req.getWithPetRequired());
        activity.setStatus(req.getStatus() == null ? STATUS_SIGNING : req.getStatus());
    }

    private void validateRegistration(Activity activity, ActivityRegistrationReq req) {
        if (req == null) {
            throw new IllegalArgumentException("registration payload is empty");
        }
        if (!StringUtils.hasText(req.getNickname())) {
            throw new IllegalArgumentException("nickname is empty");
        }
        if (!StringUtils.hasText(req.getPhone())) {
            throw new IllegalArgumentException("phone is empty");
        }
        if (req.getAge() == null || req.getAge() <= 0) {
            throw new IllegalArgumentException("age is invalid");
        }
        if (!matchesAgeLimit(req.getAge(), activity.getAgeLimit())) {
            throw new IllegalArgumentException("年龄不符合当前活动限制");
        }
        if (Integer.valueOf(1).equals(activity.getWithChildRequired()) && !truthy(req.getHasChild())) {
            throw new IllegalArgumentException("当前活动要求携带儿童");
        }
        if (Integer.valueOf(1).equals(activity.getWithPetRequired()) && !truthy(req.getHasPet())) {
            throw new IllegalArgumentException("当前活动要求携带宠物");
        }
    }

    boolean matchesAgeLimit(Integer age, String ageLimit) {
        if (age == null || age <= 0 || !StringUtils.hasText(ageLimit)) {
            return true;
        }
        String text = ageLimit.trim()
                .replace(" ", "")
                .replace("≥", ">=")
                .replace("≤", "<=");
        if (text.contains("-")) {
            String[] parts = text.split("-");
            if (parts.length == 2) {
                try {
                    int min = Integer.parseInt(parts[0]);
                    int max = Integer.parseInt(parts[1]);
                    return age >= min && age <= max;
                } catch (NumberFormatException ignored) {
                    return true;
                }
            }
        }
        for (String operator : List.of(">=", "<=", ">", "<")) {
            if (text.startsWith(operator)) {
                try {
                    int target = Integer.parseInt(text.substring(operator.length()));
                    return switch (operator) {
                        case ">=" -> age >= target;
                        case "<=" -> age <= target;
                        case ">" -> age > target;
                        case "<" -> age < target;
                        default -> true;
                    };
                } catch (NumberFormatException ignored) {
                    return true;
                }
            }
        }
        return true;
    }

    private Activity requireActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || Integer.valueOf(1).equals(activity.getIsDeleted())) {
            throw new IllegalArgumentException("activity not found");
        }
        return activity;
    }

    private ActivityRegistration findUserRegistration(Long activityId, Long userId) {
        if (userId == null) {
            return null;
        }
        return activityRegistrationMapper.selectOne(new LambdaQueryWrapper<ActivityRegistration>()
                .eq(ActivityRegistration::getActivityId, activityId)
                .eq(ActivityRegistration::getUserId, userId)
                .eq(ActivityRegistration::getIsDeleted, 0)
                .ne(ActivityRegistration::getStatus, REG_STATUS_CANCELED)
                .last("LIMIT 1"));
    }

    private Map<String, Object> toActivityRow(Activity activity, ActivityRecommendation recommendation) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", activity.getId());
        row.put("title", activity.getTitle());
        row.put("description", activity.getDescription());
        row.put("type", activity.getType());
        row.put("imageUrl", activity.getImageUrl());
        row.put("startTime", activity.getStartTime());
        row.put("endTime", activity.getEndTime());
        row.put("location", activity.getLocation());
        row.put("maxParticipants", activity.getMaxParticipants());
        row.put("currentParticipants", activity.getCurrentParticipants());
        row.put("remainingSlots", remainingSlots(activity));
        row.put("ageLimit", activity.getAgeLimit());
        row.put("withChildRequired", activity.getWithChildRequired());
        row.put("withPetRequired", activity.getWithPetRequired());
        row.put("status", activity.getStatus());
        row.put("statusText", Integer.valueOf(STATUS_FINISHED).equals(activity.getStatus()) ? "已结束" : "报名中");
        row.put("canRegister", canRegister(activity));
        row.put("createTime", activity.getCreateTime());
        row.put("recommended", recommendation != null && recommendation.score() > 0);
        row.put("recommendScore", recommendation == null ? 0 : recommendation.score());
        row.put("recommendReason", recommendation == null ? "" : recommendation.reason());
        return row;
    }

    private User loadOwnerProfile(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            return null;
        }
        return user;
    }

    private int compareOwnerActivity(OwnerActivityView left, OwnerActivityView right, boolean personalize) {
        if (personalize) {
            int recommendedCompare = Boolean.compare(right.recommendation().score() > 0, left.recommendation().score() > 0);
            if (recommendedCompare != 0) {
                return recommendedCompare;
            }
            int scoreCompare = Integer.compare(right.recommendation().score(), left.recommendation().score());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
        }
        int statusCompare = Comparator.nullsLast(Integer::compareTo).compare(left.activity().getStatus(), right.activity().getStatus());
        if (statusCompare != 0) {
            return statusCompare;
        }
        return Comparator.nullsLast(LocalDateTime::compareTo).compare(left.activity().getStartTime(), right.activity().getStartTime());
    }

    private ActivityRecommendation buildRecommendation(Activity activity, User user) {
        if (activity == null || user == null) {
            return ActivityRecommendation.NONE;
        }
        String text = normalizeActivityText(activity);
        int score = 0;
        Set<String> reasons = new LinkedHashSet<>();

        // 活动推荐是可解释的打分规则，分数越高越靠前。
        // 如果老师要求调推荐效果，主要改下面这些加分值和关键词。
        if (truthy(user.getHasChild())) {
            if (Integer.valueOf(1).equals(activity.getWithChildRequired())) {
                score += 5;
                reasons.add("适合亲子家庭");
            }
            if (containsAny(text, List.of("亲子", "儿童", "手工", "家庭互动", "成长", "绘本"))) {
                score += 3;
                reasons.add("与家有儿童画像匹配");
            }
        }

        if (truthy(user.getHasPet())) {
            if (Integer.valueOf(1).equals(activity.getWithPetRequired())) {
                score += 5;
                reasons.add("适合携宠参与");
            }
            if (containsAny(text, List.of("宠物", "萌宠", "遛狗", "养宠", "爱宠"))) {
                score += 3;
                reasons.add("与家有宠物画像匹配");
            }
        }

        if (truthy(user.getHasElderly()) && containsAny(text, List.of("夕阳红", "长者", "老人", "敬老", "义诊", "健康讲座", "养生"))) {
            score += 4;
            reasons.add("与家有老人画像匹配");
        }

        if ((user.getHouseArea() == null ? 0 : user.getHouseArea()) > 120
                && containsAny(text, List.of("家居", "收纳", "整理", "保洁", "空间"))) {
            score += 2;
            reasons.add("与大户型家庭画像匹配");
        }

        if (score <= 0) {
            return ActivityRecommendation.NONE;
        }
        String reason = reasons.isEmpty() ? "根据您的画像优先推荐" : reasons.iterator().next();
        return new ActivityRecommendation(score, reason);
    }

    private String normalizeActivityText(Activity activity) {
        StringBuilder builder = new StringBuilder();
        // 把活动类型、标题、简介、地点拼成一段文本，统一做关键词匹配。
        appendActivityText(builder, activity.getType());
        appendActivityText(builder, activity.getTitle());
        appendActivityText(builder, activity.getDescription());
        appendActivityText(builder, activity.getLocation());
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private void appendActivityText(StringBuilder builder, String text) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(text.trim());
    }

    private boolean containsAny(String text, List<String> keywords) {
        if (!StringUtils.hasText(text) || keywords == null || keywords.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean canRegister(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (!Integer.valueOf(STATUS_SIGNING).equals(activity.getStatus())) {
            return false;
        }
        if (activity.getEndTime() != null && LocalDateTime.now().isAfter(activity.getEndTime())) {
            return false;
        }
        return remainingSlots(activity) > 0;
    }

    private int remainingSlots(Activity activity) {
        int max = activity.getMaxParticipants() == null ? 0 : activity.getMaxParticipants();
        int current = activity.getCurrentParticipants() == null ? 0 : activity.getCurrentParticipants();
        return Math.max(max - current, 0);
    }

    private boolean truthy(Integer value) {
        return value != null && value == 1;
    }

    private String clean(String value, String fallback) {
        String text = value == null ? "" : value.trim();
        return StringUtils.hasText(text) ? text : fallback;
    }

    private record ActivityRecommendation(int score, String reason) {
        private static final ActivityRecommendation NONE = new ActivityRecommendation(0, "");
    }

    private record OwnerActivityView(Activity activity, ActivityRecommendation recommendation) {
    }
}
