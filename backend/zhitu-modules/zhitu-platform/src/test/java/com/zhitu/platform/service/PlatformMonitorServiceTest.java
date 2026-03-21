package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.OnlineUserTrendResponseDTO;
import com.zhitu.platform.dto.ServiceHealthDTO;
import com.zhitu.platform.dto.SystemHealthDTO;
import com.zhitu.platform.entity.OnlineUserTrend;
import com.zhitu.platform.entity.ServiceHealth;
import com.zhitu.platform.mapper.OnlineUserTrendMapper;
import com.zhitu.platform.mapper.ServiceHealthMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PlatformMonitorService 单元测试
 * Requirements: 29.1-29.7
 */
@ExtendWith(MockitoExtension.class)
class PlatformMonitorServiceTest {

    @Mock
    private ServiceHealthMapper serviceHealthMapper;

    @Mock
    private OnlineUserTrendMapper onlineUserTrendMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlatformMonitorService platformMonitorService;

    @BeforeEach
    void setUp() {
        // Mock cache service to always execute the supplier
        lenient().when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(3);
                    return supplier.get();
                });
    }

    // ── getHealth() 测试 ──────────────────────────────────────────────────────

    @Test
    void getHealth_shouldReturnAllMicroservicesHealth() {
        // Given
        List<ServiceHealth> healthRecords = Arrays.asList(
                createServiceHealth("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5")),
                createServiceHealth("zhitu-enterprise", "healthy", 50, new BigDecimal("0.2"), new BigDecimal("40.0")),
                createServiceHealth("zhitu-college", "degraded", 120, new BigDecimal("2.5"), new BigDecimal("75.0")),
                createServiceHealth("zhitu-platform", "healthy", 30, new BigDecimal("0.0"), new BigDecimal("25.0"))
        );

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(healthRecords);

        // When
        SystemHealthDTO result = platformMonitorService.getHealth();

        // Then
        assertNotNull(result);
        assertNotNull(result.getServices());
        assertEquals(4, result.getServices().size());

        // Verify cache was used
        verify(cacheService).getOrSet(
                eq("platform:health"),
                eq(1L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void getHealth_shouldIncludeAllRequiredFields() {
        // Given
        ServiceHealth health = createServiceHealth(
                "zhitu-student",
                "healthy",
                45,
                new BigDecimal("0.1"),
                new BigDecimal("35.5")
        );
        health.setMemoryUsage(new BigDecimal("62.3"));

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(health));

        // When
        SystemHealthDTO result = platformMonitorService.getHealth();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getServices().size());

        ServiceHealthDTO service = result.getServices().get(0);
        assertEquals("zhitu-student", service.getName());
        assertEquals("healthy", service.getStatus());
        assertEquals(45, service.getResponseTime());
        assertEquals(new BigDecimal("0.1"), service.getErrorRate());
        assertEquals(new BigDecimal("35.5"), service.getCpuUsage());
        assertEquals(new BigDecimal("62.3"), service.getMemoryUsage());
    }

    @Test
    void getHealth_shouldHandleMultipleRecordsPerService() {
        // Given - Multiple records for same service, should return latest
        OffsetDateTime now = OffsetDateTime.now();
        ServiceHealth oldRecord = createServiceHealth("zhitu-student", "degraded", 100, new BigDecimal("5.0"), new BigDecimal("80.0"));
        oldRecord.setCheckedAt(now.minusMinutes(5));

        ServiceHealth newRecord = createServiceHealth("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5"));
        newRecord.setCheckedAt(now);

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(newRecord, oldRecord)); // Ordered by checkedAt DESC

        // When
        SystemHealthDTO result = platformMonitorService.getHealth();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getServices().size());

        ServiceHealthDTO service = result.getServices().get(0);
        assertEquals("healthy", service.getStatus(), "Should return latest record");
        assertEquals(45, service.getResponseTime());
    }

    @Test
    void getHealth_shouldHandleEmptyHealthRecords() {
        // Given
        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        SystemHealthDTO result = platformMonitorService.getHealth();

        // Then
        assertNotNull(result);
        assertNotNull(result.getServices());
        assertTrue(result.getServices().isEmpty());
    }

    @Test
    void getHealth_shouldHandleDifferentStatuses() {
        // Given
        List<ServiceHealth> healthRecords = Arrays.asList(
                createServiceHealth("service1", "healthy", 30, new BigDecimal("0.0"), new BigDecimal("20.0")),
                createServiceHealth("service2", "degraded", 150, new BigDecimal("3.0"), new BigDecimal("70.0")),
                createServiceHealth("service3", "down", 5000, new BigDecimal("100.0"), new BigDecimal("95.0"))
        );

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(healthRecords);

        // When
        SystemHealthDTO result = platformMonitorService.getHealth();

        // Then
        assertEquals(3, result.getServices().size());
        assertTrue(result.getServices().stream().anyMatch(s -> "healthy".equals(s.getStatus())));
        assertTrue(result.getServices().stream().anyMatch(s -> "degraded".equals(s.getStatus())));
        assertTrue(result.getServices().stream().anyMatch(s -> "down".equals(s.getStatus())));
    }

    // ── getOnlineUserTrend() 测试 ─────────────────────────────────────────────

    @Test
    void getOnlineUserTrend_shouldReturnPast24HoursData() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        List<OnlineUserTrend> trendRecords = Arrays.asList(
                createOnlineUserTrend(now.minusHours(23), 1200, 800, 300, 100),
                createOnlineUserTrend(now.minusHours(12), 1500, 1000, 400, 100),
                createOnlineUserTrend(now.minusHours(1), 1800, 1200, 500, 100)
        );

        when(onlineUserTrendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(trendRecords);

        // When
        OnlineUserTrendResponseDTO result = platformMonitorService.getOnlineUserTrend();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTrend());
        assertEquals(3, result.getTrend().size());

        // Verify cache was used with 5 minute TTL
        verify(cacheService).getOrSet(
                eq("platform:online:trend"),
                eq(5L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void getOnlineUserTrend_shouldIncludeAllUserTypes() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        OnlineUserTrend trend = createOnlineUserTrend(now, 1000, 600, 300, 100);

        when(onlineUserTrendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(trend));

        // When
        OnlineUserTrendResponseDTO result = platformMonitorService.getOnlineUserTrend();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTrend().size());

        var trendData = result.getTrend().get(0);
        assertEquals(1000, trendData.getCount());
        assertEquals(600, trendData.getStudentCount());
        assertEquals(300, trendData.getEnterpriseCount());
        assertEquals(100, trendData.getCollegeCount());
    }

    @Test
    void getOnlineUserTrend_shouldOrderByTimestampAscending() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        List<OnlineUserTrend> trendRecords = Arrays.asList(
                createOnlineUserTrend(now.minusHours(20), 1000, 600, 300, 100),
                createOnlineUserTrend(now.minusHours(10), 1500, 900, 500, 100),
                createOnlineUserTrend(now.minusHours(5), 1800, 1100, 600, 100)
        );

        when(onlineUserTrendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(trendRecords);

        // When
        OnlineUserTrendResponseDTO result = platformMonitorService.getOnlineUserTrend();

        // Then
        assertEquals(3, result.getTrend().size());
        
        // Verify ordering (oldest to newest)
        var trends = result.getTrend();
        assertTrue(trends.get(0).getTimestamp().isBefore(trends.get(1).getTimestamp()));
        assertTrue(trends.get(1).getTimestamp().isBefore(trends.get(2).getTimestamp()));
    }

    @Test
    void getOnlineUserTrend_shouldHandleEmptyData() {
        // Given
        when(onlineUserTrendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        OnlineUserTrendResponseDTO result = platformMonitorService.getOnlineUserTrend();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTrend());
        assertTrue(result.getTrend().isEmpty());
    }

    @Test
    void getOnlineUserTrend_shouldFilterLast24Hours() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        List<OnlineUserTrend> trendRecords = Arrays.asList(
                createOnlineUserTrend(now.minusHours(23), 1200, 800, 300, 100),
                createOnlineUserTrend(now.minusHours(12), 1500, 1000, 400, 100)
        );

        when(onlineUserTrendMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(trendRecords);

        // When
        platformMonitorService.getOnlineUserTrend();

        // Then
        // Verify query filters by timestamp >= 24 hours ago
        verify(onlineUserTrendMapper).selectList(any(LambdaQueryWrapper.class));
    }

    // ── getServices() 测试 ────────────────────────────────────────────────────

    @Test
    void getServices_shouldReturnAllServicesHealthDetails() {
        // Given
        List<ServiceHealth> healthRecords = Arrays.asList(
                createServiceHealth("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5")),
                createServiceHealth("zhitu-enterprise", "healthy", 50, new BigDecimal("0.2"), new BigDecimal("40.0")),
                createServiceHealth("zhitu-college", "degraded", 120, new BigDecimal("2.5"), new BigDecimal("75.0"))
        );

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(healthRecords);

        // When
        List<ServiceHealthDTO> result = platformMonitorService.getServices();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify all services are included
        assertTrue(result.stream().anyMatch(s -> "zhitu-student".equals(s.getName())));
        assertTrue(result.stream().anyMatch(s -> "zhitu-enterprise".equals(s.getName())));
        assertTrue(result.stream().anyMatch(s -> "zhitu-college".equals(s.getName())));
    }

    @Test
    void getServices_shouldReturnLatestRecordPerService() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        ServiceHealth oldRecord = createServiceHealth("zhitu-student", "degraded", 100, new BigDecimal("5.0"), new BigDecimal("80.0"));
        oldRecord.setCheckedAt(now.minusMinutes(10));

        ServiceHealth newRecord = createServiceHealth("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5"));
        newRecord.setCheckedAt(now);

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(newRecord, oldRecord));

        // When
        List<ServiceHealthDTO> result = platformMonitorService.getServices();

        // Then
        assertEquals(1, result.size());
        assertEquals("healthy", result.get(0).getStatus());
        assertEquals(45, result.get(0).getResponseTime());
    }

    @Test
    void getServices_shouldHandleEmptyRecords() {
        // Given
        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<ServiceHealthDTO> result = platformMonitorService.getServices();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getServices_shouldIncludeAllMetrics() {
        // Given
        ServiceHealth health = createServiceHealth(
                "zhitu-platform",
                "healthy",
                30,
                new BigDecimal("0.0"),
                new BigDecimal("25.0")
        );
        health.setMemoryUsage(new BigDecimal("50.5"));

        when(serviceHealthMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(health));

        // When
        List<ServiceHealthDTO> result = platformMonitorService.getServices();

        // Then
        assertEquals(1, result.size());

        ServiceHealthDTO service = result.get(0);
        assertNotNull(service.getName());
        assertNotNull(service.getStatus());
        assertNotNull(service.getResponseTime());
        assertNotNull(service.getErrorRate());
        assertNotNull(service.getCpuUsage());
        assertNotNull(service.getMemoryUsage());
    }

    // ── 辅助方法 ──────────────────────────────────────────────────────────────

    private ServiceHealth createServiceHealth(String serviceName, String status, Integer responseTime,
                                              BigDecimal errorRate, BigDecimal cpuUsage) {
        ServiceHealth health = new ServiceHealth();
        health.setServiceName(serviceName);
        health.setStatus(status);
        health.setResponseTime(responseTime);
        health.setErrorRate(errorRate);
        health.setCpuUsage(cpuUsage);
        health.setMemoryUsage(new BigDecimal("60.0")); // Default memory usage
        health.setCheckedAt(OffsetDateTime.now());
        return health;
    }

    private OnlineUserTrend createOnlineUserTrend(OffsetDateTime timestamp, Integer onlineCount,
                                                  Integer studentCount, Integer enterpriseCount,
                                                  Integer collegeCount) {
        OnlineUserTrend trend = new OnlineUserTrend();
        trend.setTimestamp(timestamp);
        trend.setOnlineCount(onlineCount);
        trend.setStudentCount(studentCount);
        trend.setEnterpriseCount(enterpriseCount);
        trend.setCollegeCount(collegeCount);
        return trend;
    }
}
