package com.smartcommunity.service.impl;

import com.smartcommunity.service.RepairService;
import org.springframework.stereotype.Service;

@Service
public class RepairServiceImpl implements RepairService {
    @Override
    public String module() {
        return "RepairService";
    }
}
