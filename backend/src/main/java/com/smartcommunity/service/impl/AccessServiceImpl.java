package com.smartcommunity.service.impl;

import com.smartcommunity.service.AccessService;
import org.springframework.stereotype.Service;

@Service
public class AccessServiceImpl implements AccessService {
    @Override
    public String module() {
        return "AccessService";
    }
}
