package com.smartcommunity.service.impl;

import com.smartcommunity.service.StatisticsService;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Override
    public String module() {
        return "StatisticsService";
    }
}
