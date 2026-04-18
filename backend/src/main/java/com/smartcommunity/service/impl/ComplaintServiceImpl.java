package com.smartcommunity.service.impl;

import com.smartcommunity.service.ComplaintService;
import org.springframework.stereotype.Service;

@Service
public class ComplaintServiceImpl implements ComplaintService {
    @Override
    public String module() {
        return "ComplaintService";
    }
}
