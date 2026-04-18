package com.smartcommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import com.smartcommunity.dto.request.InviteVisitorReq;
import com.smartcommunity.dto.request.RenewInviteReq;
import com.smartcommunity.dto.request.VerifyVisitorInviteReq;
import com.smartcommunity.dto.request.VisitorBlacklistReq;
import com.smartcommunity.dto.request.VisitorDynamicTokenReq;
import com.smartcommunity.dto.request.VerifyVisitorTokenReq;
import com.smartcommunity.dto.response.VisitorHistoryResp;
import com.smartcommunity.dto.response.VisitorInviteResp;
import com.smartcommunity.dto.response.VisitorNotifyResp;
import com.smartcommunity.entity.AccessToken;
import com.smartcommunity.entity.VisitorBlacklist;
import com.smartcommunity.entity.VisitorInvite;
import com.smartcommunity.entity.VisitorNotify;
import com.smartcommunity.mapper.AccessTokenMapper;
import com.smartcommunity.mapper.VisitorBlacklistMapper;
import com.smartcommunity.mapper.VisitorInviteMapper;
import com.smartcommunity.mapper.VisitorNotifyMapper;
import com.smartcommunity.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/access")
@RequiredArgsConstructor
public class AccessController {

    private static final SecureRandom CODE_RANDOM = new SecureRandom();
    private static final char[] CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final List<DateTimeFormatter> TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    private static final long DYNAMIC_TOKEN_EXPIRE_SECONDS = 30L;
    private static final long DYNAMIC_TOKEN_CACHE_SECONDS = 35L;
    private static final String DYNAMIC_TOKEN_KEY_PREFIX = "visitor:dynamic:token:";
    private static final String DYNAMIC_LATEST_KEY_PREFIX = "visitor:dynamic:latest:";

    private final AccessTokenMapper accessTokenMapper;
    private final VisitorInviteMapper visitorInviteMapper;
    private final VisitorBlacklistMapper visitorBlacklistMapper;
    private final VisitorNotifyMapper visitorNotifyMapper;
    private final RedisUtil redisUtil;

    @PostMapping("/qrcode")
    public Result<Map<String, Object>> generateQr() {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        AccessToken at = new AccessToken();
        at.setUserId(AuthContext.getUserId());
        at.setToken(token);
        at.setStatus(1);
        at.setExpireTime(now.plusSeconds(30));
        at.setCreateTime(now);
        at.setUpdateTime(now);
        at.setIsDeleted(0);
        accessTokenMapper.insert(at);

        return Result.success(Map.of(
                "token", token,
                "expireSeconds", 30,
                "qrContent", "access://token/" + token
        ));
    }

    @PostMapping("/invite")
    public Result<VisitorInviteResp> invite(@RequestBody InviteVisitorReq req) {
        if (isBlank(req.getVisitorName()) || isBlank(req.getVisitorPhone())) {
            return Result.fail("visitorName and visitorPhone are required");
        }

        String phone = normalizePhone(req.getVisitorPhone());
        if (isBlacklisted(phone)) {
            return Result.fail("该手机号已被物业拉黑，无法生成邀请");
        }

        LocalDateTime now = LocalDateTime.now();
        String validityType = normalizeValidityType(req.getValidityType());
        LocalDateTime visitTime = parseOptionalDateTime(req.getVisitTime(), now);
        LocalDateTime expireTime = resolveExpireTime(validityType, req.getCustomExpireTime(), now);
        if (!expireTime.isAfter(visitTime)) {
            return Result.fail("有效期结束时间必须晚于来访时间");
        }

        VisitorInvite invite = new VisitorInvite();
        invite.setHostUserId(AuthContext.getUserId());
        invite.setVisitorName(req.getVisitorName().trim());
        invite.setVisitorPhone(phone);
        invite.setCode(generateUniqueCode());
        invite.setValidityType(validityType);
        invite.setVisitTime(visitTime);
        invite.setExpireTime(expireTime);
        invite.setMaxUses(resolveMaxUses(validityType, req.getMaxUses()));
        invite.setUsedCount(0);
        invite.setShareLink(buildShareLink(invite.getCode()));
        invite.setCreateTime(now);
        invite.setUpdateTime(now);
        invite.setIsDeleted(0);
        visitorInviteMapper.insert(invite);
        return Result.success(toInviteResp(invite, now));
    }

