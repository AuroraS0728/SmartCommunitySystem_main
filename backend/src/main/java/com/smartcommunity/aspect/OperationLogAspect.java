package com.smartcommunity.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcommunity.common.AuthContext;
import com.smartcommunity.common.Result;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger OP_LOGGER = LoggerFactory.getLogger("operationLogger");
    private final ObjectMapper objectMapper;

    @Around("within(com.smartcommunity.controller..*) && (" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public Object aroundWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = attrs == null ? "N/A" : attrs.getRequest().getMethod();
        String uri = attrs == null ? "N/A" : attrs.getRequest().getRequestURI();
        Long userId = AuthContext.getUserId();
        Integer role = AuthContext.getRole();
        String args = truncate(serializeArgs(joinPoint.getArgs()), 1200);

        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            Integer code = null;
            String message = "success";
            if (result instanceof Result<?> res) {
                code = res.getCode();
                message = res.getMessage();
            }
            OP_LOGGER.info("status=success method={} uri={} handler={} userId={} role={} code={} message={} costMs={} args={}",
                    method, uri, joinPoint.getSignature().toShortString(), userId, role, code, message, cost, args);
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            OP_LOGGER.error("status=error method={} uri={} handler={} userId={} role={} costMs={} error={} args={}",
                    method, uri, joinPoint.getSignature().toShortString(), userId, role, cost, ex.getMessage(), args);
            throw ex;
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        List<Object> safeArgs = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (arg == null) {
                safeArgs.add(null);
                continue;
            }
            if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
                safeArgs.add(arg.getClass().getSimpleName());
                continue;
            }
            if (arg instanceof MultipartFile file) {
                safeArgs.add("MultipartFile(" + file.getOriginalFilename() + ")");
                continue;
            }
            safeArgs.add(arg);
        }
        try {
            return objectMapper.writeValueAsString(safeArgs);
        } catch (JsonProcessingException e) {
            return "[unserializable-args]";
        }
    }

    private String truncate(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}
