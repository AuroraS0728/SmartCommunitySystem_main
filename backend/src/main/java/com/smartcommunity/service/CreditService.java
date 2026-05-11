package com.smartcommunity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcommunity.entity.CreditLog;

public interface CreditService {
    void changeCredit(Long userId, int delta, String reason);

    Page<CreditLog> getCreditLog(Long userId, int page, int size);
}
