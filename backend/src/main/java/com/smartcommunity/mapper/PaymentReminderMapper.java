package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcommunity.dto.response.OverdueFeeBillReminderRow;
import com.smartcommunity.entity.PaymentReminder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentReminderMapper extends BaseMapper<PaymentReminder> {
    List<OverdueFeeBillReminderRow> selectOverdueFeeBillsForReminder();
}
