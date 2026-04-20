package com.smartcommunity.aspect;

import com.smartcommunity.common.Result;
import com.smartcommunity.common.StatusCode;
import com.smartcommunity.service.LocalCacheService;
import com.smartcommunity.service.RealtimeNotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class DataChangeAspect {

    private final RealtimeNotifyService realtimeNotifyService;
    private final LocalCacheService localCacheService;

    @Around("within(com.smartcommunity.controller..*) && (" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object afterWrite(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (!(result instanceof Result<?> res) || res.getCode() == null || res.getCode() != StatusCode.SUCCESS) {
            return result;
        }
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return result;
        }
        HttpServletRequest request = attrs.getRequest();
        MutationRoute route = resolveRoute(request.getRequestURI());
        if (route == null) {
            return result;
        }

        for (String cachePrefix : route.cachePrefixes()) {
            localCacheService.evictByPrefix(cachePrefix);
        }
        if (route.evictOverview()) {
            localCacheService.evict("statistics:overview");
        }
        realtimeNotifyService.publishDataChanged(route.topic(), "updated", request.getRequestURI());
        return result;
    }

    private MutationRoute resolveRoute(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }
        if (uri.startsWith("/api/house")) {
            return new MutationRoute("house", List.of("house:list:", "user:properties:"), true);
        }
        if (uri.startsWith("/api/notice")) {
            return new MutationRoute("notice", List.of("notice:list"), true);
        }
        if (uri.startsWith("/api/repair")) {
            return new MutationRoute("repair", List.of("repair:list:", "fee:subjects:"), true);
        }
        if (uri.startsWith("/api/fee") || uri.startsWith("/api/parking")) {
            return new MutationRoute("fee", List.of("fee:subjects:", "fee:bills:", "parking:orders:"), true);
        }
        if (uri.startsWith("/api/user")) {
            return new MutationRoute("user", List.of("user:me:", "user:properties:"), true);
        }
        if (uri.startsWith("/api/worker")) {
            return new MutationRoute("worker", List.of("repair:list:"), true);
        }
        return null;
    }

    private record MutationRoute(String topic, List<String> cachePrefixes, boolean evictOverview) {
    }
}
