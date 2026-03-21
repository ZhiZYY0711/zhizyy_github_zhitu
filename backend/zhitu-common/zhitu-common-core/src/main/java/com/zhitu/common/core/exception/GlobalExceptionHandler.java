package com.zhitu.common.core.exception;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.event.SecurityEvent;
import com.zhitu.common.core.result.Result;
import com.zhitu.common.core.result.ResultCode;
import com.zhitu.common.core.util.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理参数校验异常 (JSR-303)
     * Requirement 43.1: 返回 400 状态和字段级错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理绑定异常
     * Requirement 43.1: 返回 400 状态和字段级错误信息
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理业务异常
     * Requirement 43.1: 返回 400 状态和业务错误信息
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证异常
     * Requirement 43.2: 返回 401 状态和认证错误信息
     * Requirement 41.7: 记录认证失败到安全日志
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthentication(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage(), e);
        
        // 发布安全事件
        Long userId = UserContext.getUserId();
        String ipAddress = IpUtil.getClientIp();
        eventPublisher.publishEvent(new SecurityEvent(
            this, "warning", "login_failed", userId, ipAddress,
            "Authentication failed: " + e.getMessage(),
            null
        ));
        
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理授权异常
     * Requirement 43.3: 返回 403 状态和权限错误信息
     * Requirement 41.7: 记录权限拒绝到安全日志
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage(), e);
        
        // 发布安全事件
        Long userId = UserContext.getUserId();
        String ipAddress = IpUtil.getClientIp();
        eventPublisher.publishEvent(new SecurityEvent(
            this, "warning", "permission_denied", userId, ipAddress,
            "Access denied: " + e.getMessage(),
            null
        ));
        
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 处理资源不存在异常
     * Requirement 43.4: 返回 404 状态和资源标识符
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        return Result.fail(ResultCode.NOT_FOUND.getCode(), e.getMessage());
    }

    /**
     * 处理通用异常
     * Requirement 43.5: 返回 500 状态和错误 ID
     * Requirement 43.6: 记录完整堆栈跟踪
     * Requirement 43.7: 不暴露敏感信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        String errorId = UUID.randomUUID().toString();
        log.error("系统异常 [错误ID: {}]", errorId, e);
        return Result.fail(ResultCode.SERVER_ERROR.getCode(), 
                "服务器内部错误，请联系技术支持并提供错误ID: " + errorId);
    }
}
