package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.service.RealEstateArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/real-estate")
@RequiredArgsConstructor
public class RealEstateArchiveController {

    private final RealEstateArchiveService realEstateArchiveService;

    @GetMapping("/archive")
    public Result<Map<String, Object>> archive(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(realEstateArchiveService.archive(keyword, status));
    }
}
