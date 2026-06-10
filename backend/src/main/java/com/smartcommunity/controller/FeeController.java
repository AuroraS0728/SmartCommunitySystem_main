package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.RoleUtils;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.FeeAnnualDiscountReq;
import com.smartcommunity.dto.request.FeeWechatPayReq;
import com.smartcommunity.dto.request.GenerateFeeBillsReq;
import com.smartcommunity.dto.request.PayFeeReq;
import com.smartcommunity.dto.request.WechatPayCallbackReq;
import com.smartcommunity.dto.response.PaymentSubjectVO;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.RepairFeeBill;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.RepairFeeBillMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.FeeBillingService;
import com.smartcommunity.service.LocalCacheService;
import com.smartcommunity.service.PointsService;
import com.smartcommunity.utils.WechatUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fee")
@RequiredArgsConstructor
public class FeeController {

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("5.00");
    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int BUSINESS_TYPE_FEE = 1;
    private static final int BUSINESS_TYPE_PARKING = 2;
    private static final int BUSINESS_TYPE_REPAIR = 3;

    private final WechatUtil wechatUtil;
    private final FeeBillingService feeBillingService;
    private final PointsService pointsService;
    private final FeeBillMapper feeBillMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final RepairFeeBillMapper repairFeeBillMapper;
    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;
    private final LocalCacheService localCacheService;
    @Qualifier("queryExecutor")
    private final Executor queryExecutor;