    @PostMapping("/invite/dynamic-token")
    public Result<Map<String, Object>> issueDynamicToken(@RequestBody VisitorDynamicTokenReq req) {
        if (isBlank(req.getCode())) {
            return Result.fail("code is required");
        }
        VisitorInvite invite = findInviteByCode(req.getCode());
        if (invite == null) {
            return Result.fail("邀请码不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String error = validateInviteUsable(invite, null, now, true);
        if (error != null) {
            return Result.fail(error);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisUtil.set(dynamicTokenKey(token), String.valueOf(invite.getId()), DYNAMIC_TOKEN_CACHE_SECONDS);
        redisUtil.set(dynamicLatestKey(invite.getId()), token, DYNAMIC_TOKEN_CACHE_SECONDS);

        return Result.success(Map.of(
                "token", token,
                "expireSeconds", DYNAMIC_TOKEN_EXPIRE_SECONDS,
                "refreshIntervalSeconds", DYNAMIC_TOKEN_EXPIRE_SECONDS,
                "qrContent", buildDynamicQrContent(token)
        ));
    }

    @PostMapping("/verify-token")
    public Result<Map<String, Object>> verifyDynamicToken(@RequestBody VerifyVisitorTokenReq req) {
        if (isBlank(req.getToken())) {
            return Result.fail("token is required");
        }

        String token = req.getToken().trim();
        String inviteIdText = redisUtil.get(dynamicTokenKey(token));
        if (isBlank(inviteIdText)) {
            return Result.fail("动态二维码已失效，请刷新后重试");
        }

        Long inviteId;
        try {
            inviteId = Long.parseLong(inviteIdText);
        } catch (NumberFormatException e) {
            return Result.fail("动态二维码无效");
        }

        VisitorInvite invite = visitorInviteMapper.selectById(inviteId);
        if (invite == null) {
            return Result.fail("邀请记录不存在");
        }

        String latestToken = redisUtil.get(dynamicLatestKey(inviteId));
        if (isBlank(latestToken) || !token.equals(latestToken)) {
            return Result.fail("二维码已刷新，请使用最新二维码");
        }

        LocalDateTime now = LocalDateTime.now();
        String error = validateInviteUsable(invite, null, now, true);
        if (error != null) {
            return Result.fail(error);
        }

        Map<String, Object> result = consumeInviteAndNotify(invite, now);
        redisUtil.delete(dynamicTokenKey(token));
        return Result.success(result);
    }

    @PostMapping("/verify-invite")
    public Result<Map<String, Object>> verifyInvite(@RequestBody VerifyVisitorInviteReq req) {
        if (isBlank(req.getCode())) {
            return Result.fail("code is required");
        }
        VisitorInvite invite = findInviteByCode(req.getCode());
        if (invite == null) {
            return Result.fail("邀请码不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String error = validateInviteUsable(invite, req.getVisitorPhone(), now, true);
        if (error != null) {
            return Result.fail(error);
        }

        return Result.success(consumeInviteAndNotify(invite, now));
    }

    @GetMapping("/visitor-records")
    public Result<List<VisitorInviteResp>> visitorRecords() {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || (role != 1 && role != 2)) {
            return Result.fail("无权限访问访客记录");
        }

        LambdaQueryWrapper<VisitorInvite> wrapper = new LambdaQueryWrapper<VisitorInvite>()
                .orderByDesc(VisitorInvite::getId);
        if (role == 1) {
            wrapper.eq(VisitorInvite::getHostUserId, userId);
        }

        LocalDateTime now = LocalDateTime.now();
        List<VisitorInviteResp> records = visitorInviteMapper.selectList(wrapper)
                .stream()
                .map(invite -> toInviteResp(invite, now))
                .collect(Collectors.toList());
        return Result.success(records);
    }

    @GetMapping("/my-visitors")
    public Result<List<VisitorHistoryResp>> myVisitors() {
        if (!isOwner()) {
            return Result.fail("仅业主可查看常用访客");
        }
        List<VisitorInvite> invites = visitorInviteMapper.selectList(new LambdaQueryWrapper<VisitorInvite>()
                .eq(VisitorInvite::getHostUserId, AuthContext.getUserId())
                .orderByDesc(VisitorInvite::getId));
        Map<String, VisitorHistoryResp> grouped = new LinkedHashMap<>();
        for (VisitorInvite invite : invites) {
            String phone = normalizePhone(invite.getVisitorPhone());
            if (isBlank(phone)) {
                phone = "UNKNOWN-" + invite.getId();
            }
            VisitorHistoryResp item = grouped.get(phone);
            if (item == null) {
                item = new VisitorHistoryResp();
                item.setVisitorName(invite.getVisitorName());
                item.setVisitorPhone(normalizePhone(invite.getVisitorPhone()));
                item.setLastInviteTime(invite.getCreateTime());
                item.setLastVisitTime(invite.getUsedTime());
                item.setTotalInviteCount(0);
                grouped.put(phone, item);
            }
            item.setTotalInviteCount(item.getTotalInviteCount() + 1);
            if (item.getLastVisitTime() == null && invite.getUsedTime() != null) {
                item.setLastVisitTime(invite.getUsedTime());
            }
        }
        return Result.success(new ArrayList<>(grouped.values()));
    }

    @PostMapping("/invite/{id}/renew")
    public Result<VisitorInviteResp> renewInvite(@PathVariable Long id, @RequestBody RenewInviteReq req) {
        VisitorInvite invite = visitorInviteMapper.selectById(id);
        if (invite == null) {
            return Result.fail("invite not found");
        }
        if (!canManageInvite(invite)) {
            return Result.fail("无权限续期该邀请");
        }
        if (isBlacklisted(invite.getVisitorPhone())) {
            return Result.fail("该访客手机号已被拉黑，无法续期");
        }

        LocalDateTime now = LocalDateTime.now();
        String validityType = normalizeValidityType(
                isBlank(req.getValidityType()) ? invite.getValidityType() : req.getValidityType()
        );
        LocalDateTime visitTime = parseOptionalDateTime(
                req.getVisitTime(),
                invite.getVisitTime() == null ? now : invite.getVisitTime()
        );
        LocalDateTime expireTime = resolveExpireTime(validityType, req.getCustomExpireTime(), now);
        if (!expireTime.isAfter(visitTime)) {
            return Result.fail("有效期结束时间必须晚于来访时间");
        }

        invite.setCode(generateUniqueCode());
        invite.setValidityType(validityType);
        invite.setVisitTime(visitTime);
        invite.setExpireTime(expireTime);
        invite.setMaxUses(resolveMaxUses(validityType, req.getMaxUses() == null ? invite.getMaxUses() : req.getMaxUses()));
        invite.setUsedCount(0);
        invite.setUsedTime(null);
        invite.setShareLink(buildShareLink(invite.getCode()));
        invite.setUpdateTime(now);
        visitorInviteMapper.updateById(invite);
        return Result.success(toInviteResp(invite, now));
    }

    @GetMapping("/owner-notifications")
    public Result<List<VisitorNotifyResp>> ownerNotifications() {
        if (!isOwner()) {
            return Result.fail("仅业主可查看通知");
        }
        List<VisitorNotifyResp> list = visitorNotifyMapper.selectList(new LambdaQueryWrapper<VisitorNotify>()
                        .eq(VisitorNotify::getHostUserId, AuthContext.getUserId())
                        .orderByDesc(VisitorNotify::getId)
                        .last("limit 50"))
                .stream()
                .map(this::toNotifyResp)
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @PutMapping("/owner-notifications/{id}/read")
    public Result<Void> readNotification(@PathVariable Long id) {
        if (!isOwner()) {
            return Result.fail("仅业主可操作通知");
        }
        VisitorNotify notify = visitorNotifyMapper.selectById(id);
        if (notify == null || !AuthContext.getUserId().equals(notify.getHostUserId())) {
            return Result.fail("通知不存在");
        }
        notify.setReadFlag(1);
        notify.setUpdateTime(LocalDateTime.now());
        visitorNotifyMapper.updateById(notify);
        return Result.success("ok", null);
    }

    @GetMapping("/blacklist")
    public Result<List<VisitorBlacklist>> blacklist() {
        if (!isPropertyAdmin()) {
            return Result.fail("仅物业可查看黑名单");
        }
        List<VisitorBlacklist> list = visitorBlacklistMapper.selectList(new LambdaQueryWrapper<VisitorBlacklist>()
                .orderByDesc(VisitorBlacklist::getId));
        return Result.success(list);
    }

    @PostMapping("/blacklist")
    public Result<VisitorBlacklist> addBlacklist(@RequestBody VisitorBlacklistReq req) {
        if (!isPropertyAdmin()) {
            return Result.fail("仅物业可管理黑名单");
        }
        if (isBlank(req.getPhone())) {
            return Result.fail("phone is required");
        }
        String phone = normalizePhone(req.getPhone());
        VisitorBlacklist existing = visitorBlacklistMapper.selectOne(new LambdaQueryWrapper<VisitorBlacklist>()
                .eq(VisitorBlacklist::getPhone, phone)
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setReason(req.getReason());
            existing.setCreatedBy(AuthContext.getUserId());
            existing.setUpdateTime(now);
            existing.setIsDeleted(0);
            visitorBlacklistMapper.updateById(existing);
            return Result.success(existing);
        }

        VisitorBlacklist item = new VisitorBlacklist();
        item.setPhone(phone);
        item.setReason(req.getReason());
        item.setCreatedBy(AuthContext.getUserId());
        item.setCreateTime(now);
        item.setUpdateTime(now);
        item.setIsDeleted(0);
        visitorBlacklistMapper.insert(item);
        return Result.success(item);
    }

    @DeleteMapping("/blacklist/{id}")
    public Result<Void> removeBlacklist(@PathVariable Long id) {
        if (!isPropertyAdmin()) {
            return Result.fail("仅物业可管理黑名单");
        }
        visitorBlacklistMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    @DeleteMapping("/invite/{id}")
    public Result<Void> deleteInvite(@PathVariable Long id) {
        VisitorInvite invite = visitorInviteMapper.selectById(id);
        if (invite == null) {
            return Result.fail("invite not found");
        }
        if (!canManageInvite(invite)) {
            return Result.fail("无权限删除该邀请");
        }
        visitorInviteMapper.deleteById(id);
        return Result.success("deleted", null);
    }

    private VisitorInvite findInviteByCode(String code) {
        String normCode = code == null ? null : code.trim().toUpperCase();
        if (isBlank(normCode)) {
            return null;
        }
        return visitorInviteMapper.selectOne(new LambdaQueryWrapper<VisitorInvite>()
                .eq(VisitorInvite::getCode, normCode)
                .last("limit 1"));
    }

    private String validateInviteUsable(VisitorInvite invite, String requestVisitorPhone, LocalDateTime now, boolean checkVisitTime) {
        if (invite == null) {
            return "邀请码不存在";
        }
        if (isBlacklisted(invite.getVisitorPhone())) {
            return "该访客已被拉黑，无法通行";
        }
        if (!isBlank(requestVisitorPhone)
                && !normalizePhone(requestVisitorPhone).equals(normalizePhone(invite.getVisitorPhone()))) {
            return "访客手机号与邀请码不匹配";
        }
        if (checkVisitTime && invite.getVisitTime() != null && now.isBefore(invite.getVisitTime())) {
            return "来访时间未到，暂不可核验";
        }
        if (invite.getExpireTime() != null && now.isAfter(invite.getExpireTime())) {
            return "邀请码已过期";
        }
        int usedCount = invite.getUsedCount() == null ? 0 : invite.getUsedCount();
        int maxUses = invite.getMaxUses() == null ? 1 : invite.getMaxUses();
        if (usedCount >= maxUses) {
            return "邀请码已使用";
        }
        return null;
    }

    private Map<String, Object> consumeInviteAndNotify(VisitorInvite invite, LocalDateTime now) {
        int usedCount = invite.getUsedCount() == null ? 0 : invite.getUsedCount();
        invite.setUsedCount(usedCount + 1);
        invite.setUsedTime(now);
        invite.setUpdateTime(now);
        visitorInviteMapper.updateById(invite);

        String notifyContent = "您的访客【" + invite.getVisitorName() + "】已进入小区";
        VisitorNotify notify = new VisitorNotify();
        notify.setHostUserId(invite.getHostUserId());
        notify.setInviteId(invite.getId());
        notify.setVisitorName(invite.getVisitorName());
        notify.setContent(notifyContent);
        notify.setReadFlag(0);
        notify.setCreateTime(now);
        notify.setUpdateTime(now);
        notify.setIsDeleted(0);
        visitorNotifyMapper.insert(notify);

        return Map.of(
                "verified", true,
                "inviteId", invite.getId(),
                "status", calcStatus(invite, now),
                "message", notifyContent
        );
    }

    private boolean canManageInvite(VisitorInvite invite) {
        Integer role = AuthContext.getRole();
        Long userId = AuthContext.getUserId();
        if (role == null || userId == null || invite == null) {
            return false;
        }
        if (role == 2) {
            return true;
        }
        return role == 1 && userId.equals(invite.getHostUserId());
    }

    private boolean isOwner() {
        Integer role = AuthContext.getRole();
        return role != null && role == 1;
    }

    private boolean isPropertyAdmin() {
        Integer role = AuthContext.getRole();
        return role != null && role == 2;
    }

    private boolean isBlacklisted(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        return visitorBlacklistMapper.selectCount(new LambdaQueryWrapper<VisitorBlacklist>()
                .eq(VisitorBlacklist::getPhone, normalizePhone(phone))) > 0;
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 20; i++) {
            String code = randomCode(6);
            Long count = visitorInviteMapper.selectCount(new LambdaQueryWrapper<VisitorInvite>()
                    .eq(VisitorInvite::getCode, code));
            if (count == null || count == 0) {
                return code;
            }
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CODE_CHARS[CODE_RANDOM.nextInt(CODE_CHARS.length)]);
        }
        return sb.toString();
    }

    private String normalizeValidityType(String raw) {
        if (isBlank(raw)) {
            return "SINGLE_2H";
        }
        String val = raw.trim().toUpperCase();
        if ("SINGLE".equals(val) || "ONCE".equals(val)) {
            return "SINGLE_2H";
        }
        if ("TODAY".equals(val)) {
            return "TODAY_END";
        }
        if ("CUSTOM".equals(val) || "SINGLE_2H".equals(val) || "TODAY_END".equals(val)) {
            return val;
        }
        throw new IllegalArgumentException("invalid validityType");
    }

    private int resolveMaxUses(String validityType, Integer reqMaxUses) {
        if ("SINGLE_2H".equals(validityType)) {
            return 1;
        }
        int maxUses = reqMaxUses == null ? 1 : reqMaxUses;
        if (maxUses < 1) {
            maxUses = 1;
        }
        if (maxUses > 20) {
            maxUses = 20;
        }
        return maxUses;
    }

    private LocalDateTime resolveExpireTime(String validityType, String customExpireText, LocalDateTime now) {
        LocalDateTime expireTime;
        switch (validityType) {
            case "SINGLE_2H":
                expireTime = now.plusHours(2);
                break;
            case "TODAY_END":
                expireTime = LocalDate.now().atTime(23, 59, 59);
                break;
            case "CUSTOM":
                if (isBlank(customExpireText)) {
                    throw new IllegalArgumentException("customExpireTime is required when validityType=CUSTOM");
                }
                expireTime = parseDateTime(customExpireText);
                break;
            default:
                throw new IllegalArgumentException("invalid validityType");
        }
        if (!expireTime.isAfter(now)) {
            throw new IllegalArgumentException("有效期结束时间必须晚于当前时间");
        }
        return expireTime;
    }

    private LocalDateTime parseOptionalDateTime(String text, LocalDateTime fallback) {
        if (isBlank(text)) {
            return fallback;
        }
        return parseDateTime(text);
    }

    private LocalDateTime parseDateTime(String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("invalid datetime");
        }
        String value = text.trim().replace("T", " ");
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        if (value.length() == 10) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("时间格式错误，支持 yyyy-MM-dd HH:mm:ss / yyyy-MM-dd HH:mm");
    }

    private String calcStatus(VisitorInvite invite, LocalDateTime now) {
        int usedCount = invite.getUsedCount() == null ? 0 : invite.getUsedCount();
        int maxUses = invite.getMaxUses() == null ? 1 : invite.getMaxUses();
        if (usedCount >= maxUses) {
            return "USED";
        }
        if (invite.getExpireTime() != null && now.isAfter(invite.getExpireTime())) {
            return "EXPIRED";
        }
        return "UNUSED";
    }

    private String statusText(String status) {
        switch (status) {
            case "USED":
                return "已使用";
            case "EXPIRED":
                return "已过期";
            default:
                return "未使用";
        }
    }

    private VisitorInviteResp toInviteResp(VisitorInvite invite, LocalDateTime now) {
        String status = calcStatus(invite, now);
        VisitorInviteResp resp = new VisitorInviteResp();
        resp.setId(invite.getId());
        resp.setHostUserId(invite.getHostUserId());
        resp.setVisitorName(invite.getVisitorName());
        resp.setVisitorPhone(invite.getVisitorPhone());
        resp.setCode(invite.getCode());
        resp.setValidityType(invite.getValidityType());
        resp.setVisitTime(invite.getVisitTime());
        resp.setExpireTime(invite.getExpireTime());
        resp.setMaxUses(invite.getMaxUses());
        resp.setUsedCount(invite.getUsedCount());
        resp.setUsedTime(invite.getUsedTime());
        resp.setStatus(status);
        resp.setStatusText(statusText(status));
        resp.setCanRenew("EXPIRED".equals(status));
        resp.setCreateTime(invite.getCreateTime());
        if (invite.getExpireTime() == null || now.isAfter(invite.getExpireTime())) {
            resp.setCountdownSeconds(0L);
        } else {
            resp.setCountdownSeconds(Duration.between(now, invite.getExpireTime()).getSeconds());
        }
        resp.setQrContent(buildInviteQrContent(invite.getCode()));
        resp.setShareLink(isBlank(invite.getShareLink()) ? buildShareLink(invite.getCode()) : invite.getShareLink());
        return resp;
    }

    private VisitorNotifyResp toNotifyResp(VisitorNotify notify) {
        VisitorNotifyResp resp = new VisitorNotifyResp();
        resp.setId(notify.getId());
        resp.setInviteId(notify.getInviteId());
        resp.setContent(notify.getContent());
        resp.setReadFlag(notify.getReadFlag());
        resp.setCreateTime(notify.getCreateTime());
        return resp;
    }

    private String buildInviteQrContent(String code) {
        return "smartcommunity://visitor/verify?code=" + code;
    }

    private String buildDynamicQrContent(String token) {
        return "smartcommunity://visitor/dynamic?token=" + token;
    }

    private String buildShareLink(String code) {
        return "/pages/door/verify?code=" + code;
    }

    private String dynamicTokenKey(String token) {
        return DYNAMIC_TOKEN_KEY_PREFIX + token;
    }

    private String dynamicLatestKey(Long inviteId) {
        return DYNAMIC_LATEST_KEY_PREFIX + inviteId;
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("\\s+", "");
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }
}
