package com.smartcommunity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.smartcommunity.entity.LostFound;
import com.smartcommunity.entity.SecondHand;
import com.smartcommunity.entity.UserImplicitProfile;
import com.smartcommunity.mapper.LostFoundMapper;
import com.smartcommunity.mapper.SecondHandMapper;
import com.smartcommunity.mapper.UserImplicitProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImplicitProfileService {

    private static final TypeReference<List<String>> KEYWORD_LIST_TYPE = new TypeReference<>() {
    };

    private final SecondHandMapper secondHandMapper;
    private final LostFoundMapper lostFoundMapper;
    private final UserImplicitProfileMapper userImplicitProfileMapper;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void extractRecentProfiles() {
        try {
            extractRecentProfilesNow();
        } catch (Exception ex) {
            log.warn("Implicit profile extraction failed", ex);
        }
    }

    public void extractRecentProfilesNow() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Map<Long, StringBuilder> userTexts = new LinkedHashMap<>();

        List<SecondHand> secondHands = secondHandMapper.selectList(new LambdaQueryWrapper<SecondHand>()
                .eq(SecondHand::getIsDeleted, 0)
                .ge(SecondHand::getCreateTime, since)
                .isNotNull(SecondHand::getUserId));
        for (SecondHand item : secondHands) {
            appendText(userTexts, item.getUserId(), item.getTitle());
        }

        List<LostFound> lostFounds = lostFoundMapper.selectList(new LambdaQueryWrapper<LostFound>()
                .eq(LostFound::getIsDeleted, 0)
                .ge(LostFound::getCreateTime, since)
                .isNotNull(LostFound::getUserId));
        for (LostFound item : lostFounds) {
            appendText(userTexts, item.getUserId(), item.getDescription());
        }

        for (Map.Entry<Long, StringBuilder> entry : userTexts.entrySet()) {
            List<String> keywords = extractKeywords(entry.getValue().toString());
            if (!keywords.isEmpty()) {
                upsertProfile(entry.getKey(), keywords);
            }
        }
    }

    public List<String> getKeywords(Long userId) {
        if (userId == null) {
            return List.of();
        }
        UserImplicitProfile profile = userImplicitProfileMapper.selectOne(new LambdaQueryWrapper<UserImplicitProfile>()
                .eq(UserImplicitProfile::getUserId, userId)
                .eq(UserImplicitProfile::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (profile == null || !StringUtils.hasText(profile.getKeywordsJson())) {
            return List.of();
        }
        try {
            return objectMapper.readValue(profile.getKeywordsJson(), KEYWORD_LIST_TYPE);
        } catch (Exception ex) {
            log.warn("Parse implicit profile keywords failed, userId={}", userId, ex);
            return List.of();
        }
    }

    private void appendText(Map<Long, StringBuilder> userTexts, Long userId, String text) {
        if (userId == null || !StringUtils.hasText(text)) {
            return;
        }
        userTexts.computeIfAbsent(userId, key -> new StringBuilder()).append(' ').append(text.trim());
    }

    private List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        try {
            Map<String, Integer> countMap = new HashMap<>();
            List<Term> terms = HanLP.segment(text);
            for (Term term : terms) {
                if (term == null || !StringUtils.hasText(term.word) || term.nature == null) {
                    continue;
                }
                String nature = term.nature.toString();
                String word = term.word.trim();
                if (("n".equals(nature) || "vn".equals(nature) || "an".equals(nature)) && word.length() >= 2) {
                    countMap.merge(word, 1, Integer::sum);
                }
            }
            return countMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                            .thenComparing(Map.Entry::getKey))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();
        } catch (Exception ex) {
            log.warn("HanLP implicit profile extraction failed, text={}", text, ex);
            return List.of();
        }
    }

    private void upsertProfile(Long userId, List<String> keywords) {
        try {
            UserImplicitProfile profile = userImplicitProfileMapper.selectOne(new LambdaQueryWrapper<UserImplicitProfile>()
                    .eq(UserImplicitProfile::getUserId, userId)
                    .eq(UserImplicitProfile::getIsDeleted, 0)
                    .last("LIMIT 1"));
            if (profile == null) {
                profile = new UserImplicitProfile();
                profile.setUserId(userId);
                profile.setCreateTime(LocalDateTime.now());
                profile.setIsDeleted(0);
            }
            profile.setKeywordsJson(objectMapper.writeValueAsString(keywords));
            profile.setUpdateTime(LocalDateTime.now());
            if (profile.getId() == null) {
                userImplicitProfileMapper.insert(profile);
            } else {
                userImplicitProfileMapper.updateById(profile);
            }
        } catch (Exception ex) {
            log.warn("Upsert implicit profile failed, userId={}, keywords={}", userId, keywords, ex);
        }
    }
}
