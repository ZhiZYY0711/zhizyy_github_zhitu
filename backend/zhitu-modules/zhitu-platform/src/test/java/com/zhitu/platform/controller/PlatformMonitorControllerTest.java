package com.zhitu.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.platform.dto.OnlineUserTrendDTO;
import com.zhitu.platform.dto.OnlineUserTrendResponseDTO;
import com.zhitu.platform.dto.ServiceHealthDTO;
import com.zhitu.platform.dto.SystemHealthDTO;
import com.zhitu.platform.service.PlatformMonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PlatformMonitorController 单元测试
 * Requirements: 29.1-29.7
 */
@WebMvcTest(PlatformMonitorController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlatformMonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformMonitorService platformMonitorService;

    // ── GET /api/monitor/v1/health 测试 ───────────────────────────────────────

    @Test
    void health_shouldReturnSystemHealthSuccessfully() throws Exception {
        // Given
        List<ServiceHealthDTO> services = Arrays.asList(
                new ServiceHealthDTO("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5"), new BigDecimal("62.3")),
                new ServiceHealthDTO("zhitu-enterprise", "healthy", 50, new BigDecimal("0.2"), new BigDecimal("40.0"), new BigDecimal("65.0")),
                new ServiceHealthDTO("zhitu-college", "degraded", 120, new BigDecimal("2.5"), new BigDecimal("75.0"), new BigDecimal("80.0"))
        );
        SystemHealthDTO health = new SystemHealthDTO(services);

        when(platformMonitorService.getHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.services").isArray())
                .andExpect(jsonPath("$.data.services.length()").value(3))
                .andExpect(jsonPath("$.data.services[0].name").value("zhitu-student"))
                .andExpect(jsonPath("$.data.services[0].status").value("healthy"))
                .andExpect(jsonPath("$.data.services[0].responseTime").value(45))
                .andExpect(jsonPath("$.data.services[0].errorRate").value(0.1))
                .andExpect(jsonPath("$.data.services[0].cpuUsage").value(35.5))
                .andExpect(jsonPath("$.data.services[0].memoryUsage").value(62.3));

        verify(platformMonitorService, times(1)).getHealth();
    }

    @Test
    void health_shouldIncludeAllRequiredFields() throws Exception {
        // Given
        ServiceHealthDTO service = new ServiceHealthDTO(
                "zhitu-platform",
                "healthy",
                30,
                new BigDecimal("0.0"),
                new BigDecimal("25.0"),
                new BigDecimal("50.5")
        );
        SystemHealthDTO health = new SystemHealthDTO(Collections.singletonList(service));

        when(platformMonitorService.getHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.services[0].name").exists())
                .andExpect(jsonPath("$.data.services[0].status").exists())
                .andExpect(jsonPath("$.data.services[0].responseTime").exists())
                .andExpect(jsonPath("$.data.services[0].errorRate").exists())
                .andExpect(jsonPath("$.data.services[0].cpuUsage").exists())
                .andExpect(jsonPath("$.data.services[0].memoryUsage").exists());
    }

    @Test
    void health_shouldHandleEmptyServices() throws Exception {
        // Given
        SystemHealthDTO health = new SystemHealthDTO(Collections.emptyList());

        when(platformMonitorService.getHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.services").isArray())
                .andExpect(jsonPath("$.data.services.length()").value(0));
    }

    @Test
    void health_shouldHandleDifferentStatuses() throws Exception {
        // Given
        List<ServiceHealthDTO> services = Arrays.asList(
                new ServiceHealthDTO("service1", "healthy", 30, new BigDecimal("0.0"), new BigDecimal("20.0"), new BigDecimal("40.0")),
                new ServiceHealthDTO("service2", "degraded", 150, new BigDecimal("3.0"), new BigDecimal("70.0"), new BigDecimal("85.0")),
                new ServiceHealthDTO("service3", "down", 5000, new BigDecimal("100.0"), new BigDecimal("95.0"), new BigDecimal("98.0"))
        );
        SystemHealthDTO health = new SystemHealthDTO(services);

        when(platformMonitorService.getHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.services[0].status").value("healthy"))
                .andExpect(jsonPath("$.data.services[1].status").value("degraded"))
                .andExpect(jsonPath("$.data.services[2].status").value("down"));
    }

    @Test
    void health_shouldUseCorrectEndpoint() throws Exception {
        // Given
        SystemHealthDTO health = new SystemHealthDTO(Collections.emptyList());
        when(platformMonitorService.getHealth()).thenReturn(health);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk());
    }

    // ── GET /api/monitor/v1/users/online-trend 测试 ───────────────────────────

    @Test
    void onlineTrend_shouldReturnTrendDataSuccessfully() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        List<OnlineUserTrendDTO> trend = Arrays.asList(
                new OnlineUserTrendDTO(now.minusHours(23), 1200, 800, 300, 100),
                new OnlineUserTrendDTO(now.minusHours(12), 1500, 1000, 400, 100),
                new OnlineUserTrendDTO(now.minusHours(1), 1800, 1200, 500, 100)
        );
        OnlineUserTrendResponseDTO response = new OnlineUserTrendResponseDTO(trend);

        when(platformMonitorService.getOnlineUserTrend()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.trend").isArray())
                .andExpect(jsonPath("$.data.trend.length()").value(3))
                .andExpect(jsonPath("$.data.trend[0].count").value(1200))
                .andExpect(jsonPath("$.data.trend[0].studentCount").value(800))
                .andExpect(jsonPath("$.data.trend[0].enterpriseCount").value(300))
                .andExpect(jsonPath("$.data.trend[0].collegeCount").value(100));

        verify(platformMonitorService, times(1)).getOnlineUserTrend();
    }

    @Test
    void onlineTrend_shouldIncludeAllUserTypes() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        OnlineUserTrendDTO trendData = new OnlineUserTrendDTO(now, 1000, 600, 300, 100);
        OnlineUserTrendResponseDTO response = new OnlineUserTrendResponseDTO(Collections.singletonList(trendData));

        when(platformMonitorService.getOnlineUserTrend()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trend[0].count").exists())
                .andExpect(jsonPath("$.data.trend[0].studentCount").exists())
                .andExpect(jsonPath("$.data.trend[0].enterpriseCount").exists())
                .andExpect(jsonPath("$.data.trend[0].collegeCount").exists())
                .andExpect(jsonPath("$.data.trend[0].timestamp").exists());
    }

    @Test
    void onlineTrend_shouldHandleEmptyTrend() throws Exception {
        // Given
        OnlineUserTrendResponseDTO response = new OnlineUserTrendResponseDTO(Collections.emptyList());

        when(platformMonitorService.getOnlineUserTrend()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trend").isArray())
                .andExpect(jsonPath("$.data.trend.length()").value(0));
    }

    @Test
    void onlineTrend_shouldUseCorrectEndpoint() throws Exception {
        // Given
        OnlineUserTrendResponseDTO response = new OnlineUserTrendResponseDTO(Collections.emptyList());
        when(platformMonitorService.getOnlineUserTrend()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk());
    }

    // ── GET /api/monitor/v1/services 测试 ─────────────────────────────────────

    @Test
    void getServiceStatuses_shouldReturnAllServicesSuccessfully() throws Exception {
        // Given
        List<ServiceHealthDTO> services = Arrays.asList(
                new ServiceHealthDTO("zhitu-student", "healthy", 45, new BigDecimal("0.1"), new BigDecimal("35.5"), new BigDecimal("62.3")),
                new ServiceHealthDTO("zhitu-enterprise", "healthy", 50, new BigDecimal("0.2"), new BigDecimal("40.0"), new BigDecimal("65.0")),
                new ServiceHealthDTO("zhitu-college", "degraded", 120, new BigDecimal("2.5"), new BigDecimal("75.0"), new BigDecimal("80.0"))
        );

        when(platformMonitorService.getServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("zhitu-student"))
                .andExpect(jsonPath("$.data[0].status").value("healthy"))
                .andExpect(jsonPath("$.data[1].name").value("zhitu-enterprise"))
                .andExpect(jsonPath("$.data[2].name").value("zhitu-college"))
                .andExpect(jsonPath("$.data[2].status").value("degraded"));

        verify(platformMonitorService, times(1)).getServices();
    }

    @Test
    void getServiceStatuses_shouldIncludeAllMetrics() throws Exception {
        // Given
        ServiceHealthDTO service = new ServiceHealthDTO(
                "zhitu-platform",
                "healthy",
                30,
                new BigDecimal("0.0"),
                new BigDecimal("25.0"),
                new BigDecimal("50.5")
        );

        when(platformMonitorService.getServices()).thenReturn(Collections.singletonList(service));

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("zhitu-platform"))
                .andExpect(jsonPath("$.data[0].status").value("healthy"))
                .andExpect(jsonPath("$.data[0].responseTime").value(30))
                .andExpect(jsonPath("$.data[0].errorRate").value(0.0))
                .andExpect(jsonPath("$.data[0].cpuUsage").value(25.0))
                .andExpect(jsonPath("$.data[0].memoryUsage").value(50.5));
    }

    @Test
    void getServiceStatuses_shouldHandleEmptyServices() throws Exception {
        // Given
        when(platformMonitorService.getServices()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void getServiceStatuses_shouldUseCorrectEndpoint() throws Exception {
        // Given
        when(platformMonitorService.getServices()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk());
    }

    // ── 通用测试 ──────────────────────────────────────────────────────────────

    @Test
    void allEndpoints_shouldReturnJsonContentType() throws Exception {
        // Given
        SystemHealthDTO health = new SystemHealthDTO(Collections.emptyList());
        OnlineUserTrendResponseDTO trend = new OnlineUserTrendResponseDTO(Collections.emptyList());
        List<ServiceHealthDTO> services = Collections.emptyList();

        when(platformMonitorService.getHealth()).thenReturn(health);
        when(platformMonitorService.getOnlineUserTrend()).thenReturn(trend);
        when(platformMonitorService.getServices()).thenReturn(services);

        // When & Then
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void allEndpoints_shouldUseGetMethod() throws Exception {
        // Given
        SystemHealthDTO health = new SystemHealthDTO(Collections.emptyList());
        OnlineUserTrendResponseDTO trend = new OnlineUserTrendResponseDTO(Collections.emptyList());
        List<ServiceHealthDTO> services = Collections.emptyList();

        when(platformMonitorService.getHealth()).thenReturn(health);
        when(platformMonitorService.getOnlineUserTrend()).thenReturn(trend);
        when(platformMonitorService.getServices()).thenReturn(services);

        // When & Then - GET should work
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk());
    }

    @Test
    void allEndpoints_shouldCallServiceOnce() throws Exception {
        // Given
        SystemHealthDTO health = new SystemHealthDTO(Collections.emptyList());
        OnlineUserTrendResponseDTO trend = new OnlineUserTrendResponseDTO(Collections.emptyList());
        List<ServiceHealthDTO> services = Collections.emptyList();

        when(platformMonitorService.getHealth()).thenReturn(health);
        when(platformMonitorService.getOnlineUserTrend()).thenReturn(trend);
        when(platformMonitorService.getServices()).thenReturn(services);

        // When
        mockMvc.perform(get("/api/monitor/v1/health"));
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"));
        mockMvc.perform(get("/api/monitor/v1/services"));

        // Then
        verify(platformMonitorService, times(1)).getHealth();
        verify(platformMonitorService, times(1)).getOnlineUserTrend();
        verify(platformMonitorService, times(1)).getServices();
    }
}
