package com.smartcommunity.service.impl;

import com.smartcommunity.service.HouseService;
import org.springframework.stereotype.Service;

@Service
public class HouseServiceImpl implements HouseService {
    @Override
    public String module() {
        return "HouseService";
    }
}
