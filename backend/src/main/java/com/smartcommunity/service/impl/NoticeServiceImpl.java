package com.smartcommunity.service.impl;

import com.smartcommunity.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Override
    public String module() {
        return "NoticeService";
    }
}
