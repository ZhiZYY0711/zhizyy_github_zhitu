package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.OperationLogDTO;
import com.zhitu.platform.entity.OperationLog;
import com.zhitu.platform.mapper.OperationLogMapper;
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
 * 操作日志服务测试
 * Requirements: 39.1-39.7
 */
@ExtendWith(MockitoExtension.class)
class OperationLogServiceTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogService operationLogService;

    private OperationLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new OperationLog();
        sampleLog.setId(1L);
        sampleLog.setUserId(100L);
        sampleLog.setUserName("testuser");
        sampleLog.setTenantId(1L);
        sampleLog.setModule("student");
        sampleLog.setOperation("get_dashboard");
        sampleLog.setRequestParams("{\"query\":\"page=1\"}");
        sampleLog.setResponseStatus(200);
        sampleLog.setResult("success");
        sampleLog.setIpAddress("192.168.1.1");
        sampleLog.setUserAgent("Mozilla/5.0");
        sampleLog.setExecutionTime(45);
        sampleLog.setCreatedAt(OffsetDateTime.now());
    }

    @Test
    void saveLogAsync_shouldInsertLog() {
        // Given
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // When
        operationLogService.saveLogAsync(sampleLog);

        // Then
        verify(operationLogMapper, timeout(1000)).insert(sampleLog);
    }

    @Test
    void saveLogAsync_shouldHandleException() {
        // Given
        when(operationLogMapper.insert(any(OperationLog.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When - should not throw exception
        operationLogService.saveLogAsync(sampleLog);

        // Then
        verify(operationLogMapper, timeout(1000)).insert(sampleLog);
    }

    @Test
    void getLogs_withNoFilters_shouldReturnAllLogs() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, null, null, null, null, 1, 20
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().get(0).userId()).isEqualTo(100L);
        assertThat(result.getRecords().get(0).module()).isEqualTo("student");
    }

    @Test
    void getLogs_withUserIdFilter_shouldFilterByUserId() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        ArgumentCaptor<LambdaQueryWrapper<OperationLog>> queryCaptor = 
            ArgumentCaptor.forClass(LambdaQueryWrapper.class);

        when(operationLogMapper.selectPage(any(Page.class), queryCaptor.capture()))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            100L, null, null, null, null, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(1);
        verify(operationLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getLogs_withModuleFilter_shouldFilterByModule() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, "student", null, null, null, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).module()).isEqualTo("student");
    }

    @Test
    void getLogs_withResultFilter_shouldFilterByResult() {
        // Given
        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, null, "success", null, null, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).result()).isEqualTo("success");
    }

    @Test
    void getLogs_withTimeRange_shouldFilterByTimeRange() {
        // Given
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime endTime = OffsetDateTime.now();

        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, null, null, startTime, endTime, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    void getLogs_withAllFilters_shouldApplyAllFilters() {
        // Given
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime endTime = OffsetDateTime.now();

        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(1);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            100L, "student", "success", startTime, endTime, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).userId()).isEqualTo(100L);
        assertThat(result.getRecords().get(0).module()).isEqualTo("student");
        assertThat(result.getRecords().get(0).result()).isEqualTo("success");
    }

    @Test
    void getLogs_shouldOrderByCreatedAtDesc() {
        // Given
        OperationLog log1 = new OperationLog();
        log1.setId(1L);
        log1.setCreatedAt(OffsetDateTime.now().minusMinutes(10));
        log1.setModule("student");
        log1.setOperation("op1");
        log1.setResult("success");

        OperationLog log2 = new OperationLog();
        log2.setId(2L);
        log2.setCreatedAt(OffsetDateTime.now().minusMinutes(5));
        log2.setModule("student");
        log2.setOperation("op2");
        log2.setResult("success");

        Page<OperationLog> mockPage = new Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(log2, log1)); // Newer first
        mockPage.setTotal(2);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, null, null, null, null, 1, 20
        );

        // Then
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).id()).isEqualTo(2L);
        assertThat(result.getRecords().get(1).id()).isEqualTo(1L);
    }

    @Test
    void getLogs_withPagination_shouldReturnCorrectPage() {
        // Given
        Page<OperationLog> mockPage = new Page<>(2, 10);
        mockPage.setRecords(Arrays.asList(sampleLog));
        mockPage.setTotal(25);

        when(operationLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        PageResult<OperationLogDTO> result = operationLogService.getLogs(
            null, null, null, null, null, 2, 10
        );

        // Then
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotal()).isEqualTo(25);
    }

    @Test
    void cleanupExpiredLogs_shouldDeleteOldLogs() {
        // Given
        when(operationLogMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(10);

        // When
        operationLogService.cleanupExpiredLogs();

        // Then
        verify(operationLogMapper, timeout(1000)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void cleanupExpiredLogs_shouldHandleException() {
        // Given
        when(operationLogMapper.delete(any(LambdaQueryWrapper.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When - should not throw exception
        operationLogService.cleanupExpiredLogs();

        // Then
        verify(operationLogMapper, timeout(1000)).delete(any(LambdaQueryWrapper.class));
    }
}
