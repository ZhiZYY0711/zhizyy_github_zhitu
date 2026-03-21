# Rate Limiting Configuration

## Overview

The Zhitu Gateway implements rate limiting to prevent abuse and ensure fair resource allocation across all users. The implementation uses Spring Cloud Gateway's built-in rate limiting with Redis-backed sliding window algorithm.

## Requirements

**Validates Requirements: 49.1-49.6**

- 49.1: Rate limiting per user and per IP address
- 49.2: Authenticated users limited to 1000 requests per hour
- 49.3: Unauthenticated requests limited to 100 requests per hour per IP
- 49.4: Return 429 status with Retry-After header when limit exceeded
- 49.5: Sliding window rate limiting algorithm
- 49.6: Configurable rate limits per tenant (future enhancement)

## Architecture

### Components

1. **RateLimitConfig** (`config/RateLimitConfig.java`)
   - Defines KeyResolver beans for user and IP-based rate limiting
   - Extracts user ID from JWT token (via X-User-Id header)
   - Falls back to IP address for unauthenticated requests
   - Handles X-Forwarded-For header for proxied requests

2. **RateLimitResponseFilter** (`filter/RateLimitResponseFilter.java`)
   - Intercepts rate limit exceeded responses
   - Adds Retry-After header (3600 seconds = 1 hour)
   - Adds X-RateLimit-* headers for client information
   - Returns proper JSON error response

3. **Redis Storage**
   - Stores rate limit counters with sliding window
   - Key pattern: `request_rate_limiter.{user:userId}.tokens` or `request_rate_limiter.{ip:ipAddress}.tokens`
   - Automatic expiration after rate limit window

### Rate Limiting Strategy

#### Authenticated Users (User-based)
- **Limit**: 1000 requests per hour
- **Burst Capacity**: 1200 requests (20% buffer for burst traffic)
- **Key**: `user:{userId}` extracted from JWT token
- **Applies to**: All authenticated API requests

#### Unauthenticated Users (IP-based)
- **Limit**: 100 requests per hour
- **Burst Capacity**: 120 requests (20% buffer)
- **Key**: `ip:{ipAddress}` extracted from request
- **Applies to**: Unauthenticated requests (e.g., login, public endpoints)

### Sliding Window Algorithm

Spring Cloud Gateway uses Redis-based sliding window algorithm:

1. **Token Bucket**: Each user/IP has a token bucket
2. **Replenish Rate**: Tokens are added at configured rate (1000/hour or 100/hour)
3. **Burst Capacity**: Maximum tokens that can accumulate
4. **Request Cost**: Each request consumes 1 token
5. **Sliding Window**: Tokens replenish continuously, not in fixed intervals

## Configuration

### Application Configuration (`application.yml`)

```yaml
spring:
  cloud:
    gateway:
      redis-rate-limiter:
        include-headers: true
        config:
          user-rate-limit:
            replenish-rate: 1000  # Tokens per hour
            burst-capacity: 1200   # Max burst
            requested-tokens: 1    # Tokens per request
          ip-rate-limit:
            replenish-rate: 100
            burst-capacity: 120
            requested-tokens: 1
      routes:
        - id: student-portal-service
          uri: lb://zhitu-student
          predicates:
            - Path=/api/student-portal/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 1000
                redis-rate-limiter.burstCapacity: 1200
                redis-rate-limiter.requestedTokens: 1
                key-resolver: "#{@userKeyResolver}"
```

### Route-Specific Configuration

Rate limiting is applied to the following routes:
- `/api/student-portal/**` - Student portal APIs
- `/api/portal-enterprise/**` - Enterprise portal APIs
- `/api/portal-college/**` - College portal APIs
- `/api/portal-platform/**` - Platform administration APIs
- `/api/system/**` - System APIs
- `/api/internship/v1/enterprise/**` - Enterprise internship APIs

### Whitelisted Routes

The following routes are NOT rate limited:
- `/api/auth/v1/login` - Login endpoint
- `/api/auth/v1/token/refresh` - Token refresh
- `/actuator/**` - Health check endpoints

## Response Format

### Successful Request (Within Limit)

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1704067200
Content-Type: application/json

