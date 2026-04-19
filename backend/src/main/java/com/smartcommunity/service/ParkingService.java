package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.OwnerParkingQuota;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.UserVehicle;
import com.smartcommunity.mapper.OwnerParkingQuotaMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.UserVehicleMapper;
import com.smartcommunity.utils.WechatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingService {

    private static final BigDecimal MONTH_CARD_AMOUNT = new BigDecimal("500.00");
    private static final BigDecimal TEMP_HOURLY_AMOUNT = new BigDecimal("2.00");
    private static final BigDecimal DAILY_CAP = new BigDecimal("30.00");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ParkingOrderMapper parkingOrderMapper;
    private final UserVehicleMapper userVehicleMapper;
    private final OwnerParkingQuotaMapper ownerParkingQuotaMapper;
    private final WechatUtil wechatUtil;

    @Transactional(rollbackFor = Exception.class)
    public UserVehicle bindOwnerVehicle(Long userId, String vehicleNo) {
        String plate = normalizePlate(vehicleNo);
        UserVehicle existed = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleNo, plate)
                .eq(UserVehicle::getIsVisitor, 0)
                .last("limit 1"));
        if (existed != null) {
            return existed;
        }
        UserVehicle v = new UserVehicle();
        v.setUserId(userId);
        v.setVehicleNo(plate);
        v.setIsVisitor(0);
        v.setStatus(1);
        v.setIsDeleted(0);
        v.setCreateTime(LocalDateTime.now());
        v.setUpdateTime(LocalDateTime.now());
        userVehicleMapper.insert(v);
        return v;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserVehicle bindVisitorVehicle(Long hostUserId, String vehicleNo, LocalDateTime parkingDeadline) {
        String plate = normalizePlate(vehicleNo);
        UserVehicle existed = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getHostUserId, hostUserId)
                .eq(UserVehicle::getVehicleNo, plate)
                .eq(UserVehicle::getIsVisitor, 1)
                .last("limit 1"));
        LocalDateTime remindTime = calcReminderTime(parkingDeadline);
        if (existed != null) {
            existed.setParkingDeadline(parkingDeadline);
            existed.setRemindTime(remindTime);
            existed.setStatus(1);
            existed.setUpdateTime(LocalDateTime.now());
            userVehicleMapper.updateById(existed);
            return existed;
        }
        UserVehicle v = new UserVehicle();
        v.setUserId(hostUserId);
        v.setVehicleNo(plate);
        v.setIsVisitor(1);
        v.setHostUserId(hostUserId);
        v.setParkingDeadline(parkingDeadline);
        v.setRemindTime(remindTime);
        v.setStatus(1);
        v.setIsDeleted(0);
        v.setCreateTime(LocalDateTime.now());
        v.setUpdateTime(LocalDateTime.now());
        userVehicleMapper.insert(v);
        return v;
    }

    @Transactional(rollbackFor = Exception.class)
    public ParkingOrder createEntryOrder(Long currentUserId, String vehicleNo, Integer sourceType) {
        String plate = normalizePlate(vehicleNo);
        ParkingOrder existed = parkingOrderMapper.selectOne(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getVehicleNo, plate)
                .eq(ParkingOrder::getOrderType, 1)
                .eq(ParkingOrder::getStatus, 0)
                .orderByDesc(ParkingOrder::getId)
                .last("limit 1"));
        if (existed != null) {
            return existed;
        }
        int src = (sourceType != null && sourceType == 2) ? 2 : 1;
        Long ownerId = currentUserId;
        if (src == 2) {
            UserVehicle visitorVehicle = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                    .eq(UserVehicle::getVehicleNo, plate)
                    .eq(UserVehicle::getIsVisitor, 1)
                    .eq(UserVehicle::getStatus, 1)
                    .orderByDesc(UserVehicle::getId)
                    .last("limit 1"));
            if (visitorVehicle != null && visitorVehicle.getHostUserId() != null) {
                ownerId = visitorVehicle.getHostUserId();
            }
        } else {
            UserVehicle ownerVehicle = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                    .eq(UserVehicle::getVehicleNo, plate)
                    .eq(UserVehicle::getIsVisitor, 0)
                    .eq(UserVehicle::getStatus, 1)
                    .orderByDesc(UserVehicle::getId)
                    .last("limit 1"));
            if (ownerVehicle != null && ownerVehicle.getUserId() != null) {
                ownerId = ownerVehicle.getUserId();
            }
        }
        LocalDateTime now = LocalDateTime.now();
        ParkingOrder order = new ParkingOrder();
        order.setUserId(ownerId);
        order.setVehicleNo(plate);
        order.setOrderType(1);
        order.setSourceType(src);
        order.setAmount(BigDecimal.ZERO);
        order.setParkHours(0);
        order.setFreeHours(0);
        order.setDailyCap(DAILY_CAP);
        order.setStartTime(now);
        order.setEndTime(now);
        order.setStatus(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(0);
        parkingOrderMapper.insert(order);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> settleExitByVehicle(String vehicleNo) {
        String plate = normalizePlate(vehicleNo);
        ParkingOrder order = parkingOrderMapper.selectOne(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getVehicleNo, plate)
                .eq(ParkingOrder::getOrderType, 1)
                .eq(ParkingOrder::getStatus, 0)
                .orderByDesc(ParkingOrder::getId)
                .last("limit 1"));
        if (order == null) {
            throw new IllegalArgumentException("未找到待结算临停车订单");
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = Math.max(0, Duration.between(order.getStartTime(), now).getSeconds());
        int parkedHours = (int) Math.max(1, Math.ceil(seconds / 3600.0));
        int freeHours = 0;
        BigDecimal payable = BigDecimal.ZERO;

        if (!hasActiveMonthCard(order.getVehicleNo(), now)) {
            if (order.getSourceType() != null && order.getSourceType() == 2) {
                freeHours = applyVisitorFreeHours(order.getVehicleNo(), parkedHours);
            }
            int chargeHours = Math.max(0, parkedHours - freeHours);
            payable = TEMP_HOURLY_AMOUNT.multiply(BigDecimal.valueOf(chargeHours));
            if (payable.compareTo(DAILY_CAP) > 0) {
                payable = DAILY_CAP;
            }
        }

        order.setParkHours(parkedHours);
        order.setFreeHours(freeHours);
        order.setDailyCap(DAILY_CAP);
        order.setAmount(payable.setScale(2, RoundingMode.HALF_UP));
        order.setEndTime(now);
        order.setUpdateTime(now);
        if (payable.compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus(1);
            order.setPaymentTime(now);
            if (order.getTransactionId() == null || order.getTransactionId().isBlank()) {
                order.setTransactionId("FREE_PARK_" + order.getId());
            }
        } else {
            order.setStatus(0);
        }
        parkingOrderMapper.updateById(order);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getId());
        payload.put("vehicleNo", order.getVehicleNo());
        payload.put("parkHours", parkedHours);
        payload.put("freeHours", freeHours);
        payload.put("amount", order.getAmount());
        payload.put("status", order.getStatus());
        payload.put("needPay", order.getStatus() == 0);
        return payload;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createMonthCardOrder(Long userId, String vehicleNo) {
        String plate = normalizePlate(vehicleNo);
        bindOwnerVehicle(userId, plate);
        LocalDateTime now = LocalDateTime.now();
        ParkingOrder active = findActiveMonthCard(plate, now);
        LocalDateTime start = active != null ? active.getEndTime() : now;
        LocalDateTime end = start.plusDays(30);

        ParkingOrder order = new ParkingOrder();
        order.setUserId(userId);
        order.setVehicleNo(plate);
        order.setOrderType(2);
        order.setSourceType(1);
        order.setAmount(MONTH_CARD_AMOUNT);
        order.setParkHours(null);
        order.setFreeHours(0);
        order.setDailyCap(DAILY_CAP);
        order.setStartTime(start);
        order.setEndTime(end);
        order.setStatus(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(0);
        parkingOrderMapper.insert(order);
        String outTradeNo = "PARK_CARD_" + order.getId() + "_" + System.currentTimeMillis();
        order.setTransactionId(outTradeNo);
        order.setUpdateTime(LocalDateTime.now());
        parkingOrderMapper.updateById(order);

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", order.getId());
        payload.put("vehicleNo", plate);
        payload.put("startTime", start);
        payload.put("endTime", end);
        payload.put("payParams", wechatUtil.mockMiniPay(outTradeNo, MONTH_CARD_AMOUNT));
        return payload;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTempOrderPayment(Long userId, Integer role, Long orderId) {
        ParkingOrder order = parkingOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (role != null && role != 2 && !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权限支付该订单");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            return Map.of("orderId", orderId, "status", 1, "message", "订单已支付");
        }
        if (order.getAmount() == null || order.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            if (order.getTransactionId() == null || order.getTransactionId().isBlank()) {
                order.setTransactionId("FREE_PARK_" + order.getId());
            }
            order.setUpdateTime(LocalDateTime.now());
            parkingOrderMapper.updateById(order);
            return Map.of("orderId", orderId, "status", 1, "message", "无需支付，已自动完成");
        }
        if (order.getTransactionId() == null || order.getTransactionId().isBlank()) {
            String outTradeNo = (order.getOrderType() != null && order.getOrderType() == 2 ? "PARK_CARD_" : "PARK_TMP_")
                    + order.getId() + "_" + System.currentTimeMillis();
            order.setTransactionId(outTradeNo);
            order.setUpdateTime(LocalDateTime.now());
            parkingOrderMapper.updateById(order);
        }
        return Map.of(
                "orderId", order.getId(),
                "amount", order.getAmount(),
                "outTradeNo", order.getTransactionId(),
                "payParams", wechatUtil.mockMiniPay(order.getTransactionId(), order.getAmount())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ParkingOrder handlePayCallback(String outTradeNo, Boolean success) {
        ParkingOrder order = parkingOrderMapper.selectOne(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getTransactionId, outTradeNo)
                .last("limit 1"));
        if (order == null) {
            throw new IllegalArgumentException("交易单不存在");
        }
        if (success == null || success) {
            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            parkingOrderMapper.updateById(order);
        }
        return order;
    }

    public Map<String, Object> monthCardStatus(Long userId, String vehicleNo) {
        String plate = normalizePlate(vehicleNo);
        ParkingOrder active = findActiveMonthCard(plate, LocalDateTime.now());
        if (active == null || !userId.equals(active.getUserId())) {
            return Map.of(
                    "vehicleNo", plate,
                    "active", false
            );
        }
        long remainDays = Math.max(0, Duration.between(LocalDateTime.now(), active.getEndTime()).toDays());
        return Map.of(
                "vehicleNo", plate,
                "active", true,
                "startTime", active.getStartTime(),
                "endTime", active.getEndTime(),
                "remainDays", remainDays
        );
    }

    public List<ParkingOrder> listRenewReminders(Long userId, Integer role) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getOrderType, 2)
                .eq(ParkingOrder::getStatus, 1)
                .gt(ParkingOrder::getEndTime, now)
                .le(ParkingOrder::getEndTime, now.plusDays(3))
                .orderByAsc(ParkingOrder::getEndTime);
        if (role == null || role != 2) {
            wrapper.eq(ParkingOrder::getUserId, userId);
        }
        return parkingOrderMapper.selectList(wrapper);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void scanMonthCardRenewReminder() {
        LocalDateTime now = LocalDateTime.now();
        long count = parkingOrderMapper.selectCount(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getOrderType, 2)
                .eq(ParkingOrder::getStatus, 1)
                .gt(ParkingOrder::getEndTime, now)
                .le(ParkingOrder::getEndTime, now.plusDays(3)));
        if (count > 0) {
            log.info("Parking month-card reminder scan: {} card(s) expiring within 3 days.", count);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public OwnerParkingQuota setExtraHours(Long userId, String monthKey, Integer extraHours) {
        String mk = (monthKey == null || monthKey.isBlank())
                ? YearMonth.now().format(MONTH_FMT)
                : monthKey.trim();
        OwnerParkingQuota quota = getOrCreateQuota(userId, mk);
        quota.setOwnerExtraHours(extraHours);
        quota.setUpdateTime(LocalDateTime.now());
        ownerParkingQuotaMapper.updateById(quota);
        return quota;
    }

    public OwnerParkingQuota queryQuota(Long userId, String monthKey) {
        String mk = (monthKey == null || monthKey.isBlank())
                ? YearMonth.now().format(MONTH_FMT)
                : monthKey.trim();
        return getOrCreateQuota(userId, mk);
    }

    public Map<String, Object> queryPaymentEntrance(Long userId, Integer role, String vehicleNo) {
        String plate = normalizePlate(vehicleNo);
        LambdaQueryWrapper<ParkingOrder> unpaidWrapper = new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getVehicleNo, plate)
                .eq(ParkingOrder::getOrderType, 1)
                .eq(ParkingOrder::getStatus, 0)
                .orderByDesc(ParkingOrder::getId)
                .last("limit 1");
        if (role == null || role != 2) {
            unpaidWrapper.eq(ParkingOrder::getUserId, userId);
        }
        ParkingOrder unpaid = parkingOrderMapper.selectOne(unpaidWrapper);
        ParkingOrder activeCard = findActiveMonthCard(plate, LocalDateTime.now());
        Map<String, Object> payload = new HashMap<>();
        payload.put("vehicleNo", plate);
        payload.put("unpaidTempOrder", unpaid);
        payload.put("activeMonthCard", activeCard);
        return payload;
    }

    public List<UserVehicle> listMyVehicles(Long userId, boolean visitor) {
        LambdaQueryWrapper<UserVehicle> wrapper = new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getStatus, 1)
                .eq(UserVehicle::getIsVisitor, visitor ? 1 : 0);
        if (visitor) {
            wrapper.eq(UserVehicle::getHostUserId, userId);
        } else {
            wrapper.eq(UserVehicle::getUserId, userId);
        }
        wrapper.orderByDesc(UserVehicle::getId);
        return userVehicleMapper.selectList(wrapper);
    }

    private int applyVisitorFreeHours(String vehicleNo, int parkedHours) {
        UserVehicle visitor = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getVehicleNo, vehicleNo)
                .eq(UserVehicle::getIsVisitor, 1)
                .eq(UserVehicle::getStatus, 1)
                .orderByDesc(UserVehicle::getId)
                .last("limit 1"));
        if (visitor == null || visitor.getHostUserId() == null) {
            return 0;
        }
        String monthKey = YearMonth.now().format(MONTH_FMT);
        OwnerParkingQuota quota = getOrCreateQuota(visitor.getHostUserId(), monthKey);
        int total = safeInt(quota.getFreeHoursTotal()) + safeInt(quota.getOwnerExtraHours());
        int used = safeInt(quota.getFreeHoursUsed());
        int available = Math.max(0, total - used);
        int free = Math.min(available, parkedHours);
        if (free > 0) {
            quota.setFreeHoursUsed(used + free);
            quota.setUpdateTime(LocalDateTime.now());
            ownerParkingQuotaMapper.updateById(quota);
        }
        return free;
    }

    private OwnerParkingQuota getOrCreateQuota(Long userId, String monthKey) {
        OwnerParkingQuota quota = ownerParkingQuotaMapper.selectOne(new LambdaQueryWrapper<OwnerParkingQuota>()
                .eq(OwnerParkingQuota::getUserId, userId)
                .eq(OwnerParkingQuota::getMonthKey, monthKey)
                .last("limit 1"));
        if (quota != null) {
            return quota;
        }
        OwnerParkingQuota created = new OwnerParkingQuota();
        created.setUserId(userId);
        created.setMonthKey(monthKey);
        created.setFreeHoursTotal(10);
        created.setFreeHoursUsed(0);
        created.setOwnerExtraHours(0);
        created.setCreateTime(LocalDateTime.now());
        created.setUpdateTime(LocalDateTime.now());
        ownerParkingQuotaMapper.insert(created);
        return created;
    }

    private ParkingOrder findActiveMonthCard(String vehicleNo, LocalDateTime now) {
        return parkingOrderMapper.selectOne(new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getVehicleNo, vehicleNo)
                .eq(ParkingOrder::getOrderType, 2)
                .eq(ParkingOrder::getStatus, 1)
                .gt(ParkingOrder::getEndTime, now)
                .orderByDesc(ParkingOrder::getEndTime)
                .last("limit 1"));
    }

    private boolean hasActiveMonthCard(String vehicleNo, LocalDateTime now) {
        return findActiveMonthCard(vehicleNo, now) != null;
    }

    private LocalDateTime calcReminderTime(LocalDateTime deadline) {
        int hour = deadline.getHour();
        if (hour >= 22) {
            return deadline.toLocalDate().atTime(20, 0);
        }
        if (hour < 6) {
            LocalDate remindDay = deadline.toLocalDate().minusDays(1);
            return remindDay.atTime(20, 0);
        }
        return deadline.minusHours(1);
    }

    private String normalizePlate(String vehicleNo) {
        if (vehicleNo == null || vehicleNo.isBlank()) {
            throw new IllegalArgumentException("vehicleNo不能为空");
        }
        return vehicleNo.trim().toUpperCase();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
