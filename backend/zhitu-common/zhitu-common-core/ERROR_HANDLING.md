# Global Error Handling Documentation

## Overview

The Global Error Handling system provides consistent error responses across all microservices in the Zhitu Cloud Platform. It implements all requirements from Requirement 43 (Error Handling) in the specification.

## Components

### 1. GlobalExceptionHandler

**Location:** `com.zhitu.common.core.exception.GlobalExceptionHandler`

A centralized exception handler using `@RestControllerAdvice` that intercepts exceptions across all controllers and returns consistent error responses.

#### Handled Exception Types

| Exception Type | HTTP Status | Description | Requirement |
|---------------|-------------|-------------|-------------|
| `MethodArgumentNotValidException` | 400 | JSR-303 validation failures with field-specific errors | 43.1 |
| `BindException` | 400 | Request parameter binding failures | 43.1 |
| `BusinessException` | 400 | Custom business logic errors | 43.1 |
| `AuthenticationException` | 401 | Authentication failures (invalid credentials, expired tokens) | 43.2 |
| `AccessDeniedException` | 403 | Authorization failures (insufficient permissions) | 43.3 |
| `ResourceNotFoundException` | 404 | Resource not found errors | 43.4 |
| `Exception` | 500 | Unexpected server errors with error ID | 43.5 |

### 2. ResourceNotFoundException

**Location:** `com.zhitu.common.core.exception.ResourceNotFoundException`

A custom exception for resource not found scenarios. Provides structured information about the missing resource.

#### Usage Examples

```java
// With resource type and ID
throw new ResourceNotFoundException("User", "12345");
// Message: "User not found: 12345"

// With custom message
throw new ResourceNotFoundException("The requested project does not exist");
```

### 3. BusinessException

**Location:** `com.zhitu.common.core.exception.BusinessException`

A custom exception for business logic errors. Already existed in the codebase.

#### Usage Examples

```java
// With message only (uses default 400 code)
throw new BusinessException("用户名已存在");

// With custom code and message
throw new BusinessException(1001, "用户不存在");

// With ResultCode enum
throw new BusinessException(ResultCode.USER_NOT_FOUND);
```

## Error Response Format

All error responses follow the consistent `Result<T>` wrapper format:

```json
{
  "code": 400,
  "message": "email: must be a valid email; age: must be greater than 0",
  "data": null
}
```

### Validation Error Example (400)

```json
{
  "code": 422,
  "message": "email: invalid format; password: too short",
  "data": null
}
```

### Authentication Error Example (401)

```json
{
  "code": 401,
  "message": "未登录或 Token 已过期",
  "data": null
}
```

### Authorization Error Example (403)

```json
{
  "code": 403,
  "message": "无权限访问",
  "data": null
}
```

### Resource Not Found Example (404)

```json
{
  "code": 404,
  "message": "User not found: 12345",
  "data": null
}
```

### Server Error Example (500)

```json
{
  "code": 500,
  "message": "服务器内部错误，请联系技术支持并提供错误ID: 5e496b67-18ac-40b3-a06c-d8d359c92b89",
  "data": null
}
```

## Logging Behavior

### Log Levels

- **WARN**: Used for 4xx errors (client errors)
  - Validation failures
  - Business exceptions
  - Authentication failures
  - Authorization failures
  - Resource not found

- **ERROR**: Used for 5xx errors (server errors)
  - Unexpected exceptions
  - System failures

### Stack Traces

- **Requirement 43.6**: All errors are logged with complete stack traces for debugging
- **Requirement 43.7**: Stack traces are NEVER exposed in API responses (security)
- Error IDs are generated for 500 errors to correlate user reports with server logs

### Example Log Output

```
2024-03-21 14:58:34.452 [main] ERROR GlobalExceptionHandler -- 系统异常 [错误ID: 5e496b67-18ac-40b3-a06c-d8d359c92b89]
java.lang.RuntimeException: Unexpected error
    at com.zhitu.platform.service.PlatformService.processData(PlatformService.java:45)
    ...
```