{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

### Rate Limit Exceeded

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 3600
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704067200
Content-Type: application/json

{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "retryAfter": 3600
}
```

### Response Headers

- **Retry-After**: Seconds until rate limit resets (3600 = 1 hour)
- **X-RateLimit-Limit**: Maximum requests allowed in window
- **X-RateLimit-Remaining**: Remaining requests in current window
- **X-RateLimit-Reset**: Unix timestamp when rate limit resets

## Client IP Extraction

The gateway extracts client IP in the following order:

1. **X-Forwarded-For header** (for proxied requests)
   - Takes the first IP in the comma-separated list (original client)
   - Example: `X-Forwarded-For: 203.0.113.195, 70.41.3.18` → uses `203.0.113.195`

2. **Remote Address** (direct requests)
   - Uses the socket remote address

## Testing

### Unit Tests

- `RateLimitConfigTest`: Tests key resolver logic
- `RateLimitResponseFilterTest`: Tests response handling

### Integration Tests

- `RateLimitIntegrationTest`: Tests complete rate limiting flow
  - User-based rate limiting
  - IP-based rate limiting
  - 429 response with headers
  - Multiple users isolation
  - X-Forwarded-For handling

### Running Tests

```bash
cd backend/zhitu-gateway
mvn test
```

## Monitoring

### Redis Keys

Monitor rate limit usage by checking Redis keys:

```bash
# List all rate limit keys
redis-cli KEYS "request_rate_limiter.*"

# Check specific user's remaining tokens
redis-cli GET "request_rate_limiter.{user:12345}.tokens"

# Check specific IP's remaining tokens
redis-cli GET "request_rate_limiter.{ip:192.168.1.100}.tokens"
```

### Metrics

Spring Cloud Gateway exposes metrics for rate limiting:

- `spring.cloud.gateway.requests` - Total requests
- `spring.cloud.gateway.rate.limiter.dropped` - Rate limited requests

Access metrics at: `http://gateway:8888/actuator/metrics`

## Future Enhancements

### Per-Tenant Rate Limits (Requirement 49.6)

To implement configurable rate limits per tenant:

1. Create `TenantRateLimitConfig` table in database
2. Add `TenantKeyResolver` that includes tenant ID in key
3. Load tenant-specific limits from database/cache
4. Apply different limits based on subscription plan

Example:
```java
@Bean
public KeyResolver tenantKeyResolver() {
    return exchange -> {
        String tenantId = exchange.getRequest()
            .getHeaders().getFirst("X-Tenant-Id");
        String userId = exchange.getRequest()
            .getHeaders().getFirst("X-User-Id");
        return Mono.just("tenant:" + tenantId + ":user:" + userId);
    };
}
```

### Dynamic Rate Limit Adjustment

- Load rate limits from Nacos configuration
- Support hot reload without gateway restart
- Different limits for different API endpoints
- Premium tier with higher limits

## Troubleshooting

### Rate Limit Not Working

1. **Check Redis Connection**
   ```bash
   redis-cli PING
   ```

2. **Verify KeyResolver Bean**
   - Check logs for "Rate limit key: user:xxx" or "Rate limit key: ip:xxx"
   - Ensure AuthFilter is setting X-User-Id header

3. **Check Route Configuration**
   - Verify RequestRateLimiter filter is configured on route
   - Ensure key-resolver references correct bean name

### False Positives

If legitimate users are being rate limited:

1. **Check burst capacity** - May need to increase for burst traffic
2. **Verify IP extraction** - Ensure X-Forwarded-For is handled correctly
3. **Check Redis expiration** - Ensure keys are expiring properly

### Performance Issues

If rate limiting causes performance degradation:

1. **Redis Performance** - Ensure Redis has sufficient resources
2. **Connection Pooling** - Configure Redis connection pool size
3. **Network Latency** - Co-locate Redis with gateway for low latency

## References

- [Spring Cloud Gateway Rate Limiting](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestratelimiter-gatewayfilter-factory)
- [Redis Rate Limiter](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-redis-ratelimiter)
- [Sliding Window Algorithm](https://en.wikipedia.org/wiki/Sliding_window_protocol)
