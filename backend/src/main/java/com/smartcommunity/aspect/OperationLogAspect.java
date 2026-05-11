package com.smartcommunity.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger OP_LOGGER = LoggerFactory.getLogger("operationLogger");
    private static final long SLOW_REQUEST_MS = 800L;
    private static final int MAX_LOG_STRING_LENGTH = 160;
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
            String handler = joinPoint.getSignature().toShortString();
            if (cost >= SLOW_REQUEST_MS) {
                OP_LOGGER.warn("status=success slow=true method={} uri={} handler={} userId={} role={} code={} message={} costMs={} args={}",
                        method, uri, handler, userId, role, code, message, cost, args);
            } else {
                OP_LOGGER.info("status=success method={} uri={} handler={} userId={} role={} code={} message={} costMs={} args={}",
                        method, uri, handler, userId, role, code, message, cost, args);
            }
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
                safeArgs.add("MultipartFile(size=" + file.getSize() + ")");
                continue;
            }
            safeArgs.add(arg);
        }
        try {
            return objectMapper.writeValueAsString(maskSensitiveData(objectMapper.valueToTree(safeArgs)));
        } catch (JsonProcessingException e) {
            return "[unserializable-args]";
        } catch (IllegalArgumentException e) {
            return "[unserializable-args]";
        }
    }

    private JsonNode maskSensitiveData(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitiveKey(field.getKey())) {
                    objectNode.put(field.getKey(), "[REDACTED]");
                } else {
                    objectNode.set(field.getKey(), maskSensitiveData(field.getValue()));
                }
            }
            return objectNode;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, maskSensitiveData(arrayNode.get(i)));
            }
            return arrayNode;
        }
        if (node instanceof TextNode textNode) {
            return TextNode.valueOf(sanitizeText(textNode.asText()));
        }
        return node;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("authorization")
                || normalized.contains("secret")
                || normalized.contains("imagebase64")
                || normalized.contains("base64")
                || normalized.contains("openid")
                || normalized.contains("unionid")
                || normalized.contains("phone")
                || normalized.contains("mobile")
                || normalized.contains("email")
                || normalized.contains("idcard")
                || normalized.contains("address");
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("Bearer ")
                || trimmed.startsWith("data:image/")
                || (trimmed.split("\\.").length == 3 && trimmed.length() > 80)) {
            return "[REDACTED]";
        }
        if (text.length() > MAX_LOG_STRING_LENGTH) {
            return text.substring(0, MAX_LOG_STRING_LENGTH) + "...[truncated length=" + text.length() + "]";
        }
        return text;
    }

    private String truncate(String value, int maxLen) {
        if (value == null || value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}
