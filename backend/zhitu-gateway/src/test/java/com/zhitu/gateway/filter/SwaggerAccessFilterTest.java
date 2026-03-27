package com.zhitu.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.gateway.config.SwaggerProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * SwaggerAccessFilter 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SwaggerAccessFilterTest {

    private SwaggerAccessFilter filter;
    
    @Mock
    private GatewayFilterChain chain;
    
    private SwaggerProperties swaggerProperties;
    private ObjectMapper objectMapper;
    
    private static final String JWT_SECRET = "zhitu-secret-key-2024-spring-cloud-alibaba-microservices";
    
    @BeforeEach
    void setUp() {
        swaggerProperties = new SwaggerProperties();
        swaggerProperties.setEnabled(true);
        
        SwaggerProperties.AccessControl accessControl = new SwaggerProperties.AccessControl();
        accessControl.setEnabled(false);
        accessControl.setAuditEnabled(true);
        swaggerProperties.setAccessControl(accessControl);
        
        objectMapper = new ObjectMapper();
        
        filter = new SwaggerAccessFilter(swaggerProperties, objectMapper);
        ReflectionTestUtils.setField(filter, "jwtSecret", JWT_SECRET);
        
        // Mock chain to return completed Mono (lenient to avoid unnecessary stubbing errors)
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowNonSwaggerPaths() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/auth/v1/login")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldBlockSwaggerWhenDisabled() {
        // Given
        swaggerProperties.setEnabled(false);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowSwaggerWhenAccessControlDisabled() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(false);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldBlockSwaggerWithoutToken() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldBlockSwaggerWithInvalidToken() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        // Invalid token results in forbidden (403) because authorization check fails
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowSwaggerWithValidTokenAndNoRoleRestriction() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        String token = generateToken(1L, "admin");
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldAllowSwaggerWithValidTokenAndMatchingRole() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        swaggerProperties.getAccessControl().setAllowedRoles(List.of("ROLE_ADMIN", "ROLE_DEVELOPER"));
        String token = generateToken(1L, "admin");
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldBlockSwaggerWithValidTokenButWrongRole() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        swaggerProperties.getAccessControl().setAllowedRoles(List.of("ROLE_ADMIN"));
        String token = generateToken(1L, "student");
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRecognizeSwaggerUiPath() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui/index.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRecognizeApiDocsPath() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/v3/api-docs")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRecognizeServiceApiDocsPath() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/api/auth/v3/api-docs")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldRecognizeWebjarsPath() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/webjars/swagger-ui/index.html")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldHaveCorrectFilterOrder() {
        // Then
        assertThat(filter.getOrder()).isEqualTo(-90);
    }

    @Test
    void shouldMatchRoleWithoutPrefix() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        swaggerProperties.getAccessControl().setAllowedRoles(List.of("admin"));
        String token = generateToken(1L, "admin");
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void shouldBlockTokenWithoutRole() {
        // Given
        swaggerProperties.getAccessControl().setEnabled(true);
        swaggerProperties.getAccessControl().setAllowedRoles(List.of("ROLE_ADMIN"));
        
        // Generate token without role claim
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
            .subject("1")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(key)
            .compact();
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/swagger-ui.html")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // When
        Mono<Void> result = filter.filter(exchange, chain);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
        
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * 生成测试用 JWT token
     */
    private String generateToken(Long userId, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .claim("userId", userId.toString())
            .claim("tenantId", "0")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(key)
            .compact();
    }
}
