package com.zhitu.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for rate limiting functionality
 * 
 * Tests the complete rate limiting flow:
 * - User-based rate limiting (1000 req/hour)
 * - IP-based rate limiting (100 req/hour)
 * - 429 response with Retry-After header
 * - Sliding window algorithm behavior
 * 
 * Validates: Requirements 49.1-49.5
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("Rate Limit Integration Tests")
class RateLimitIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zhitu.jwt.secret}")
    private String jwtSecret;

    private String validToken;
    private static final String TEST_USER_ID = "test-user-12345";
    private static final String TOKEN_KEY_PREFIX = "token:access:";

    @BeforeEach
    void setUp() {
        // Generate valid JWT token for authenticated tests
        validToken = generateToken(TEST_USER_ID, "STUDENT", "0");
        
        // Store token in Redis (simulating successful login)
        reactiveRedisTemplate.opsForValue()
                .set(TOKEN_KEY_PREFIX + TEST_USER_ID, validToken, Duration.ofHours(1))
                .block();
        
        // Clear any existing rate limit keys
        clearRateLimitKeys();
    }

    @Test
    @DisplayName("Should allow requests within user rate limit")
    void testUserRateLimit_WithinLimit() {
        // Given: Authenticated user making requests
        String authHeader = "Bearer " + validToken;

        // When: Make 5 requests (well within 1000/hour limit)
        for (int i = 0; i < 5; i++) {
            webTestClient.get()
                    .uri("/api/student-portal/v1/dashboard")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchange()
                    // Then: All requests should succeed (or return expected status)
                    .expectStatus().isOk()
                    .expectHeader().exists("X-RateLimit-Remaining");
        }
    }

    @Test
    @DisplayName("Should return 429 when user rate limit exceeded")
    void testUserRateLimit_ExceedsLimit() {
        // Given: Authenticated user
        String authHeader = "Bearer " + validToken;
        String rateLimitKey = "request_rate_limiter.{user:" + TEST_USER_ID + "}.tokens";

        // When: Simulate rate limit exhaustion by setting Redis counter to limit
        // Spring Cloud Gateway uses Redis with key pattern: request_rate_limiter.{key}.tokens
        reactiveRedisTemplate.opsForValue()
                .set(rateLimitKey, "0", Duration.ofHours(1))
                .block();

        // Then: Next request should be rate limited
        webTestClient.get()
                .uri("/api/student-portal/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().exists("Retry-After")
                .expectHeader().valueEquals("Retry-After", "3600")
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.code").isEqualTo(429)
                .jsonPath("$.message").exists()
                .jsonPath("$.retryAfter").isEqualTo(3600);
    }

    @Test
    @DisplayName("Should allow requests within IP rate limit for unauthenticated users")
    void testIpRateLimit_WithinLimit() {
        // Given: Unauthenticated request (no auth header)
        // When: Make 3 requests (well within 100/hour limit)
        for (int i = 0; i < 3; i++) {
            webTestClient.get()
                    .uri("/api/auth/v1/login")
                    .exchange()
                    // Then: Requests should not be rate limited
                    // (may return 401 or other status, but not 429)
                    .expectStatus().value(status -> 
                        assertThat(status).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
                    );
        }
    }

    @Test
    @DisplayName("Should return 429 when IP rate limit exceeded")
    void testIpRateLimit_ExceedsLimit() {
        // Given: Unauthenticated request
        String clientIp = "127.0.0.1"; // Default test client IP
        String rateLimitKey = "request_rate_limiter.{ip:" + clientIp + "}.tokens";

        // When: Simulate rate limit exhaustion
        reactiveRedisTemplate.opsForValue()
                .set(rateLimitKey, "0", Duration.ofHours(1))
                .block();

        // Then: Next request should be rate limited
        webTestClient.get()
                .uri("/api/auth/v1/login")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().exists("Retry-After")
                .expectHeader().valueEquals("Retry-After", "3600");
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header for IP-based rate limiting")
    void testIpRateLimit_WithXForwardedFor() {
        // Given: Request with X-Forwarded-For header
        String forwardedIp = "203.0.113.195";
        String rateLimitKey = "request_rate_limiter.{ip:" + forwardedIp + "}.tokens";

        // When: Simulate rate limit exhaustion for forwarded IP
        reactiveRedisTemplate.opsForValue()
                .set(rateLimitKey, "0", Duration.ofHours(1))
                .block();

        // Then: Request with that forwarded IP should be rate limited
        webTestClient.get()
                .uri("/api/auth/v1/login")
                .header("X-Forwarded-For", forwardedIp)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("Should apply different rate limits for different users")
    void testUserRateLimit_DifferentUsers() {
        // Given: Two different users
        String user1Token = generateToken("user-1", "STUDENT", "0");
        String user2Token = generateToken("user-2", "STUDENT", "0");
        
        // Store tokens in Redis
        reactiveRedisTemplate.opsForValue()
                .set(TOKEN_KEY_PREFIX + "user-1", user1Token, Duration.ofHours(1))
                .block();
        reactiveRedisTemplate.opsForValue()
                .set(TOKEN_KEY_PREFIX + "user-2", user2Token, Duration.ofHours(1))
                .block();

        // When: Exhaust rate limit for user-1
        String rateLimitKey1 = "request_rate_limiter.{user:user-1}.tokens";
        reactiveRedisTemplate.opsForValue()
                .set(rateLimitKey1, "0", Duration.ofHours(1))
                .block();

        // Then: User-1 should be rate limited
        webTestClient.get()
                .uri("/api/student-portal/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user1Token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        // But: User-2 should still be allowed
        webTestClient.get()
                .uri("/api/student-portal/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token)
                .exchange()
                .expectStatus().value(status -> 
                    assertThat(status).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
                );
    }

    @Test
    @DisplayName("Should include rate limit headers in response")
    void testRateLimitHeaders() {
        // Given: Authenticated user
        String authHeader = "Bearer " + validToken;

        // When: Make request
        webTestClient.get()
                .uri("/api/student-portal/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .exchange()
                // Then: Response should include rate limit headers
                .expectHeader().exists("X-RateLimit-Remaining")
                .expectHeader().exists("X-RateLimit-Limit")
                .expectHeader().exists("X-RateLimit-Reset");
    }

    @Test
    @DisplayName("Should apply rate limiting to all protected routes")
    void testRateLimit_MultipleRoutes() {
        // Given: Authenticated user
        String authHeader = "Bearer " + validToken;
        String rateLimitKey = "request_rate_limiter.{user:" + TEST_USER_ID + "}.tokens";

        // When: Exhaust rate limit
        reactiveRedisTemplate.opsForValue()
                .set(rateLimitKey, "0", Duration.ofHours(1))
                .block();

        // Then: All routes should be rate limited
        String[] routes = {
                "/api/student-portal/v1/dashboard",
                "/api/portal-enterprise/v1/dashboard/stats",
                "/api/portal-college/v1/dashboard/stats",
                "/api/system/v1/dashboard/stats"
        };

        for (String route : routes) {
            webTestClient.get()
                    .uri(route)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    // Helper methods

    private String generateToken(String userId, String role, String tenantId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofHours(1))))
                .signWith(key)
                .compact();
    }

    private void clearRateLimitKeys() {
        // Clear rate limit keys for test user and test IP
        String[] patterns = {
                "request_rate_limiter.*",
        };
        
        for (String pattern : patterns) {
            reactiveRedisTemplate.keys(pattern)
                    .flatMap(key -> reactiveRedisTemplate.delete(key))
                    .blockLast();
        }
    }
}
