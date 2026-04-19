package com.smartcommunity.service;

import com.smartcommunity.config.NeighborModuleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentSafetyService {

    private final NeighborModuleConfig neighborModuleConfig;

    public void validateText(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String normalized = text.trim();
        List<String> words = neighborModuleConfig.getSensitiveWords();
        if (words == null || words.isEmpty()) {
            return;
        }
        for (String word : words) {
            if (word != null && !word.isBlank() && normalized.contains(word.trim())) {
                throw new IllegalArgumentException("内容包含敏感词，请修改后重试");
            }
        }
    }

    public void validateText(String... texts) {
        if (texts == null || texts.length == 0) {
            return;
        }
        for (String text : texts) {
            validateText(text);
        }
    }
}

