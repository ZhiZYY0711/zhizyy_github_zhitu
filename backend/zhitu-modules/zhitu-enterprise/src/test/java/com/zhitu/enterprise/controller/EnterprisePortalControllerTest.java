package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.DashboardStatsDTO;
import com.zhitu.enterprise.dto.TalentPoolDTO;
import com.zhitu.enterprise.dto.TodoDTO;
import com.zhitu.enterprise.service.EnterprisePortalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 企业门户控制器测试
 */
@ExtendWith(MockitoExtension.class)
class EnterprisePortalControllerTest {

    @Mock
    private EnterprisePortalService enterprisePortalService;

    @InjectMocks
    private EnterprisePortalController enterprisePortalController;

    @Test
    void getDashboardStats_shouldReturnSuccessResult() {
        // Given
        DashboardStatsDTO mockStats = new DashboardStatsDTO(8, 25, 12, 3);
        when(enterprisePortalService.getDashboardStats()).thenReturn(mockStats);

        // When
        Result<DashboardStatsDTO> result = enterprisePortalController.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(8, result.getData().getActiveJobCount());
        assertEquals(25, result.getData().getPendingApplicationCount());
        assertEquals(12, result.getData().getActiveInternCount());
        assertEquals(3, result.getData().getTrainingProjectCount());

        verify(enterprisePortalService, times(1)).getDashboardStats();
    }

