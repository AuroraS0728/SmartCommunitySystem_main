package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.Result;
import com.smartcommunity.entity.AccessToken;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.Notice;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.RepairOrder;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.VisitorInvite;
import com.smartcommunity.mapper.AccessTokenMapper;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.NoticeMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.RepairOrderMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.VisitorInviteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private static final DateTimeFormatter DAY_LABEL_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter NOTICE_TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final DateTimeFormatter VISITOR_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PropertyMapper propertyMapper;
    private final FeeBillMapper feeBillMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final UserMapper userMapper;
    private final AccessTokenMapper accessTokenMapper;
    private final NoticeMapper noticeMapper;
    private final VisitorInviteMapper visitorInviteMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Property> properties = propertyMapper.selectList(new LambdaQueryWrapper<>());
        List<FeeBill> bills = feeBillMapper.selectList(new LambdaQueryWrapper<>());
        List<RepairOrder> orders = repairOrderMapper.selectList(new LambdaQueryWrapper<>());
        List<ParkingOrder> parkingOrders = parkingOrderMapper.selectList(new LambdaQueryWrapper<>());
        List<AccessToken> accessTokens = accessTokenMapper.selectList(new LambdaQueryWrapper<>());
        List<Notice> latestNotices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getPublishTime)
                .last("LIMIT 3"));
        List<VisitorInvite> latestVisitors = visitorInviteMapper.selectList(new LambdaQueryWrapper<VisitorInvite>()
                .orderByDesc(VisitorInvite::getCreateTime)
                .last("LIMIT 5"));

        long totalProperty = properties.size();
        long occupied = properties.stream()
                .filter(v -> Integer.valueOf(4).equals(v.getStatus()) || Integer.valueOf(5).equals(v.getStatus()))
                .count();
        double occupancyRate = totalProperty == 0 ? 0 : (double) occupied / totalProperty;

        BigDecimal totalAmount = bills.stream()
                .map(FeeBill::getAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = bills.stream()
                .map(b -> safeMin(safe(b.getPaidAmount()), safe(b.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentMonthPaid = bills.stream()
                .filter(b -> b.getPaymentTime() != null)
                .filter(b -> isSameMonth(b.getPaymentTime(), now))
                .map(b -> safeMin(safe(b.getPaidAmount()), safe(b.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentMonthParkingIncome = parkingOrders.stream()
                .filter(p -> p.getPaymentTime() != null)
                .filter(p -> Integer.valueOf(1).equals(p.getStatus()))
                .filter(p -> isSameMonth(p.getPaymentTime(), now))
                .map(ParkingOrder::getAmount)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long repairTotal = orders.size();
        long waiting = orders.stream().filter(r -> Integer.valueOf(1).equals(r.getStatus())).count();
        long urgent = orders.stream()
                .filter(r -> Integer.valueOf(1).equals(r.getStatus()))
                .filter(r -> r.getCreateTime() != null)
                .filter(r -> r.getCreateTime().isBefore(now.minusHours(48)))
                .count();

        Map<String, Long> repairCategory = new LinkedHashMap<>();
        for (RepairOrder order : orders) {
            repairCategory.merge(order.getCategory(), 1L, Long::sum);
        }

        Map<String, Object> trend = calcRepairTrend(orders, today);
        Map<String, Object> parkingTrend = calcParkingIncomeTrend(parkingOrders, today);
        List<Map<String, Object>> feeSegments = calcFeeSegments(bills, now, totalAmount);
        List<Map<String, Object>> noticeCards = latestNotices.stream().map(n -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", n.getTitle());
            item.put("time", n.getPublishTime() == null ? "--" : n.getPublishTime().format(NOTICE_TIME_FMT));
            item.put("summary", abbreviate(n.getContent(), 64));
            return item;
        }).toList();
        List<Map<String, Object>> visitorCards = latestVisitors.stream().map(v -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", v.getVisitorName());
            item.put("reason", visitorReason(v, now));
            item.put("target", "host#" + v.getHostUserId());
            item.put("time", v.getCreateTime() == null ? "--" : v.getCreateTime().format(VISITOR_TIME_FMT));
            item.put("tagType", visitorTag(v, now));
            return item;
        }).toList();

        long activeVisitors = visitorInviteMapper.selectCount(new LambdaQueryWrapper<VisitorInvite>()
                .gt(VisitorInvite::getExpireTime, now));

        long ownerTotal = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 1));
        double ownerGrowthRate = calcOwnerGrowth(now);

        long onlineDeviceCount = accessTokens.stream()
                .filter(t -> Integer.valueOf(1).equals(t.getStatus()))
                .filter(t -> t.getExpireTime() != null && t.getExpireTime().isAfter(now))
                .count();
        double deviceOnlineRate = accessTokens.isEmpty()
                ? 0
                : (double) onlineDeviceCount / accessTokens.size() * 100;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("propertyTotal", totalProperty);
        data.put("occupancyRate", round4(occupancyRate));
        data.put("feeTotal", totalAmount);
        data.put("feePaid", paidAmount);
        data.put("feePaidRate", totalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : paidAmount.divide(totalAmount, 4, RoundingMode.HALF_UP));
        data.put("repairTotal", repairTotal);
        data.put("repairWaiting", waiting);
        data.put("repairCategory", repairCategory);

        data.put("ownerTotal", ownerTotal);
        data.put("ownerGrowthRate", round1(ownerGrowthRate));
        data.put("feeIncome", currentMonthPaid);
        data.put("parkingIncome", currentMonthParkingIncome);
        data.put("feeCompletionRate", totalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : paidAmount.multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, RoundingMode.HALF_UP));
        data.put("repairUrgent", urgent);
        data.put("deviceOnlineRate", round1(deviceOnlineRate));
        data.put("activeVisitors", activeVisitors);
        data.put("trendLabels", trend.get("trendLabels"));
        data.put("newRepairs", trend.get("newRepairs"));
        data.put("finishedRepairs", trend.get("finishedRepairs"));
        data.put("parkingTrendLabels", parkingTrend.get("parkingTrendLabels"));
        data.put("parkingIncomeTrend", parkingTrend.get("parkingIncomeTrend"));
        data.put("feeSegments", feeSegments);
        data.put("notices", noticeCards);
        data.put("visitors", visitorCards);
        return Result.success(data);
    }

    private Map<String, Object> calcRepairTrend(List<RepairOrder> orders, LocalDate today) {
        LocalDate start = today.minusDays(6);
        List<String> labels = new ArrayList<>();
        List<Integer> newRepairs = new ArrayList<>();
        List<Integer> finishedRepairs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            labels.add(day.format(DAY_LABEL_FMT));
            int created = 0;
            int done = 0;
            for (RepairOrder order : orders) {
                if (order.getCreateTime() != null && order.getCreateTime().toLocalDate().equals(day)) {
                    created++;
                }
                if (order.getCompletionTime() != null && order.getCompletionTime().toLocalDate().equals(day)) {
                    done++;
                }
            }
            newRepairs.add(created);
            finishedRepairs.add(done);
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("trendLabels", labels);
        trend.put("newRepairs", newRepairs);
        trend.put("finishedRepairs", finishedRepairs);
        return trend;
    }

    private Map<String, Object> calcParkingIncomeTrend(List<ParkingOrder> orders, LocalDate today) {
        LocalDate start = today.minusDays(6);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            labels.add(day.format(DAY_LABEL_FMT));
            BigDecimal income = orders.stream()
                    .filter(o -> Integer.valueOf(1).equals(o.getStatus()))
                    .filter(o -> o.getPaymentTime() != null && o.getPaymentTime().toLocalDate().equals(day))
                    .map(ParkingOrder::getAmount)
                    .map(this::safe)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            incomeTrend.add(income);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("parkingTrendLabels", labels);
        result.put("parkingIncomeTrend", incomeTrend);
        return result;
    }

    private List<Map<String, Object>> calcFeeSegments(List<FeeBill> bills, LocalDateTime now, BigDecimal totalAmount) {
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        for (FeeBill bill : bills) {
            BigDecimal amount = safe(bill.getAmount());
            BigDecimal paidAmount = safeMin(safe(bill.getPaidAmount()), amount);
            BigDecimal remain = amount.subtract(paidAmount);
            paid = paid.add(paidAmount);
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (bill.getDueDate() != null && bill.getDueDate().isBefore(now)) {
                overdue = overdue.add(remain);
            } else {
                pending = pending.add(remain);
            }
        }
        List<Map<String, Object>> segments = new ArrayList<>();
        segments.add(feeSegment("已缴费", "#3b82f6", toPercent(paid, totalAmount)));
        segments.add(feeSegment("待缴费", "#10b981", toPercent(pending, totalAmount)));
        segments.add(feeSegment("逾期未缴", "#f59e0b", toPercent(overdue, totalAmount)));
        return segments;
    }

    private Map<String, Object> feeSegment(String name, String color, double percent) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("color", color);
        map.put("percent", round1(percent));
        return map;
    }

    private double calcOwnerGrowth(LocalDateTime now) {
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime firstDayOfPrevMonth = firstDayOfMonth.minusMonths(1);
        long current = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 1)
                .ge(User::getCreateTime, firstDayOfMonth)
                .lt(User::getCreateTime, firstDayOfMonth.plusMonths(1)));
        long prev = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 1)
                .ge(User::getCreateTime, firstDayOfPrevMonth)
                .lt(User::getCreateTime, firstDayOfMonth));
        if (prev == 0) {
            return current == 0 ? 0 : 100;
        }
        return ((double) current - prev) / prev * 100;
    }

    private boolean isSameMonth(LocalDateTime time, LocalDateTime now) {
        return time.getYear() == now.getYear() && time.getMonthValue() == now.getMonthValue();
    }

    private String abbreviate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String visitorReason(VisitorInvite invite, LocalDateTime now) {
        if (invite.getExpireTime() != null && invite.getExpireTime().isBefore(now)) {
            return "已过期";
        }
        if (invite.getUsedCount() != null && invite.getUsedCount() > 0) {
            return "已通行";
        }
        return "待通行";
    }

    private String visitorTag(VisitorInvite invite, LocalDateTime now) {
        if (invite.getExpireTime() != null && invite.getExpireTime().isBefore(now)) {
            return "orange";
        }
        if (invite.getUsedCount() != null && invite.getUsedCount() > 0) {
            return "green";
        }
        return "blue";
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal safeMin(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) > 0 ? right : left;
    }

    private double toPercent(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(total, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
