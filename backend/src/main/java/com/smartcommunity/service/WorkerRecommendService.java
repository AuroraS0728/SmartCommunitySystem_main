package com.smartcommunity.service;

import com.smartcommunity.mapper.WorkerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerRecommendService {

    private final WorkerMapper workerMapper;

    public Long recommendWorker(String category) {
        String normalizedCategory = normalizeCategory(category);
        if (!StringUtils.hasText(normalizedCategory)) {
            return null;
        }

        return selectFromUserStaffing(normalizedCategory);
    }

    private Long selectFromUserStaffing(String category) {
        try {
            return workerMapper.selectAvailableUserWorkerByCategory(category);
        } catch (Exception ex) {
            log.warn("Failed to recommend worker from worker_staffing, category={}", category, ex);
            return null;
        }
    }

    private String normalizeCategory(String category) {
        return category == null ? "" : category.trim();
    }
}
