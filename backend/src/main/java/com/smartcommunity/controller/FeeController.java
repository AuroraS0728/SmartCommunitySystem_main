package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.PayFeeReq;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import com.smartcommunity.utils.WechatUtil;
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

    private final WechatUtil wechatUtil;
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
        FeeBill bill = feeBillMapper.selectById(req.getBillId());
        if (bill == null) {
            return Result.fail("bill not found");
        }
        BigDecimal paid = bill.getPaidAmount() == null ? BigDecimal.ZERO : bill.getPaidAmount();
        BigDecimal amount = req.getAmount() == null ? bill.getAmount() : req.getAmount();
        bill.setPaidAmount(paid.add(amount));
        int status = bill.getPaidAmount().compareTo(bill.getAmount()) >= 0 ? 2 : 1;
        bill.setStatus(status);
        bill.setPaymentTime(LocalDateTime.now());
        String outTradeNo = "FEE_" + bill.getId() + "_" + System.currentTimeMillis();
        bill.setTransactionId(outTradeNo);
        bill.setUpdateTime(LocalDateTime.now());
        feeBillMapper.updateById(bill);
        return Result.success(wechatUtil.mockMiniPay(outTradeNo, amount));
    }

    @PostMapping("/bill")
    public Result<FeeBill> createBill(@RequestBody FeeBill bill) {
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
        String outTradeNo = "PARK_" + req.getId() + "_" + System.currentTimeMillis();
        req.setTransactionId(outTradeNo);
        req.setUpdateTime(LocalDateTime.now());
        parkingOrderMapper.updateById(req);
        return Result.success(wechatUtil.mockMiniPay(outTradeNo, req.getAmount()));
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
}