## Security Considerations

### Requirement 43.7: No Sensitive Information Exposure

The error handling system ensures:

1. **Stack traces** are logged but never returned in API responses
2. **Internal error details** are replaced with generic messages for 500 errors
3. **Error IDs** allow support teams to correlate user reports with server logs without exposing internals
4. **Validation errors** only expose field names and validation rules, not internal state

### Safe Error Messages

❌ **Bad** (exposes internals):
```json
{
  "message": "NullPointerException at UserService.java:123"
}
```

✅ **Good** (safe for users):
```json
{
  "message": "服务器内部错误，请联系技术支持并提供错误ID: abc-123"
}
```

## Integration

### Automatic Registration

The `GlobalExceptionHandler` is automatically registered in all Spring Boot applications that include the `zhitu-common-core` dependency because it uses `@RestControllerAdvice`.

### Dependencies

The handler requires Spring Security for `AuthenticationException` and `AccessDeniedException`. This is included as an optional dependency in the `zhitu-common-core` module.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
    <optional>true</optional>
</dependency>
```

## Testing

Comprehensive unit tests are provided in:
- `GlobalExceptionHandlerTest`: Tests all exception handlers
- `ResourceNotFoundExceptionTest`: Tests the custom exception

### Running Tests

```bash
cd backend
mvn test -pl zhitu-common/zhitu-common-core
```

## Best Practices

### 1. Use Appropriate Exception Types

```java
// For business logic errors
if (user == null) {
    throw new BusinessException("用户不存在");
}

// For resource not found
if (project == null) {
    throw new ResourceNotFoundException("Project", projectId);
}

// For validation (use JSR-303 annotations instead)
@NotBlank(message = "用户名不能为空")
private String username;
```

### 2. Provide Meaningful Error Messages

```java
// ❌ Bad: Generic message
throw new BusinessException("操作失败");

// ✅ Good: Specific message
throw new BusinessException("无法删除项目：该项目下还有未完成的任务");
```

### 3. Use Error Codes for Client Handling

```java
// Define custom error codes in ResultCode enum
public enum ResultCode {
    PROJECT_HAS_TASKS(2001, "项目下还有未完成的任务"),
    STUDENT_ALREADY_ENROLLED(2002, "学生已经报名该项目");
}

// Use in business logic
throw new BusinessException(ResultCode.PROJECT_HAS_TASKS);
```

### 4. Log Context Information

```java
try {
    processPayment(orderId);
} catch (Exception e) {
    log.error("支付处理失败: orderId={}", orderId, e);
    throw new BusinessException("支付处理失败，请稍后重试");
}
```

## Requirements Compliance

| Requirement | Status | Implementation |
|------------|--------|----------------|
| 43.1 - Validation errors return 400 with field details | ✅ | `handleValidation()`, `handleBind()` |
| 43.2 - Authentication errors return 401 | ✅ | `handleAuthentication()` |
| 43.3 - Authorization errors return 403 | ✅ | `handleAccessDenied()` |
| 43.4 - Resource not found returns 404 | ✅ | `handleResourceNotFound()` |
| 43.5 - Server errors return 500 with error ID | ✅ | `handleException()` with UUID generation |
| 43.6 - Log all errors with stack traces | ✅ | All handlers use `log.warn()` or `log.error()` with exception parameter |
| 43.7 - No sensitive information in responses | ✅ | Generic messages for 500 errors, no stack traces in responses |

## Future Enhancements

1. **Error Code Registry**: Centralized registry of all error codes with descriptions
2. **Internationalization**: Support for multiple languages in error messages
3. **Error Metrics**: Track error rates and types for monitoring
4. **Custom Error Pages**: Friendly error pages for web UI
5. **Error Recovery**: Automatic retry mechanisms for transient failures
