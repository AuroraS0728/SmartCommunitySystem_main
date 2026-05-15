package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.dto.request.AdditionalServiceOrderCreateReq;
import com.smartcommunity.dto.response.AdditionalServiceOrderVO;
import com.smartcommunity.dto.response.ServiceRecommendation;
import com.smartcommunity.entity.AdditionalServiceOrder;
import com.smartcommunity.entity.PropertyTask;
import com.smartcommunity.entity.RecommendRule;
import com.smartcommunity.entity.SysMessage;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.AdditionalServiceOrderMapper;
import com.smartcommunity.mapper.PropertyTaskMapper;
import com.smartcommunity.mapper.RecommendRuleMapper;
import com.smartcommunity.mapper.SysMessageMapper;
import com.smartcommunity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdditionalServiceOrderService {

    private static final Set<String> ALLOWED_TIME_SLOTS = Set.of(
            "09:00-11:00",
            "11:00-13:00",
            "13:00-15:00",
            "15:00-17:00",
            "17:00-19:00"
    );

    private final RecommendRuleMapper recommendRuleMapper;
    private final RecommendRuleService recommendRuleService;
    private final AdditionalServiceOrderMapper additionalServiceOrderMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;
    private final PropertyTaskMapper propertyTaskMapper;
    private final SysMessageMapper sysMessageMapper;
    private final ObjectMapper objectMapper;

    public List<ServiceRecommendation> catalog(Long userId) {
        Map<String, ServiceRecommendation> recommendedMap = recommendRuleService.recommend(userId, 100, false)
                .stream()
                .peek(item -> item.setRecommended(Boolean.TRUE))
                .collect(Collectors.toMap(ServiceRecommendation::getServiceId, item -> item, (left, right) -> left));

        List<ServiceRecommendation> items = new ArrayList<>();
        for (RecommendRule rule : activeRules()) {
            String serviceKey = safeText(rule.getServiceId());
            ServiceRecommendation matched = recommendedMap.get(serviceKey);
            if (matched != null) {
                if (!StringUtils.hasText(matched.getActionPath())) {
                    matched.setActionPath("/pages/service/service");
                }
                matched.setRecommended(Boolean.TRUE);
                items.add(matched);
                continue;
            }
            ServiceRecommendation item = ServiceRecommendation.of(
                    serviceKey,
                    safeText(rule.getServiceName(), safeText(rule.getRuleName(), "附加服务")),
                    rule.getPrice() == null ? 0 : rule.getPrice(),
                    fallbackReason(rule),
                    safeText(rule.getImageUrl()),
                    "/pages/service/service"
            );
            item.setRecommended(Boolean.FALSE);
            items.add(item);
        }

        items.sort(Comparator
                .comparing((ServiceRecommendation item) -> !Boolean.TRUE.equals(item.getRecommended()))
                .thenComparing(ServiceRecommendation::getPrice, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ServiceRecommendation::getServiceName, Comparator.nullsLast(String::compareTo)));
        return items;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdditionalServiceOrderVO createOrder(Long userId, AdditionalServiceOrderCreateReq req) {
        User user = requireOwner(userId);
        RecommendRule rule = requireRule(req.getServiceId());
        LocalDate appointmentDate = parseAppointmentDate(req.getAppointmentDate());
        String appointmentTimeSlot = normalizeTimeSlot(req.getAppointmentTimeSlot());
        String serviceName = safeText(rule.getServiceName(), safeText(rule.getRuleName(), "附加服务"));
        int price = rule.getPrice() == null ? 0 : Math.max(rule.getPrice(), 0);
        if (price <= 0) {
            throw new IllegalArgumentException("当前附加服务价格无效");
        }

        AdditionalServiceOrder order = new AdditionalServiceOrder();
        order.setUserId(userId);
        order.setServiceId(safeText(rule.getServiceId(), safeText(req.getServiceId())));
        order.setServiceName(serviceName);
        order.setPrice(price);
        order.setPointsCost(price);
        order.setAppointmentDate(appointmentDate);
        order.setAppointmentTimeSlot(appointmentTimeSlot);
        order.setContactName(displayName(user));
        order.setContactPhone(safeText(user.getPhone(), "-"));
        order.setRemark(trimToLength(req.getRemark(), 200));
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(order.getCreateTime());
        order.setIsDeleted(0);
        additionalServiceOrderMapper.insert(order);

        pointsService.consumeCustomBusiness(userId, 4, order.getId(), price);

        order.setPaidTime(LocalDateTime.now());
        order.setUpdateTime(order.getPaidTime());
        additionalServiceOrderMapper.updateById(order);

        createPropertyTask(user, order);
        notifyOwner(userId, order);
        notifyAdmins(user, order);

        return toVO(order);
    }

    public Integer getCurrentPoints(Long userId) {
        return pointsService.getBalance(userId);
    }

    public List<AdditionalServiceOrderVO> listMyOrders(Long userId, Integer limit) {
        int size = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);
        return additionalServiceOrderMapper.selectList(new LambdaQueryWrapper<AdditionalServiceOrder>()
                        .eq(AdditionalServiceOrder::getUserId, userId)
                        .eq(AdditionalServiceOrder::getIsDeleted, 0)
                        .orderByDesc(AdditionalServiceOrder::getCreateTime)
                        .orderByDesc(AdditionalServiceOrder::getId)
                        .last("limit " + size))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private List<RecommendRule> activeRules() {
        return recommendRuleMapper.selectList(new LambdaQueryWrapper<RecommendRule>()
                .eq(RecommendRule::getIsDeleted, 0)
                .eq(RecommendRule::getEnabled, 1)
                .orderByAsc(RecommendRule::getPriority)
                .orderByAsc(RecommendRule::getId));
    }

    private RecommendRule requireRule(String serviceId) {
        String normalized = safeText(serviceId);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("serviceId不能为空");
        }
        RecommendRule rule = recommendRuleMapper.selectOne(new LambdaQueryWrapper<RecommendRule>()
                .eq(RecommendRule::getServiceId, normalized)
                .eq(RecommendRule::getEnabled, 1)
                .eq(RecommendRule::getIsDeleted, 0)
                .last("limit 1"));
        if (rule == null) {
            throw new IllegalArgumentException("附加服务不存在或已下线");
        }
        return rule;
    }

    private User requireOwner(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private LocalDate parseAppointmentDate(String value) {
        try {
            LocalDate date = LocalDate.parse(safeText(value));
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("预约日期不能早于今天");
            }
            return date;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("appointmentDate格式应为yyyy-MM-dd");
        }
    }

    private String normalizeTimeSlot(String value) {
        String slot = safeText(value);
        if (!ALLOWED_TIME_SLOTS.contains(slot)) {
            throw new IllegalArgumentException("appointmentTimeSlot无效");
        }
        return slot;
    }

    private void createPropertyTask(User user, AdditionalServiceOrder order) {
        PropertyTask task = new PropertyTask();
        task.setTitle("附加服务预约");
        task.setDescription(trimToLength(
                displayName(user) + " 预约了 " + order.getServiceName() + "，时间 " +
                        order.getAppointmentDate() + " " + order.getAppointmentTimeSlot(),
                200
        ));
        task.setAssignedTo(null);
        task.setStatus(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(task.getCreateTime());
        task.setIsDeleted(0);
        propertyTaskMapper.insert(task);
    }

    private void notifyOwner(Long userId, AdditionalServiceOrder order) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle("附加服务预约成功");
        message.setContent("您已成功预约 " + order.getServiceName() + "，时间：" +
                order.getAppointmentDate() + " " + order.getAppointmentTimeSlot() +
                "，已扣除 " + order.getPointsCost() + " 积分。");
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(message.getCreateTime());
        message.setIsDeleted(0);
        sysMessageMapper.insert(message);
    }

    private void notifyAdmins(User user, AdditionalServiceOrder order) {
        List<User> admins = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 2)
                .eq(User::getIsDeleted, 0)
                .eq(User::getStatus, 1));
        if (admins.isEmpty()) {
            return;
        }
        for (User admin : admins) {
            SysMessage message = new SysMessage();
            message.setUserId(admin.getId());
            message.setTitle("新的附加服务预约");
            message.setContent(trimToLength(
                    "业主 " + displayName(user) + " 预约了 " + order.getServiceName() +
                            "，时间：" + order.getAppointmentDate() + " " + order.getAppointmentTimeSlot(),
                    500
            ));
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(message.getCreateTime());
            message.setIsDeleted(0);
            sysMessageMapper.insert(message);
        }
    }

    private AdditionalServiceOrderVO toVO(AdditionalServiceOrder order) {
        AdditionalServiceOrderVO vo = new AdditionalServiceOrderVO();
        vo.setId(order.getId());
        vo.setServiceId(order.getServiceId());
        vo.setServiceName(order.getServiceName());
        vo.setPrice(order.getPrice());
        vo.setPointsCost(order.getPointsCost());
        vo.setAppointmentDate(order.getAppointmentDate());
        vo.setAppointmentTimeSlot(order.getAppointmentTimeSlot());
        vo.setRemark(order.getRemark());
        vo.setStatus(order.getStatus());
        vo.setStatusText(statusText(order.getStatus()));
        vo.setPaidTime(order.getPaidTime());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 1 -> "已预约";
            case 2 -> "已受理";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String fallbackReason(RecommendRule rule) {
        try {
            JsonNode jsonNode = objectMapper.readTree(safeText(rule.getConditionJson(), "{}"));
            String reason = jsonNode.path("reason").asText("");
            if (StringUtils.hasText(reason)) {
                return reason.trim();
            }
        } catch (Exception ignored) {
        }
        return safeText(rule.getRuleName(), "推荐附加服务");
    }

    private String displayName(User user) {
        return safeText(user.getNickname(), safeText(user.getAccount(), "业主"));
    }

    private String trimToLength(String value, int maxLength) {
        String text = safeText(value);
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String safeText(String value) {
        return safeText(value, "");
    }

    private String safeText(String value, String fallback) {
        String text = value == null ? "" : value.trim();
        return StringUtils.hasText(text) ? text : fallback;
    }
}
