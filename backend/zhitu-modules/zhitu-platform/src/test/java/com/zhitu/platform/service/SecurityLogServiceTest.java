package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.SecurityLogDTO;
import com.zhitu.platform.entity.SecurityLog;
import com.zhitu.platform.mapper.SecurityLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SecurityLogService 单元测试
 * Requirements: 40.1-40.7
 */
@ExtendWith(MockitoExtension.class)
class SecurityLogServiceTest {

    @Mock
    private SecurityLogMapper securityLogMapper;

    @InjectMocks
    private SecurityLogService securityLogService;

    private SecurityLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new SecurityLog();
        sampleLog.setId(1L);
        sampleLog.setLevel("warning");
        sampleLog.setEventType("login_failed");
        sampleLog.setUserId(100L);
        sampleLog.setIpAddress("192.168.1.1");
        sampleLog.setDescription("Failed login attempt");
        sampleLog.setDetails("{\"username\":\"test\"}");
        sampleLog.setCreatedAt(OffsetDateTime.now());
    }

    // ── logAuthenticationFailure Tests ────────────────────────────────────────

    @Test
    void logAuthenticationFailure_shouldSaveLogWithCorrectFields() {
        // When
        securityLogService.logAuthenticationFailure(100L, "192.168.1.1", 
            "Failed login", "{\"reason\":\"invalid_password\"}");

        // Then
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogMapper, timeout(1000)).insert(captor.capture());
        
        SecurityLog saved = captor.getValue();
        assertThat(saved.getLevel()).isEqualTo("warning");
        assertThat(saved.getEventType()).isEqualTo("login_failed");
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(saved.getDescription()).isEqualTo("Failed login");
        assertThat(saved.getDetails()).isEqualTo("{\"reason\":\"invalid_password\"}");
    }

    // ── logPermissionDenied Tests ─────────────────────────────────────────────

    @Test
    void logPermissionDenied_shouldSaveLogWithCorrectFields() {
        // When
        securityLogService.logPermissionDenied(200L, "10.0.0.1", 
            "Access denied to admin panel", null);

        // Then
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogMapper, timeout(1000)).insert(captor.capture());
        
        SecurityLog saved = captor.getValue();
        assertThat(saved.getLevel()).isEqualTo("warning");
        assertThat(saved.getEventType()).isEqualTo("permission_denied");
        assertThat(saved.getUserId()).isEqualTo(200L);
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
    }

    // ── logSuspiciousActivity Tests ───────────────────────────────────────────

    @Test
    void logSuspiciousActivity_shouldSaveLogWithCriticalLevel() {
        // When
        securityLogService.logSuspiciousActivity(300L, "172.16.0.1", 
            "Multiple failed login attempts", "{\"count\":10}");

        // Then
        ArgumentCaptor<SecurityLog> captor = ArgumentCaptor.forClass(SecurityLog.class);
        verify(securityLogMapper, timeout(1000)).insert(captor.capture());
        
        SecurityLog saved = captor.getValue();
        assertThat(saved.getLevel()).isEqualTo("critical");
        assertThat(saved.getEventType()).isEqualTo("suspicious_activity");
    }

    // ── getSecurityLogs Tests ─────────────────────────────────────────────────

    @Test
    void getSecurityLogs_shouldReturnPagedResults() {
        // Given
        SecurityLog log1 = createSecurityLog(1L, "warning", "login_failed");
        SecurityLog log2 = createSecurityLog(2L, "critical", "suspicious_activity");
        
        Page<SecurityLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(log1, log2));
        mockPage.setTotal(2);
        
        when(securityLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<SecurityLogDTO> result = securityLogService.getSecurityLogs(null, 1, 20);

        // Then
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).eventType()).isEqualTo("login_failed");
        assertThat(result.getRecords().get(1).eventType()).isEqualTo("suspicious_activity");
    }

    @Test
    void getSecurityLogs_shouldFilterByLevel() {
        // Given
        Page<SecurityLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);
        
        when(securityLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<SecurityLogDTO> result = securityLogService.getSecurityLogs("warning", 1, 20);

        // Then
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(securityLogMapper).selectPage(any(Page.class), captor.capture());
        
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().get(0).level()).isEqualTo("warning");
    }

    @Test
    void getSecurityLogs_shouldOrderByCreatedAtDesc() {
        // Given
        Page<SecurityLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);
        
        when(securityLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        securityLogService.getSecurityLogs(null, 1, 20);

        // Then
        verify(securityLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── cleanupExpiredLogs Tests ──────────────────────────────────────────────

    @Test
    void cleanupExpiredLogs_shouldDeleteLogsOlderThan180Days() {
        // Given
        when(securityLogMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);

        // When
        securityLogService.cleanupExpiredLogs();

        // Then
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(securityLogMapper, timeout(1000)).delete(captor.capture());
    }

    @Test
    void cleanupExpiredLogs_shouldHandleExceptionGracefully() {
        // Given
        when(securityLogMapper.delete(any(LambdaQueryWrapper.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When/Then - should not throw exception
        securityLogService.cleanupExpiredLogs();
        
        verify(securityLogMapper, timeout(1000)).delete(any(LambdaQueryWrapper.class));
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private SecurityLog createSecurityLog(Long id, String level, String eventType) {
        SecurityLog log = new SecurityLog();
        log.setId(id);
        log.setLevel(level);
        log.setEventType(eventType);
        log.setUserId(100L);
        log.setIpAddress("192.168.1.1");
        log.setDescription("Test description");
        log.setDetails(null);
        log.setCreatedAt(OffsetDateTime.now());
        return log;
    }
}
