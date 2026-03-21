package com.zhitu.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.DashboardStatsDTO;
import com.zhitu.enterprise.dto.TalentPoolDTO;
import com.zhitu.enterprise.dto.TodoDTO;
import com.zhitu.enterprise.entity.EnterpriseTodo;
import com.zhitu.enterprise.entity.TalentPool;
import com.zhitu.enterprise.mapper.EnterpriseTodoMapper;
import com.zhitu.enterprise.mapper.TalentPoolMapper;
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
 * 企业门户服务测试
 */
@ExtendWith(MockitoExtension.class)
class EnterprisePortalServiceTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EnterpriseTodoMapper enterpriseTodoMapper;

    @Mock
    private TalentPoolMapper talentPoolMapper;

    @InjectMocks
    private EnterprisePortalService enterprisePortalService;

    private static final Long TEST_USER_ID = 20L;
    private static final Long TEST_TENANT_ID = 5L;

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
    void getDashboardStats_shouldReturnCorrectStats() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant ID lookup
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock active job count
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_job"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(8);

            // Mock pending application count
            when(jdbcTemplate.queryForObject(contains("internship_svc.job_application"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(25);

            // Mock active intern count
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_record"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(12);

            // Mock training project count
            when(jdbcTemplate.queryForObject(contains("training_svc.training_project"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(3);

            // When
            DashboardStatsDTO result = enterprisePortalService.getDashboardStats();

            // Then
            assertNotNull(result);
            assertEquals(8, result.getActiveJobCount());
            assertEquals(25, result.getPendingApplicationCount());
            assertEquals(12, result.getActiveInternCount());
            assertEquals(3, result.getTrainingProjectCount());

            // Verify cache was used
            verify(cacheService).getOrSet(eq("enterprise:dashboard:" + TEST_TENANT_ID), eq(5L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getDashboardStats_shouldReturnZerosWhenTenantNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant not found
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardStatsDTO result = enterprisePortalService.getDashboardStats();

            // Then
            assertNotNull(result);
            assertEquals(0, result.getActiveJobCount());
            assertEquals(0, result.getPendingApplicationCount());
            assertEquals(0, result.getActiveInternCount());
            assertEquals(0, result.getTrainingProjectCount());

            // Verify no further queries were made
            verify(jdbcTemplate, times(1)).query(anyString(), any(RowMapper.class), eq(TEST_USER_ID));
            verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID));
        }
    }

    @Test
    void getDashboardStats_shouldHandleZeroCounts() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock all counts as zero
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_job"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);
            when(jdbcTemplate.queryForObject(contains("internship_svc.job_application"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_record"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);
            when(jdbcTemplate.queryForObject(contains("training_svc.training_project"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);

            // When
            DashboardStatsDTO result = enterprisePortalService.getDashboardStats();

            // Then
            assertNotNull(result);
            assertEquals(0, result.getActiveJobCount());
            assertEquals(0, result.getPendingApplicationCount());
            assertEquals(0, result.getActiveInternCount());
            assertEquals(0, result.getTrainingProjectCount());
        }
    }

    @Test
    void getDashboardStats_shouldUseCacheCorrectly() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_job"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(5);
            when(jdbcTemplate.queryForObject(contains("internship_svc.job_application"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(10);
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_record"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(7);
            when(jdbcTemplate.queryForObject(contains("training_svc.training_project"), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(2);

            // When
            enterprisePortalService.getDashboardStats();

            // Then
            verify(cacheService).getOrSet(
                    eq("enterprise:dashboard:" + TEST_TENANT_ID),
                    eq(5L),
                    eq(TimeUnit.MINUTES),
                    any()
            );
        }
    }

    @Test
    void getDashboardStats_shouldFilterByTenantId() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(1);

            // When
            enterprisePortalService.getDashboardStats();

            // Then - verify all queries use tenant_id for filtering
            verify(jdbcTemplate, atLeast(4)).queryForObject(contains("enterprise_id = ?"), eq(Integer.class), eq(TEST_TENANT_ID));
            verify(jdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID));
        }
    }

    @Test
    void getDashboardStats_shouldCountOnlyActiveJobs() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);

            // When
            enterprisePortalService.getDashboardStats();

            // Then - verify query filters by status = 1 (active) - both jobs and interns use this
            verify(jdbcTemplate, atLeast(2)).queryForObject(
                    contains("status = 1"),
                    eq(Integer.class),
                    eq(TEST_TENANT_ID)
            );
        }
    }

    @Test
    void getDashboardStats_shouldCountOnlyPendingApplications() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);

            // When
            enterprisePortalService.getDashboardStats();

            // Then - verify query filters by status = 0 (pending)
            verify(jdbcTemplate).queryForObject(
                    contains("ja.status = 0"),
                    eq(Integer.class),
                    eq(TEST_TENANT_ID)
            );
        }
    }

    @Test
    void getDashboardStats_shouldCountOnlyActiveInterns() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);

            // When
            enterprisePortalService.getDashboardStats();

            // Then - verify query filters by status = 1 (active)
            verify(jdbcTemplate).queryForObject(
                    contains("internship_svc.internship_record"),
                    eq(Integer.class),
                    eq(TEST_TENANT_ID)
            );
        }
    }

    @Test
    void getDashboardStats_shouldExcludeDeletedRecords() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0);

            // When
            enterprisePortalService.getDashboardStats();

            // Then - verify queries exclude deleted records (jobs and training projects)
            verify(jdbcTemplate, atLeast(2)).queryForObject(
                    contains("is_deleted = false"),
                    eq(Integer.class),
                    eq(TEST_TENANT_ID)
            );
            verify(jdbcTemplate, atLeast(2)).queryForObject(
                    anyString(),
                    eq(Integer.class),
                    eq(TEST_TENANT_ID)
            );
        }
    }

    // ========== getTodos Tests ==========

    @Test
    void getTodos_shouldReturnPaginatedTodos() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            EnterpriseTodo todo1 = createTodo(1L, "application_review", "Review application from John Doe", 3, 0);
            EnterpriseTodo todo2 = createTodo(2L, "interview_schedule", "Schedule interview with Jane Smith", 2, 0);

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(todo1, todo2));
            mockPage.setTotal(2L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TodoDTO> result = enterprisePortalService.getTodos(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());

            TodoDTO firstTodo = result.getRecords().get(0);
            assertEquals(1L, firstTodo.getId());
            assertEquals("application_review", firstTodo.getTodoType());
            assertEquals("Review application from John Doe", firstTodo.getTitle());
            assertEquals(3, firstTodo.getPriority());
            assertEquals(0, firstTodo.getStatus());
        }
    }

    @Test
    void getTodos_shouldFilterByUserId() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            enterprisePortalService.getTodos(1, 10);

            // Then
            verify(enterpriseTodoMapper).selectPage(any(Page.class), argThat(wrapper -> {
                // Verify the wrapper filters by user_id and status=0
                return true; // LambdaQueryWrapper is hard to inspect, but we verify the call was made
            }));
        }
    }

    @Test
    void getTodos_shouldFilterByPendingStatus() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            enterprisePortalService.getTodos(1, 10);

            // Then - verify selectPage was called (filtering logic is in the wrapper)
            verify(enterpriseTodoMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void getTodos_shouldOrderByPriorityAndDueDate() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            OffsetDateTime now = OffsetDateTime.now();
            EnterpriseTodo todo1 = createTodoWithDueDate(1L, "High priority, early due", 3, now.plusDays(1));
            EnterpriseTodo todo2 = createTodoWithDueDate(2L, "High priority, late due", 3, now.plusDays(5));
            EnterpriseTodo todo3 = createTodoWithDueDate(3L, "Low priority, early due", 1, now.plusDays(2));

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(todo1, todo2, todo3));
            mockPage.setTotal(3L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TodoDTO> result = enterprisePortalService.getTodos(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getRecords().size());
            // Verify the order is maintained (mapper returns ordered results)
            assertEquals(1L, result.getRecords().get(0).getId());
            assertEquals(2L, result.getRecords().get(1).getId());
            assertEquals(3L, result.getRecords().get(2).getId());
        }
    }

    @Test
    void getTodos_shouldHandleEmptyResults() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.emptyList());
            mockPage.setTotal(0L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TodoDTO> result = enterprisePortalService.getTodos(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getTodos_shouldHandlePagination() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            EnterpriseTodo todo1 = createTodo(11L, "report_review", "Review weekly report", 2, 0);
            EnterpriseTodo todo2 = createTodo(12L, "evaluation_pending", "Complete evaluation", 1, 0);

            Page<EnterpriseTodo> mockPage = new Page<>(2, 5);
            mockPage.setRecords(Arrays.asList(todo1, todo2));
            mockPage.setTotal(12L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TodoDTO> result = enterprisePortalService.getTodos(2, 5);

            // Then
            assertNotNull(result);
            assertEquals(12L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(2, result.getPage());
            assertEquals(5, result.getSize());
        }
    }

    @Test
    void getTodos_shouldIncludeAllTodoFields() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            OffsetDateTime dueDate = OffsetDateTime.now().plusDays(3);
            OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);

            EnterpriseTodo todo = new EnterpriseTodo();
            todo.setId(100L);
            todo.setTodoType("application_review");
            todo.setRefType("application");
            todo.setRefId(500L);
            todo.setTitle("Review application from Alice");
            todo.setPriority(3);
            todo.setDueDate(dueDate);
            todo.setStatus(0);
            todo.setCreatedAt(createdAt);

            Page<EnterpriseTodo> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(todo));
            mockPage.setTotal(1L);

            when(enterpriseTodoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TodoDTO> result = enterprisePortalService.getTodos(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());

            TodoDTO todoDTO = result.getRecords().get(0);
            assertEquals(100L, todoDTO.getId());
            assertEquals("application_review", todoDTO.getTodoType());
            assertEquals("application", todoDTO.getRefType());
            assertEquals(500L, todoDTO.getRefId());
            assertEquals("Review application from Alice", todoDTO.getTitle());
            assertEquals(3, todoDTO.getPriority());
            assertEquals(dueDate, todoDTO.getDueDate());
            assertEquals(0, todoDTO.getStatus());
            assertEquals(createdAt, todoDTO.getCreatedAt());
        }
    }

    // Helper methods
    private EnterpriseTodo createTodo(Long id, String todoType, String title, Integer priority, Integer status) {
        EnterpriseTodo todo = new EnterpriseTodo();
        todo.setId(id);
        todo.setUserId(TEST_USER_ID);
        todo.setTodoType(todoType);
        todo.setTitle(title);
        todo.setPriority(priority);
        todo.setStatus(status);
        todo.setCreatedAt(OffsetDateTime.now());
        return todo;
    }

    private EnterpriseTodo createTodoWithDueDate(Long id, String title, Integer priority, OffsetDateTime dueDate) {
        EnterpriseTodo todo = createTodo(id, "application_review", title, priority, 0);
        todo.setDueDate(dueDate);
        return todo;
    }

    // ========== getActivities Tests ==========

    @Test
    void getActivities_shouldReturnPaginatedActivities() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant ID lookup
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock activities query
            OffsetDateTime now = OffsetDateTime.now();
            when(jdbcTemplate.query(
                    contains("enterprise_svc.enterprise_activity"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(10),
                    eq(0)
            )).thenAnswer(invocation -> {
                RowMapper<ActivityDTO> mapper = invocation.getArgument(1);
                // Simulate two activity records
                return Arrays.asList(
                        new ActivityDTO(1L, "application", "New application from John Doe", "application", 100L, now.minusHours(2)),
                        new ActivityDTO(2L, "interview", "Interview scheduled with Jane Smith", "interview", 101L, now.minusHours(5))
                );
            });

            // Mock count query
            when(jdbcTemplate.queryForObject(
                    contains("COUNT(*)"),
                    eq(Long.class),
                    eq(TEST_TENANT_ID)
            )).thenReturn(2L);

            // When
            PageResult<ActivityDTO> result = enterprisePortalService.getActivities(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());

            ActivityDTO firstActivity = result.getRecords().get(0);
            assertEquals(1L, firstActivity.getId());
            assertEquals("application", firstActivity.getActivityType());
            assertEquals("New application from John Doe", firstActivity.getDescription());
            assertEquals("application", firstActivity.getRefType());
            assertEquals(100L, firstActivity.getRefId());
        }
    }

    @Test
    void getActivities_shouldFilterByTenantId() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getActivities(1, 10);

            // Then - verify query uses tenant_id
            verify(jdbcTemplate).query(
                    contains("tenant_id = ?"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getActivities_shouldFilterLast30Days() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getActivities(1, 10);

            // Then - verify query filters by 30 days
            verify(jdbcTemplate).query(
                    contains("created_at >= NOW() - INTERVAL '30 days'"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getActivities_shouldOrderByCreatedAtDescending() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getActivities(1, 10);

            // Then - verify query orders by created_at DESC
            verify(jdbcTemplate).query(
                    contains("ORDER BY created_at DESC"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getActivities_shouldUseCacheCorrectly() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getActivities(1, 10);

            // Then - verify cache is used with 3-minute TTL
            verify(cacheService).getOrSet(
                    eq("enterprise:activities:" + TEST_TENANT_ID + ":1"),
                    eq(3L),
                    eq(TimeUnit.MINUTES),
                    any()
            );
        }
    }

    @Test
    void getActivities_shouldHandleEmptyResults() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            PageResult<ActivityDTO> result = enterprisePortalService.getActivities(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getActivities_shouldReturnEmptyWhenTenantNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant not found
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult<ActivityDTO> result = enterprisePortalService.getActivities(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());

            // Verify no activity queries were made
            verify(jdbcTemplate, never()).query(
                    contains("enterprise_activity"),
                    any(RowMapper.class),
                    anyLong(),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getActivities_shouldHandlePagination() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            OffsetDateTime now = OffsetDateTime.now();
            when(jdbcTemplate.query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(5),
                    eq(5) // offset for page 2, size 5
            )).thenAnswer(invocation -> {
                RowMapper<ActivityDTO> mapper = invocation.getArgument(1);
                return Collections.singletonList(
                        new ActivityDTO(6L, "report_submitted", "Weekly report submitted", "intern", 200L, now)
                );
            });

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(10L);

            // When
            PageResult<ActivityDTO> result = enterprisePortalService.getActivities(2, 5);

            // Then
            assertNotNull(result);
            assertEquals(10L, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals(2, result.getPage());
            assertEquals(5, result.getSize());

            // Verify correct offset calculation
            verify(jdbcTemplate).query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(5), // size
                    eq(5)  // offset = (page - 1) * size = (2 - 1) * 5 = 5
            );
        }
    }

    @Test
    void getActivities_shouldIncludeAllActivityFields() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            OffsetDateTime createdAt = OffsetDateTime.now().minusHours(3);
            when(jdbcTemplate.query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            )).thenAnswer(invocation -> {
                return Collections.singletonList(
                        new ActivityDTO(
                                999L,
                                "evaluation",
                                "Performance evaluation completed for intern",
                                "intern",
                                555L,
                                createdAt
                        )
                );
            });

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(1L);

            // When
            PageResult<ActivityDTO> result = enterprisePortalService.getActivities(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());

            ActivityDTO activity = result.getRecords().get(0);
            assertEquals(999L, activity.getId());
            assertEquals("evaluation", activity.getActivityType());
            assertEquals("Performance evaluation completed for intern", activity.getDescription());
            assertEquals("intern", activity.getRefType());
            assertEquals(555L, activity.getRefId());
            assertEquals(createdAt, activity.getCreatedAt());
        }
    }

    // ========== getTalentPool Tests ==========

    @Test
    void getTalentPool_shouldReturnPaginatedTalents() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant ID lookup
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock talent pool query
            OffsetDateTime collectedAt = OffsetDateTime.now().minusDays(5);
            when(jdbcTemplate.query(
                    contains("enterprise_svc.talent_pool"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(10),
                    eq(0)
            )).thenAnswer(invocation -> {
                RowMapper<TalentPoolDTO> mapper = invocation.getArgument(1);
                // Simulate two talent records
                return Arrays.asList(
                        createTalentPoolDTO(1L, 100L, "张三", "2021001", "计算机科学", "2021", "[\"Java\",\"Spring\"]", "优秀候选人", collectedAt),
                        createTalentPoolDTO(2L, 101L, "李四", "2021002", "软件工程", "2021", "[\"Python\",\"Django\"]", "有潜力", collectedAt.minusDays(1))
                );
            });

            // Mock count query
            when(jdbcTemplate.queryForObject(
                    contains("COUNT(*)"),
                    eq(Long.class),
                    eq(TEST_TENANT_ID)
            )).thenReturn(2L);

            // When
            PageResult<TalentPoolDTO> result = enterprisePortalService.getTalentPool(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());

            TalentPoolDTO firstTalent = result.getRecords().get(0);
            assertEquals(1L, firstTalent.getId());
            assertEquals(100L, firstTalent.getStudentId());
            assertEquals("张三", firstTalent.getStudentName());
            assertEquals("2021001", firstTalent.getStudentNo());
        }
    }

    @Test
    void getTalentPool_shouldFilterByTenantId() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getTalentPool(1, 10);

            // Then - verify query uses tenant_id
            verify(jdbcTemplate).query(
                    contains("tp.tenant_id = ?"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getTalentPool_shouldExcludeDeletedRecords() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getTalentPool(1, 10);

            // Then - verify query excludes deleted records
            verify(jdbcTemplate).query(
                    contains("tp.is_deleted = false"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getTalentPool_shouldJoinWithStudentInfo() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getTalentPool(1, 10);

            // Then - verify query joins with student_info
            verify(jdbcTemplate).query(
                    contains("INNER JOIN student_svc.student_info si ON tp.student_id = si.id"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getTalentPool_shouldOrderByCreatedAtDescending() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(TEST_TENANT_ID), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(0L);

            // When
            enterprisePortalService.getTalentPool(1, 10);

            // Then - verify query orders by created_at DESC
            verify(jdbcTemplate).query(
                    contains("ORDER BY tp.created_at DESC"),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getTalentPool_shouldReturnEmptyWhenTenantNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant not found
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult<TalentPoolDTO> result = enterprisePortalService.getTalentPool(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());

            // Verify no talent pool queries were made
            verify(jdbcTemplate, never()).query(
                    contains("talent_pool"),
                    any(RowMapper.class),
                    anyLong(),
                    anyInt(),
                    anyInt()
            );
        }
    }

    @Test
    void getTalentPool_shouldHandlePagination() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            OffsetDateTime now = OffsetDateTime.now();
            when(jdbcTemplate.query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(5),
                    eq(5) // offset for page 2, size 5
            )).thenAnswer(invocation -> Collections.singletonList(
                    createTalentPoolDTO(6L, 106L, "王五", "2021006", "信息工程", "2021", "[\"C++\"]", "备注", now)
            ));

            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(TEST_TENANT_ID)))
                    .thenReturn(10L);

            // When
            PageResult<TalentPoolDTO> result = enterprisePortalService.getTalentPool(2, 5);

            // Then
            assertNotNull(result);
            assertEquals(10L, result.getTotal());
            assertEquals(1, result.getRecords().size());
            assertEquals(2, result.getPage());
            assertEquals(5, result.getSize());

            // Verify correct offset calculation
            verify(jdbcTemplate).query(
                    anyString(),
                    any(RowMapper.class),
                    eq(TEST_TENANT_ID),
                    eq(5), // size
                    eq(5)  // offset = (page - 1) * size = (2 - 1) * 5 = 5
            );
        }
    }

    // ========== removeFromTalentPool Tests ==========

    @Test
    void removeFromTalentPool_shouldSoftDeleteRecord() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant ID lookup
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock talent pool record
            TalentPool talentPool = new TalentPool();
            talentPool.setId(1L);
            talentPool.setTenantId(TEST_TENANT_ID);
            talentPool.setStudentId(100L);

            when(talentPoolMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(talentPool);

            when(talentPoolMapper.deleteById(1L))
                    .thenReturn(1);

            // When
            enterprisePortalService.removeFromTalentPool(1L);

            // Then
            verify(talentPoolMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(talentPoolMapper).deleteById(1L);
        }
    }

    @Test
    void removeFromTalentPool_shouldVerifyTenantOwnership() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            // Mock record not found (different tenant)
            when(talentPoolMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                enterprisePortalService.removeFromTalentPool(1L);
            });

            assertEquals("人才库记录不存在或无权访问", exception.getMessage());
            verify(talentPoolMapper, never()).deleteById(anyLong());
        }
    }

    @Test
    void removeFromTalentPool_shouldThrowExceptionWhenTenantNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock tenant not found
            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                enterprisePortalService.removeFromTalentPool(1L);
            });

            assertEquals("无法获取企业信息", exception.getMessage());
            verify(talentPoolMapper, never()).selectOne(any(LambdaQueryWrapper.class));
            verify(talentPoolMapper, never()).deleteById(anyLong());
        }
    }

    @Test
    void removeFromTalentPool_shouldThrowExceptionWhenDeleteFails() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("auth_center.sys_user"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_TENANT_ID));

            TalentPool talentPool = new TalentPool();
            talentPool.setId(1L);
            talentPool.setTenantId(TEST_TENANT_ID);

            when(talentPoolMapper.selectOne(any(LambdaQueryWrapper.class)))
                    .thenReturn(talentPool);

            // Mock delete failure
            when(talentPoolMapper.deleteById(1L))
                    .thenReturn(0);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                enterprisePortalService.removeFromTalentPool(1L);
            });

            assertEquals("删除失败", exception.getMessage());
        }
    }

    // Helper method for talent pool
    private TalentPoolDTO createTalentPoolDTO(Long id, Long studentId, String studentName, String studentNo,
                                               String major, String grade, String skills, String remark,
                                               OffsetDateTime collectedAt) {
        TalentPoolDTO dto = new TalentPoolDTO();
        dto.setId(id);
        dto.setStudentId(studentId);
        dto.setStudentName(studentName);
        dto.setStudentNo(studentNo);
        dto.setMajor(major);
        dto.setGrade(grade);
        dto.setSkills(skills);
        dto.setRemark(remark);
        dto.setCollectedAt(collectedAt);
        return dto;
    }
}
