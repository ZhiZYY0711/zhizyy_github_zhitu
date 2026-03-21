package com.zhitu.common.core.exception;

import com.zhitu.common.core.result.Result;
import com.zhitu.common.core.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 单元测试
 * 验证所有异常处理器是否正确处理异常并返回符合要求的响应
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        handler = new GlobalExceptionHandler(eventPublisher);
    }

    @Test
    @DisplayName("处理 MethodArgumentNotValidException - 返回 400 和字段级错误信息")
    void testHandleValidation() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        
        FieldError fieldError1 = new FieldError("user", "email", "must be a valid email");
        FieldError fieldError2 = new FieldError("user", "age", "must be greater than 0");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));

        // When
        Result<Void> result = handler.handleValidation(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("email"));
        assertTrue(result.getMessage().contains("age"));
        assertTrue(result.getMessage().contains("must be a valid email"));
        assertTrue(result.getMessage().contains("must be greater than 0"));
    }

    @Test
    @DisplayName("处理 BindException - 返回 400 和字段级错误信息")
    void testHandleBind() {
        // Given
        BindException exception = new BindException(new Object(), "target");
        exception.addError(new FieldError("target", "name", "must not be blank"));

        // When
        Result<Void> result = handler.handleBind(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.PARAM_ERROR.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("name"));
        assertTrue(result.getMessage().contains("must not be blank"));
    }

    @Test
    @DisplayName("处理 BusinessException - 返回 400 和业务错误信息")
    void testHandleBusiness() {
        // Given
        BusinessException exception = new BusinessException(1001, "用户不存在");

        // When
        Result<Void> result = handler.handleBusiness(exception);

        // Then
        assertNotNull(result);
        assertEquals(1001, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("处理 AuthenticationException - 返回 401 和认证错误信息")
    void testHandleAuthentication() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        Result<Void> result = handler.handleAuthentication(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
        assertEquals(ResultCode.UNAUTHORIZED.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理 AccessDeniedException - 返回 403 和权限错误信息")
    void testHandleAccessDenied() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        Result<Void> result = handler.handleAccessDenied(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.FORBIDDEN.getCode(), result.getCode());
        assertEquals(ResultCode.FORBIDDEN.getMessage(), result.getMessage());
    }

    @Test
    @DisplayName("处理 ResourceNotFoundException - 返回 404 和资源标识符")
    void testHandleResourceNotFound() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "12345");

        // When
        Result<Void> result = handler.handleResourceNotFound(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.NOT_FOUND.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("User"));
        assertTrue(result.getMessage().contains("12345"));
    }

    @Test
    @DisplayName("处理通用 Exception - 返回 500 和错误 ID")
    void testHandleException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        Result<Void> result = handler.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals(ResultCode.SERVER_ERROR.getCode(), result.getCode());
        assertTrue(result.getMessage().contains("错误ID"));
        assertTrue(result.getMessage().contains("技术支持"));
        // 验证错误消息不包含敏感信息（堆栈跟踪）
        assertFalse(result.getMessage().contains("RuntimeException"));
        assertFalse(result.getMessage().contains("Unexpected error"));
    }

    @Test
    @DisplayName("验证错误 ID 的唯一性")
    void testErrorIdUniqueness() {
        // Given
        Exception exception1 = new RuntimeException("Error 1");
        Exception exception2 = new RuntimeException("Error 2");

        // When
        Result<Void> result1 = handler.handleException(exception1);
        Result<Void> result2 = handler.handleException(exception2);

        // Then
        assertNotNull(result1.getMessage());
        assertNotNull(result2.getMessage());
        // 两个错误应该有不同的错误 ID
        assertNotEquals(result1.getMessage(), result2.getMessage());
    }

    @Test
    @DisplayName("验证多个字段错误的格式")
    void testMultipleFieldErrors() {
        // Given
        BindException exception = new BindException(new Object(), "user");
        exception.addError(new FieldError("user", "email", "invalid format"));
        exception.addError(new FieldError("user", "password", "too short"));
        exception.addError(new FieldError("user", "age", "must be positive"));

        // When
        Result<Void> result = handler.handleBind(exception);

        // Then
        assertNotNull(result);
        String message = result.getMessage();
        assertTrue(message.contains("email"));
        assertTrue(message.contains("password"));
        assertTrue(message.contains("age"));
        // 验证使用分号分隔
        assertTrue(message.contains(";"));
    }
}
