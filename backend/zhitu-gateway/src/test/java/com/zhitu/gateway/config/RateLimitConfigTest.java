package com.zhitu.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitConfig
 * 
 * Tests rate limiting key resolution strategies:
 * - User-based key resolution (authenticated requests)
 * - IP-based key resolution (unauthenticated requests)
 * - X-Forwarded-For header handling
 * 
 * Validates: Requirements 49.1, 49.2, 49.3
 */
@DisplayName("Rate Limit Configuration Tests")
class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;
    private KeyResolver userKeyResolver;
    private KeyResolver ipKeyResolver;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        userKeyResolver = rateLimitConfig.userKeyResolver();
        ipKeyResolver = rateLimitConfig.ipKeyResolver();
    }

    @Test
    @DisplayName("User key resolver should use user ID from header for authenticated requests")
    void testUserKeyResolver_WithUserId() {
        // Given: Request with X-User-Id header (authenticated)
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .header("X-User-Id", "12345")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = userKeyResolver.resolve(exchange);

        // Then: Key should be user-based
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("user:12345");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("User key resolver should fall back to IP for unauthenticated requests")
    void testUserKeyResolver_WithoutUserId() {
        // Given: Request without X-User-Id header (unauthenticated)
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/v1/login")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = userKeyResolver.resolve(exchange);

        // Then: Key should be IP-based
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:192.168.1.100");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("IP key resolver should extract IP from remote address")
    void testIpKeyResolver_FromRemoteAddress() {
        // Given: Request with remote address
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .remoteAddress(new InetSocketAddress("10.0.0.50", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = ipKeyResolver.resolve(exchange);

        // Then: Key should be IP-based
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:10.0.0.50");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("IP key resolver should extract IP from X-Forwarded-For header")
    void testIpKeyResolver_FromXForwardedFor() {
        // Given: Request with X-Forwarded-For header (proxied request)
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .header("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = ipKeyResolver.resolve(exchange);

        // Then: Key should use first IP from X-Forwarded-For (original client)
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:203.0.113.195");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("User key resolver should handle X-Forwarded-For for unauthenticated requests")
    void testUserKeyResolver_WithXForwardedFor_NoUserId() {
        // Given: Unauthenticated request with X-Forwarded-For header
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/auth/v1/login")
                .header("X-Forwarded-For", "198.51.100.42")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = userKeyResolver.resolve(exchange);

        // Then: Key should use IP from X-Forwarded-For
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:198.51.100.42");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("User key resolver should prefer user ID over IP even with X-Forwarded-For")
    void testUserKeyResolver_UserIdTakesPrecedence() {
        // Given: Authenticated request with both X-User-Id and X-Forwarded-For
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .header("X-User-Id", "67890")
                .header("X-Forwarded-For", "203.0.113.195")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = userKeyResolver.resolve(exchange);

        // Then: Key should be user-based (user ID takes precedence)
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("user:67890");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("User key resolver should handle empty X-User-Id header")
    void testUserKeyResolver_EmptyUserId() {
        // Given: Request with empty X-User-Id header
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .header("X-User-Id", "")
                .remoteAddress(new InetSocketAddress("192.168.1.100", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = userKeyResolver.resolve(exchange);

        // Then: Key should fall back to IP
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:192.168.1.100");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("IP key resolver should handle single IP in X-Forwarded-For")
    void testIpKeyResolver_SingleIpInXForwardedFor() {
        // Given: Request with single IP in X-Forwarded-For
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .header("X-Forwarded-For", "203.0.113.195")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When: Resolve rate limit key
        Mono<String> keyMono = ipKeyResolver.resolve(exchange);

        // Then: Key should use the single IP
        StepVerifier.create(keyMono)
                .assertNext(key -> {
                    assertThat(key).isEqualTo("ip:203.0.113.195");
                })
                .verifyComplete();
    }
}
