package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.dto.response.PointsRechargeRecordVO;
import com.smartcommunity.dto.response.PointsRecordVO;
import com.smartcommunity.entity.FeeBill;
import com.smartcommunity.entity.ParkingOrder;
import com.smartcommunity.entity.PointsConsumptionRecord;
import com.smartcommunity.entity.PointsRechargeRecord;
import com.smartcommunity.entity.RepairFeeBill;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.UserProperty;
import com.smartcommunity.mapper.FeeBillMapper;
import com.smartcommunity.mapper.ParkingOrderMapper;
import com.smartcommunity.mapper.PointsConsumptionRecordMapper;
import com.smartcommunity.mapper.PointsRechargeRecordMapper;
import com.smartcommunity.mapper.RepairFeeBillMapper;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.mapper.UserPropertyMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserMapper userMapper;
    private final UserPropertyMapper userPropertyMapper;
    private final FeeBillMapper feeBillMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final RepairFeeBillMapper repairFeeBillMapper;
    private final PointsRechargeRecordMapper pointsRechargeRecordMapper;
    private final PointsConsumptionRecordMapper pointsConsumptionRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public RechargeResult recharge(Long userId, Integer amount, Long operatorId, String remark) {
        if (operatorId == null || operatorId <= 0) {
            throw new IllegalArgumentException("operatorId invalid");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId invalid");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        User user = lockUser(userId);
        int before = safePoints(user.getPoints());
        int after = before + amount;
        user.setPoints(after);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        PointsRechargeRecord record = new PointsRechargeRecord();
        record.setUserId(userId);
        record.setOperatorId(operatorId);
        record.setAmount(amount);
        record.setBeforePoints(before);
        record.setAfterPoints(after);
        record.setRemark((remark == null || remark.isBlank()) ? "物业充值" : remark.trim());
        record.setCreateTime(LocalDateTime.now());
        pointsRechargeRecordMapper.insert(record);
        log.info("Points recharge success. userId={}, operatorId={}, amount={}, before={}, after={}",
                userId, operatorId, amount, before, after);
        return new RechargeResult(userId, amount, before, after);
    }

    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consume(Long userId, Integer businessType, Long businessId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId invalid");
        }
        if (businessType == null || businessType < 1 || businessType > 3) {
            throw new IllegalArgumentException("businessType range is 1~3");
        }
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("businessId invalid");
        }
        BusinessTarget target = resolveBusinessTarget(userId, businessType, businessId);

        User user = lockUser(userId);
        int before = safePoints(user.getPoints());
        if (before < target.getNeedPoints()) {
            throw new IllegalArgumentException("积分不足，请先充值");
        }
        int after = before - target.getNeedPoints();
        user.setPoints(after);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        completeBusiness(target);

        PointsConsumptionRecord record = new PointsConsumptionRecord();
        record.setUserId(userId);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setPoints(target.getNeedPoints());
        record.setBeforePoints(before);
        record.setAfterPoints(after);
        record.setCreateTime(LocalDateTime.now());
        pointsConsumptionRecordMapper.insert(record);
        log.info("Points consume success. userId={}, businessType={}, businessId={}, points={}, before={}, after={}",
                userId, businessType, businessId, target.getNeedPoints(), before, after);
        return new ConsumeResult(userId, businessType, businessId, target.getNeedPoints(), before, after);
    }

    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult consumeCustomBusiness(Long userId, Integer businessType, Long businessId, Integer needPoints) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId invalid");
        }
        if (businessType == null || businessType <= 0) {
            throw new IllegalArgumentException("businessType invalid");
        }
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("businessId invalid");
        }
        if (needPoints == null || needPoints <= 0) {
            throw new IllegalArgumentException("needPoints must be greater than 0");
        }

        User user = lockUser(userId);
        int before = safePoints(user.getPoints());
        if (before < needPoints) {
            throw new IllegalArgumentException("积分不足，请先充值");
        }
        int after = before - needPoints;
        user.setPoints(after);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        PointsConsumptionRecord record = new PointsConsumptionRecord();
        record.setUserId(userId);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setPoints(needPoints);
        record.setBeforePoints(before);
        record.setAfterPoints(after);
        record.setCreateTime(LocalDateTime.now());
        pointsConsumptionRecordMapper.insert(record);
        log.info("Points consume custom success. userId={}, businessType={}, businessId={}, points={}, before={}, after={}",
                userId, businessType, businessId, needPoints, before, after);
        return new ConsumeResult(userId, businessType, businessId, needPoints, before, after);
    }

    public Integer getBalance(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return safePoints(user.getPoints());
    }

    public List<PointsRecordVO> listUserRecords(Long userId) {
        List<PointsRecordVO> records = new ArrayList<>();

        List<PointsRechargeRecord> rechargeList = pointsRechargeRecordMapper.selectList(
                new LambdaQueryWrapper<PointsRechargeRecord>()
                        .eq(PointsRechargeRecord::getUserId, userId)
                        .orderByDesc(PointsRechargeRecord::getCreateTime)
                        .orderByDesc(PointsRechargeRecord::getId)
        );
        for (PointsRechargeRecord item : rechargeList) {
            PointsRecordVO vo = new PointsRecordVO();
            vo.setId(item.getId());
            vo.setRecordType(1);
            vo.setPoints(item.getAmount());
            vo.setBeforePoints(item.getBeforePoints());
            vo.setAfterPoints(item.getAfterPoints());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime());
            records.add(vo);
        }

        List<PointsConsumptionRecord> consumeList = pointsConsumptionRecordMapper.selectList(
                new LambdaQueryWrapper<PointsConsumptionRecord>()
                        .eq(PointsConsumptionRecord::getUserId, userId)
                        .orderByDesc(PointsConsumptionRecord::getCreateTime)
                        .orderByDesc(PointsConsumptionRecord::getId)
        );
        for (PointsConsumptionRecord item : consumeList) {
            PointsRecordVO vo = new PointsRecordVO();
            vo.setId(item.getId());
            vo.setRecordType(2);
            vo.setPoints(item.getPoints());
            vo.setBeforePoints(item.getBeforePoints());
            vo.setAfterPoints(item.getAfterPoints());
            vo.setBusinessType(item.getBusinessType());
            vo.setBusinessId(item.getBusinessId());
            vo.setRemark(businessTypeText(item.getBusinessType()));
            vo.setCreateTime(item.getCreateTime());
            records.add(vo);
        }

        records.sort(Comparator
                .comparing(PointsRecordVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(PointsRecordVO::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return records;
    }

    public List<PointsRechargeRecordVO> listRechargeRecords(String ownerName, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<PointsRechargeRecord> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(PointsRechargeRecord::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PointsRechargeRecord::getCreateTime, endTime);
        }
        if (ownerName != null && !ownerName.isBlank()) {
            String keyword = ownerName.trim();
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .and(w -> w.like(User::getNickname, keyword)
                                    .or()
                                    .like(User::getAccount, keyword)))
                    .stream()
                    .map(User::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(PointsRechargeRecord::getUserId, userIds);
        }
        wrapper.orderByDesc(PointsRechargeRecord::getCreateTime)
                .orderByDesc(PointsRechargeRecord::getId);

        List<PointsRechargeRecord> rechargeList = pointsRechargeRecordMapper.selectList(wrapper);
        if (rechargeList.isEmpty()) {
            return List.of();
        }
        Set<Long> allUserIds = rechargeList.stream()
                .flatMap(item -> List.of(item.getUserId(), item.getOperatorId()).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = toUserNameMap(allUserIds);

        return rechargeList.stream().map(item -> {
            PointsRechargeRecordVO vo = new PointsRechargeRecordVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setUserName(nameMap.getOrDefault(item.getUserId(), "-"));
            vo.setOperatorId(item.getOperatorId());
            vo.setOperatorName(nameMap.getOrDefault(item.getOperatorId(), "-"));
            vo.setAmount(item.getAmount());
            vo.setBeforePoints(item.getBeforePoints());
            vo.setAfterPoints(item.getAfterPoints());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private BusinessTarget resolveBusinessTarget(Long userId, Integer businessType, Long businessId) {
        if (businessType == 1) {
            FeeBill bill = feeBillMapper.selectOne(new LambdaQueryWrapper<FeeBill>()
                    .eq(FeeBill::getId, businessId)
                    .last("for update"));
            if (bill == null) {
                throw new IllegalArgumentException("fee bill not found");
            }
            UserProperty userProperty = userPropertyMapper.selectOne(new LambdaQueryWrapper<UserProperty>()
                    .eq(UserProperty::getUserId, userId)
                    .eq(UserProperty::getPropertyId, bill.getPropertyId())
                    .last("limit 1"));
            if (userProperty == null) {
                throw new IllegalArgumentException("no permission to pay this fee bill");
            }
            if (bill.getStatus() != null && bill.getStatus() == 2) {
                throw new IllegalArgumentException("fee bill already paid");
            }
            int needPoints = bill.getNeedPoints() != null && bill.getNeedPoints() > 0
                    ? bill.getNeedPoints()
                    : amountToPoints(bill.getAmount());
            return new BusinessTarget(businessType, businessId, needPoints, bill, null, null);
        }

        if (businessType == 2) {
            ParkingOrder order = parkingOrderMapper.selectOne(new LambdaQueryWrapper<ParkingOrder>()
                    .eq(ParkingOrder::getId, businessId)
                    .last("for update"));
            if (order == null) {
                throw new IllegalArgumentException("parking order not found");
            }
            if (!userId.equals(order.getUserId())) {
                throw new IllegalArgumentException("no permission to pay this parking order");
            }
            if (order.getStatus() != null && order.getStatus() == 1) {
                throw new IllegalArgumentException("parking order already paid");
            }
            int needPoints = amountToPoints(order.getAmount());
            return new BusinessTarget(businessType, businessId, needPoints, null, order, null);
        }

        RepairFeeBill repairBill = repairFeeBillMapper.selectOne(new LambdaQueryWrapper<RepairFeeBill>()
                .eq(RepairFeeBill::getId, businessId)
                .last("for update"));
        if (repairBill == null) {
            throw new IllegalArgumentException("repair fee bill not found");
        }
        if (!userId.equals(repairBill.getUserId())) {
            throw new IllegalArgumentException("no permission to pay this repair fee bill");
        }
        if (repairBill.getStatus() != null && repairBill.getStatus() == 1) {
            throw new IllegalArgumentException("repair fee bill already paid");
        }
        int needPoints = repairBill.getNeedPoints() != null && repairBill.getNeedPoints() > 0
                ? repairBill.getNeedPoints()
                : amountToPoints(repairBill.getAmount());
        return new BusinessTarget(businessType, businessId, needPoints, null, null, repairBill);
    }

    private void completeBusiness(BusinessTarget target) {
        LocalDateTime now = LocalDateTime.now();
        if (target.getBusinessType() == 1 && target.getFeeBill() != null) {
            FeeBill bill = target.getFeeBill();
            bill.setNeedPoints(target.getNeedPoints());
            bill.setPaidAmount(bill.getAmount());
            bill.setStatus(2);
            bill.setPaymentTime(now);
            bill.setTransactionId("POINTS_FEE_" + bill.getId() + "_" + System.currentTimeMillis());
            bill.setUpdateTime(now);
            feeBillMapper.updateById(bill);
            return;
        }
        if (target.getParkingOrder() != null) {
            ParkingOrder order = target.getParkingOrder();
            order.setStatus(1);
            order.setPaymentTime(now);
            order.setTransactionId("POINTS_PARK_" + order.getId() + "_" + System.currentTimeMillis());
            order.setUpdateTime(now);
            parkingOrderMapper.updateById(order);
            return;
        }
        if (target.getRepairFeeBill() != null) {
            RepairFeeBill bill = target.getRepairFeeBill();
            bill.setNeedPoints(target.getNeedPoints());
            bill.setPaidPoints(target.getNeedPoints());
            bill.setStatus(1);
            bill.setPaymentTime(now);
            bill.setTransactionId("POINTS_REPAIR_" + bill.getId() + "_" + System.currentTimeMillis());
            bill.setUpdateTime(now);
            repairFeeBillMapper.updateById(bill);
        }
    }

    private User lockUser(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .last("for update"));
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        return user;
    }

    private Map<Long, String> toUserNameMap(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds));
        Map<Long, String> result = new HashMap<>();
        for (User user : users) {
            String name = (user.getNickname() == null || user.getNickname().isBlank())
                    ? user.getAccount()
                    : user.getNickname();
            result.put(user.getId(), name == null ? "-" : name);
        }
        return result;
    }

    private int safePoints(Integer points) {
        return points == null ? 0 : points;
    }

    /**
     * 1 point = 1 CNY.
     * Amount may include decimals, use ceiling so points can fully cover the amount.
     */
    private int amountToPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return amount.setScale(0, RoundingMode.UP).intValue();
    }

    private String businessTypeText(Integer businessType) {
        if (businessType == null) {
            return "积分消费";
        }
        return switch (businessType) {
            case 1 -> "物业费缴纳";
            case 2 -> "停车费缴纳";
            case 3 -> "维修费用缴纳";
            case 4 -> "附加服务预约";
            default -> "积分消费";
        };
    }

    @Data
    @AllArgsConstructor
    private static class BusinessTarget {
        private Integer businessType;
        private Long businessId;
        private Integer needPoints;
        private FeeBill feeBill;
        private ParkingOrder parkingOrder;
        private RepairFeeBill repairFeeBill;
    }

    @Data
    @AllArgsConstructor
    public static class RechargeResult {
        private Long userId;
        private Integer amount;
        private Integer beforePoints;
        private Integer afterPoints;
    }

    @Data
    @AllArgsConstructor
    public static class ConsumeResult {
        private Long userId;
        private Integer businessType;
        private Long businessId;
        private Integer consumePoints;
        private Integer beforePoints;
        private Integer afterPoints;
    }
}