    @GetMapping("/bills")
    public Result<List<FeeBill>> bills(@RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        String cacheKey = "fee:bills:" + role + ":" + userId + ":" + status;
        List<FeeBill> rows = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(15), () -> {
            LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<FeeBill>()
                    .eq(FeeBill::getIsDeleted, 0);
            if (status != null) {
                wrapper.eq(FeeBill::getStatus, status);
            }
            if (role != null && role == 1) {
                List<Long> propertyIds = userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                                .eq(UserProperty::getUserId, userId)
                                .eq(UserProperty::getIsDeleted, 0))
                        .stream().map(UserProperty::getPropertyId).collect(Collectors.toList());
                if (propertyIds.isEmpty()) {
                    return List.of();
                }
                wrapper.in(FeeBill::getPropertyId, propertyIds);
            }
            wrapper.orderByDesc(FeeBill::getBillPeriod).orderByDesc(FeeBill::getId);
            return feeBillMapper.selectList(wrapper);
        });
        return Result.success(rows);
    }

    /**
     * 统一缴费主体查询：1-物业费，2-停车费，3-维修费。
     */
    @GetMapping("/subjects")
    public Result<List<PaymentSubjectVO>> paymentSubjects(@RequestParam(required = false) Integer businessType,
                                                          @RequestParam(required = false) Integer status) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1 && role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner or admin can query payment subjects");
        }
        if (businessType != null && (businessType < 1 || businessType > 3)) {
            return Result.fail(StatusCode.BAD_REQUEST, "businessType must be 1~3");
        }

        String cacheKey = "fee:subjects:" + role + ":" + userId + ":" + businessType + ":" + status;
        List<PaymentSubjectVO> subjects = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(15), () -> {
            List<Long> ownerPropertyIds = queryOwnerPropertyIds(userId, role);
            List<PaymentSubjectVO> rows = new ArrayList<>();
            List<CompletableFuture<List<PaymentSubjectVO>>> tasks = new ArrayList<>();
            if (businessType == null || businessType == BUSINESS_TYPE_FEE) {
                tasks.add(CompletableFuture.supplyAsync(() -> queryPropertyFeeSubjects(role, ownerPropertyIds, status), queryExecutor));
            }
            if (businessType == null || businessType == BUSINESS_TYPE_PARKING) {
                tasks.add(CompletableFuture.supplyAsync(() -> queryParkingFeeSubjects(role, userId, status), queryExecutor));
            }
            if (businessType == null || businessType == BUSINESS_TYPE_REPAIR) {
                tasks.add(CompletableFuture.supplyAsync(() -> queryRepairFeeSubjects(role, userId, status), queryExecutor));
            }
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<List<PaymentSubjectVO>> task : tasks) {
                rows.addAll(task.join());
            }
            rows.sort(Comparator
                    .comparing(PaymentSubjectVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(PaymentSubjectVO::getBusinessType, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(PaymentSubjectVO::getBusinessId, Comparator.nullsLast(Comparator.reverseOrder())));
            return rows;
        });
        return Result.success(subjects);
    }

    /**
     * 积分支付入口（兼容历史版本）。
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/pay")
    public Result<Map<String, Object>> payByPoints(@RequestBody PayFeeReq req) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || userId == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can pay bill");
        }
        if (req == null || req.getBillId() == null) {
            return Result.fail("billId is required");
        }
        PointsService.ConsumeResult consumeResult = pointsService.consume(userId, 1, req.getBillId());
        return Result.success(Map.of(
                "billId", req.getBillId(),
                "consumePoints", consumeResult.getConsumePoints(),
                "beforePoints", consumeResult.getBeforePoints(),
                "afterPoints", consumeResult.getAfterPoints(),
                "balance", consumeResult.getAfterPoints()
        ));
    }

    /**
     * 微信支付（模拟）下单。
     */
    @PostMapping("/pay/wechat")
    public Result<Map<String, Object>> payWechat(@Valid @RequestBody FeeWechatPayReq req) {
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        if (userId == null || role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 1) {
            return Result.fail(StatusCode.FORBIDDEN, "only owner can pay fee bill");
        }
        FeeBill bill = feeBillMapper.selectById(req.getBillId());
        if (bill == null) {
            return Result.fail("bill not found");
        }
        boolean belongs = userPropertyMapper.selectCount(new LambdaQueryWrapper<UserProperty>()
                .eq(UserProperty::getUserId, userId)
                .eq(UserProperty::getPropertyId, bill.getPropertyId())) > 0;
        if (!belongs) {
            return Result.fail(StatusCode.FORBIDDEN, "no permission for this bill");
        }
        if (bill.getStatus() != null && bill.getStatus() == 2) {
            return Result.fail("bill already paid");
        }
        String outTradeNo = bill.getTransactionId();
        if (outTradeNo == null || outTradeNo.isBlank()) {
            outTradeNo = "FEE_" + bill.getId() + "_" + System.currentTimeMillis();
            bill.setTransactionId(outTradeNo);
            bill.setUpdateTime(LocalDateTime.now());
            feeBillMapper.updateById(bill);
        }
        return Result.success(Map.of(
                "billId", bill.getId(),
                "outTradeNo", outTradeNo,
                "amount", bill.getAmount(),
                "payParams", wechatUtil.mockMiniPay(outTradeNo, bill.getAmount())
        ));
    }

    /**
     * 微信支付回调（模拟）。
     */
    @PostMapping("/pay/callback")
    public Result<FeeBill> payCallback(@Valid @RequestBody WechatPayCallbackReq req) {
        FeeBill bill = feeBillMapper.selectOne(new LambdaQueryWrapper<FeeBill>()
                .eq(FeeBill::getTransactionId, req.getOutTradeNo())
                .last("limit 1"));
        if (bill == null) {
            return Result.fail("bill not found");
        }
        if (req.getSuccess() == null || req.getSuccess()) {
            bill.setPaidAmount(bill.getAmount());
            bill.setStatus(2);
            bill.setPaymentTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());
            feeBillMapper.updateById(bill);
        }
        return Result.success(bill);
    }

    @PostMapping("/bill")
    public Result<FeeBill> createBill(@RequestBody FeeBill bill) {
        if (!isPropertyAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can create bill");
        }
        if (bill == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "bill body is required");
        }
        fillFeeBillByRules(bill);
        bill.setNeedPoints(toNeedPoints(bill.getAmount()));
        bill.setCreateTime(LocalDateTime.now());
        bill.setUpdateTime(LocalDateTime.now());
        bill.setIsDeleted(0);
        if (bill.getStatus() == null) {
            bill.setStatus(0);
        }
        if (bill.getPaidAmount() == null) {
            bill.setPaidAmount(BigDecimal.ZERO);
        }
        feeBillMapper.insert(bill);
        return Result.success(bill);
    }

    @PutMapping("/bill/{id}")
    public Result<FeeBill> updateBill(@PathVariable Long id, @RequestBody FeeBill req) {
        if (!isPropertyAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can update bill");
        }
        if (req == null) {
            return Result.fail(StatusCode.BAD_REQUEST, "bill body is required");
        }
        FeeBill bill = feeBillMapper.selectById(id);
        if (bill == null) {
            return Result.fail("bill not found");
        }
        bill.setPropertyId(req.getPropertyId());
        bill.setBillPeriod(req.getBillPeriod());
        bill.setAreaSnapshot(req.getAreaSnapshot());
        bill.setUnitPrice(req.getUnitPrice());
        bill.setDiscountAmount(req.getDiscountAmount());
        bill.setAmount(req.getAmount());
        fillFeeBillByRules(bill);
        bill.setNeedPoints(toNeedPoints(bill.getAmount()));
        bill.setPaidAmount(req.getPaidAmount());
        bill.setStatus(req.getStatus());
        bill.setDueDate(req.getDueDate());
        bill.setPaymentTime(req.getPaymentTime());
        bill.setTransactionId(req.getTransactionId());
        bill.setUpdateTime(LocalDateTime.now());
        feeBillMapper.updateById(bill);
        return Result.success(bill);
    }

    @DeleteMapping("/bill/{id}")
    public Result<Void> deleteBill(@PathVariable Long id) {
        if (!isPropertyAdmin()) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can delete bill");
        }
        feeBillMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @PostMapping("/bill/generate")
    public Result<List<FeeBill>> generateBills(@Valid @RequestBody GenerateFeeBillsReq req) {
        Integer role = AuthContext.getRole();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can generate bills");
        }
        // 管理端手动生成某个月的物业费账单，propertyId为空时表示给全部房产生成。
        return Result.success(feeBillingService.generateBillsForPeriod(req.getBillPeriod(), req.getPropertyId()));
    }

    @PostMapping("/bill/annual-discount")
    public Result<FeeBill> annualDiscount(@Valid @RequestBody FeeAnnualDiscountReq req) {
        Integer role = AuthContext.getRole();
        if (role == null) {
            return Result.fail(StatusCode.UNAUTHORIZED, "unauthorized");
        }
        if (role != 2) {
            return Result.fail(StatusCode.FORBIDDEN, "only admin can apply annual discount");
        }
        // 年缴优惠入口：从startPeriod开始生成12个月账单，并对第12个月做减免。
        return Result.success(feeBillingService.applyAnnualDiscount(req.getPropertyId(), req.getStartPeriod()));
    }

    @GetMapping("/collection-rate")
    public Result<Map<String, Object>> collectionRate(@RequestParam(required = false) String billPeriod) {
        // 给后台统计卡片使用，billPeriod为空时统计全部账单。
        return Result.success(feeBillingService.collectionRate(billPeriod));
    }

    /**
     * 兼容旧版本停车订单创建。
     */
    @PostMapping("/parking/order")
    public Result<Map<String, Object>> createParkingOrder(@RequestBody ParkingOrder req) {
        req.setUserId(AuthContext.getUserId());
        req.setSourceType(req.getSourceType() == null ? 1 : req.getSourceType());
        req.setStatus(0);
        req.setParkHours(req.getParkHours() == null ? 0 : req.getParkHours());
        req.setFreeHours(req.getFreeHours() == null ? 0 : req.getFreeHours());
        req.setDailyCap(req.getDailyCap() == null ? new BigDecimal("30.00") : req.getDailyCap());
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        req.setIsDeleted(0);
        if (req.getEndTime() == null) {
            req.setEndTime(req.getStartTime() == null ? LocalDateTime.now() : req.getStartTime());
        }
        parkingOrderMapper.insert(req);
        return Result.success(Map.of(
                "orderId", req.getId(),
                "status", req.getStatus(),
                "amount", req.getAmount()
        ));
    }

    @GetMapping("/parking/orders")
    public Result<List<ParkingOrder>> parkingOrders() {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        String cacheKey = "parking:orders:" + role + ":" + userId;
        List<ParkingOrder> rows = localCacheService.getOrLoad(cacheKey, Duration.ofSeconds(15), () -> {
            LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<ParkingOrder>()
                    .eq(ParkingOrder::getIsDeleted, 0);
            if (role != null && role != 2) {
                wrapper.eq(ParkingOrder::getUserId, userId);
            }
            wrapper.orderByDesc(ParkingOrder::getId);
            return parkingOrderMapper.selectList(wrapper);
        });
        return Result.success(rows);
    }

    /**
     * 积分与金额换算规则：1元=1积分，向上取整。
     */
    private int toNeedPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return amount.setScale(0, RoundingMode.UP).intValue();
    }

    private boolean isPropertyAdmin() {
        return RoleUtils.isPropertyAdmin(AuthContext.getRole());
    }

    private void fillFeeBillByRules(FeeBill bill) {
        if (bill.getPropertyId() == null) {
            throw new IllegalArgumentException("propertyId is required");
        }
        Property property = propertyMapper.selectById(bill.getPropertyId());
        if (property == null) {
            throw new IllegalArgumentException("property not found");
        }
        BigDecimal area = property.getArea() == null
                ? BigDecimal.ZERO
                : property.getArea().setScale(2, RoundingMode.HALF_UP);
        if (bill.getAreaSnapshot() == null || bill.getAreaSnapshot().compareTo(BigDecimal.ZERO) <= 0) {
            bill.setAreaSnapshot(area);
        }
        if (bill.getUnitPrice() == null || bill.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            bill.setUnitPrice(DEFAULT_UNIT_PRICE);
        }
        if (bill.getDiscountAmount() == null || bill.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            bill.setDiscountAmount(BigDecimal.ZERO);
        }

        // 物业费核心公式：面积 × 单价（默认 5元/㎡·月）。
        BigDecimal calculated = bill.getAreaSnapshot().multiply(bill.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = bill.getAmount() == null ? calculated : bill.getAmount();
        amount = amount.subtract(bill.getDiscountAmount());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            amount = BigDecimal.ZERO;
        }
        bill.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        if (bill.getDueDate() == null && bill.getBillPeriod() != null && !bill.getBillPeriod().isBlank()) {
            bill.setDueDate(dueDateByPeriod(bill.getBillPeriod()));
        }
    }

    private LocalDateTime dueDateByPeriod(String period) {
        try {
            YearMonth ym = YearMonth.parse(period.trim(), PERIOD_FMT);
            return ym.plusMonths(1).atDay(10).atTime(23, 59, 59);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now().plusDays(10);
        }
    }

    private List<Long> queryOwnerPropertyIds(Long userId, Integer role) {
        if (role == null || role != 1) {
            return List.of();
        }
        return userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                        .eq(UserProperty::getUserId, userId)
                        .eq(UserProperty::getIsDeleted, 0))
                .stream()
                .map(UserProperty::getPropertyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<PaymentSubjectVO> queryPropertyFeeSubjects(Integer role, List<Long> ownerPropertyIds, Integer status) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<FeeBill>()
                .eq(FeeBill::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(FeeBill::getStatus, status);
        }
        if (role != null && role == 1) {
            if (ownerPropertyIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(FeeBill::getPropertyId, ownerPropertyIds);
        }
        wrapper.orderByDesc(FeeBill::getId);
        List<FeeBill> bills = feeBillMapper.selectList(wrapper);
        if (bills.isEmpty()) {
            return List.of();
        }
        Set<Long> propertyIds = bills.stream()
                .map(FeeBill::getPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Property> propertyMap = propertyMap(propertyIds);

        List<PaymentSubjectVO> subjects = new ArrayList<>(bills.size());
        for (FeeBill bill : bills) {
            Property property = propertyMap.get(bill.getPropertyId());
            PaymentSubjectVO row = new PaymentSubjectVO();
            row.setBusinessType(BUSINESS_TYPE_FEE);
            row.setBusinessTypeText("物业费");
            row.setBusinessId(bill.getId());
            row.setBusinessRef(bill.getBillPeriod());
            row.setPropertyId(bill.getPropertyId());
            row.setPropertyCode(resolvePropertyCode(property, bill.getPropertyId()));
            row.setOwnerName(resolveOwnerName(property));
            row.setSubjectName(formatSubjectName(property, bill.getPropertyId()));
            row.setAmount(bill.getAmount());
            row.setPaidAmount(bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount());
            row.setNeedPoints(bill.getNeedPoints() == null ? toNeedPoints(bill.getAmount()) : bill.getNeedPoints());
            row.setStatus(bill.getStatus());
            row.setStatusText(feeStatusText(bill.getStatus()));
            row.setDueDate(bill.getDueDate());
            row.setPaymentTime(bill.getPaymentTime());
            row.setCreateTime(bill.getCreateTime());
            subjects.add(row);
        }
        return subjects;
    }

    private List<PaymentSubjectVO> queryParkingFeeSubjects(Integer role, Long userId, Integer status) {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<ParkingOrder>()
                .eq(ParkingOrder::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(ParkingOrder::getStatus, status);
        }
        if (role != null && role == 1) {
            wrapper.eq(ParkingOrder::getUserId, userId);
        }
        wrapper.orderByDesc(ParkingOrder::getId);
        List<ParkingOrder> orders = parkingOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) {
            return List.of();
        }
        Set<Long> propertyIds = orders.stream()
                .map(ParkingOrder::getPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Property> propertyMap = propertyMap(propertyIds);
        List<PaymentSubjectVO> subjects = new ArrayList<>(orders.size());
        for (ParkingOrder order : orders) {
            Property property = propertyMap.get(order.getPropertyId());
            PaymentSubjectVO row = new PaymentSubjectVO();
            row.setBusinessType(BUSINESS_TYPE_PARKING);
            row.setBusinessTypeText("停车费");
            row.setBusinessId(order.getId());
            row.setBusinessRef(orderTypeText(order.getOrderType()) + " / 车牌:" + defaultText(order.getVehicleNo(), "--"));
            row.setPropertyId(order.getPropertyId());
            row.setPropertyCode(resolvePropertyCode(property, order.getPropertyId()));
            row.setOwnerName(resolveOwnerName(property));
            row.setSubjectName(formatSubjectName(property, order.getPropertyId()));
            row.setAmount(order.getAmount());
            row.setPaidAmount(isPaid(order.getStatus()) ? order.getAmount() : BigDecimal.ZERO);
            row.setNeedPoints(toNeedPoints(order.getAmount()));
            row.setStatus(order.getStatus());
            row.setStatusText(parkingStatusText(order.getStatus()));
            row.setDueDate(order.getEndTime());
            row.setPaymentTime(order.getPaymentTime());
            row.setCreateTime(order.getCreateTime());
            subjects.add(row);
        }
        return subjects;
    }

    private List<PaymentSubjectVO> queryRepairFeeSubjects(Integer role, Long userId, Integer status) {
        LambdaQueryWrapper<RepairFeeBill> wrapper = new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getIsDeleted, 0);
        if (status != null) {
            wrapper.eq(RepairFeeBill::getStatus, status);
        }
        if (role != null && role == 1) {
            wrapper.eq(RepairFeeBill::getUserId, userId);
        }
        wrapper.orderByDesc(RepairFeeBill::getId);
        List<RepairFeeBill> repairBills = repairFeeBillMapper.selectList(wrapper);
        if (repairBills.isEmpty()) {
            return List.of();
        }
        Set<Long> propertyIds = repairBills.stream()
                .map(RepairFeeBill::getPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Property> propertyMap = propertyMap(propertyIds);
        List<PaymentSubjectVO> subjects = new ArrayList<>(repairBills.size());
        for (RepairFeeBill bill : repairBills) {
            Property property = propertyMap.get(bill.getPropertyId());
            PaymentSubjectVO row = new PaymentSubjectVO();
            row.setBusinessType(BUSINESS_TYPE_REPAIR);
            row.setBusinessTypeText("维修费");
            row.setBusinessId(bill.getId());
            row.setBusinessRef("工单#" + bill.getOrderId());
            row.setPropertyId(bill.getPropertyId());
            row.setPropertyCode(resolvePropertyCode(property, bill.getPropertyId()));
            row.setOwnerName(resolveOwnerName(property));
            row.setSubjectName(formatSubjectName(property, bill.getPropertyId()));
            row.setAmount(bill.getAmount());
            row.setPaidAmount(isPaid(bill.getStatus()) ? bill.getAmount() : BigDecimal.ZERO);
            row.setNeedPoints(bill.getNeedPoints() == null ? toNeedPoints(bill.getAmount()) : bill.getNeedPoints());
            row.setStatus(bill.getStatus());
            row.setStatusText(repairStatusText(bill.getStatus()));
            row.setDueDate(bill.getDueDate());
            row.setPaymentTime(bill.getPaymentTime());
            row.setCreateTime(bill.getCreateTime());
            subjects.add(row);
        }
        return subjects;
    }

    private Map<Long, Property> propertyMap(Set<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            return Map.of();
        }
        List<Property> properties = propertyMapper.selectList(new LambdaQueryWrapper<Property>()
                .in(Property::getId, propertyIds)
                .eq(Property::getIsDeleted, 0));
        Map<Long, Property> result = new HashMap<>();
        for (Property property : properties) {
            result.put(property.getId(), property);
        }
        return result;
    }

    private String resolvePropertyCode(Property property, Long propertyId) {
        if (property != null && property.getPropertyCode() != null && !property.getPropertyCode().isBlank()) {
            return property.getPropertyCode();
        }
        return propertyId == null ? "-" : ("房屋#" + propertyId);
    }

    private String resolveOwnerName(Property property) {
        if (property != null && property.getOwnerName() != null && !property.getOwnerName().isBlank()) {
            return property.getOwnerName().trim();
        }
        return "未登记业主";
    }

    private String formatSubjectName(Property property, Long propertyId) {
        return resolvePropertyCode(property, propertyId) + " / " + resolveOwnerName(property);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String feeStatusText(Integer status) {
        if (status == null || status == 0) {
            return "待缴";
        }
        if (status == 1) {
            return "部分缴纳";
        }
        if (status == 2) {
            return "已缴纳";
        }
        return "未知";
    }

    private String parkingStatusText(Integer status) {
        return isPaid(status) ? "已缴纳" : "待缴";
    }

    private String repairStatusText(Integer status) {
        return isPaid(status) ? "已缴纳" : "待缴";
    }

    private String orderTypeText(Integer orderType) {
        if (orderType != null && orderType == 2) {
            return "月卡";
        }
        return "临停";
    }

    private boolean isPaid(Integer status) {
        return status != null && status == 1;
    }
}
