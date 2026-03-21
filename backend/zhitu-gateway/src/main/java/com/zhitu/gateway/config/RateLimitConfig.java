package com.zhitu.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Rate Limiting Configuration
 * 
 * Implements sliding window rate limiting with two strategies:
 * 1. User-based: 1000 requests per hour for authenticated users
 * 2. IP-based: 100 requests per hour for unauthenticated requests
 * 
 * Requirements: 49.1-49.6
 */
@Slf4j
@Configuration
public class RateLimitConfig {

    /**
     * User-based rate limiter key resolver
     * Extracts user_id from X-User-Id header (set by AuthFilter)
     * Falls back to IP address if user is not authenticated
     * 
     * @return KeyResolver that resolves to user_id or IP
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get user ID from header (set by AuthFilter for authenticated requests)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (userId != null && !userId.isEmpty()) {
                log.debug("Rate limit key: user:{}", userId);
                return Mono.just("user:" + userId);
            }
            
            // Fall back to IP for unauthenticated requests
            String ip = getClientIp(exchange);
            log.debug("Rate limit key: ip:{}", ip);
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * IP-based rate limiter key resolver
     * Extracts client IP address from request
     * Handles X-Forwarded-For header for proxied requests
     * 
     * @return KeyResolver that resolves to client IP
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = getClientIp(exchange);
            log.debug("Rate limit key (IP only): ip:{}", ip);
            return Mono.just("ip:" + ip);
        };
    }

    /**
     * Extract client IP address from request
     * Checks X-Forwarded-For header first (for proxied requests)
     * Falls back to remote address
     * 
     * @param exchange ServerWebExchange
     * @return Client IP address
     */
    private String getClientIp(org.springframework.web.server.ServerWebExchange exchange) {
        // Check X-Forwarded-For header (for requests through load balancers/proxies)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Fall back to remote address
        return Objects.requireNonNull(
            exchange.getRequest().getRemoteAddress(),
            "Remote address cannot be null"
        ).getAddress().getHostAddress();
    }
}
