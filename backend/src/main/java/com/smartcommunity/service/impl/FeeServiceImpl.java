package com.smartcommunity.service.impl;

import com.smartcommunity.service.FeeService;
import org.springframework.stereotype.Service;

@Service
public class FeeServiceImpl implements FeeService {
    @Override
    public String module() {
        return "FeeService";
    }
}
