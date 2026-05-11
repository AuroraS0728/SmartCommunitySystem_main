package com.smartcommunity.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OverdueFeeBillReminderRow {
    private Long feeBillId;
    private Long propertyId;
    private Long userId;
    private String ownerName;
    private String billPeriod;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDateTime dueDate;
}
