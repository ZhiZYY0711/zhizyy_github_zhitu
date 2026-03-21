package com.zhitu.platform.integration;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.interceptor.OperationLogInterceptor;
import com.zhitu.platform.entity.OperationLog;
import com.zhitu.platform.mapper.OperationLogMapper;
import com.zhitu.platform.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 操作日志集成测试
 * 测试从拦截器到服务的完整流程
 * 
 * Requirements: 39.1-39.7
 */
@SpringBootTest
class OperationLogIntegrationTest {

    @Autowired
    private OperationLogService operationLogService;

    @MockBean
    private OperationLogMapper operationLogMapper;

    @MockBean
    private HttpServletRequest request;

    @MockBean
    private HttpServletResponse response;

    private OperationLogInterceptor interceptor;

    @BeforeEach
    void setUp() {
        // Setup interceptor with service consumer
        interceptor = new OperationLogInterceptor(this::saveLog);

        // Setup default request mocks
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/student-portal/v1/dashboard");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getQueryString()).thenReturn("page=1");

        // Setup user context
        UserContext.LoginUser user = UserContext.LoginUser.builder()
            .userId(100L)
            .username("testuser")
            .tenantId(1L)
            .build();
        UserContext.set(user);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void fullFlow_shouldCaptureAndSaveOperationLog() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // When
        interceptor.preHandle(request, response, null);
        Thread.sleep(50); // Simulate some processing
        interceptor.afterCompletion(request, response, null, null);

        // Then
        // Wait for async operation
        Thread.sleep(500);
        verify(operationLogMapper, atLeastOnce()).insert(any(OperationLog.class));
    }

    @Test
    void fullFlow_withFailedRequest_shouldLogFailure() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(500);
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        Thread.sleep(500);
        verify(operationLogMapper, atLeastOnce()).insert(any(OperationLog.class));
    }

    @Test
    void fullFlow_withoutUserContext_shouldStillSaveLog() throws Exception {
        // Given
        UserContext.clear();
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        Thread.sleep(500);
        verify(operationLogMapper, atLeastOnce()).insert(any(OperationLog.class));
    }

    /**
     * Consumer method that saves the log
     */
    private void saveLog(Map<String, Object> logData) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(getLong(logData, "userId"));
        operationLog.setUserName(getString(logData, "userName"));
        operationLog.setTenantId(getLong(logData, "tenantId"));
        operationLog.setModule(getString(logData, "module"));
        operationLog.setOperation(getString(logData, "operation"));
        operationLog.setRequestParams(getString(logData, "requestParams"));
        operationLog.setResponseStatus(getInteger(logData, "responseStatus"));
        operationLog.setResult(getString(logData, "result"));
        operationLog.setIpAddress(getString(logData, "ipAddress"));
        operationLog.setUserAgent(getString(logData, "userAgent"));
        operationLog.setExecutionTime(getInteger(logData, "executionTime"));
        operationLog.setCreatedAt(OffsetDateTime.now());

        operationLogService.saveLogAsync(operationLog);
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
