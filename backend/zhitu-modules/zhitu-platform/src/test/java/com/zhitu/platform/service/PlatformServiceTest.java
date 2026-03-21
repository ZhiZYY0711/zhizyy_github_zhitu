package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.PlatformDashboardStatsDTO;
import com.zhitu.platform.dto.TenantDTO;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.SysTenant;
import com.zhitu.platform.entity.SysUser;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.mapper.EnterpriseInfoMapper;
import com.zhitu.platform.mapper.SysTenantMapper;
import com.zhitu.platform.mapper.SysUserMapper;
import com.zhitu.platform.mapper.TrainingProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformServiceTest {

    @Mock
    private TrainingProjectMapper trainingProjectMapper;

    @Mock
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Mock
    private SysTenantMapper sysTenantMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlatformService platformService;

    @BeforeEach
    void setUp() {
        // Mock cache service to always execute the supplier (lenient for tests that don't use cache)
        lenient().when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(3);
                    return supplier.get();
                });
    }

    @Test
    void getDashboardStats_shouldReturnCorrectStatistics() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)  // Total tenants (excluding platform)
                .thenReturn(800L); // Total enterprises (type=2)

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L); // Total users

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L); // Active users (last 30 days)

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(15L); // Pending enterprise audits

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(10L); // Pending project audits

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(150L, result.getTotalTenantCount());
        assertEquals(50000L, result.getTotalUserCount());
        assertEquals(12000L, result.getActiveUserCount());
        assertEquals(800L, result.getTotalEnterpriseCount());
        assertEquals(25L, result.getPendingAuditCount()); // 15 + 10

        // Verify cache was used with correct parameters
        verify(cacheService).getOrSet(
                eq("platform:dashboard:stats"),
                eq(10L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void getDashboardStats_shouldHandleZeroValues() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L)  // No tenants
                .thenReturn(0L); // No enterprises

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(0L); // No users

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L); // No active users

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L); // No pending enterprise audits

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L); // No pending project audits

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getTotalTenantCount());
        assertEquals(0L, result.getTotalUserCount());
        assertEquals(0L, result.getActiveUserCount());
        assertEquals(0L, result.getTotalEnterpriseCount());
        assertEquals(0L, result.getPendingAuditCount());
    }

    @Test
    void getDashboardStats_shouldCalculatePendingAuditCountCorrectly() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(100L)
                .thenReturn(500L);

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(10000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(3000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(20L); // Pending enterprise audits

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(5L); // Pending project audits

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertEquals(25L, result.getPendingAuditCount(), "Pending audit count should be sum of enterprise and project audits");
    }

    @Test
    void getDashboardStats_shouldExcludePlatformTenantsFromTotalCount() {
        // Given - Mock tenant count query (excluding type=0)
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)  // Total tenants (excluding platform type=0)
                .thenReturn(800L); // Total enterprises

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertEquals(150L, result.getTotalTenantCount());

        // Verify the query excludes platform tenants (type != 0)
        verify(sysTenantMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getDashboardStats_shouldCountOnlyEnterpriseTypeForEnterpriseCount() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)  // Total tenants
                .thenReturn(800L); // Only enterprises (type=2)

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertEquals(800L, result.getTotalEnterpriseCount());

        // Verify the query filters by enterprise type (type=2)
        verify(sysTenantMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getDashboardStats_shouldCountActiveUsersWithin30Days() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)
                .thenReturn(800L);

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L); // Total users

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L); // Active users (last 30 days)

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertEquals(50000L, result.getTotalUserCount());
        assertEquals(12000L, result.getActiveUserCount());
        assertTrue(result.getActiveUserCount() <= result.getTotalUserCount(),
                "Active users should not exceed total users");

        // Verify active user query uses date filter
        verify(sysUserMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getDashboardStats_shouldUseCacheWithCorrectTTL() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)
                .thenReturn(800L);

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(15L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(10L);

        // When
        platformService.getDashboardStats();

        // Then
        verify(cacheService).getOrSet(
                eq("platform:dashboard:stats"),
                eq(10L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void getDashboardStats_shouldHandleLargeNumbers() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(10000L)
                .thenReturn(50000L);

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(1000000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(500000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1000L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(2000L);

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(10000L, result.getTotalTenantCount());
        assertEquals(1000000L, result.getTotalUserCount());
        assertEquals(500000L, result.getActiveUserCount());
        assertEquals(50000L, result.getTotalEnterpriseCount());
        assertEquals(3000L, result.getPendingAuditCount());
    }

    @Test
    void getDashboardStats_shouldReturnConsistentDataStructure() {
        // Given
        when(sysTenantMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L)
                .thenReturn(800L);

        when(sysUserMapper.selectCount(isNull()))
                .thenReturn(50000L);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(12000L);

        when(enterpriseInfoMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(15L);

        when(trainingProjectMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(10L);

        // When
        PlatformDashboardStatsDTO result = platformService.getDashboardStats();

        // Then
        assertNotNull(result);
        assertNotNull(result.getTotalTenantCount());
        assertNotNull(result.getTotalUserCount());
        assertNotNull(result.getActiveUserCount());
        assertNotNull(result.getTotalEnterpriseCount());
        assertNotNull(result.getPendingAuditCount());
    }

    // ── Tenant List Tests ─────────────────────────────────────────────────────

    @Test
    void getTenantList_shouldReturnAllTenantsWithoutFilters() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);
        SysTenant tenant2 = createTenant(2L, "Enterprise B", 2, 1);
        SysTenant tenant3 = createTenant(3L, "College C", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2, tenant3));
        mockPage.setTotal(3);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)  // tenant1 users
                .thenReturn(30L)  // tenant2 users
                .thenReturn(20L); // tenant3 users

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getRecords().size());
        assertEquals(3, result.getTotal());

        TenantDTO dto1 = result.getRecords().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("College A", dto1.getName());
        assertEquals(1, dto1.getType());
        assertEquals(1, dto1.getStatus());
        assertEquals(50L, dto1.getUserCount());

        // Verify query excludes platform tenants (type != 0)
        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(sysUserMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldFilterByType() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);
        SysTenant tenant2 = createTenant(2L, "College B", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2));
        mockPage.setTotal(2);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)
                .thenReturn(30L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList("1", null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        result.getRecords().forEach(dto -> assertEquals(1, dto.getType()));

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldFilterByStatus() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);
        SysTenant tenant2 = createTenant(2L, "Enterprise B", 2, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2));
        mockPage.setTotal(2);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)
                .thenReturn(30L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, "1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        result.getRecords().forEach(dto -> assertEquals(1, dto.getStatus()));

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldFilterByTypeAndStatus() {
        // Given
        SysTenant tenant1 = createTenant(1L, "Enterprise A", 2, 1);
        SysTenant tenant2 = createTenant(2L, "Enterprise B", 2, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2));
        mockPage.setTotal(2);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)
                .thenReturn(30L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList("2", "1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        result.getRecords().forEach(dto -> {
            assertEquals(2, dto.getType());
            assertEquals(1, dto.getStatus());
        });

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldSupportPagination() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);
        SysTenant tenant2 = createTenant(2L, "College B", 1, 1);

        Page<SysTenant> mockPage = new Page<>(2, 5);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2));
        mockPage.setTotal(12);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)
                .thenReturn(30L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 2, 5);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(12, result.getTotal());
        assertEquals(2, result.getCurrent());
        assertEquals(5, result.getSize());

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldIncludeUserCount() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(tenant1));
        mockPage.setTotal(1);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(150L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        TenantDTO dto = result.getRecords().get(0);
        assertEquals(150L, dto.getUserCount());

        // Verify user count query for tenant
        verify(sysUserMapper).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldIncludeAllRequiredFields() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        SysTenant tenant = createTenant(1L, "College A", 1, 1);
        tenant.setCreatedAt(now);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(tenant));
        mockPage.setTotal(1);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 1, 10);

        // Then
        assertNotNull(result);
        TenantDTO dto = result.getRecords().get(0);

        // Requirement 30.4: Include tenant name, type, status, creation date, and user count
        assertNotNull(dto.getId());
        assertNotNull(dto.getName());
        assertNotNull(dto.getType());
        assertNotNull(dto.getStatus());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUserCount());

        assertEquals(1L, dto.getId());
        assertEquals("College A", dto.getName());
        assertEquals(1, dto.getType());
        assertEquals(1, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(50L, dto.getUserCount());

        // Requirement 30.5: Include subscription plan and expiration date (placeholders for now)
        // These fields exist but are null until subscription feature is implemented
        assertNull(dto.getSubscriptionPlan());
        assertNull(dto.getExpirationDate());
    }

    @Test
    void getTenantList_shouldHandleEmptyResults() {
        // Given
        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
        assertEquals(0, result.getTotal());

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(sysUserMapper, never()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldHandleInvalidTypeParameter() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(tenant1));
        mockPage.setTotal(1);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L);

        // When - invalid type parameter should be ignored
        IPage<TenantDTO> result = platformService.getTenantList("invalid", null, 1, 10);

        // Then - should still return results (filter ignored)
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldHandleInvalidStatusParameter() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(tenant1));
        mockPage.setTotal(1);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L);

        // When - invalid status parameter should be ignored
        IPage<TenantDTO> result = platformService.getTenantList(null, "invalid", 1, 10);

        // Then - should still return results (filter ignored)
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldUseDefaultPaginationWhenNotProvided() {
        // Given
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(tenant1));
        mockPage.setTotal(1);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L);

        // When - null page and size should use defaults (1, 10)
        IPage<TenantDTO> result = platformService.getTenantList(null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getTenantList_shouldExcludePlatformTenants() {
        // Given - Platform tenant (type=0) should be excluded
        SysTenant tenant1 = createTenant(1L, "College A", 1, 1);
        SysTenant tenant2 = createTenant(2L, "Enterprise B", 2, 1);

        Page<SysTenant> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(tenant1, tenant2));
        mockPage.setTotal(2);

        when(sysTenantMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        when(sysUserMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(50L)
                .thenReturn(30L);

        // When
        IPage<TenantDTO> result = platformService.getTenantList(null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());

        // Verify no platform tenants (type=0) in results
        result.getRecords().forEach(dto -> assertNotEquals(0, dto.getType()));

        verify(sysTenantMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private SysTenant createTenant(Long id, String name, Integer type, Integer status) {
        SysTenant tenant = new SysTenant();
        tenant.setId(id);
        tenant.setName(name);
        tenant.setType(type);
        tenant.setStatus(status);
        tenant.setCreatedAt(OffsetDateTime.now());
        tenant.setUpdatedAt(OffsetDateTime.now());
        tenant.setIsDeleted(false);
        return tenant;
    }
}
