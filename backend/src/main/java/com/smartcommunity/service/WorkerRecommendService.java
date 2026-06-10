package com.smartcommunity.service;

import com.smartcommunity.mapper.WorkerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerRecommendService {

    private final WorkerMapper workerMapper;

    /**
     * 智能派单入口。
     * 根据工单文本扩展出可能的维修类型，
     * 再去数据库中查找技能匹配、状态可用、当前任务量较少的维修人员。
     */
    public Long recommendWorker(String category) {
        for (String candidate : recommendCandidates(category)) {
            Long workerId = selectFromUserStaffing(candidate);
            if (workerId != null) {
                return workerId;
            }
        }
        return null;
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

    private List<String> recommendCandidates(String text) {
        String normalized = normalizeCategory(text);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);

        // 根据关键词扩展候选技能，用户填写内容不标准的问题。
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "plumbing", "water", "drain", "leak")
                || containsAny(normalized, "水", "漏", "下水", "管", "马桶", "地库排水")) {
            addAll(candidates, "水管漏水", "下水道堵塞", "马桶疏通", "plumber", "water", "drain", "水电维修");
        }
        if (containsAny(lower, "electric", "light", "power")
                || containsAny(normalized, "电", "灯", "照明", "插座", "线路")) {
            addAll(candidates, "电路跳闸", "灯具维修", "线路老化更换", "electric", "power", "水电维修");
        }
        if (containsAny(lower, "appliance", "air", "fridge", "washer")
                || containsAny(normalized, "空调", "冰箱", "洗衣机", "热水器", "家电")) {
            addAll(candidates, "家电维修", "appliance");
        }
        if (containsAny(lower, "security", "elevator", "public", "access", "door")
                || containsAny(normalized, "门禁", "电梯", "公共", "大门", "道闸", "监控")) {
            addAll(candidates, "智能设备", "专项服务", "其他", "outsource");
        }

        return new ArrayList<>(candidates);
    }

    private boolean containsAny(String text, String... needles) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String needle : needles) {
            if (StringUtils.hasText(needle) && text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private void addAll(Set<String> target, String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                target.add(value);
            }
        }
    }
}
