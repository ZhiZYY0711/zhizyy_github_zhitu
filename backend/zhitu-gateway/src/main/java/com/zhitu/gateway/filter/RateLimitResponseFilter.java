package com.zhitu.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limit Response Filter
 * 
 * Intercepts rate limit exceeded responses and adds proper headers
 * Returns 429 status with Retry-After header
 * 
 * Requirements: 49.4
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitResponseFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return -50; // Run after AuthFilter but before routing
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            
            // Check if rate limit was exceeded (Spring Cloud Gateway sets this status)
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("Rate limit exceeded for path: {}", exchange.getRequest().getPath());
                
                // Add Retry-After header (3600 seconds = 1 hour, matching our rate limit window)
                response.getHeaders().add("Retry-After", "3600");
                
                // Add X-RateLimit headers for client information
                response.getHeaders().add("X-RateLimit-Limit", "1000"); // Default to user limit
                response.getHeaders().add("X-RateLimit-Remaining", "0");
                response.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 3600));
                
                // Ensure proper content type
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                
                // Create error response body
                Map<String, Object> body = new HashMap<>();
                body.put("code", 429);
                body.put("message", "请求过于频繁，请稍后再试");
                body.put("retryAfter", 3600);
                
                byte[] bytes;
                try {
                    bytes = objectMapper.writeValueAsBytes(body);
                } catch (JsonProcessingException e) {
                    bytes = "{\"code\":429,\"message\":\"Too Many Requests\"}".getBytes(StandardCharsets.UTF_8);
                }
                
                DataBuffer buffer = response.bufferFactory().wrap(bytes);
                return response.writeWith(Mono.just(buffer));
            }
            
            return Mono.empty();
        }));
    }
}
