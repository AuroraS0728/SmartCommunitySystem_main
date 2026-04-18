package com.smartcommunity.config;

import com.smartcommunity.common.AuthContext;
import com.smartcommunity.utils.JwtUtil;
import com.smartcommunity.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
        AuthContext.set(userId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
