package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.PlatformDashboardStatsDTO;
import com.zhitu.platform.dto.SecurityLogDTO;
import com.zhitu.platform.dto.TenantDTO;
import com.zhitu.platform.service.OperationLogService;
import com.zhitu.platform.service.PlatformService;
import com.zhitu.platform.service.SecurityLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlatformSystemController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlatformSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformService platformService;

    @MockBean
    private OperationLogService operationLogService;

    @MockBean
    private SecurityLogService securityLogService;

    @Test
    void getDashboardStats_shouldReturnStatsSuccessfully() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                150L,   // totalTenantCount
                50000L, // totalUserCount
                12000L, // activeUserCount
                800L,   // totalEnterpriseCount
                25L     // pendingAuditCount
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.totalTenantCount").value(150))
                .andExpect(jsonPath("$.data.totalUserCount").value(50000))
                .andExpect(jsonPath("$.data.activeUserCount").value(12000))
                .andExpect(jsonPath("$.data.totalEnterpriseCount").value(800))
                .andExpect(jsonPath("$.data.pendingAuditCount").value(25));

        verify(platformService, times(1)).getDashboardStats();
    }

    @Test
    void getDashboardStats_shouldReturnZeroValues() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                0L, 0L, 0L, 0L, 0L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTenantCount").value(0))
                .andExpect(jsonPath("$.data.totalUserCount").value(0))
                .andExpect(jsonPath("$.data.activeUserCount").value(0))
                .andExpect(jsonPath("$.data.totalEnterpriseCount").value(0))
                .andExpect(jsonPath("$.data.pendingAuditCount").value(0));

        verify(platformService, times(1)).getDashboardStats();
    }

    @Test
    void getDashboardStats_shouldReturnCorrectResponseStructure() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                100L, 10000L, 3000L, 500L, 10L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalTenantCount").exists())
                .andExpect(jsonPath("$.data.totalUserCount").exists())
                .andExpect(jsonPath("$.data.activeUserCount").exists())
                .andExpect(jsonPath("$.data.totalEnterpriseCount").exists())
                .andExpect(jsonPath("$.data.pendingAuditCount").exists());
    }

    @Test
    void getDashboardStats_shouldHandleLargeNumbers() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                10000L,
                1000000L,
                500000L,
                50000L,
                3000L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTenantCount").value(10000))
                .andExpect(jsonPath("$.data.totalUserCount").value(1000000))
                .andExpect(jsonPath("$.data.activeUserCount").value(500000))
                .andExpect(jsonPath("$.data.totalEnterpriseCount").value(50000))
                .andExpect(jsonPath("$.data.pendingAuditCount").value(3000));
    }

    @Test
    void getDashboardStats_shouldUseCorrectEndpoint() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                150L, 50000L, 12000L, 800L, 25L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then - Correct endpoint should work
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk());

        // Wrong endpoints return error (handled by global exception handler)
        mockMvc.perform(get("/api/system/v1/dashboard"))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get("/api/system/v1/stats"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getDashboardStats_shouldUseGetMethod() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                150L, 50000L, 12000L, 800L, 25L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then - GET should work
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk());

        // POST should not be allowed (handled by global exception handler)
        mockMvc.perform(post("/api/system/v1/dashboard/stats"))
                .andExpect(status().is5xxServerError());

        // PUT should not be allowed
        mockMvc.perform(put("/api/system/v1/dashboard/stats"))
                .andExpect(status().is5xxServerError());

        // DELETE should not be allowed
        mockMvc.perform(delete("/api/system/v1/dashboard/stats"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getDashboardStats_shouldReturnJsonContentType() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                150L, 50000L, 12000L, 800L, 25L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getDashboardStats_shouldCallServiceOnce() throws Exception {
        // Given
        PlatformDashboardStatsDTO stats = new PlatformDashboardStatsDTO(
                150L, 50000L, 12000L, 800L, 25L
        );

        when(platformService.getDashboardStats()).thenReturn(stats);

        // When
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk());

        // Then
        verify(platformService, times(1)).getDashboardStats();
        verifyNoMoreInteractions(platformService);
    }

    // ── Tenant List Tests ─────────────────────────────────────────────────────

    @Test
    void getTenantList_shouldReturnTenantsWithoutFilters() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "College A", 1, 1, 50L);
        TenantDTO tenant2 = createTenantDTO(2L, "Enterprise B", 2, 1, 30L);

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(tenant1, tenant2));
        page.setTotal(2);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("College A"))
                .andExpect(jsonPath("$.data.records[0].type").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value(1))
                .andExpect(jsonPath("$.data.records[0].userCount").value(50));

        verify(platformService, times(1)).getTenantList(null, null, 1, 10);
    }

    @Test
    void getTenantList_shouldFilterByType() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "College A", 1, 1, 50L);
        TenantDTO tenant2 = createTenantDTO(2L, "College B", 1, 1, 30L);

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(tenant1, tenant2));
        page.setTotal(2);

        when(platformService.getTenantList("1", null, 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].type").value(1))
                .andExpect(jsonPath("$.data.records[1].type").value(1));

        verify(platformService, times(1)).getTenantList("1", null, 1, 10);
    }

    @Test
    void getTenantList_shouldFilterByStatus() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "College A", 1, 1, 50L);

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of(tenant1));
        page.setTotal(1);

        when(platformService.getTenantList(null, "1", 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value(1));

        verify(platformService, times(1)).getTenantList(null, "1", 1, 10);
    }

    @Test
    void getTenantList_shouldFilterByTypeAndStatus() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "Enterprise A", 2, 1, 50L);

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of(tenant1));
        page.setTotal(1);

        when(platformService.getTenantList("2", "1", 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("type", "2")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value(2))
                .andExpect(jsonPath("$.data.records[0].status").value(1));

        verify(platformService, times(1)).getTenantList("2", "1", 1, 10);
    }

    @Test
    void getTenantList_shouldSupportPagination() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "College A", 1, 1, 50L);
        TenantDTO tenant2 = createTenantDTO(2L, "College B", 1, 1, 30L);

        Page<TenantDTO> page = new Page<>(2, 5);
        page.setRecords(Arrays.asList(tenant1, tenant2));
        page.setTotal(12);

        when(platformService.getTenantList(null, null, 2, 5)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.records.length()").value(2));

        verify(platformService, times(1)).getTenantList(null, null, 2, 5);
    }

    @Test
    void getTenantList_shouldUseDefaultPaginationWhenNotProvided() throws Exception {
        // Given
        TenantDTO tenant1 = createTenantDTO(1L, "College A", 1, 1, 50L);

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of(tenant1));
        page.setTotal(1);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then - No page/size params should use defaults
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));

        verify(platformService, times(1)).getTenantList(null, null, 1, 10);
    }

    @Test
    void getTenantList_shouldIncludeAllRequiredFields() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        TenantDTO tenant = new TenantDTO(
                1L,
                "College A",
                1,
                1,
                now,
                50L,
                null,  // subscriptionPlan (placeholder)
                null   // expirationDate (placeholder)
        );

        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of(tenant));
        page.setTotal(1);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("College A"))
                .andExpect(jsonPath("$.data.records[0].type").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value(1))
                .andExpect(jsonPath("$.data.records[0].createdAt").exists())
                .andExpect(jsonPath("$.data.records[0].userCount").value(50))
                .andExpect(jsonPath("$.data.records[0].subscriptionPlan").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].expirationDate").doesNotExist());

        verify(platformService, times(1)).getTenantList(null, null, 1, 10);
    }

    @Test
    void getTenantList_shouldHandleEmptyResults() throws Exception {
        // Given
        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(platformService, times(1)).getTenantList(null, null, 1, 10);
    }

    @Test
    void getTenantList_shouldUseCorrectEndpoint() throws Exception {
        // Given
        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then - Correct endpoint should work
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk());

        // Wrong endpoints return error
        mockMvc.perform(get("/api/system/v1/tenants"))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get("/api/system/v1/colleges"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getTenantList_shouldUseGetMethod() throws Exception {
        // Given
        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then - GET should work
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk());

        // POST should not be allowed
        mockMvc.perform(post("/api/system/v1/tenants/colleges"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getTenantList_shouldReturnJsonContentType() throws Exception {
        // Given
        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void getTenantList_shouldCallServiceOnce() throws Exception {
        // Given
        Page<TenantDTO> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(platformService.getTenantList(null, null, 1, 10)).thenReturn(page);

        // When
        mockMvc.perform(get("/api/system/v1/tenants/colleges"))
                .andExpect(status().isOk());

        // Then
        verify(platformService, times(1)).getTenantList(null, null, 1, 10);
        verifyNoMoreInteractions(platformService);
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private TenantDTO createTenantDTO(Long id, String name, Integer type, Integer status, Long userCount) {
        return new TenantDTO(
                id,
                name,
                type,
                status,
                OffsetDateTime.now(),
                userCount,
                null,  // subscriptionPlan
                null   // expirationDate
        );
    }

    // ── Security Logs Tests ───────────────────────────────────────────────────

    @Test
    void getSecurityLogs_shouldReturnLogsWithoutFilter() throws Exception {
        // Given
        SecurityLogDTO log1 = createSecurityLogDTO(1L, "warning", "login_failed");
        SecurityLogDTO log2 = createSecurityLogDTO(2L, "critical", "suspicious_activity");

        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(2L, Arrays.asList(log1, log2), 1, 20);

        when(securityLogService.getSecurityLogs(null, 1, 20)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].eventType").value("login_failed"))
                .andExpect(jsonPath("$.data.records[1].eventType").value("suspicious_activity"));

        verify(securityLogService, times(1)).getSecurityLogs(null, 1, 20);
    }

    @Test
    void getSecurityLogs_shouldFilterByLevel() throws Exception {
        // Given
        SecurityLogDTO log1 = createSecurityLogDTO(1L, "warning", "login_failed");

        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(1L, List.of(log1), 1, 20);

        when(securityLogService.getSecurityLogs("warning", 1, 20)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security")
                        .param("level", "warning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].level").value("warning"));

        verify(securityLogService, times(1)).getSecurityLogs("warning", 1, 20);
    }

    @Test
    void getSecurityLogs_shouldSupportPagination() throws Exception {
        // Given
        SecurityLogDTO log1 = createSecurityLogDTO(1L, "warning", "login_failed");

        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(25L, List.of(log1), 2, 10);

        when(securityLogService.getSecurityLogs(null, 2, 10)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").value(25));

        verify(securityLogService, times(1)).getSecurityLogs(null, 2, 10);
    }

    @Test
    void getSecurityLogs_shouldUseDefaultPagination() throws Exception {
        // Given
        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(0L, List.of(), 1, 20);

        when(securityLogService.getSecurityLogs(null, 1, 20)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));

        verify(securityLogService, times(1)).getSecurityLogs(null, 1, 20);
    }

    @Test
    void getSecurityLogs_shouldIncludeAllRequiredFields() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        SecurityLogDTO log = new SecurityLogDTO(
                1L,
                "warning",
                "login_failed",
                100L,
                "192.168.1.1",
                "Failed login attempt",
                "{\"username\":\"test\"}",
                now
        );

        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(1L, List.of(log), 1, 20);

        when(securityLogService.getSecurityLogs(null, 1, 20)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].level").value("warning"))
                .andExpect(jsonPath("$.data.records[0].eventType").value("login_failed"))
                .andExpect(jsonPath("$.data.records[0].userId").value(100))
                .andExpect(jsonPath("$.data.records[0].ipAddress").value("192.168.1.1"))
                .andExpect(jsonPath("$.data.records[0].description").value("Failed login attempt"))
                .andExpect(jsonPath("$.data.records[0].details").value("{\"username\":\"test\"}"))
                .andExpect(jsonPath("$.data.records[0].createdAt").exists());

        verify(securityLogService, times(1)).getSecurityLogs(null, 1, 20);
    }

    @Test
    void getSecurityLogs_shouldHandleEmptyResults() throws Exception {
        // Given
        com.zhitu.common.core.result.PageResult<SecurityLogDTO> pageResult = 
            PageResult.of(0L, List.of(), 1, 20);

        when(securityLogService.getSecurityLogs(null, 1, 20)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(securityLogService, times(1)).getSecurityLogs(null, 1, 20);
    }

    private SecurityLogDTO createSecurityLogDTO(Long id, String level, String eventType) {
        return new SecurityLogDTO(
                id,
                level,
                eventType,
                100L,
                "192.168.1.1",
                "Test description",
                null,
                OffsetDateTime.now()
        );
    }
}
