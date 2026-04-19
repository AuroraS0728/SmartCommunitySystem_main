package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.dto.request.PayFeeReq;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fee")
@RequiredArgsConstructor
public class FeeController {

    private final PointsService pointsService;
    private final FeeBillMapper feeBillMapper;
    private final ParkingOrderMapper parkingOrderMapper;
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
        wrapper.orderByAsc(FeeBill::getId);
        return Result.success(feeBillMapper.selectList(wrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/pay")
    public Result<Map<String, Object>> pay(@RequestBody PayFeeReq req) {
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

    @PostMapping("/bill")
    public Result<FeeBill> createBill(@RequestBody FeeBill bill) {
        if (bill.getAmount() != null) {
            bill.setNeedPoints(toNeedPoints(bill.getAmount()));
        }
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
        bill.setAmount(req.getAmount());
        if (req.getAmount() != null) {
            bill.setNeedPoints(toNeedPoints(req.getAmount()));
        }
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

    @PostMapping("/parking/order")
    public Result<Map<String, Object>> createParkingOrder(@RequestBody ParkingOrder req) {
        req.setUserId(AuthContext.getUserId());
        req.setStatus(0);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        req.setIsDeleted(0);
        parkingOrderMapper.insert(req);
        Integer needPoints = toNeedPoints(req.getAmount());
        return Result.success(Map.of(
                "orderId", req.getId(),
                "needPoints", needPoints,
                "status", req.getStatus()
        ));
    }

    @GetMapping("/parking/orders")
    public Result<List<ParkingOrder>> parkingOrders() {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<>();
        Integer role = AuthContext.getRole();
        if (role != null && role != 2) {
            wrapper.eq(ParkingOrder::getUserId, AuthContext.getUserId());
        }
        wrapper.orderByAsc(ParkingOrder::getId);
        return Result.success(parkingOrderMapper.selectList(wrapper));
    }

    /**
     * 积分与金额换算规则：1元 = 1积分。
     */
    private int toNeedPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        try {
            return amount.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("amount must be integer yuan for points payment");
        }
    }
}
