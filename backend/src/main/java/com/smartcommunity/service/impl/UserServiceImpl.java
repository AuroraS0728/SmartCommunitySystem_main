package com.smartcommunity.service.impl;

import com.smartcommunity.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public String module() {
        return "UserService";
    }
}
