# Bugfix Requirements Document

## Introduction

The `RateLimitIntegrationTest` integration test suite is failing with 7 out of 8 tests failing. The tests are designed to validate rate limiting functionality in the gateway service, but they fail because:

1. Tests attempt to route requests to downstream services (student-portal-service on port 8081, auth-service on port 8085, etc.) that are not running during test execution
2. The `AuthFilter` validates JWT tokens against Redis and then attempts to forward requests to downstream services, resulting in "Connection refused" errors
3. Tests receive 401 UNAUTHORIZED or 500 INTERNAL_SERVER_ERROR responses instead of the expected 200 OK or 429 TOO_MANY_REQUESTS responses
4. Rate limit headers (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset) are missing from responses because requests fail before rate limiting logic executes

The root cause is that integration tests are configured to call real downstream service URIs (http://localhost:8081, http://localhost:8085, etc.) without providing mock implementations or test doubles for these services.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a test makes an authenticated request to `/api/student-portal/v1/dashboard` with a valid JWT token THEN the system attempts to route the request to `http://localhost:8081` (student-portal-service), which is not running, resulting in "Connection refused" error and the test fails with 500 INTERNAL_SERVER_ERROR or 401 UNAUTHORIZED instead of testing rate limiting

1.2 WHEN a test makes an unauthenticated request to `/api/auth/v1/login` THEN the system attempts to route the request to `http://localhost:8085` (auth-service), which is not running, resulting in "Connection refused" error and the test fails instead of testing IP-based rate limiting

1.3 WHEN tests simulate rate limit exhaustion by setting Redis keys to "0" and make requests THEN the system attempts to forward requests to non-existent downstream services before rate limit responses can be generated, causing tests to fail with connection errors instead of receiving 429 TOO_MANY_REQUESTS responses

1.4 WHEN tests expect rate limit headers (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset) in responses THEN these headers are missing because requests fail during routing before the `RateLimitResponseFilter` can add them

1.5 WHEN the gateway routes requests in test environment THEN it uses the same route configuration as production (pointing to localhost:8081-8085) without test-specific mock endpoints, causing all routed requests to fail

### Expected Behavior (Correct)

2.1 WHEN a test makes an authenticated request to `/api/student-portal/v1/dashboard` with a valid JWT token THEN the system SHALL successfully complete the request flow (either by mocking the downstream service or using a test endpoint that returns a valid response) allowing rate limiting logic to be tested, and the test SHALL receive the expected status code (200 OK when within limits, 429 when rate limited)

2.2 WHEN a test makes an unauthenticated request to `/api/auth/v1/login` THEN the system SHALL successfully complete the request flow (either by mocking the downstream service or using a test endpoint) allowing IP-based rate limiting to be tested without connection errors

2.3 WHEN tests simulate rate limit exhaustion by setting Redis keys to "0" and make requests THEN the system SHALL return 429 TOO_MANY_REQUESTS responses with proper Retry-After headers and error body containing code 429, message, and retryAfter fields

2.4 WHEN tests make requests within rate limits THEN the system SHALL return responses with rate limit headers (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset) present in the response

2.5 WHEN the gateway runs in test environment THEN it SHALL use test-specific route configurations that either mock downstream services using WireMock/MockWebServer, stub responses using WebTestClient, or route to test endpoints that return valid responses without requiring actual service instances

### Unchanged Behavior (Regression Prevention)

3.1 WHEN the gateway runs in production or development environments THEN it SHALL CONTINUE TO route requests to actual downstream services at their configured URIs (localhost:8081-8085 or service discovery endpoints)

3.2 WHEN the `AuthFilter` processes requests in production THEN it SHALL CONTINUE TO validate JWT tokens against Redis and forward authenticated requests to downstream services with user information headers (X-User-Id, X-User-Role, X-Tenant-Id)

3.3 WHEN rate limiting is triggered in production THEN it SHALL CONTINUE TO return 429 TOO_MANY_REQUESTS responses with Retry-After headers and proper error bodies

3.4 WHEN the `RateLimitResponseFilter` processes responses in production THEN it SHALL CONTINUE TO add rate limit headers (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset) to all responses

3.5 WHEN requests match whitelist patterns (e.g., `/api/auth/v1/login`, `/actuator/**`) in production THEN the system SHALL CONTINUE TO bypass authentication and allow these requests through without token validation
