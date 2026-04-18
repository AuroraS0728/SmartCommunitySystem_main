package com.smartcommunity.service.impl;

import com.smartcommunity.service.WorkerService;
import org.springframework.stereotype.Service;

@Service
public class WorkerServiceImpl implements WorkerService {
    @Override
    public String module() {
        return "WorkerService";
    }
}
