package com.zhitu.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RateLimitResponseFilter
 * 
 * Tests the rate limit response handling:
 * - Adding Retry-After header
 * - Adding X-RateLimit headers
 * - Proper JSON error response
 * 
 * Validates: Requirement 49.4
 */
@DisplayName("Rate Limit Response Filter Tests")
class RateLimitResponseFilterTest {

    private RateLimitResponseFilter filter;
    private ObjectMapper objectMapper;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new RateLimitResponseFilter(objectMapper);
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    @DisplayName("Should add Retry-After header when rate limit exceeded")
    void testRateLimitExceeded_AddsRetryAfterHeader() {
        // Given: Request that will be rate limited
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        ServerWebExchange exchange = MockServerWebExchange.from(request).mutate().response(response).build();

        // Mock chain to return completed mono
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When: Filter processes the exchange
        Mono<Void> result = filter.filter(exchange, chain);

        // Then: Response should have Retry-After header
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(response.getHeaders().getFirst("Retry-After")).isEqualTo("3600");
        assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("1000");
        assertThat(response.getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(response.getHeaders().getFirst("X-RateLimit-Reset")).isNotNull();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should not modify response when status is not 429")
    void testNormalResponse_NoModification() {
        // Given: Normal successful request
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.setStatusCode(HttpStatus.OK);
        ServerWebExchange exchange = MockServerWebExchange.from(request).mutate().response(response).build();

        // Mock chain to return completed mono
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When: Filter processes the exchange
        Mono<Void> result = filter.filter(exchange, chain);

        // Then: Response should not be modified
        StepVerifier.create(result)
                .verifyComplete();

        assertThat(response.getHeaders().getFirst("Retry-After")).isNull();
        assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isNull();
    }

    @Test
    @DisplayName("Should return proper JSON error body when rate limited")
    void testRateLimitExceeded_ReturnsJsonError() throws Exception {
        // Given: Request that will be rate limited
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/student-portal/v1/dashboard")
                .build();
        MockServerHttpResponse response = new MockServerHttpResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        ServerWebExchange exchange = MockServerWebExchange.from(request).mutate().response(response).build();

        // Mock chain to return completed mono
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When: Filter processes the exchange
        Mono<Void> result = filter.filter(exchange, chain);

        // Then: Response body should contain proper error JSON
        StepVerifier.create(result)
                .verifyComplete();

        byte[] bodyBytes = response.getBodyAsString().block().getBytes(StandardCharsets.UTF_8);
        Map<String, Object> body = objectMapper.readValue(bodyBytes, Map.class);

        assertThat(body.get("code")).isEqualTo(429);
        assertThat(body.get("message")).isNotNull();
        assertThat(body.get("retryAfter")).isEqualTo(3600);
    }

    @Test
    @DisplayName("Filter should have correct order")
    void testFilterOrder() {
        // Then: Filter should run after AuthFilter (-100) but before routing
        assertThat(filter.getOrder()).isEqualTo(-50);
    }
}
