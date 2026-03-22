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

@WebMvcTest(CollegeWarningController.class)
@AutoConfigureMockMvc(addFilters = false)
class CollegeWarningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private com.zhitu.college.service.CollegeService collegeService;

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

        when(collegeService.getWarnings(anyInt(), anyInt(), anyInt()))
            .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/portal-college/v1/warnings")
                .param("status", "0")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").value(1));

        verify(collegeService).getWarnings(0, 1, 10);
    }

    @Test
    void getWarnings_shouldHandleNoFilters() throws Exception {
        // Given
        Page<WarningRecord> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(collegeService.getWarnings(isNull(), anyInt(), anyInt()))
            .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/portal-college/v1/warnings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").value(0));

        verify(collegeService).getWarnings(null, 1, 10);
    }

    @Test
    void interveneWarning_shouldSucceed() throws Exception {
        // Given
        Long warningId = 1L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("已与学生沟通，制定改进计划");
        request.setExpectedOutcome("下周考勤恢复正常");

        doNothing().when(collegeService).interveneWarning(eq(warningId), any(InterveneRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-college/v1/warnings/{id}/intervene", warningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(collegeService).interveneWarning(eq(warningId), any(InterveneRequest.class));
    }

    @Test
    void interveneWarning_shouldHandleNotFound() throws Exception {
        // Given
        Long warningId = 999L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("Test note");

        doThrow(new RuntimeException("预警记录不存在"))
            .when(collegeService).interveneWarning(eq(warningId), any(InterveneRequest.class));

        // When & Then
        mockMvc.perform(post("/api/portal-college/v1/warnings/{id}/intervene", warningId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is5xxServerError());

        verify(collegeService).interveneWarning(eq(warningId), any(InterveneRequest.class));
    }
}
