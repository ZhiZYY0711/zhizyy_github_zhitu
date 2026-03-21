package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.AnalyticsDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseAnalyticsServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EnterpriseAnalyticsService enterpriseAnalyticsService;

    private static final Long TEST_USER_ID = 1001L;
    private static final Long TEST_TENANT_ID = 2001L;

    @BeforeEach
    void setUp() {
        UserContext.set(UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .tenantId(TEST_TENANT_ID)
                .role("ENTERPRISE")
                .build());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testGetAnalytics_Success() {
        // Mock tenant ID lookup
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList(TEST_TENANT_ID));

        // Mock cache miss - execute the supplier
        when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    // Execute the supplier (4th argument)
                    return ((java.util.function.Supplier<?>) invocation.getArgument(3)).get();
                });

        // Mock application trends
        when(jdbcTemplate.query(contains("job_application"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-01", 10),
                        new AnalyticsDTO.TrendDataPoint("2024-02", 15)
                ));

        // Mock intern performance
        when(jdbcTemplate.queryForObject(contains("COUNT(DISTINCT student_id)"), eq(Integer.class), eq(TEST_TENANT_ID)))
                .thenReturn(20);
        
        when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(
                        new AnalyticsDTO.InternPerformanceMetrics(85.5, 20, 15)
                ));

        // Mock project completion rate
        when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(75.0));

        // Mock mentor satisfaction
        when(jdbcTemplate.query(contains("is_mentor"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(4.2));

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("month");

        // Verify
        assertNotNull(result);
        assertNotNull(result.getApplicationTrends());
        assertEquals(2, result.getApplicationTrends().size());
        assertEquals("2024-01", result.getApplicationTrends().get(0).getPeriod());
        assertEquals(10, result.getApplicationTrends().get(0).getCount());
        
        assertNotNull(result.getInternPerformance());
        assertEquals(85.5, result.getInternPerformance().getAverageScore());
        assertEquals(20, result.getInternPerformance().getTotalInterns());
        assertEquals(15, result.getInternPerformance().getEvaluatedInterns());
        
        assertEquals(75.0, result.getProjectCompletionRate());
        assertEquals(4.2, result.getMentorSatisfaction());

        // Verify cache was called
        verify(cacheService).getOrSet(
                eq("enterprise:analytics:" + TEST_TENANT_ID + ":month"),
                eq(30L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void testGetAnalytics_NoTenant() {
        // Mock tenant ID lookup - no tenant found
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList());

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("month");

        // Verify - should return empty analytics
        assertNotNull(result);
        assertNotNull(result.getApplicationTrends());
        assertTrue(result.getApplicationTrends().isEmpty());
        assertEquals(0.0, result.getInternPerformance().getAverageScore());
        assertEquals(0, result.getInternPerformance().getTotalInterns());
        assertEquals(0.0, result.getProjectCompletionRate());
        assertEquals(0.0, result.getMentorSatisfaction());

        // Verify cache was NOT called
        verify(cacheService, never()).getOrSet(anyString(), anyLong(), any(TimeUnit.class), any());
    }

    @Test
    void testGetAnalytics_WeekRange() {
        // Mock tenant ID lookup
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList(TEST_TENANT_ID));

        // Mock cache miss
        when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(3)).get());

        // Mock application trends with week format
        when(jdbcTemplate.query(contains("job_application"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-W01", 5),
                        new AnalyticsDTO.TrendDataPoint("2024-W02", 8)
                ));

        // Mock other metrics
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                .thenReturn(10);
        when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(new AnalyticsDTO.InternPerformanceMetrics(80.0, 10, 8)));
        when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(70.0));
        when(jdbcTemplate.query(contains("is_mentor"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(4.0));

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("week");

        // Verify
        assertNotNull(result);
        assertEquals(2, result.getApplicationTrends().size());
        assertEquals("2024-W01", result.getApplicationTrends().get(0).getPeriod());
        
        // Verify cache key includes "week"
        verify(cacheService).getOrSet(
                eq("enterprise:analytics:" + TEST_TENANT_ID + ":week"),
                eq(30L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    @Test
    void testGetAnalytics_QuarterRange() {
        // Mock tenant ID lookup
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList(TEST_TENANT_ID));

        // Mock cache miss
        when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(3)).get());

        // Mock application trends with quarter format
        when(jdbcTemplate.query(contains("job_application"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2024-Q1", 30),
                        new AnalyticsDTO.TrendDataPoint("2024-Q2", 35)
                ));

        // Mock other metrics
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                .thenReturn(10);
        when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(new AnalyticsDTO.InternPerformanceMetrics(80.0, 10, 8)));
        when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(70.0));
        when(jdbcTemplate.query(contains("is_mentor"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(4.0));

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("quarter");

        // Verify
        assertNotNull(result);
        assertEquals(2, result.getApplicationTrends().size());
        assertEquals("2024-Q1", result.getApplicationTrends().get(0).getPeriod());
    }

    @Test
    void testGetAnalytics_YearRange() {
        // Mock tenant ID lookup
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList(TEST_TENANT_ID));

        // Mock cache miss
        when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(3)).get());

        // Mock application trends with year format
        when(jdbcTemplate.query(contains("job_application"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(
                        new AnalyticsDTO.TrendDataPoint("2023", 120),
                        new AnalyticsDTO.TrendDataPoint("2024", 150)
                ));

        // Mock other metrics
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                .thenReturn(10);
        when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(new AnalyticsDTO.InternPerformanceMetrics(80.0, 10, 8)));
        when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(70.0));
        when(jdbcTemplate.query(contains("is_mentor"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(4.0));

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("year");

        // Verify
        assertNotNull(result);
        assertEquals(2, result.getApplicationTrends().size());
        assertEquals("2023", result.getApplicationTrends().get(0).getPeriod());
    }

    @Test
    void testGetAnalytics_NoData() {
        // Mock tenant ID lookup
        when(jdbcTemplate.query(contains("SELECT tenant_id"), any(RowMapper.class), eq(TEST_USER_ID)))
                .thenReturn(Arrays.asList(TEST_TENANT_ID));

        // Mock cache miss
        when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> ((java.util.function.Supplier<?>) invocation.getArgument(3)).get());

        // Mock empty results
        when(jdbcTemplate.query(contains("job_application"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                .thenReturn(0);
        when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(new AnalyticsDTO.InternPerformanceMetrics(0.0, 0, 0)));
        when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(0.0));
        when(jdbcTemplate.query(contains("is_mentor"), any(RowMapper.class), eq(TEST_TENANT_ID)))
                .thenReturn(Arrays.asList(0.0));

        // Execute
        AnalyticsDTO result = enterpriseAnalyticsService.getAnalytics("month");

        // Verify - should return zero values
        assertNotNull(result);
        assertTrue(result.getApplicationTrends().isEmpty());
        assertEquals(0.0, result.getInternPerformance().getAverageScore());
        assertEquals(0, result.getInternPerformance().getTotalInterns());
        assertEquals(0.0, result.getProjectCompletionRate());
        assertEquals(0.0, result.getMentorSatisfaction());
    }
}
