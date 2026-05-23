package com.smartcommunity.config;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.entity.User;
import com.smartcommunity.mapper.UserMapper;
import com.smartcommunity.utils.JwtUtil;
import com.smartcommunity.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token = auth.substring(7);
        boolean blacklisted = false;
        try {
            blacklisted = redisUtil.hasKey("jwt:blacklist:" + token);
        } catch (Exception ignored) {
            // Allow local mock联调 without redis.
        }
        if (blacklisted || !jwtUtil.validateToken(token)) {
            response.setStatus(401);
            return false;
        }
        Long userId = jwtUtil.getUserId(token);
        Integer role = jwtUtil.getRole(token);
        if (role != null && role == 1) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getMustChangePassword() != null && user.getMustChangePassword() == 1) {
                response.setStatus(403);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write("{\"code\":403,\"message\":\"must change password\"}");
                } catch (Exception ignored) {
                    // keep 403 when response writer is unavailable
                }
                return false;
            }
        }
        AuthContext.set(userId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
