package com.zhitu.common.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${zhitu.jwt.secret}")
    private String secret;

    @Value("${zhitu.jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    @Value("${zhitu.jwt.refresh-token-expiration:2592000}")
    private long refreshTokenExpiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 access_token
     */
    public String generateAccessToken(Long userId, String role, String subRole, Long tenantId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString()); // 字符串传输，避免 JS 大数精度问题
        claims.put("role", role);
        if (subRole != null) claims.put("subRole", subRole);
        claims.put("tenantId", tenantId != null ? tenantId.toString() : "0");

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(getKey())
                .compact();
    }

    /**
     * 生成 refresh_token（随机 UUID，不含业务信息）
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .id(UUID.randomUUID().toString())
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 token 是否有效（不抛异常版本）
     */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 判断 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 从 token 中提取 userId
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * 从 token 中提取 role
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 获取 access_token 过期时间（秒）
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取 refresh_token 过期时间（秒）
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
