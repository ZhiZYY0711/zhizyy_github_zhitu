package com.zhitu.common.core.interceptor;

import com.zhitu.common.core.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 操作日志拦截器测试
 * Requirements: 39.1-39.7, 48.1-48.2
 */
@ExtendWith(MockitoExtension.class)
class OperationLogInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Consumer<Map<String, Object>> logConsumer;

    private OperationLogInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new OperationLogInterceptor(logConsumer);
        
        // Setup default request mocks
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/student-portal/v1/dashboard");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getQueryString()).thenReturn("page=1&size=10");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void preHandle_shouldRecordStartTime() throws Exception {
        // When
        boolean result = interceptor.preHandle(request, response, null);

        // Then
        assertThat(result).isTrue();
        verify(request).setAttribute(eq("startTime"), any(Long.class));
    }

    @Test
    void afterCompletion_shouldCaptureBasicRequestInfo() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("method")).isEqualTo("GET");
        assertThat(logData.get("uri")).isEqualTo("/api/student-portal/v1/dashboard");
        assertThat(logData.get("ipAddress")).isEqualTo("192.168.1.1");
        assertThat(logData.get("userAgent")).isEqualTo("Mozilla/5.0");
    }

    @Test
    void afterCompletion_shouldCaptureUserContext() throws Exception {
        // Given
        UserContext.LoginUser user = UserContext.LoginUser.builder()
            .userId(100L)
            .username("testuser")
            .tenantId(1L)
            .build();
        UserContext.set(user);

        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("userId")).isEqualTo(100L);
        assertThat(logData.get("userName")).isEqualTo("testuser");
        assertThat(logData.get("tenantId")).isEqualTo(1L);
    }

    @Test
    void afterCompletion_shouldExtractModuleFromStudentPortal() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/student-portal/v1/dashboard");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("module")).isEqualTo("student");
    }

    @Test
    void afterCompletion_shouldExtractModuleFromEnterprisePortal() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/portal-enterprise/v1/dashboard/stats");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("module")).isEqualTo("enterprise");
    }

    @Test
    void afterCompletion_shouldExtractModuleFromCollegePortal() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/college/v1/internship/inspections");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("module")).isEqualTo("college");
    }

    @Test
    void afterCompletion_shouldExtractModuleFromPlatformPortal() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/system/v1/logs/operation");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("module")).isEqualTo("platform");
    }

    @Test
    void afterCompletion_shouldCalculateExecutionTime() throws Exception {
        // Given
        long startTime = System.currentTimeMillis() - 150;
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        Integer executionTime = (Integer) logData.get("executionTime");
        assertThat(executionTime).isGreaterThanOrEqualTo(100);
    }

    @Test
    void afterCompletion_shouldMarkSuccessForStatus200() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("responseStatus")).isEqualTo(200);
        assertThat(logData.get("result")).isEqualTo("success");
    }

    @Test
    void afterCompletion_shouldMarkFailureForStatus400() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(400);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("responseStatus")).isEqualTo(400);
        assertThat(logData.get("result")).isEqualTo("failure");
    }

    @Test
    void afterCompletion_shouldMarkFailureForStatus500() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(500);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("responseStatus")).isEqualTo(500);
        assertThat(logData.get("result")).isEqualTo("failure");
    }

    @Test
    void afterCompletion_shouldExtractIpFromXForwardedFor() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("ipAddress")).isEqualTo("10.0.0.1");
    }

    @Test
    void afterCompletion_shouldExtractIpFromXRealIp() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.2");
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("ipAddress")).isEqualTo("10.0.0.2");
    }

    @Test
    void afterCompletion_shouldHandleExceptionGracefully() throws Exception {
        // Given
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);
        doThrow(new RuntimeException("Consumer error")).when(logConsumer).accept(any());

        // When - should not throw exception
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(any());
    }

    @Test
    void afterCompletion_withoutUserContext_shouldStillLog() throws Exception {
        // Given
        UserContext.clear();
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(response.getStatus()).thenReturn(200);

        ArgumentCaptor<Map<String, Object>> logCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        interceptor.preHandle(request, response, null);
        interceptor.afterCompletion(request, response, null, null);

        // Then
        verify(logConsumer).accept(logCaptor.capture());
        Map<String, Object> logData = logCaptor.getValue();

        assertThat(logData.get("userId")).isNull();
        assertThat(logData.get("userName")).isNull();
        assertThat(logData.get("tenantId")).isNull();
        assertThat(logData.get("module")).isNotNull();
        assertThat(logData.get("operation")).isNotNull();
    }
}
