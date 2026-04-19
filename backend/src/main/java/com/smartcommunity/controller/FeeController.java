package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.FeeAnnualDiscountReq;
import com.smartcommunity.dto.request.FeeWechatPayReq;
import com.smartcommunity.dto.request.GenerateFeeBillsReq;
import com.smartcommunity.dto.request.PayFeeReq;
import com.smartcommunity.dto.request.WechatPayCallbackReq;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.Property;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.PropertyMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.FeeBillingService;
import com.smartcommunity.service.PointsService;
import com.smartcommunity.utils.WechatUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fee")
@RequiredArgsConstructor
public class FeeController {

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("5.00");
    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final WechatUtil wechatUtil;
    private final FeeBillingService feeBillingService;
    private final PointsService pointsService;
    private final FeeBillMapper feeBillMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final PropertyMapper propertyMapper;
    private final UserPropertyMapper userPropertyMapper;

    @GetMapping("/bills")
    public Result<List<FeeBill>> bills(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FeeBill::getStatus, status);
        }
        Integer role = AuthContext.getRole();
        if (role != null && role == 1) {
            List<Long> propertyIds = userPropertyMapper.selectList(new LambdaQueryWrapper<UserProperty>()
                            .eq(UserProperty::getUserId, AuthContext.getUserId()))
                    .stream().map(UserProperty::getPropertyId).collect(Collectors.toList());
            if (propertyIds.isEmpty()) {
                return Result.success(List.of());
            }
            wrapper.in(FeeBill::getPropertyId, propertyIds);
        }
        wrapper.orderByDesc(FeeBill::getBillPeriod).orderByDesc(FeeBill::getId);
        return Result.success(feeBillMapper.selectList(wrapper));
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
        return Result.success(feeBillingService.applyAnnualDiscount(req.getPropertyId(), req.getStartPeriod()));
    }

    @GetMapping("/collection-rate")
    public Result<Map<String, Object>> collectionRate(@RequestParam(required = false) String billPeriod) {
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
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<>();
        Integer role = AuthContext.getRole();
        if (role != null && role != 2) {
            wrapper.eq(ParkingOrder::getUserId, AuthContext.getUserId());
        }
        wrapper.orderByDesc(ParkingOrder::getId);
        return Result.success(parkingOrderMapper.selectList(wrapper));
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
}
