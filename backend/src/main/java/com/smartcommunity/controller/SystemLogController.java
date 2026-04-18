package com.smartcommunity.controller;

import com.smartcommunity.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemLogController {

    @GetMapping("/logs")
    public Result<List<String>> logs(@RequestParam(defaultValue = "200") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 1000);
        Path logFile = Path.of("logs", "operation.log");
        if (!Files.exists(logFile)) {
            return Result.success(List.of());
        }
        Deque<String> tail = new ArrayDeque<>(safeLimit);
        try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (tail.size() == safeLimit) {
                    tail.removeFirst();
                }
                tail.addLast(line);
            }
        } catch (IOException e) {
            return Result.fail("read log failed: " + e.getMessage());
        }
        return Result.success(new ArrayList<>(tail));
    }
}
