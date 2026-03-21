# Operation Logging

## Overview

The Operation Logging feature automatically captures and stores detailed information about all API requests across the Zhitu Cloud Platform. This provides comprehensive audit trails and helps with troubleshooting, security monitoring, and compliance.

**Requirements**: 39.1-39.7, 48.1-48.2

## Architecture

### Components

1. **OperationLogInterceptor** (zhitu-common-core)
   - Spring HandlerInterceptor that captures request/response details
   - Shared across all microservices
   - Executes on every API request

2. **OperationLogService** (zhitu-platform)
   - Asynchronously saves logs to database
   - Provides query methods with filtering and pagination
   - Handles log cleanup (90-day retention)

3. **OperationLogController** (zhitu-platform)
   - REST API for viewing operation logs
   - Supports filtering by user, module, result, time range
   - Returns paginated results

### Data Flow

```
API Request
    ↓
OperationLogInterceptor.preHandle()
    - Record start time
    ↓
Controller → Service → Mapper (normal request processing)
    ↓
OperationLogInterceptor.afterCompletion()
    - Collect request/response data
    - Extract user context
    - Calculate execution time
    - Call log consumer
    ↓
OperationLogService.saveLogAsync()
    - Async save to database
    - Non-blocking
```

## Captured Information

Each operation log includes:

| Field | Description | Source |
|-------|-------------|--------|
| `user_id` | User ID from JWT | UserContext |
| `user_name` | Username from JWT | UserContext |
| `tenant_id` | Tenant ID from JWT | UserContext |
| `module` | Module name (student/enterprise/college/platform) | URI parsing |
| `operation` | Operation name (e.g., get_dashboard) | URI parsing |
| `request_params` | Query params and request body (truncated) | HttpServletRequest |
| `response_status` | HTTP status code (200, 400, 500, etc.) | HttpServletResponse |
| `result` | success or failure | Status code < 400 |
| `ip_address` | Client IP (considers X-Forwarded-For) | Request headers |
| `user_agent` | Browser/client user agent | Request headers |
| `execution_time` | Request duration in milliseconds | Time difference |
| `created_at` | Timestamp | Current time |

## Configuration

### Registering the Interceptor

Each microservice must register the interceptor in its configuration:

```java
@Configuration
@RequiredArgsConstructor
public class OperationLogConfig implements WebMvcConfigurer {

    private final OperationLogService operationLogService;

    @Bean
    public OperationLogInterceptor operationLogInterceptor() {
        return new OperationLogInterceptor(this::saveOperationLog);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",           // Exclude auth endpoints
                    "/api/system/v1/health",  // Exclude health checks
                    "/api/actuator/**"        // Exclude monitoring
                );
    }

    private void saveOperationLog(Map<String, Object> logData) {
        // Convert logData to OperationLog entity
        // Call operationLogService.saveLogAsync()
    }
}
```

### Async Configuration

Async execution is configured in `AsyncConfig`:

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

## API Endpoints

### Get Operation Logs

**Endpoint**: `GET /api/system/v1/logs/operation`

**Query Parameters**:
- `userId` (optional): Filter by user ID
- `module` (optional): Filter by module (student/enterprise/college/platform)
- `result` (optional): Filter by result (success/failure)
- `startTime` (optional): Filter by start time (ISO 8601 format)
- `endTime` (optional): Filter by end time (ISO 8601 format)
- `page` (default: 1): Page number
- `size` (default: 20): Page size

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "page": 1,
    "size": 20,
    "total": 150,
    "records": [
      {
        "id": 1,
        "userId": 100,
        "userName": "testuser",
        "tenantId": 1,
        "module": "student",
        "operation": "get_dashboard",
        "requestParams": "{\"query\":\"page=1\"}",
        "responseStatus": 200,
        "result": "success",
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0",
        "executionTime": 45,
        "createdAt": "2024-02-15T10:30:00Z"
      }
    ]
  }
}
```

## Module Extraction

The interceptor automatically extracts the module from the URI:

| URI Pattern | Module |
|-------------|--------|
| `/api/student-portal/**` | student |
| `/api/student/**` | student |
| `/api/portal-enterprise/**` | enterprise |
| `/api/enterprise/**` | enterprise |
| `/api/college/**` | college |
| `/api/portal-platform/**` | platform |
| `/api/system/**` | platform |

## Operation Extraction

Operations are derived from the last two URI segments:

| URI | Operation |
|-----|-----------|
| `/api/student-portal/v1/dashboard` | `v1_dashboard` |
| `/api/enterprise/v1/jobs/123` | `jobs_{id}` |
| `/api/college/v1/internship/inspections` | `internship_inspections` |

## Performance Considerations

### Async Execution

All log saves are asynchronous to avoid blocking API requests:

```java
@Async("asyncExecutor")
public void saveLogAsync(OperationLog operationLog) {
    operationLogMapper.insert(operationLog);
}
```

### Request Parameter Truncation

Request parameters are truncated to 2000 characters to prevent excessive storage:

```java
private static final int MAX_PARAM_LENGTH = 2000;

if (result.length() > MAX_PARAM_LENGTH) {
    result = result.substring(0, MAX_PARAM_LENGTH) + "...[truncated]";
}
```

### Excluded Endpoints

High-frequency endpoints are excluded to reduce log volume:
- Authentication endpoints (`/api/auth/**`)
- Health checks (`/api/system/v1/health`)
- Monitoring endpoints (`/api/actuator/**`)

## Data Retention

Operation logs are retained for 90 days (Requirement 39.7).

### Cleanup Job

A scheduled job should be configured to run daily:

```java
@Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
public void cleanupExpiredLogs() {
    operationLogService.cleanupExpiredLogs();
}
```

The cleanup method deletes logs older than 90 days:

```java
@Async("asyncExecutor")
public void cleanupExpiredLogs() {
    OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(90);
    operationLogMapper.delete(
        new LambdaQueryWrapper<OperationLog>()
            .lt(OperationLog::getCreatedAt, cutoffDate)
    );
}
```

## Security Considerations

### Sensitive Data

- Passwords and tokens are NOT logged
- Request bodies are truncated to prevent logging large payloads
- PII should be masked in production environments

### Access Control

- Only platform administrators can view operation logs
- Logs include tenant_id for multi-tenant isolation
- IP addresses are captured for security auditing

## Testing

### Unit Tests

- `OperationLogServiceTest`: Tests service methods
- `OperationLogInterceptorTest`: Tests interceptor logic
- `OperationLogControllerTest`: Tests API endpoints

### Integration Tests

- `OperationLogIntegrationTest`: Tests full flow from interceptor to database

Run tests:
```bash
mvn test -Dtest=OperationLog*
```

## Troubleshooting

### Logs Not Being Saved

1. Check async executor is configured and enabled
2. Verify interceptor is registered in WebMvcConfigurer
3. Check database connection and table exists
4. Review application logs for exceptions

### High Database Load

1. Increase async thread pool size
2. Add more excluded endpoints
3. Reduce log retention period
4. Consider batching log inserts

### Missing User Context

- Ensure UserContext is populated by gateway/filter
- Check JWT token is valid and contains user info
- Verify request headers are passed correctly

## Future Enhancements

1. **Log Aggregation**: Send logs to centralized logging system (ELK, Splunk)
2. **Real-time Monitoring**: Stream logs to monitoring dashboard
3. **Anomaly Detection**: Alert on suspicious patterns
4. **Performance Analytics**: Analyze slow endpoints
5. **Compliance Reports**: Generate audit reports for compliance
