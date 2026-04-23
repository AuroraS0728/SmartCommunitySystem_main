package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.Property;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeBillingService {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("5.00");

    private final FeeBillMapper feeBillMapper;
    private final PropertyMapper propertyMapper;

    @Transactional(rollbackFor = Exception.class)
    public List<FeeBill> generateBillsForPeriod(String billPeriod, Long propertyId) {
        YearMonth ym = parsePeriod(billPeriod);
        LambdaQueryWrapper<Property> wrapper = new LambdaQueryWrapper<>();
        if (propertyId != null) {
            wrapper.eq(Property::getId, propertyId);
        }
        List<Property> properties = propertyMapper.selectList(wrapper);
        if (properties.isEmpty()) {
            return List.of();
        }

        List<FeeBill> created = new ArrayList<>();
        for (Property property : properties) {
            FeeBill existed = feeBillMapper.selectOne(new LambdaQueryWrapper<FeeBill>()
                    .eq(FeeBill::getPropertyId, property.getId())
                    .eq(FeeBill::getBillPeriod, billPeriod)
                    .last("limit 1"));
            if (existed != null) {
                continue;
            }
            BigDecimal area = safeArea(property.getArea());
            BigDecimal amount = area.multiply(DEFAULT_UNIT_PRICE).setScale(2, RoundingMode.HALF_UP);
            FeeBill bill = new FeeBill();
            bill.setPropertyId(property.getId());
            bill.setBillPeriod(billPeriod);
            bill.setAreaSnapshot(area);
            bill.setUnitPrice(DEFAULT_UNIT_PRICE);
            bill.setAmount(amount);
            bill.setDiscountAmount(BigDecimal.ZERO);
            bill.setNeedPoints(toNeedPoints(amount));
            bill.setPaidAmount(BigDecimal.ZERO);
            bill.setStatus(0);
            bill.setDueDate(ym.plusMonths(1).atDay(10).atTime(23, 59, 59));
            bill.setCreateTime(LocalDateTime.now());
            bill.setUpdateTime(LocalDateTime.now());
            bill.setIsDeleted(0);
            feeBillMapper.insert(bill);
            created.add(bill);
        }
        return created;
    }

    @Transactional(rollbackFor = Exception.class)
    public FeeBill applyAnnualDiscount(Long propertyId, String startPeriod) {
        if (propertyId == null || propertyId <= 0) {
            throw new IllegalArgumentException("propertyId无效");
        }
        YearMonth start = parsePeriod(startPeriod);
        // 预缴一年减免一个月：默认对第12个月账单减免一个月金额。
        for (int i = 0; i < 12; i++) {
            String period = start.plusMonths(i).format(PERIOD_FMT);
            generateBillsForPeriod(period, propertyId);
        }
        String discountPeriod = start.plusMonths(11).format(PERIOD_FMT);
        FeeBill bill = feeBillMapper.selectOne(new LambdaQueryWrapper<FeeBill>()
                .eq(FeeBill::getPropertyId, propertyId)
                .eq(FeeBill::getBillPeriod, discountPeriod)
                .last("limit 1"));
        if (bill == null) {
            throw new IllegalArgumentException("目标账单不存在");
        }
        if (bill.getStatus() != null && bill.getStatus() == 2) {
            throw new IllegalArgumentException("账单已缴清，不能再折扣");
        }
        if (safeMoney(bill.getDiscountAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return bill;
        }
        BigDecimal discount = safeMoney(bill.getAreaSnapshot())
                .multiply(safeMoney(bill.getUnitPrice()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal newAmount = safeMoney(bill.getAmount()).subtract(discount);
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            newAmount = BigDecimal.ZERO;
        }
        bill.setDiscountAmount(discount);
        bill.setAmount(newAmount);
        if (safeMoney(bill.getPaidAmount()).compareTo(newAmount) >= 0) {
            bill.setPaidAmount(newAmount);
            bill.setStatus(2);
        } else if (safeMoney(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            bill.setStatus(1);
        } else {
            bill.setStatus(0);
        }
        bill.setNeedPoints(toNeedPoints(newAmount));
        bill.setUpdateTime(LocalDateTime.now());
        feeBillMapper.updateById(bill);
        return bill;
    }

    public Map<String, Object> collectionRate(String billPeriod) {
        LambdaQueryWrapper<FeeBill> wrapper = new LambdaQueryWrapper<>();
        if (billPeriod != null && !billPeriod.isBlank()) {
            wrapper.eq(FeeBill::getBillPeriod, billPeriod.trim());
        }
        List<FeeBill> bills = feeBillMapper.selectList(wrapper);
        BigDecimal total = bills.stream()
                .map(FeeBill::getAmount)
                .map(this::safeMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = bills.stream()
                .map(FeeBill::getPaidAmount)
                .map(this::safeMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            rate = paid.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
        }
        return Map.of(
                "billPeriod", billPeriod == null ? "ALL" : billPeriod,
                "billCount", bills.size(),
                "totalAmount", total,
                "paidAmount", paid,
                "collectionRate", rate
        );
    }

    @Scheduled(cron = "${smartcommunity.fee.auto-generate-cron:0 0 9 23 * ?}", zone = "Asia/Shanghai")
    public void autoGenerateMonthlyBills() {
        String period = YearMonth.now().format(PERIOD_FMT);
        try {
            List<FeeBill> created = generateBillsForPeriod(period, null);
            log.info("Auto generated monthly fee bills. period={}, count={}", period, created.size());
        } catch (Exception ex) {
            log.error("Auto generate monthly fee bills failed. period={}", period, ex);
        }
    }

    private YearMonth parsePeriod(String billPeriod) {
        if (billPeriod == null || billPeriod.isBlank()) {
            throw new IllegalArgumentException("billPeriod不能为空，格式：yyyy-MM");
        }
        try {
            return YearMonth.parse(billPeriod.trim(), PERIOD_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("billPeriod格式错误，应为yyyy-MM");
        }
    }

    private BigDecimal safeArea(BigDecimal area) {
        if (area == null || area.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return area.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private int toNeedPoints(BigDecimal amount) {
        return amount == null ? 0 : amount.setScale(0, RoundingMode.UP).intValue();
    }
}
