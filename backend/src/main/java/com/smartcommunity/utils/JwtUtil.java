package com.smartcommunity.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:smart-community-secret-key-smart-community}")
    private String secret;

    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    private SecretKey key() {
        String fixed = secret;
        if (fixed.length() < 32) {
            fixed = fixed + "-padding-padding-padding-padding";
        }
        return Keys.hmacShaKeyFor(fixed.substring(0, 32).getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, Integer role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + Duration.ofSeconds(expireSeconds).toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public Integer getRole(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        Object role = claims.get("role");
        return role == null ? 1 : Integer.parseInt(role.toString());
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
