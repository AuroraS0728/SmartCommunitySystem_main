package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import com.smartcommunity.service.RealtimeNotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/realtime")
@RequiredArgsConstructor
public class RealtimeController {

    private final RealtimeNotifyService realtimeNotifyService;

    @GetMapping("/stream")
    public SseEmitter stream() {
        return realtimeNotifyService.registerSse();
    }

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("ok");
    }
}
