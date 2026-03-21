package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.platform.dto.AuditEnterpriseRequest;
import com.zhitu.platform.dto.AuditProjectRequest;
import com.zhitu.platform.dto.EnterpriseAuditDTO;
import com.zhitu.platform.dto.ProjectAuditDTO;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.SysTenant;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.mapper.EnterpriseInfoMapper;
import com.zhitu.platform.mapper.SysTenantMapper;
import com.zhitu.platform.mapper.TrainingProjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAuditServiceTest {

    @Mock
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Mock
    private SysTenantMapper sysTenantMapper;

    @Mock
    private TrainingProjectMapper trainingProjectMapper;

    @InjectMocks
    private PlatformAuditService platformAuditService;

    // ── getEnterpriseAudits Tests ─────────────────────────────────────────────

    @Test
    void getEnterpriseAudits_shouldReturnAllAuditsWithoutStatusFilter() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 0);
        EnterpriseInfo enterprise2 = createEnterpriseInfo(2L, 20L, "Enterprise B", 1);
        EnterpriseInfo enterprise3 = createEnterpriseInfo(3L, 30L, "Enterprise C", 2);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(enterprise1, enterprise2, enterprise3));
        mockPage.setTotal(3);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getRecords().size());
        assertEquals(3, result.getTotal());

        EnterpriseAuditDTO dto1 = result.getRecords().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals(10L, dto1.getTenantId());
        assertEquals("Enterprise A", dto1.getEnterpriseName());
        assertEquals(0, dto1.getAuditStatus());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldFilterByPendingStatus() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 0);
        EnterpriseInfo enterprise2 = createEnterpriseInfo(2L, 20L, "Enterprise B", 0);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(enterprise1, enterprise2));
        mockPage.setTotal(2);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(0, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        result.getRecords().forEach(dto -> assertEquals(0, dto.getAuditStatus()));

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldFilterByPassedStatus() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 1);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(enterprise1));
        mockPage.setTotal(1);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(1, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getRecords().get(0).getAuditStatus());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldFilterByRejectedStatus() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 2);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(enterprise1));
        mockPage.setTotal(1);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(2, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(2, result.getRecords().get(0).getAuditStatus());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldSupportPagination() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 0);
        EnterpriseInfo enterprise2 = createEnterpriseInfo(2L, 20L, "Enterprise B", 0);

        Page<EnterpriseInfo> mockPage = new Page<>(2, 5);
        mockPage.setRecords(Arrays.asList(enterprise1, enterprise2));
        mockPage.setTotal(12);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(null, 2, 5);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(12, result.getTotal());
        assertEquals(2, result.getCurrent());
        assertEquals(5, result.getSize());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldIncludeAllRequiredFields() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        EnterpriseInfo enterprise = createEnterpriseInfo(1L, 10L, "Enterprise A", 0);
        enterprise.setEnterpriseCode("LICENSE123");
        enterprise.setContactName("John Doe");
        enterprise.setContactPhone("13800138000");
        enterprise.setIndustry("Technology");
        enterprise.setCity("Beijing");
        enterprise.setCreatedAt(now);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(enterprise));
        mockPage.setTotal(1);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        EnterpriseAuditDTO dto = result.getRecords().get(0);

        // Requirement 31.7: Include enterprise name, business license, contact person, and submission date
        assertEquals("Enterprise A", dto.getEnterpriseName());
        assertEquals("LICENSE123", dto.getBusinessLicense());
        assertEquals("John Doe", dto.getContactPerson());
        assertEquals("13800138000", dto.getContactPhone());
        assertEquals("Technology", dto.getIndustry());
        assertEquals("Beijing", dto.getCity());
        assertEquals(now, dto.getSubmissionDate());
    }

    @Test
    void getEnterpriseAudits_shouldHandleEmptyResults() {
        // Given
        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
        assertEquals(0, result.getTotal());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getEnterpriseAudits_shouldUseDefaultPaginationWhenNotProvided() {
        // Given
        EnterpriseInfo enterprise1 = createEnterpriseInfo(1L, 10L, "Enterprise A", 0);

        Page<EnterpriseInfo> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(enterprise1));
        mockPage.setTotal(1);

        when(enterpriseInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<EnterpriseAuditDTO> result = platformAuditService.getEnterpriseAudits(null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());

        verify(enterpriseInfoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ── auditEnterprise Tests ─────────────────────────────────────────────────

    @Test
    void auditEnterprise_shouldPassAuditAndActivateAccount() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, tenantId, "Enterprise A", 0);
        SysTenant tenant = createTenant(tenantId, "Enterprise A", 2, 0);

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);
        when(sysTenantMapper.selectById(tenantId))
                .thenReturn(tenant);
        when(enterpriseInfoMapper.updateById(any(EnterpriseInfo.class)))
                .thenReturn(1);
        when(sysTenantMapper.updateById(any(SysTenant.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditEnterprise(tenantId, request);

        // Then
        assertEquals(1, enterpriseInfo.getAuditStatus()); // 1 = passed
        assertEquals("审核通过", enterpriseInfo.getAuditRemark());
        assertEquals(1, tenant.getStatus()); // 1 = active

        verify(enterpriseInfoMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(enterpriseInfoMapper).updateById((EnterpriseInfo) any());
        verify(sysTenantMapper).selectById(tenantId);
        verify(sysTenantMapper).updateById((SysTenant) any());
    }

    @Test
    void auditEnterprise_shouldRejectAuditWithReason() {
        // Given
        Long tenantId = 10L;
        String rejectReason = "Business license is invalid";
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("reject", rejectReason);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, tenantId, "Enterprise A", 0);

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);
        when(enterpriseInfoMapper.updateById(any(EnterpriseInfo.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditEnterprise(tenantId, request);

        // Then
        assertEquals(2, enterpriseInfo.getAuditStatus()); // 2 = rejected
        assertEquals(rejectReason, enterpriseInfo.getAuditRemark());

        verify(enterpriseInfoMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(enterpriseInfoMapper).updateById((EnterpriseInfo) any());
        verify(sysTenantMapper, never()).selectById(any());
        verify(sysTenantMapper, never()).updateById((SysTenant) any());
    }

    @Test
    void auditEnterprise_shouldThrowExceptionWhenRejectReasonMissing() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("reject", null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditEnterprise(tenantId, request);
        });

        assertEquals("reject_reason is required when action is 'reject'", exception.getMessage());

        verify(enterpriseInfoMapper, never()).selectOne(any());
        verify(enterpriseInfoMapper, never()).updateById((EnterpriseInfo) any());
    }

    @Test
    void auditEnterprise_shouldThrowExceptionWhenRejectReasonEmpty() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("reject", "   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditEnterprise(tenantId, request);
        });

        assertEquals("reject_reason is required when action is 'reject'", exception.getMessage());

        verify(enterpriseInfoMapper, never()).selectOne(any());
    }

    @Test
    void auditEnterprise_shouldThrowExceptionWhenActionInvalid() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("invalid", null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditEnterprise(tenantId, request);
        });

        assertEquals("Invalid action: must be 'pass' or 'reject'", exception.getMessage());

        verify(enterpriseInfoMapper, never()).selectOne(any());
    }

    @Test
    void auditEnterprise_shouldThrowExceptionWhenEnterpriseNotFound() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditEnterprise(tenantId, request);
        });

        assertEquals("Enterprise not found with tenant_id: " + tenantId, exception.getMessage());

        verify(enterpriseInfoMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(enterpriseInfoMapper, never()).updateById((EnterpriseInfo) any());
    }

    @Test
    void auditEnterprise_shouldThrowExceptionWhenAlreadyAudited() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, tenantId, "Enterprise A", 1); // Already passed

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            platformAuditService.auditEnterprise(tenantId, request);
        });

        assertEquals("Enterprise has already been audited", exception.getMessage());

        verify(enterpriseInfoMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(enterpriseInfoMapper, never()).updateById((EnterpriseInfo) any());
    }

    @Test
    void auditEnterprise_shouldHandleTenantNotFoundDuringActivation() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, tenantId, "Enterprise A", 0);

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);
        when(sysTenantMapper.selectById(tenantId))
                .thenReturn(null); // Tenant not found
        when(enterpriseInfoMapper.updateById(any(EnterpriseInfo.class)))
                .thenReturn(1);

        // When - should not throw exception, just log warning
        platformAuditService.auditEnterprise(tenantId, request);

        // Then
        assertEquals(1, enterpriseInfo.getAuditStatus());

        verify(enterpriseInfoMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(enterpriseInfoMapper).updateById((EnterpriseInfo) any());
        verify(sysTenantMapper).selectById(tenantId);
        verify(sysTenantMapper, never()).updateById((SysTenant) any());
    }

    @Test
    void auditEnterprise_shouldUpdateTenantTimestamp() {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, tenantId, "Enterprise A", 0);
        SysTenant tenant = createTenant(tenantId, "Enterprise A", 2, 0);
        OffsetDateTime originalTime = tenant.getUpdatedAt();

        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);
        when(sysTenantMapper.selectById(tenantId))
                .thenReturn(tenant);
        when(enterpriseInfoMapper.updateById(any(EnterpriseInfo.class)))
                .thenReturn(1);
        when(sysTenantMapper.updateById(any(SysTenant.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditEnterprise(tenantId, request);

        // Then
        assertNotNull(tenant.getUpdatedAt());
        assertTrue(tenant.getUpdatedAt().isAfter(originalTime) || tenant.getUpdatedAt().isEqual(originalTime));

        verify(sysTenantMapper).updateById((SysTenant) any());
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private EnterpriseInfo createEnterpriseInfo(Long id, Long tenantId, String name, Integer auditStatus) {
        EnterpriseInfo info = new EnterpriseInfo();
        info.setId(id);
        info.setTenantId(tenantId);
        info.setEnterpriseName(name);
        info.setEnterpriseCode("LICENSE" + id);
        info.setIndustry("Technology");
        info.setCity("Beijing");
        info.setContactName("Contact " + id);
        info.setContactPhone("138" + String.format("%08d", id));
        info.setAuditStatus(auditStatus);
        info.setCreatedAt(OffsetDateTime.now());
        info.setUpdatedAt(OffsetDateTime.now());
        return info;
    }

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

    // ── getProjectAudits Tests ────────────────────────────────────────────────

    @Test
    void getProjectAudits_shouldReturnAllAuditsWithoutStatusFilter() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 0);
        TrainingProject project2 = createTrainingProject(2L, 20L, "Project B", 1);
        TrainingProject project3 = createTrainingProject(3L, 30L, "Project C", 2);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(project1, project2, project3));
        mockPage.setTotal(3);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getRecords().size());
        assertEquals(3, result.getTotal());

        ProjectAuditDTO dto1 = result.getRecords().get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Project A", dto1.getProjectName());
        assertEquals(0, dto1.getAuditStatus());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldFilterByPendingStatus() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 0);
        TrainingProject project2 = createTrainingProject(2L, 20L, "Project B", 0);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(project1, project2));
        mockPage.setTotal(2);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(0, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        result.getRecords().forEach(dto -> assertEquals(0, dto.getAuditStatus()));

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldFilterByPassedStatus() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 1);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project1));
        mockPage.setTotal(1);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(1, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getRecords().get(0).getAuditStatus());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldFilterByRejectedStatus() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 2);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project1));
        mockPage.setTotal(1);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(2, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(2, result.getRecords().get(0).getAuditStatus());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldSupportPagination() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 0);
        TrainingProject project2 = createTrainingProject(2L, 20L, "Project B", 0);

        Page<TrainingProject> mockPage = new Page<>(2, 5);
        mockPage.setRecords(Arrays.asList(project1, project2));
        mockPage.setTotal(12);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, 2, 5);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals(12, result.getTotal());
        assertEquals(2, result.getCurrent());
        assertEquals(5, result.getSize());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldIncludeAllRequiredFields() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        TrainingProject project = createTrainingProject(1L, 10L, "Project A", 0);
        project.setDescription("A great training project");
        project.setTechStack("Java, Spring Boot, React");
        project.setIndustry("Technology");
        project.setStartDate(LocalDate.of(2024, 1, 1));
        project.setEndDate(LocalDate.of(2024, 6, 30));
        project.setCreatedAt(now);

        EnterpriseInfo enterpriseInfo = createEnterpriseInfo(1L, 10L, "Enterprise A", 1);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project));
        mockPage.setTotal(1);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);
        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(enterpriseInfo);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        ProjectAuditDTO dto = result.getRecords().get(0);

        // Requirement 32.7: Include project name, creator, submission date, and description
        assertEquals("Project A", dto.getProjectName());
        assertEquals("A great training project", dto.getDescription());
        assertEquals(10L, dto.getEnterpriseId());
        assertEquals("Enterprise A", dto.getEnterpriseName());
        assertEquals("Java, Spring Boot, React", dto.getTechStack());
        assertEquals("Technology", dto.getIndustry());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2024, 6, 30), dto.getEndDate());
        assertEquals(now, dto.getSubmissionDate());
    }

    @Test
    void getProjectAudits_shouldHandleEmptyResults() {
        // Given
        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
        assertEquals(0, result.getTotal());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldUseDefaultPaginationWhenNotProvided() {
        // Given
        TrainingProject project1 = createTrainingProject(1L, 10L, "Project A", 0);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project1));
        mockPage.setTotal(1);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());

        verify(trainingProjectMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getProjectAudits_shouldHandleMissingEnterpriseInfo() {
        // Given
        TrainingProject project = createTrainingProject(1L, 10L, "Project A", 0);

        Page<TrainingProject> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project));
        mockPage.setTotal(1);

        when(trainingProjectMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);
        when(enterpriseInfoMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        // When
        IPage<ProjectAuditDTO> result = platformAuditService.getProjectAudits(null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        ProjectAuditDTO dto = result.getRecords().get(0);
        assertEquals("Project A", dto.getProjectName());
        assertNull(dto.getEnterpriseName()); // Should be null when enterprise not found
    }

    // ── auditProject Tests ────────────────────────────────────────────────────

    @Test
    void auditProject_shouldPassAuditWithoutQualityRating() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        TrainingProject project = createTrainingProject(projectId, 10L, "Project A", 0);

        when(trainingProjectMapper.selectById(projectId))
                .thenReturn(project);
        when(trainingProjectMapper.updateById(any(TrainingProject.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditProject(projectId, request);

        // Then
        assertEquals(1, project.getAuditStatus()); // 1 = passed
        assertNotNull(project.getUpdatedAt());

        verify(trainingProjectMapper).selectById(projectId);
        verify(trainingProjectMapper).updateById((TrainingProject) any());
    }

    @Test
    void auditProject_shouldPassAuditWithQualityRating() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", 4, null);

        TrainingProject project = createTrainingProject(projectId, 10L, "Project A", 0);

        when(trainingProjectMapper.selectById(projectId))
                .thenReturn(project);
        when(trainingProjectMapper.updateById(any(TrainingProject.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditProject(projectId, request);

        // Then
        assertEquals(1, project.getAuditStatus()); // 1 = passed
        assertNotNull(project.getUpdatedAt());

        verify(trainingProjectMapper).selectById(projectId);
        verify(trainingProjectMapper).updateById((TrainingProject) any());
    }

    @Test
    void auditProject_shouldRejectAuditWithReason() {
        // Given
        Long projectId = 1L;
        String rejectReason = "Content is inappropriate";
        AuditProjectRequest request = new AuditProjectRequest("reject", null, rejectReason);

        TrainingProject project = createTrainingProject(projectId, 10L, "Project A", 0);

        when(trainingProjectMapper.selectById(projectId))
                .thenReturn(project);
        when(trainingProjectMapper.updateById(any(TrainingProject.class)))
                .thenReturn(1);

        // When
        platformAuditService.auditProject(projectId, request);

        // Then
        assertEquals(2, project.getAuditStatus()); // 2 = rejected
        assertNotNull(project.getUpdatedAt());

        verify(trainingProjectMapper).selectById(projectId);
        verify(trainingProjectMapper).updateById((TrainingProject) any());
    }

    @Test
    void auditProject_shouldThrowExceptionWhenRejectReasonMissing() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("reject", null, null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditProject(projectId, request);
        });

        assertEquals("reject_reason is required when action is 'reject'", exception.getMessage());

        verify(trainingProjectMapper, never()).selectById(any());
        verify(trainingProjectMapper, never()).updateById((TrainingProject) any());
    }

    @Test
    void auditProject_shouldThrowExceptionWhenRejectReasonEmpty() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("reject", null, "   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditProject(projectId, request);
        });

        assertEquals("reject_reason is required when action is 'reject'", exception.getMessage());

        verify(trainingProjectMapper, never()).selectById(any());
    }

    @Test
    void auditProject_shouldThrowExceptionWhenActionInvalid() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("invalid", null, null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditProject(projectId, request);
        });

        assertEquals("Invalid action: must be 'pass' or 'reject'", exception.getMessage());

        verify(trainingProjectMapper, never()).selectById(any());
    }

    @Test
    void auditProject_shouldThrowExceptionWhenProjectNotFound() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        when(trainingProjectMapper.selectById(projectId))
                .thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            platformAuditService.auditProject(projectId, request);
        });

        assertEquals("Project not found with id: " + projectId, exception.getMessage());

        verify(trainingProjectMapper).selectById(projectId);
        verify(trainingProjectMapper, never()).updateById((TrainingProject) any());
    }

    @Test
    void auditProject_shouldThrowExceptionWhenAlreadyAudited() {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        TrainingProject project = createTrainingProject(projectId, 10L, "Project A", 1); // Already passed

        when(trainingProjectMapper.selectById(projectId))
                .thenReturn(project);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            platformAuditService.auditProject(projectId, request);
        });

        assertEquals("Project has already been audited", exception.getMessage());

        verify(trainingProjectMapper).selectById(projectId);
        verify(trainingProjectMapper, never()).updateById((TrainingProject) any());
    }

    private TrainingProject createTrainingProject(Long id, Long enterpriseId, String name, Integer auditStatus) {
        TrainingProject project = new TrainingProject();
        project.setId(id);
        project.setEnterpriseId(enterpriseId);
        project.setProjectName(name);
        project.setDescription("Description for " + name);
        project.setTechStack("Java, Spring Boot");
        project.setIndustry("Technology");
        project.setMaxTeams(5);
        project.setMaxMembers(20);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(6));
        project.setAuditStatus(auditStatus);
        project.setStatus(1);
        project.setCreatedAt(OffsetDateTime.now());
        project.setUpdatedAt(OffsetDateTime.now());
        return project;
    }
}
