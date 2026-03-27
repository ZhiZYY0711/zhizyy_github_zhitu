package com.zhitu.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关全局认证过滤器
 * 1. 白名单直接放行
 * 2. 验证 Bearer Token 签名
 * 3. 验证 Redis 中 token 是否有效（未被吊销）
 * 4. 将用户信息写入请求头转发给下游
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String TOKEN_KEY_PREFIX = "token:access:";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${zhitu.jwt.secret}")
    private String jwtSecret;

    private final GatewaySecurityProperties securityProperties;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return -100; // 最高优先级
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("AuthFilter 处理请求路径: {}", path);

        // 白名单放行
        if (isWhiteListed(path)) {
            log.debug("白名单路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        log.debug("非白名单路径，需要验证 token: {}", path);

        // 提取 token
        String token = extractToken(exchange.getRequest());
        if (token == null) {
            log.warn("缺少 Authorization 请求头，路径: {}", path);
            return unauthorized(exchange, "缺少 Authorization 请求头");
        }

        // 验证 token 签名
        Claims claims;
        try {
            claims = parseToken(token);
            log.debug("Token 签名验证成功，userId: {}", claims.getSubject());
        } catch (JwtException e) {
            log.warn("Token 签名验证失败: {}", e.getMessage());
            return unauthorized(exchange, "Token 无效或已过期");
        }

        String userId = claims.getSubject();
        log.debug("开始验证 Redis 中的 token，userId: {}, key: {}", userId, TOKEN_KEY_PREFIX + userId);

        // 验证 Redis 中 token 是否有效
        return reactiveRedisTemplate.opsForValue()
                .get(TOKEN_KEY_PREFIX + userId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Redis 中没有找到 token，userId: {}, key: {}", userId, TOKEN_KEY_PREFIX + userId);
                    return Mono.error(new RuntimeException("Token not found in Redis"));
                }))
                .flatMap(storedToken -> {
                    log.debug("Redis 中找到 token，长度: {}", storedToken != null ? storedToken.length() : 0);
                    log.debug("请求 token 长度: {}", token.length());
                    log.debug("Token 前20字符 - 请求: {}, Redis: {}", 
                        token.substring(0, Math.min(20, token.length())),
                        storedToken.substring(0, Math.min(20, storedToken.length())));
                    
                    if (!token.equals(storedToken)) {
                        log.warn("Token 不匹配！userId: {}", userId);
                        return unauthorized(exchange, "Token 已失效，请重新登录");
                    }
                    
                    log.debug("Token 验证通过，注入用户信息头");
                    // 将用户信息注入请求头
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Role", claims.get("role", String.class))
                            .header("X-User-Sub-Role", claims.get("subRole", String.class) != null
                                    ? claims.get("subRole", String.class) : "")
                            .header("X-Tenant-Id", claims.get("tenantId", String.class) != null
                                    ? claims.get("tenantId", String.class) : "0")
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.warn("Token 验证失败: {}", e.getMessage());
                    return unauthorized(exchange, "Token 已失效，请重新登录");
                });
    }

    private boolean isWhiteListed(String path) {
        List<String> whiteList = securityProperties.getWhiteList();
        log.debug("当前白名单配置: {}", whiteList);
        
        boolean matched = whiteList.stream()
                .anyMatch(pattern -> {
                    boolean isMatch = PATH_MATCHER.match(pattern, path);
                    if (isMatch) {
                        log.debug("路径 {} 匹配白名单模式: {}", path, pattern);
                    }
                    return isMatch;
                });
        
        if (!matched) {
            log.debug("路径 {} 不匹配任何白名单模式", path);
        }
        
        return matched;
    }

    private String extractToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":401,\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
