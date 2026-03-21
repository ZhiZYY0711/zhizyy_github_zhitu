package com.zhitu.enterprise.controller;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.AnalyticsDTO;
import com.zhitu.enterprise.service.EnterpriseAnalyticsService;
import com.zhitu.enterprise.service.EnterprisePortalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseAnalyticsControllerTest {

    @Mock
    private EnterpriseAnalyticsService enterpriseAnalyticsService;

    @Mock
    private EnterprisePortalService enterprisePortalService;

    @InjectMocks
    private EnterprisePortalController enterprisePortalController;

    private static final Long TEST_USER_ID = 1001L;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .tenantId(2001L)
                .role("ENTERPRISE")
                .build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testGetAnalytics_DefaultRange() {
        // Prepare test data
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-01", 10),
                        new AnalyticsDTO.TrendDataPoint("2024-02", 15)
                ),
                new AnalyticsDTO.InternPerformanceMetrics(85.5, 20, 15),
                75.0,
                4.2
        );

        // Mock service
        when(enterpriseAnalyticsService.getAnalytics("month")).thenReturn(analyticsDTO);

        // Execute
        Result<AnalyticsDTO> result = enterprisePortalController.getAnalytics("month");

        // Verify
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getApplicationTrends().size());
        assertEquals("2024-01", result.getData().getApplicationTrends().get(0).getPeriod());
        assertEquals(10, result.getData().getApplicationTrends().get(0).getCount());
        assertEquals(85.5, result.getData().getInternPerformance().getAverageScore());
        assertEquals(75.0, result.getData().getProjectCompletionRate());
        assertEquals(4.2, result.getData().getMentorSatisfaction());

        verify(enterpriseAnalyticsService).getAnalytics("month");
    }

    @Test
    void testGetAnalytics_WeekRange() {
        // Prepare test data
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-W01", 5),
                        new AnalyticsDTO.TrendDataPoint("2024-W02", 8)
                ),
                new AnalyticsDTO.InternPerformanceMetrics(80.0, 10, 8),
                70.0,
                4.0
        );

        // Mock service
        when(enterpriseAnalyticsService.getAnalytics("week")).thenReturn(analyticsDTO);

        // Execute
        Result<AnalyticsDTO> result = enterprisePortalController.getAnalytics("week");

        // Verify
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getApplicationTrends().size());
        assertEquals("2024-W01", result.getData().getApplicationTrends().get(0).getPeriod());

        verify(enterpriseAnalyticsService).getAnalytics("week");
    }

    @Test
    void testGetAnalytics_QuarterRange() {
        // Prepare test data
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-Q1", 30),
                        new AnalyticsDTO.TrendDataPoint("2024-Q2", 35)
                ),
                new AnalyticsDTO.InternPerformanceMetrics(82.0, 15, 12),
                72.0,
                4.1
        );

        // Mock service
        when(enterpriseAnalyticsService.getAnalytics("quarter")).thenReturn(analyticsDTO);

        // Execute
        Result<AnalyticsDTO> result = enterprisePortalController.getAnalytics("quarter");

        // Verify
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getApplicationTrends().size());
        assertEquals("2024-Q1", result.getData().getApplicationTrends().get(0).getPeriod());

        verify(enterpriseAnalyticsService).getAnalytics("quarter");
    }

    @Test
    void testGetAnalytics_YearRange() {
        // Prepare test data
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2023", 120),
                        new AnalyticsDTO.TrendDataPoint("2024", 150)
                ),
                new AnalyticsDTO.InternPerformanceMetrics(88.0, 25, 20),
                78.0,
                4.3
        );

        // Mock service
        when(enterpriseAnalyticsService.getAnalytics("year")).thenReturn(analyticsDTO);

        // Execute
        Result<AnalyticsDTO> result = enterprisePortalController.getAnalytics("year");

        // Verify
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().getApplicationTrends().size());
        assertEquals("2023", result.getData().getApplicationTrends().get(0).getPeriod());

        verify(enterpriseAnalyticsService).getAnalytics("year");
    }

    @Test
    void testGetAnalytics_EmptyData() {
        // Prepare empty test data
        AnalyticsDTO analyticsDTO = new AnalyticsDTO(
                Arrays.asList(),
                new AnalyticsDTO.InternPerformanceMetrics(0.0, 0, 0),
                0.0,
                0.0
        );

        // Mock service
        when(enterpriseAnalyticsService.getAnalytics("month")).thenReturn(analyticsDTO);

        // Execute
        Result<AnalyticsDTO> result = enterprisePortalController.getAnalytics("month");

        // Verify
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.getData().getApplicationTrends().isEmpty());
        assertEquals(0.0, result.getData().getInternPerformance().getAverageScore());
        assertEquals(0, result.getData().getInternPerformance().getTotalInterns());
        assertEquals(0.0, result.getData().getProjectCompletionRate());
        assertEquals(0.0, result.getData().getMentorSatisfaction());

        verify(enterpriseAnalyticsService).getAnalytics("month");
    }
}
