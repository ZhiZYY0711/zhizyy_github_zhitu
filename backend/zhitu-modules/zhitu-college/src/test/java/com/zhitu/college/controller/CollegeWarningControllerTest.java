package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.WarningRecord;
import com.zhitu.college.service.CollegeWarningService;
import com.zhitu.common.core.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollegePortalController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegeWarningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollegeWarningService collegeWarningService;

    @MockBean
    private com.zhitu.college.service.CollegeService collegeService;

    @MockBean
    private com.zhitu.college.service.CollegePortalService collegePortalService;

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.LoginUser.builder()
            .userId(USER_ID)
            .tenantId(TENANT_ID)
            .role("college")
            .build());
    }

    @Test
    void getWarnings_shouldReturnWarningsList() throws Exception {
        // Given
        WarningRecord record1 = new WarningRecord();
        record1.setId(1L);
        record1.setTenantId(TENANT_ID);
        record1.setStudentId(200L);
        record1.setWarningType("attendance");
        record1.setWarningLevel(2);
        record1.setStatus(0);
        record1.setDescription("考勤异常");
        record1.setCreatedAt(OffsetDateTime.now());

        Page<WarningRecord> page = new Page<>(1, 10);
        page.setRecords(List.of(record1));
        page.setTotal(1);

        when(collegeWarningService.getWarnings(anyInt(), anyString(), anyInt(), anyInt(), anyInt()))
            .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/portal-college/v1/warnings")
                .param("level", "2")
                .param("type", "attendance")
                .param("status", "0")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.page").value(1))
            .andExpect(jsonPath("$.data.size").value(10));

        verify(collegeWarningService).getWarnings(2, "attendance", 0, 1, 10);
    }

    @Test
    void getWarnings_shouldHandleNoFilters() throws Exception {
        // Given
        Page<WarningRecord> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(collegeWarningService.getWarnings(isNull(), isNull(), isNull(), anyInt(), anyInt()))
            .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/portal-college/v1/warnings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").value(0));

        verify(collegeWarningService).getWarnings(null, null, null, 1, 10);
    }

    @Test
    void getWarningStats_shouldReturnStatistics() throws Exception {
        // Given
        Map<String, Object> stats = Map.of(
            "totalCount", 25L,
            "byLevel", Map.of("low", 10L, "medium", 12L, "high", 3L),
            "byType", Map.of("attendance", 8L, "report", 10L, "evaluation", 7L),
            "byStatus", Map.of("pending", 15L, "intervened", 10L)
        );

        when(collegeWarningService.getWarningStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/portal-college/v1/warnings/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.totalCount").value(25))
            .andExpect(jsonPath("$.data.byLevel.low").value(10))
            .andExpect(jsonPath("$.data.byLevel.medium").value(12))
            .andExpect(jsonPath("$.data.byLevel.high").value(3))
            .andExpect(jsonPath("$.data.byType.attendance").value(8))
            .andExpect(jsonPath("$.data.byType.report").value(10))
            .andExpect(jsonPath("$.data.byType.evaluation").value(7))
            .andExpect(jsonPath("$.data.byStatus.pending").value(15))
            .andExpect(jsonPath("$.data.byStatus.intervened").value(10));

        verify(collegeWarningService).getWarningStats();
    }

    @Test
    void interveneWarning_shouldSucceed() throws Exception {
        // Given
        Long warningId = 1L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("已与学生沟通，制定改进计划");
        request.setExpectedOutcome("下周考勤恢复正常");

        doNothing().when(collegeWarningService).intervene(eq(warningId), any(InterveneRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-college/v1/warnings/{id}/intervene", warningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(collegeWarningService).intervene(eq(warningId), any(InterveneRequest.class));
    }

    @Test
    void interveneWarning_shouldHandleNotFound() throws Exception {
        // Given
        Long warningId = 999L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("Test note");

        doThrow(new RuntimeException("预警记录不存在"))
            .when(collegeWarningService).intervene(eq(warningId), any(InterveneRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-college/v1/warnings/{id}/intervene", warningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is5xxServerError());

        verify(collegeWarningService).intervene(eq(warningId), any(InterveneRequest.class));
    }
}
