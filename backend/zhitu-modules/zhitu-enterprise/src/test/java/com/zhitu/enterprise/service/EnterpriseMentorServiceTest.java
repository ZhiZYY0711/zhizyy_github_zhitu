package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.MentorDashboardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 企业导师服务测试
 */
@ExtendWith(MockitoExtension.class)
class EnterpriseMentorServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EnterpriseMentorService enterpriseMentorService;

    private static final Long TEST_MENTOR_ID = 30L;

    @BeforeEach
    void setUp() {
        // Mock cache service to always execute the supplier
        lenient().when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(3);
                    return supplier.get();
                });
    }

    @Test
    void getDashboard_shouldReturnCorrectDashboardData() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            // Mock assigned intern count
            when(jdbcTemplate.queryForObject(
                    contains("internship_svc.internship_record"),
                    eq(Integer.class),
                    eq(TEST_MENTOR_ID)
            )).thenReturn(5);

            // Mock pending report count
            when(jdbcTemplate.queryForObject(
                    contains("internship_svc.weekly_report"),
                    eq(Integer.class),
                    eq(TEST_MENTOR_ID)
            )).thenReturn(3);

            // Mock recent activities
            OffsetDateTime now = OffsetDateTime.now();
            when(jdbcTemplate.query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_MENTOR_ID)
            )).thenAnswer(invocation -> {
                RowMapper<ActivityDTO> mapper = invocation.getArgument(1);
                return Arrays.asList(
                        new ActivityDTO(1L, "report_submitted", "张三 提交了周报", "weekly_report", 1L, now.minusHours(2)),
                        new ActivityDTO(2L, "report_submitted", "李四 提交了周报", "weekly_report", 2L, now.minusHours(5))
                );
            });

            // When
            MentorDashboardDTO result = enterpriseMentorService.getDashboard();

            // Then
            assertNotNull(result);
            assertEquals(5, result.getAssignedInternCount());
            assertEquals(3, result.getPendingReportCount());
            assertEquals(0, result.getPendingCodeReviewCount()); // Code review not implemented yet
            assertNotNull(result.getRecentActivities());
            assertEquals(2, result.getRecentActivities().size());

            ActivityDTO firstActivity = result.getRecentActivities().get(0);
            assertEquals("report_submitted", firstActivity.getActivityType());
            assertEquals("张三 提交了周报", firstActivity.getDescription());

            // Verify cache was used
            verify(cacheService).getOrSet(
                    eq("mentor:dashboard:" + TEST_MENTOR_ID),
                    eq(5L),
                    eq(TimeUnit.MINUTES),
                    any()
            );
        }
    }

    @Test
    void getDashboard_shouldCountOnlyActiveInterns() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify query filters by status = 1 and mentor_id (active interns)
            verify(jdbcTemplate, atLeastOnce()).queryForObject(
                    contains("status = 1"),
                    eq(Integer.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldCountOnlyPendingReports() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify query filters by status = 1 (submitted, pending review)
            verify(jdbcTemplate).queryForObject(
                    contains("wr.status = 1"),
                    eq(Integer.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldFilterByMentorId() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify all queries use mentor_id for filtering
            verify(jdbcTemplate, times(2)).queryForObject(
                    contains("mentor_id = ?"),
                    eq(Integer.class),
                    eq(TEST_MENTOR_ID)
            );
            verify(jdbcTemplate).query(
                    contains("mentor_id = ?"),
                    any(RowMapper.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldReturnLast10Activities() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify query limits to 10 activities
            verify(jdbcTemplate).query(
                    contains("LIMIT 10"),
                    any(RowMapper.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldOrderActivitiesByCreatedAtDescending() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify query orders by created_at DESC
            verify(jdbcTemplate).query(
                    contains("ORDER BY wr.created_at DESC"),
                    any(RowMapper.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldHandleZeroCounts() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            MentorDashboardDTO result = enterpriseMentorService.getDashboard();

            // Then
            assertNotNull(result);
            assertEquals(0, result.getAssignedInternCount());
            assertEquals(0, result.getPendingReportCount());
            assertEquals(0, result.getPendingCodeReviewCount());
            assertNotNull(result.getRecentActivities());
            assertTrue(result.getRecentActivities().isEmpty());
        }
    }

    @Test
    void getDashboard_shouldIncludeStudentNameInActivity() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            OffsetDateTime now = OffsetDateTime.now();
            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenAnswer(invocation -> {
                        RowMapper<ActivityDTO> mapper = invocation.getArgument(1);
                        return Collections.singletonList(
                                new ActivityDTO(1L, "report_submitted", "王五 提交了周报", "weekly_report", 1L, now)
                        );
                    });

            // When
            MentorDashboardDTO result = enterpriseMentorService.getDashboard();

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecentActivities().size());
            ActivityDTO activity = result.getRecentActivities().get(0);
            assertTrue(activity.getDescription().contains("王五"));
            assertTrue(activity.getDescription().contains("提交了周报"));
        }
    }

    @Test
    void getDashboard_shouldJoinWithStudentInfo() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then - verify query joins with student_info to get student name
            verify(jdbcTemplate).query(
                    contains("student_svc.student_info"),
                    any(RowMapper.class),
                    eq(TEST_MENTOR_ID)
            );
        }
    }

    @Test
    void getDashboard_shouldUseCacheCorrectly() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            enterpriseMentorService.getDashboard();

            // Then
            verify(cacheService).getOrSet(
                    eq("mentor:dashboard:" + TEST_MENTOR_ID),
                    eq(5L),
                    eq(TimeUnit.MINUTES),
                    any()
            );
        }
    }

    @Test
    void getDashboard_shouldReturnCodeReviewCountAsZero() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_MENTOR_ID);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(5);

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_MENTOR_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            MentorDashboardDTO result = enterpriseMentorService.getDashboard();

            // Then - code review count should be 0 (not implemented yet)
            assertEquals(0, result.getPendingCodeReviewCount());
        }
    }
}
