package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.MentorDashboardDTO;
import com.zhitu.enterprise.service.EnterpriseMentorService;
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
 * 企业导师控制器测试
 */
@ExtendWith(MockitoExtension.class)
class EnterpriseMentorControllerTest {

    @Mock
    private EnterpriseMentorService enterpriseMentorService;

    @InjectMocks
    private EnterpriseMentorController enterpriseMentorController;

    @Test
    void getDashboard_shouldReturnDashboardData() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        MentorDashboardDTO dashboardDTO = new MentorDashboardDTO(
                5,
                3,
                0,
                Arrays.asList(
                        new ActivityDTO(1L, "report_submitted", "张三 提交了周报", "weekly_report", 1L, now.minusHours(2)),
                        new ActivityDTO(2L, "report_submitted", "李四 提交了周报", "weekly_report", 2L, now.minusHours(5))
                )
        );

        when(enterpriseMentorService.getDashboard()).thenReturn(dashboardDTO);

        // When
        Result<MentorDashboardDTO> result = enterpriseMentorController.getDashboard();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(5, result.getData().getAssignedInternCount());
        assertEquals(3, result.getData().getPendingReportCount());
        assertEquals(0, result.getData().getPendingCodeReviewCount());
        assertNotNull(result.getData().getRecentActivities());
        assertEquals(2, result.getData().getRecentActivities().size());
        assertEquals("report_submitted", result.getData().getRecentActivities().get(0).getActivityType());
        assertEquals("张三 提交了周报", result.getData().getRecentActivities().get(0).getDescription());

        verify(enterpriseMentorService, times(1)).getDashboard();
    }

    @Test
    void getDashboard_shouldReturnEmptyActivities() {
        // Given
        MentorDashboardDTO dashboardDTO = new MentorDashboardDTO(
                0,
                0,
                0,
                Collections.emptyList()
        );

        when(enterpriseMentorService.getDashboard()).thenReturn(dashboardDTO);

        // When
        Result<MentorDashboardDTO> result = enterpriseMentorController.getDashboard();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(0, result.getData().getAssignedInternCount());
        assertEquals(0, result.getData().getPendingReportCount());
        assertEquals(0, result.getData().getPendingCodeReviewCount());
        assertTrue(result.getData().getRecentActivities().isEmpty());

        verify(enterpriseMentorService, times(1)).getDashboard();
    }

    @Test
    void getDashboard_shouldCallServiceMethod() {
        // Given
        MentorDashboardDTO dashboardDTO = new MentorDashboardDTO(1, 1, 0, Collections.emptyList());
        when(enterpriseMentorService.getDashboard()).thenReturn(dashboardDTO);

        // When
        enterpriseMentorController.getDashboard();

        // Then
        verify(enterpriseMentorService, times(1)).getDashboard();
    }

    @Test
    void getDashboard_shouldReturnSuccessResult() {
        // Given
        MentorDashboardDTO dashboardDTO = new MentorDashboardDTO(2, 1, 0, Collections.emptyList());
        when(enterpriseMentorService.getDashboard()).thenReturn(dashboardDTO);

        // When
        Result<MentorDashboardDTO> result = enterpriseMentorController.getDashboard();

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getMessage()); // Message can be in Chinese or English

        verify(enterpriseMentorService, times(1)).getDashboard();
    }

    @Test
    void getDashboard_shouldIncludeAllDashboardFields() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        MentorDashboardDTO dashboardDTO = new MentorDashboardDTO(
                10,
                5,
                2,
                Collections.singletonList(
                        new ActivityDTO(100L, "report_submitted", "测试学生 提交了周报", "weekly_report", 100L, now)
                )
        );

        when(enterpriseMentorService.getDashboard()).thenReturn(dashboardDTO);

        // When
        Result<MentorDashboardDTO> result = enterpriseMentorController.getDashboard();

        // Then
        assertNotNull(result);
        assertEquals(10, result.getData().getAssignedInternCount());
        assertEquals(5, result.getData().getPendingReportCount());
        assertEquals(2, result.getData().getPendingCodeReviewCount());
        assertEquals(1, result.getData().getRecentActivities().size());
        
        ActivityDTO activity = result.getData().getRecentActivities().get(0);
        assertEquals(100L, activity.getId());
        assertEquals("report_submitted", activity.getActivityType());
        assertEquals("测试学生 提交了周报", activity.getDescription());
        assertEquals("weekly_report", activity.getRefType());
        assertEquals(100L, activity.getRefId());

        verify(enterpriseMentorService, times(1)).getDashboard();
    }
}
