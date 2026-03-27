package com.zhitu.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.gateway.config.SwaggerProperties;
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
 * Swagger 访问控制过滤器
 * 用于控制 Swagger UI 和 API 文档的访问权限
 * 
 * 功能：
 * 1. 检查请求路径是否为 Swagger 相关路径
 * 2. 验证 JWT token 的有效性
 * 3. 检查用户角色是否有权限访问文档
 * 4. 记录访问审计日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwaggerAccessFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    
    /**
     * Swagger 相关路径模式
     */
    private static final List<String> SWAGGER_PATHS = List.of(
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/swagger-ui/**",
        "/api/*/v3/api-docs/**"
    );

    @Value("${zhitu.jwt.secret}")
    private String jwtSecret;

    private final SwaggerProperties swaggerProperties;
    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        // 在认证过滤器之后执行
        return -90;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // 检查是否为 Swagger 路径
        if (!isSwaggerPath(path)) {
            return chain.filter(exchange);
        }

        log.debug("检测到 Swagger 路径访问: {}", path);

        // 检查 Swagger 是否启用
        if (!swaggerProperties.isEnabled()) {
            log.warn("Swagger UI 已禁用，拒绝访问: {}", path);
            return forbidden(exchange, "Swagger UI 已禁用");
        }

        // 检查访问控制是否启用 - 如果未启用，直接放行
        if (!swaggerProperties.getAccessControl().isEnabled()) {
            log.debug("Swagger 访问控制未启用，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 验证用户权限
        return isAuthorized(exchange.getRequest())
            .flatMap(authorized -> {
                if (authorized) {
                    // 记录审计日志
                    if (swaggerProperties.getAccessControl().isAuditEnabled()) {
                        logAuditAccess(exchange.getRequest(), true);
                    }
                    log.debug("用户已授权访问 Swagger: {}", path);
                    return chain.filter(exchange);
                } else {
                    // 记录审计日志
                    if (swaggerProperties.getAccessControl().isAuditEnabled()) {
                        logAuditAccess(exchange.getRequest(), false);
                    }
                    log.warn("用户无权限访问 Swagger: {}", path);
                    return forbidden(exchange, "您没有权限访问 API 文档");
                }
            })
            .onErrorResume(e -> {
                log.error("验证 Swagger 访问权限时发生错误: {}", e.getMessage());
                if (swaggerProperties.getAccessControl().isAuditEnabled()) {
                    logAuditError(exchange.getRequest(), e);
                }
                return unauthorized(exchange, "身份验证失败");
            });
    }

    /**
     * 检查路径是否为 Swagger 相关路径
     */
    private boolean isSwaggerPath(String path) {
        return SWAGGER_PATHS.stream()
            .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * 验证用户是否有权限访问
     */
    private Mono<Boolean> isAuthorized(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            // 提取 token
            String token = extractToken(request);
            if (token == null) {
                log.debug("请求中缺少 Authorization 头");
                return false;
            }

            // 验证 token 签名和有效性
            Claims claims;
            try {
                claims = parseToken(token);
                log.debug("Token 验证成功，用户ID: {}", claims.getSubject());
            } catch (JwtException e) {
                log.warn("Token 验证失败: {}", e.getMessage());
                return false;
            }

            // 提取用户角色
            String role = claims.get("role", String.class);
            if (role == null) {
                log.warn("Token 中缺少角色信息");
                return false;
            }

            // 检查角色是否在允许列表中
            List<String> allowedRoles = swaggerProperties.getAccessControl().getAllowedRoles();
            if (allowedRoles == null || allowedRoles.isEmpty()) {
                log.debug("未配置允许的角色列表，默认允许所有已认证用户");
                return true;
            }

            // 角色匹配（支持 ROLE_ 前缀和不带前缀两种格式）
            String roleWithPrefix = "ROLE_" + role.toUpperCase();
            boolean hasPermission = allowedRoles.stream()
                .anyMatch(allowedRole -> 
                    allowedRole.equalsIgnoreCase(role) || 
                    allowedRole.equalsIgnoreCase(roleWithPrefix)
                );

            if (hasPermission) {
                log.debug("用户角色 {} 有权限访问 Swagger", role);
            } else {
                log.debug("用户角色 {} 无权限访问 Swagger，允许的角色: {}", role, allowedRoles);
            }

            return hasPermission;
        });
    }

    /**
     * 从请求头中提取 JWT token
     */
    private String extractToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    /**
     * 解析 JWT token
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 记录审计日志 - 访问成功或失败
     */
    private void logAuditAccess(ServerHttpRequest request, boolean authorized) {
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
        
        String token = extractToken(request);
        String userId = "anonymous";
        String role = "none";
        
        if (token != null) {
            try {
                Claims claims = parseToken(token);
                userId = claims.getSubject();
                role = claims.get("role", String.class);
            } catch (Exception e) {
                log.debug("无法从 token 提取用户信息: {}", e.getMessage());
            }
        }

        if (authorized) {
            log.info("Swagger 访问审计 [成功] - 用户: {}, 角色: {}, IP: {}, 路径: {} {}", 
                userId, role, remoteAddress, method, path);
        } else {
            log.warn("Swagger 访问审计 [拒绝] - 用户: {}, 角色: {}, IP: {}, 路径: {} {}", 
                userId, role, remoteAddress, method, path);
        }
    }

    /**
     * 记录审计日志 - 错误
     */
    private void logAuditError(ServerHttpRequest request, Throwable error) {
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String remoteAddress = request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
        
        log.error("Swagger 访问审计 [错误] - IP: {}, 路径: {} {}, 错误: {}", 
            remoteAddress, method, path, error.getMessage());
    }

    /**
     * 返回 401 Unauthorized 响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":401,\"message\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 返回 403 Forbidden 响应
     */
    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 403);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":403,\"message\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
