package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.WarningRecord;
import com.zhitu.college.mapper.WarningRecordMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollegeWarningServiceTest {

    @Mock
    private WarningRecordMapper warningRecordMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CollegeWarningService collegeWarningService;

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
    void getWarnings_shouldReturnFilteredWarnings() {
        // Given
        Integer level = 2;
        String type = "attendance";
        Integer status = 0;
        Integer page = 1;
        Integer size = 10;

        Page<WarningRecord> mockPage = new Page<>(page, size);
        when(warningRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        IPage<WarningRecord> result = collegeWarningService.getWarnings(level, type, status, page, size);

        // Then
        assertThat(result).isNotNull();
        verify(warningRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getWarnings_shouldHandleNullFilters() {
        // Given
        Integer page = 1;
        Integer size = 10;

        Page<WarningRecord> mockPage = new Page<>(page, size);
        when(warningRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // When
        IPage<WarningRecord> result = collegeWarningService.getWarnings(null, null, null, page, size);

        // Then
        assertThat(result).isNotNull();
        verify(warningRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void getWarningStats_shouldReturnCachedStats() {
        // Given
        Map<String, Object> expectedStats = Map.of(
            "totalCount", 25L,
            "byLevel", Map.of("low", 10L, "medium", 12L, "high", 3L),
            "byType", Map.of("attendance", 8L, "report", 10L, "evaluation", 7L),
            "byStatus", Map.of("pending", 15L, "intervened", 10L)
        );

        when(cacheService.getOrSet(anyString(), anyLong(), any()))
            .thenAnswer(invocation -> {
                var supplier = invocation.getArgument(2, java.util.function.Supplier.class);
                return supplier.get();
            });

        when(warningRecordMapper.selectCount(any(LambdaQueryWrapper.class)))
            .thenReturn(25L)  // total
            .thenReturn(10L)  // low
            .thenReturn(12L)  // medium
            .thenReturn(3L)   // high
            .thenReturn(8L)   // attendance
            .thenReturn(10L)  // report
            .thenReturn(7L)   // evaluation
            .thenReturn(15L)  // pending
            .thenReturn(10L); // intervened

        // When
        Map<String, Object> result = collegeWarningService.getWarningStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("totalCount")).isEqualTo(25L);
        assertThat(result).containsKeys("byLevel", "byType", "byStatus");
        
        verify(cacheService).getOrSet(eq("college:warnings:stats:" + TENANT_ID), eq(600L), any());
    }

    @Test
    void intervene_shouldUpdateWarningRecord() {
        // Given
        Long warningId = 1L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("已与学生沟通，制定改进计划");
        request.setExpectedOutcome("下周考勤恢复正常");

        WarningRecord existingRecord = new WarningRecord();
        existingRecord.setId(warningId);
        existingRecord.setTenantId(TENANT_ID);
        existingRecord.setStudentId(200L);
        existingRecord.setWarningType("attendance");
        existingRecord.setWarningLevel(2);
        existingRecord.setStatus(0); // pending

        when(warningRecordMapper.selectById(warningId)).thenReturn(existingRecord);
        when(warningRecordMapper.updateById(any(WarningRecord.class))).thenReturn(1);

        // When
        collegeWarningService.intervene(warningId, request);

        // Then
        ArgumentCaptor<WarningRecord> captor = ArgumentCaptor.forClass(WarningRecord.class);
        verify(warningRecordMapper).updateById(captor.capture());
        
        WarningRecord updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(1); // intervened
        assertThat(updated.getInterveneNote()).isEqualTo("已与学生沟通，制定改进计划");
        assertThat(updated.getIntervenedBy()).isEqualTo(USER_ID);
        assertThat(updated.getIntervenedAt()).isNotNull();
        
        verify(cacheService).invalidate("college:warnings:stats:" + TENANT_ID);
    }

    @Test
    void intervene_shouldThrowExceptionWhenWarningNotFound() {
        // Given
        Long warningId = 999L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("Test note");

        when(warningRecordMapper.selectById(warningId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> collegeWarningService.intervene(warningId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("预警记录不存在");
        
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
        verify(cacheService, never()).invalidate(anyString());
    }

    @Test
    void intervene_shouldThrowExceptionWhenTenantMismatch() {
        // Given
        Long warningId = 1L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("Test note");

        WarningRecord existingRecord = new WarningRecord();
        existingRecord.setId(warningId);
        existingRecord.setTenantId(999L); // Different tenant
        existingRecord.setStatus(0);

        when(warningRecordMapper.selectById(warningId)).thenReturn(existingRecord);

        // When & Then
        assertThatThrownBy(() -> collegeWarningService.intervene(warningId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("无权操作此预警记录");
        
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
    }

    @Test
    void intervene_shouldThrowExceptionWhenAlreadyIntervened() {
        // Given
        Long warningId = 1L;
        InterveneRequest request = new InterveneRequest();
        request.setInterveneNote("Test note");

        WarningRecord existingRecord = new WarningRecord();
        existingRecord.setId(warningId);
        existingRecord.setTenantId(TENANT_ID);
        existingRecord.setStatus(1); // Already intervened

        when(warningRecordMapper.selectById(warningId)).thenReturn(existingRecord);

        // When & Then
        assertThatThrownBy(() -> collegeWarningService.intervene(warningId, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("该预警已处理");
        
        verify(warningRecordMapper, never()).updateById(any(WarningRecord.class));
    }
}
