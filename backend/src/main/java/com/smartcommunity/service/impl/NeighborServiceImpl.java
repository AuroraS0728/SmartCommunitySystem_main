package com.smartcommunity.service.impl;

import com.smartcommunity.service.NeighborService;
import org.springframework.stereotype.Service;

@Service
public class NeighborServiceImpl implements NeighborService {
    @Override
    public String module() {
        return "NeighborService";
    }
}
