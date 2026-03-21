package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.platform.dto.AuditEnterpriseRequest;
import com.zhitu.platform.dto.AuditProjectRequest;
import com.zhitu.platform.dto.EnterpriseAuditDTO;
import com.zhitu.platform.dto.ProjectAuditDTO;
import com.zhitu.platform.service.PlatformAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PlatformAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformAuditService platformAuditService;

    // ── GET /api/system/v1/audits/enterprises Tests ───────────────────────────

    @Test
    void getEnterpriseAudits_shouldReturnAuditList() throws Exception {
        // Given
        EnterpriseAuditDTO audit1 = createEnterpriseAuditDTO(1L, 10L, "Enterprise A", 0);
        EnterpriseAuditDTO audit2 = createEnterpriseAuditDTO(2L, 20L, "Enterprise B", 0);

        Page<EnterpriseAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(audit1, audit2));
        mockPage.setTotal(2);

        when(platformAuditService.getEnterpriseAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].enterpriseName").value("Enterprise A"))
                .andExpect(jsonPath("$.data.records[0].auditStatus").value(0))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(platformAuditService).getEnterpriseAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getEnterpriseAudits_shouldFilterByStatus() throws Exception {
        // Given
        EnterpriseAuditDTO audit1 = createEnterpriseAuditDTO(1L, 10L, "Enterprise A", 0);

        Page<EnterpriseAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(audit1));
        mockPage.setTotal(1);

        when(platformAuditService.getEnterpriseAudits(eq(0), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].auditStatus").value(0));

        verify(platformAuditService).getEnterpriseAudits(eq(0), eq(1), eq(10));
    }

    @Test
    void getEnterpriseAudits_shouldUseDefaultPagination() throws Exception {
        // Given
        Page<EnterpriseAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(platformAuditService.getEnterpriseAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/system/v1/audits/enterprises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).getEnterpriseAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getEnterpriseAudits_shouldIncludeAllRequiredFields() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        EnterpriseAuditDTO audit = new EnterpriseAuditDTO(
                1L, 10L, "Enterprise A", "LICENSE123", "John Doe",
                "13800138000", "Technology", "Beijing", 0, null, now
        );

        Page<EnterpriseAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(audit));
        mockPage.setTotal(1);

        when(platformAuditService.getEnterpriseAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/system/v1/audits/enterprises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].enterpriseName").value("Enterprise A"))
                .andExpect(jsonPath("$.data.records[0].businessLicense").value("LICENSE123"))
                .andExpect(jsonPath("$.data.records[0].contactPerson").value("John Doe"))
                .andExpect(jsonPath("$.data.records[0].contactPhone").value("13800138000"))
                .andExpect(jsonPath("$.data.records[0].industry").value("Technology"))
                .andExpect(jsonPath("$.data.records[0].city").value("Beijing"))
                .andExpect(jsonPath("$.data.records[0].submissionDate").exists());

        verify(platformAuditService).getEnterpriseAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getEnterpriseAudits_shouldHandleEmptyResults() throws Exception {
        // Given
        Page<EnterpriseAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(platformAuditService.getEnterpriseAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/system/v1/audits/enterprises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(platformAuditService).getEnterpriseAudits(isNull(), eq(1), eq(10));
    }

    // ── POST /api/system/v1/audits/enterprises/{id} Tests ─────────────────────

    @Test
    void auditEnterprise_shouldPassAudit() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        doNothing().when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    @Test
    void auditEnterprise_shouldRejectAuditWithReason() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("reject", "Business license is invalid");

        doNothing().when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    @Test
    void auditEnterprise_shouldReturn400WhenActionMissing() throws Exception {
        // Given
        Long tenantId = 10L;
        String requestBody = "{}";

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditEnterprise(anyLong(), any());
    }

    @Test
    void auditEnterprise_shouldReturn400WhenActionInvalid() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("invalid", null);

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditEnterprise(anyLong(), any());
    }

    @Test
    void auditEnterprise_shouldHandleServiceException() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        doThrow(new IllegalArgumentException("Enterprise not found"))
                .when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    @Test
    void auditEnterprise_shouldHandleAlreadyAuditedException() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        doThrow(new IllegalStateException("Enterprise has already been audited"))
                .when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    @Test
    void auditEnterprise_shouldAcceptPassActionWithoutRejectReason() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("pass", null);

        doNothing().when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    @Test
    void auditEnterprise_shouldAcceptRejectActionWithRejectReason() throws Exception {
        // Given
        Long tenantId = 10L;
        AuditEnterpriseRequest request = new AuditEnterpriseRequest("reject", "Invalid documents");

        doNothing().when(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));

        // When & Then
        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditEnterprise(eq(tenantId), any(AuditEnterpriseRequest.class));
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    private EnterpriseAuditDTO createEnterpriseAuditDTO(Long id, Long tenantId, String name, Integer auditStatus) {
        return new EnterpriseAuditDTO(
                id,
                tenantId,
                name,
                "LICENSE" + id,
                "Contact " + id,
                "138" + String.format("%08d", id),
                "Technology",
                "Beijing",
                auditStatus,
                null,
                OffsetDateTime.now()
        );
    }

    // ── GET /api/portal-platform/v1/audits/projects Tests ─────────────────────

    @Test
    void getProjectAudits_shouldReturnAuditList() throws Exception {
        // Given
        ProjectAuditDTO project1 = createProjectAuditDTO(1L, 10L, "Project A", 0);
        ProjectAuditDTO project2 = createProjectAuditDTO(2L, 20L, "Project B", 0);

        Page<ProjectAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(project1, project2));
        mockPage.setTotal(2);

        when(platformAuditService.getProjectAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].projectName").value("Project A"))
                .andExpect(jsonPath("$.data.records[0].auditStatus").value(0))
                .andExpect(jsonPath("$.data.total").value(2));

        verify(platformAuditService).getProjectAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getProjectAudits_shouldFilterByStatus() throws Exception {
        // Given
        ProjectAuditDTO project1 = createProjectAuditDTO(1L, 10L, "Project A", 0);

        Page<ProjectAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project1));
        mockPage.setTotal(1);

        when(platformAuditService.getProjectAudits(eq(0), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].auditStatus").value(0));

        verify(platformAuditService).getProjectAudits(eq(0), eq(1), eq(10));
    }

    @Test
    void getProjectAudits_shouldUseDefaultPagination() throws Exception {
        // Given
        Page<ProjectAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(platformAuditService.getProjectAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).getProjectAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getProjectAudits_shouldIncludeAllRequiredFields() throws Exception {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        ProjectAuditDTO project = new ProjectAuditDTO(
                1L, "Project A", "A great training project", 10L, "Enterprise A",
                "Java, Spring Boot", "Technology",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30),
                0, null, now
        );

        Page<ProjectAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(project));
        mockPage.setTotal(1);

        when(platformAuditService.getProjectAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].projectName").value("Project A"))
                .andExpect(jsonPath("$.data.records[0].description").value("A great training project"))
                .andExpect(jsonPath("$.data.records[0].enterpriseId").value(10))
                .andExpect(jsonPath("$.data.records[0].enterpriseName").value("Enterprise A"))
                .andExpect(jsonPath("$.data.records[0].techStack").value("Java, Spring Boot"))
                .andExpect(jsonPath("$.data.records[0].industry").value("Technology"))
                .andExpect(jsonPath("$.data.records[0].submissionDate").exists());

        verify(platformAuditService).getProjectAudits(isNull(), eq(1), eq(10));
    }

    @Test
    void getProjectAudits_shouldHandleEmptyResults() throws Exception {
        // Given
        Page<ProjectAuditDTO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of());
        mockPage.setTotal(0);

        when(platformAuditService.getProjectAudits(isNull(), eq(1), eq(10)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));

        verify(platformAuditService).getProjectAudits(isNull(), eq(1), eq(10));
    }

    // ── POST /api/portal-platform/v1/audits/projects/{id} Tests ───────────────

    @Test
    void auditProject_shouldPassAuditWithoutQualityRating() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        doNothing().when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldPassAuditWithQualityRating() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", 4, null);

        doNothing().when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldRejectAuditWithReason() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("reject", null, "Content is inappropriate");

        doNothing().when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldReturn400WhenActionMissing() throws Exception {
        // Given
        Long projectId = 1L;
        String requestBody = "{}";

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditProject(anyLong(), any());
    }

    @Test
    void auditProject_shouldReturn400WhenActionInvalid() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("invalid", null, null);

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditProject(anyLong(), any());
    }

    @Test
    void auditProject_shouldReturn400WhenQualityRatingTooLow() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", 0, null);

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditProject(anyLong(), any());
    }

    @Test
    void auditProject_shouldReturn400WhenQualityRatingTooHigh() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", 6, null);

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(platformAuditService, never()).auditProject(anyLong(), any());
    }

    @Test
    void auditProject_shouldHandleServiceException() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        doThrow(new IllegalArgumentException("Project not found"))
                .when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldHandleAlreadyAuditedException() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        doThrow(new IllegalStateException("Project has already been audited"))
                .when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldAcceptPassActionWithoutRejectReason() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("pass", null, null);

        doNothing().when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    @Test
    void auditProject_shouldAcceptRejectActionWithRejectReason() throws Exception {
        // Given
        Long projectId = 1L;
        AuditProjectRequest request = new AuditProjectRequest("reject", null, "Inappropriate content");

        doNothing().when(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(platformAuditService).auditProject(eq(projectId), any(AuditProjectRequest.class));
    }

    private ProjectAuditDTO createProjectAuditDTO(Long id, Long enterpriseId, String name, Integer auditStatus) {
        return new ProjectAuditDTO(
                id,
                name,
                "Description for " + name,
                enterpriseId,
                "Enterprise " + enterpriseId,
                "Java, Spring Boot",
                "Technology",
                LocalDate.now(),
                LocalDate.now().plusMonths(6),
                auditStatus,
                null,
                OffsetDateTime.now()
        );
    }
}