    @Test
    void getDashboardStats_shouldReturnZeroStats() {
        // Given
        DashboardStatsDTO mockStats = new DashboardStatsDTO(0, 0, 0, 0);
        when(enterprisePortalService.getDashboardStats()).thenReturn(mockStats);

        // When
        Result<DashboardStatsDTO> result = enterprisePortalController.getDashboardStats();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getActiveJobCount());
        assertEquals(0, result.getData().getPendingApplicationCount());
        assertEquals(0, result.getData().getActiveInternCount());
        assertEquals(0, result.getData().getTrainingProjectCount());
    }

    @Test
    void getDashboardStats_shouldCallServiceOnce() {
        // Given
        DashboardStatsDTO mockStats = new DashboardStatsDTO(5, 10, 7, 2);
        when(enterprisePortalService.getDashboardStats()).thenReturn(mockStats);

        // When
        enterprisePortalController.getDashboardStats();

        // Then
        verify(enterprisePortalService, times(1)).getDashboardStats();
        verifyNoMoreInteractions(enterprisePortalService);
    }

    // ========== getTodos Tests ==========

    @Test
    void getTodos_shouldReturnSuccessResult() {
        // Given
        TodoDTO todo1 = new TodoDTO(1L, "application_review", "job", 100L, 
                "Review application from John Doe", 3, OffsetDateTime.now().plusDays(1), 0, OffsetDateTime.now());
        TodoDTO todo2 = new TodoDTO(2L, "interview_schedule", "application", 200L,
                "Schedule interview with Jane Smith", 2, OffsetDateTime.now().plusDays(3), 0, OffsetDateTime.now());

        PageResult<TodoDTO> mockPageResult = PageResult.of(2L, Arrays.asList(todo1, todo2), 1, 10);
        when(enterprisePortalService.getTodos(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TodoDTO>> result = enterprisePortalController.getTodos(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2L, result.getData().getTotal());
        assertEquals(2, result.getData().getRecords().size());
        assertEquals(1, result.getData().getPage());
        assertEquals(10, result.getData().getSize());

        TodoDTO firstTodo = result.getData().getRecords().get(0);
        assertEquals("application_review", firstTodo.getTodoType());
        assertEquals("Review application from John Doe", firstTodo.getTitle());
        assertEquals(3, firstTodo.getPriority());

        verify(enterprisePortalService, times(1)).getTodos(1, 10);
    }

    @Test
    void getTodos_shouldReturnEmptyResult() {
        // Given
        PageResult<TodoDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTodos(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TodoDTO>> result = enterprisePortalController.getTodos(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0L, result.getData().getTotal());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    void getTodos_shouldUseDefaultPagination() {
        // Given
        PageResult<TodoDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTodos(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getTodos(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getTodos(1, 10);
    }

    @Test
    void getTodos_shouldHandleCustomPagination() {
        // Given
        PageResult<TodoDTO> mockPageResult = PageResult.of(50L, Collections.emptyList(), 3, 20);
        when(enterprisePortalService.getTodos(3, 20)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TodoDTO>> result = enterprisePortalController.getTodos(3, 20);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().getPage());
        assertEquals(20, result.getData().getSize());
        assertEquals(50L, result.getData().getTotal());

        verify(enterprisePortalService, times(1)).getTodos(3, 20);
    }

    @Test
    void getTodos_shouldCallServiceOnce() {
        // Given
        PageResult<TodoDTO> mockPageResult = PageResult.of(5L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTodos(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getTodos(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getTodos(1, 10);
        verifyNoMoreInteractions(enterprisePortalService);
    }

    // ========== getActivities Tests ==========

    @Test
    void getActivities_shouldReturnSuccessResult() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        ActivityDTO activity1 = new ActivityDTO(1L, "application", "New application from John Doe", 
                "application", 100L, now.minusHours(2));
        ActivityDTO activity2 = new ActivityDTO(2L, "interview", "Interview scheduled with Jane Smith",
                "interview", 101L, now.minusHours(5));

        PageResult<ActivityDTO> mockPageResult = PageResult.of(2L, Arrays.asList(activity1, activity2), 1, 10);
        when(enterprisePortalService.getActivities(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<ActivityDTO>> result = enterprisePortalController.getActivities(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2L, result.getData().getTotal());
        assertEquals(2, result.getData().getRecords().size());
        assertEquals(1, result.getData().getPage());
        assertEquals(10, result.getData().getSize());

        ActivityDTO firstActivity = result.getData().getRecords().get(0);
        assertEquals("application", firstActivity.getActivityType());
        assertEquals("New application from John Doe", firstActivity.getDescription());
        assertEquals("application", firstActivity.getRefType());
        assertEquals(100L, firstActivity.getRefId());

        verify(enterprisePortalService, times(1)).getActivities(1, 10);
    }

    @Test
    void getActivities_shouldReturnEmptyResult() {
        // Given
        PageResult<ActivityDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getActivities(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<ActivityDTO>> result = enterprisePortalController.getActivities(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0L, result.getData().getTotal());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    void getActivities_shouldUseDefaultPagination() {
        // Given
        PageResult<ActivityDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getActivities(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getActivities(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getActivities(1, 10);
    }

    @Test
    void getActivities_shouldHandleCustomPagination() {
        // Given
        PageResult<ActivityDTO> mockPageResult = PageResult.of(45L, Collections.emptyList(), 2, 15);
        when(enterprisePortalService.getActivities(2, 15)).thenReturn(mockPageResult);

        // When
        Result<PageResult<ActivityDTO>> result = enterprisePortalController.getActivities(2, 15);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getPage());
        assertEquals(15, result.getData().getSize());
        assertEquals(45L, result.getData().getTotal());

        verify(enterprisePortalService, times(1)).getActivities(2, 15);
    }

    @Test
    void getActivities_shouldCallServiceOnce() {
        // Given
        PageResult<ActivityDTO> mockPageResult = PageResult.of(10L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getActivities(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getActivities(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getActivities(1, 10);
        verifyNoMoreInteractions(enterprisePortalService);
    }

    @Test
    void getActivities_shouldIncludeAllActivityFields() {
        // Given
        OffsetDateTime createdAt = OffsetDateTime.now().minusHours(3);
        ActivityDTO activity = new ActivityDTO(
                999L,
                "evaluation",
                "Performance evaluation completed for intern",
                "intern",
                555L,
                createdAt
        );

        PageResult<ActivityDTO> mockPageResult = PageResult.of(1L, Collections.singletonList(activity), 1, 10);
        when(enterprisePortalService.getActivities(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<ActivityDTO>> result = enterprisePortalController.getActivities(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getData().getRecords().size());

        ActivityDTO returnedActivity = result.getData().getRecords().get(0);
        assertEquals(999L, returnedActivity.getId());
        assertEquals("evaluation", returnedActivity.getActivityType());
        assertEquals("Performance evaluation completed for intern", returnedActivity.getDescription());
        assertEquals("intern", returnedActivity.getRefType());
        assertEquals(555L, returnedActivity.getRefId());
        assertEquals(createdAt, returnedActivity.getCreatedAt());
    }

    // ========== getTalentPool Tests ==========

    @Test
    void getTalentPool_shouldReturnSuccessResult() {
        // Given
        OffsetDateTime collectedAt = OffsetDateTime.now().minusDays(5);
        TalentPoolDTO talent1 = new TalentPoolDTO(1L, 100L, "张三", "2021001", "计算机科学", "2021", 
                "[\"Java\",\"Spring\"]", "优秀候选人", collectedAt);
        TalentPoolDTO talent2 = new TalentPoolDTO(2L, 101L, "李四", "2021002", "软件工程", "2021",
                "[\"Python\",\"Django\"]", "有潜力", collectedAt.minusDays(1));

        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(2L, Arrays.asList(talent1, talent2), 1, 10);
        when(enterprisePortalService.getTalentPool(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TalentPoolDTO>> result = enterprisePortalController.getTalentPool(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2L, result.getData().getTotal());
        assertEquals(2, result.getData().getRecords().size());
        assertEquals(1, result.getData().getPage());
        assertEquals(10, result.getData().getSize());

        TalentPoolDTO firstTalent = result.getData().getRecords().get(0);
        assertEquals(1L, firstTalent.getId());
        assertEquals(100L, firstTalent.getStudentId());
        assertEquals("张三", firstTalent.getStudentName());
        assertEquals("2021001", firstTalent.getStudentNo());
        assertEquals("计算机科学", firstTalent.getMajor());

        verify(enterprisePortalService, times(1)).getTalentPool(1, 10);
    }

    @Test
    void getTalentPool_shouldReturnEmptyResult() {
        // Given
        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTalentPool(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TalentPoolDTO>> result = enterprisePortalController.getTalentPool(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(0L, result.getData().getTotal());
        assertTrue(result.getData().getRecords().isEmpty());
    }

    @Test
    void getTalentPool_shouldUseDefaultPagination() {
        // Given
        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(0L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTalentPool(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getTalentPool(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getTalentPool(1, 10);
    }

    @Test
    void getTalentPool_shouldHandleCustomPagination() {
        // Given
        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(30L, Collections.emptyList(), 2, 15);
        when(enterprisePortalService.getTalentPool(2, 15)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TalentPoolDTO>> result = enterprisePortalController.getTalentPool(2, 15);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getPage());
        assertEquals(15, result.getData().getSize());
        assertEquals(30L, result.getData().getTotal());

        verify(enterprisePortalService, times(1)).getTalentPool(2, 15);
    }

    @Test
    void getTalentPool_shouldCallServiceOnce() {
        // Given
        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(8L, Collections.emptyList(), 1, 10);
        when(enterprisePortalService.getTalentPool(1, 10)).thenReturn(mockPageResult);

        // When
        enterprisePortalController.getTalentPool(1, 10);

        // Then
        verify(enterprisePortalService, times(1)).getTalentPool(1, 10);
        verifyNoMoreInteractions(enterprisePortalService);
    }

    @Test
    void getTalentPool_shouldIncludeAllTalentFields() {
        // Given
        OffsetDateTime collectedAt = OffsetDateTime.now().minusDays(10);
        TalentPoolDTO talent = new TalentPoolDTO(
                999L,
                888L,
                "王五",
                "2020999",
                "信息工程",
                "2020",
                "[\"C++\",\"Qt\"]",
                "技术扎实",
                collectedAt
        );

        PageResult<TalentPoolDTO> mockPageResult = PageResult.of(1L, Collections.singletonList(talent), 1, 10);
        when(enterprisePortalService.getTalentPool(1, 10)).thenReturn(mockPageResult);

        // When
        Result<PageResult<TalentPoolDTO>> result = enterprisePortalController.getTalentPool(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getData().getRecords().size());

        TalentPoolDTO returnedTalent = result.getData().getRecords().get(0);
        assertEquals(999L, returnedTalent.getId());
        assertEquals(888L, returnedTalent.getStudentId());
        assertEquals("王五", returnedTalent.getStudentName());
        assertEquals("2020999", returnedTalent.getStudentNo());
        assertEquals("信息工程", returnedTalent.getMajor());
        assertEquals("2020", returnedTalent.getGrade());
        assertEquals("[\"C++\",\"Qt\"]", returnedTalent.getSkills());
        assertEquals("技术扎实", returnedTalent.getRemark());
        assertEquals(collectedAt, returnedTalent.getCollectedAt());
    }

    // ========== removeFromTalentPool Tests ==========

    @Test
    void removeFromTalentPool_shouldReturnSuccessResult() {
        // Given
        doNothing().when(enterprisePortalService).removeFromTalentPool(1L);

        // When
        Result<Void> result = enterprisePortalController.removeFromTalentPool(1L);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNull(result.getData());

        verify(enterprisePortalService, times(1)).removeFromTalentPool(1L);
    }

    @Test
    void removeFromTalentPool_shouldCallServiceOnce() {
        // Given
        doNothing().when(enterprisePortalService).removeFromTalentPool(5L);

        // When
        enterprisePortalController.removeFromTalentPool(5L);

        // Then
        verify(enterprisePortalService, times(1)).removeFromTalentPool(5L);
        verifyNoMoreInteractions(enterprisePortalService);
    }

    @Test
    void removeFromTalentPool_shouldHandleDifferentIds() {
        // Given
        doNothing().when(enterprisePortalService).removeFromTalentPool(anyLong());

        // When
        enterprisePortalController.removeFromTalentPool(100L);
        enterprisePortalController.removeFromTalentPool(200L);
        enterprisePortalController.removeFromTalentPool(300L);

        // Then
        verify(enterprisePortalService, times(1)).removeFromTalentPool(100L);
        verify(enterprisePortalService, times(1)).removeFromTalentPool(200L);
        verify(enterprisePortalService, times(1)).removeFromTalentPool(300L);
    }
}
