package com.zhitu.platform.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.OperationLogDTO;
import com.zhitu.platform.service.OperationLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 操作日志控制器测试
 * Requirements: 39.1, 39.2, 39.3, 39.6
 */
@WebMvcTest(PlatformSystemController.class)
class OperationLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationLogService operationLogService;

    @MockBean
    private com.zhitu.platform.service.PlatformService platformService;

    @Test
    void getOperationLogs_withNoFilters_shouldReturnAllLogs() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{\"query\":\"page=1\"}", 200, "success", "192.168.1.1",
            "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(1L, Arrays.asList(log), 1, 20);

        when(operationLogService.getLogs(isNull(), isNull(), isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].userId").value(100))
            .andExpect(jsonPath("$.data.records[0].module").value("student"))
            .andExpect(jsonPath("$.data.records[0].result").value("success"));
    }

    @Test
    void getOperationLogs_withUserIdFilter_shouldFilterByUserId() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{}", 200, "success", "192.168.1.1", "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(1L, Arrays.asList(log), 1, 20);

        when(operationLogService.getLogs(eq(100L), isNull(), isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation")
                .param("userId", "100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].userId").value(100));
    }

    @Test
    void getOperationLogs_withModuleFilter_shouldFilterByModule() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{}", 200, "success", "192.168.1.1", "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(1L, Arrays.asList(log), 1, 20);

        when(operationLogService.getLogs(isNull(), eq("student"), isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation")
                .param("module", "student"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].module").value("student"));
    }

    @Test
    void getOperationLogs_withResultFilter_shouldFilterByResult() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{}", 200, "success", "192.168.1.1", "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(1L, Arrays.asList(log), 1, 20);

        when(operationLogService.getLogs(isNull(), isNull(), eq("success"), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation")
                .param("result", "success"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].result").value("success"));
    }

    @Test
    void getOperationLogs_withPagination_shouldReturnCorrectPage() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{}", 200, "success", "192.168.1.1", "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(25L, Arrays.asList(log), 2, 10);

        when(operationLogService.getLogs(isNull(), isNull(), isNull(), isNull(), isNull(), eq(2), eq(10)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation")
                .param("page", "2")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.page").value(2))
            .andExpect(jsonPath("$.data.size").value(10))
            .andExpect(jsonPath("$.data.total").value(25));
    }

    @Test
    void getOperationLogs_withAllFilters_shouldApplyAllFilters() throws Exception {
        // Given
        OperationLogDTO log = new OperationLogDTO(
            1L, 100L, "testuser", 1L, "student", "get_dashboard",
            "{}", 200, "success", "192.168.1.1", "Mozilla/5.0", 45, OffsetDateTime.now()
        );

        PageResult<OperationLogDTO> pageResult = PageResult.of(1L, Arrays.asList(log), 1, 20);

        when(operationLogService.getLogs(eq(100L), eq("student"), eq("success"), 
                any(OffsetDateTime.class), any(OffsetDateTime.class), eq(1), eq(20)))
            .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/system/v1/logs/operation")
                .param("userId", "100")
                .param("module", "student")
                .param("result", "success")
                .param("startTime", "2024-01-01T00:00:00Z")
                .param("endTime", "2024-12-31T23:59:59Z"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].userId").value(100))
            .andExpect(jsonPath("$.data.records[0].module").value("student"))
            .andExpect(jsonPath("$.data.records[0].result").value("success"));
    }
}
