package com.zhitu.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.student.dto.CapabilityRadarDTO;
import com.zhitu.student.dto.DashboardStatsDTO;
import com.zhitu.student.dto.RecommendationDTO;
import com.zhitu.student.dto.TaskDTO;
import com.zhitu.student.entity.EvaluationRecord;
import com.zhitu.student.entity.StudentCapability;
import com.zhitu.student.entity.StudentRecommendation;
import com.zhitu.student.entity.StudentTask;
import com.zhitu.student.mapper.EvaluationRecordMapper;
import com.zhitu.student.mapper.StudentCapabilityMapper;
import com.zhitu.student.mapper.StudentRecommendationMapper;
import com.zhitu.student.mapper.StudentTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentPortalServiceTest {

    @Mock
    private StudentTaskMapper studentTaskMapper;

    @Mock
    private EvaluationRecordMapper evaluationRecordMapper;

    @Mock
    private StudentCapabilityMapper studentCapabilityMapper;

    @Mock
    private StudentRecommendationMapper studentRecommendationMapper;

    @Mock
    private CacheService cacheService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StudentPortalService studentPortalService;

    private static final Long TEST_USER_ID = 15L;
    private static final Long TEST_STUDENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock cache service to always execute the supplier (lenient for tests that don't use cache)
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

            // Mock student ID lookup
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock training project count
            when(jdbcTemplate.queryForObject(contains("training_svc.project_enrollment"), eq(Integer.class), eq(TEST_STUDENT_ID)))
                    .thenReturn(3);

            // Mock internship job count
            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_job"), eq(Integer.class)))
                    .thenReturn(15);

            // Mock pending task count
            when(studentTaskMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(5L);

            // Mock evaluation records
            EvaluationRecord eval1 = new EvaluationRecord();
            EvaluationRecord eval2 = new EvaluationRecord();
            when(evaluationRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(eval1, eval2));

            // When
            DashboardStatsDTO result = studentPortalService.getDashboardStats();

            // Then
            assertNotNull(result);
            assertEquals(3, result.getTrainingProjectCount());
            assertEquals(15, result.getInternshipJobCount());
            assertEquals(5, result.getPendingTaskCount());
            assertTrue(result.getGrowthScore() >= 0 && result.getGrowthScore() <= 100);

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:dashboard:" + TEST_USER_ID), eq(5L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getDashboardStats_shouldReturnZerosWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student not found
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardStatsDTO result = studentPortalService.getDashboardStats();

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTrainingProjectCount());
            assertEquals(0, result.getInternshipJobCount());
            assertEquals(0, result.getPendingTaskCount());
            assertEquals(0, result.getGrowthScore());
        }
    }

    @Test
    void getDashboardStats_shouldCalculateGrowthScoreCorrectly() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            when(jdbcTemplate.queryForObject(contains("training_svc.project_enrollment"), eq(Integer.class), eq(TEST_STUDENT_ID)))
                    .thenReturn(0);

            when(jdbcTemplate.queryForObject(contains("internship_svc.internship_job"), eq(Integer.class)))
                    .thenReturn(0);

            when(studentTaskMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(0L);

            // Mock no evaluations
            when(evaluationRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            DashboardStatsDTO result = studentPortalService.getDashboardStats();

            // Then
            assertEquals(0, result.getGrowthScore(), "Growth score should be 0 when no evaluations exist");
        }
    }

    @Test
    void getCapabilityRadar_shouldReturnAllDimensionsWithScores() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student ID lookup
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock capability data
            StudentCapability cap1 = new StudentCapability();
            cap1.setDimension("technical_skill");
            cap1.setScore(85);

            StudentCapability cap2 = new StudentCapability();
            cap2.setDimension("communication");
            cap2.setScore(78);

            StudentCapability cap3 = new StudentCapability();
            cap3.setDimension("teamwork");
            cap3.setScore(92);

            when(studentCapabilityMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(cap1, cap2, cap3));

            // When
            CapabilityRadarDTO result = studentPortalService.getCapabilityRadar();

            // Then
            assertNotNull(result);
            assertNotNull(result.getDimensions());
            assertEquals(5, result.getDimensions().size(), "Should return all 5 dimensions");

            // Verify specific dimensions
            CapabilityRadarDTO.DimensionScore techSkill = result.getDimensions().stream()
                    .filter(d -> "technical_skill".equals(d.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(techSkill);
            assertEquals(85, techSkill.getScore());

            CapabilityRadarDTO.DimensionScore communication = result.getDimensions().stream()
                    .filter(d -> "communication".equals(d.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(communication);
            assertEquals(78, communication.getScore());

            CapabilityRadarDTO.DimensionScore teamwork = result.getDimensions().stream()
                    .filter(d -> "teamwork".equals(d.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(teamwork);
            assertEquals(92, teamwork.getScore());

            // Verify dimensions with no data default to 0
            CapabilityRadarDTO.DimensionScore problemSolving = result.getDimensions().stream()
                    .filter(d -> "problem_solving".equals(d.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(problemSolving);
            assertEquals(0, problemSolving.getScore());

            CapabilityRadarDTO.DimensionScore innovation = result.getDimensions().stream()
                    .filter(d -> "innovation".equals(d.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(innovation);
            assertEquals(0, innovation.getScore());

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:capability:" + TEST_USER_ID), eq(10L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getCapabilityRadar_shouldReturnDefaultWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student not found
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            CapabilityRadarDTO result = studentPortalService.getCapabilityRadar();

            // Then
            assertNotNull(result);
            assertNotNull(result.getDimensions());
            assertEquals(5, result.getDimensions().size());

            // All dimensions should have score 0
            for (CapabilityRadarDTO.DimensionScore dimension : result.getDimensions()) {
                assertEquals(0, dimension.getScore(), "All dimensions should be 0 when student not found");
            }
        }
    }

    @Test
    void getCapabilityRadar_shouldReturnDefaultWhenNoCapabilityData() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student ID lookup
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock no capability data
            when(studentCapabilityMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            CapabilityRadarDTO result = studentPortalService.getCapabilityRadar();

            // Then
            assertNotNull(result);
            assertNotNull(result.getDimensions());
            assertEquals(5, result.getDimensions().size());

            // All dimensions should have score 0
            for (CapabilityRadarDTO.DimensionScore dimension : result.getDimensions()) {
                assertEquals(0, dimension.getScore(), "All dimensions should be 0 when no capability data exists");
            }
        }
    }

    @Test
    void getCapabilityRadar_shouldIncludeAllRequiredDimensions() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            when(studentCapabilityMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            CapabilityRadarDTO result = studentPortalService.getCapabilityRadar();

            // Then
            List<String> dimensionNames = result.getDimensions().stream()
                    .map(CapabilityRadarDTO.DimensionScore::getName)
                    .toList();

            assertTrue(dimensionNames.contains("technical_skill"), "Should include technical_skill");
            assertTrue(dimensionNames.contains("communication"), "Should include communication");
            assertTrue(dimensionNames.contains("teamwork"), "Should include teamwork");
            assertTrue(dimensionNames.contains("problem_solving"), "Should include problem_solving");
            assertTrue(dimensionNames.contains("innovation"), "Should include innovation");
        }
    }

    @Test
    void getCapabilityRadar_shouldHandleScoresWithinValidRange() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock capability data with boundary values
            StudentCapability cap1 = new StudentCapability();
            cap1.setDimension("technical_skill");
            cap1.setScore(0);

            StudentCapability cap2 = new StudentCapability();
            cap2.setDimension("communication");
            cap2.setScore(100);

            StudentCapability cap3 = new StudentCapability();
            cap3.setDimension("teamwork");
            cap3.setScore(50);

            when(studentCapabilityMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(cap1, cap2, cap3));

            // When
            CapabilityRadarDTO result = studentPortalService.getCapabilityRadar();

            // Then
            for (CapabilityRadarDTO.DimensionScore dimension : result.getDimensions()) {
                assertTrue(dimension.getScore() >= 0 && dimension.getScore() <= 100,
                        "Score should be between 0 and 100");
            }
        }
    }

    @Test
    void getTasks_shouldReturnPendingTasksWhenStatusIsPending() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock pending tasks
            StudentTask task1 = createMockTask(1L, "Task 1", 0);
            StudentTask task2 = createMockTask(2L, "Task 2", 0);

            Page<StudentTask> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(task1, task2));
            mockPage.setTotal(2L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("pending", 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());

            // Verify all tasks are pending
            for (TaskDTO task : result.getRecords()) {
                assertEquals(0, task.getStatus(), "All tasks should have pending status (0)");
            }
        }
    }

    @Test
    void getTasks_shouldReturnCompletedTasksWhenStatusIsCompleted() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock completed tasks
            StudentTask task1 = createMockTask(3L, "Completed Task 1", 1);
            StudentTask task2 = createMockTask(4L, "Completed Task 2", 1);

            Page<StudentTask> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(task1, task2));
            mockPage.setTotal(2L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("completed", 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());

            // Verify all tasks are completed
            for (TaskDTO task : result.getRecords()) {
                assertEquals(1, task.getStatus(), "All tasks should have completed status (1)");
            }
        }
    }

    @Test
    void getTasks_shouldReturnAllTasksWhenStatusIsNull() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock mixed tasks
            StudentTask task1 = createMockTask(1L, "Pending Task", 0);
            StudentTask task2 = createMockTask(2L, "Completed Task", 1);

            Page<StudentTask> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(task1, task2));
            mockPage.setTotal(2L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks(null, 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
        }
    }

    @Test
    void getTasks_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student not found
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("pending", 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getTasks_shouldHandlePaginationCorrectly() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock page 2 with size 5
            StudentTask task1 = createMockTask(6L, "Task 6", 0);
            StudentTask task2 = createMockTask(7L, "Task 7", 0);

            Page<StudentTask> mockPage = new Page<>(2, 5);
            mockPage.setRecords(Arrays.asList(task1, task2));
            mockPage.setTotal(12L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("pending", 2, 5);

            // Then
            assertNotNull(result);
            assertEquals(12L, result.getTotal());
            assertEquals(2, result.getRecords().size());
            assertEquals(2, result.getPage());
            assertEquals(5, result.getSize());
        }
    }

    @Test
    void getTasks_shouldIncludeAllTaskFields() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock task with all fields
            StudentTask task = new StudentTask();
            task.setId(1L);
            task.setStudentId(TEST_STUDENT_ID);
            task.setTaskType("training");
            task.setRefId(100L);
            task.setTitle("Complete project milestone");
            task.setDescription("Finish the authentication module");
            task.setPriority(2);
            task.setStatus(0);
            task.setDueDate(OffsetDateTime.now().plusDays(7));
            task.setCreatedAt(OffsetDateTime.now());

            Page<StudentTask> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Collections.singletonList(task));
            mockPage.setTotal(1L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("pending", 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());

            TaskDTO taskDTO = result.getRecords().get(0);
            assertEquals(1L, taskDTO.getId());
            assertEquals("training", taskDTO.getTaskType());
            assertEquals(100L, taskDTO.getRefId());
            assertEquals("Complete project milestone", taskDTO.getTitle());
            assertEquals("Finish the authentication module", taskDTO.getDescription());
            assertEquals(2, taskDTO.getPriority());
            assertEquals(0, taskDTO.getStatus());
            assertNotNull(taskDTO.getDueDate());
            assertNotNull(taskDTO.getCreatedAt());
        }
    }

    @Test
    void getTasks_shouldHandleEmptyStatusString() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            StudentTask task1 = createMockTask(1L, "Task 1", 0);
            StudentTask task2 = createMockTask(2L, "Task 2", 1);

            Page<StudentTask> mockPage = new Page<>(1, 10);
            mockPage.setRecords(Arrays.asList(task1, task2));
            mockPage.setTotal(2L);

            when(studentTaskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mockPage);

            // When
            PageResult<TaskDTO> result = studentPortalService.getTasks("", 1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());
        }
    }

    /**
     * Helper method to create mock StudentTask
     */
    private StudentTask createMockTask(Long id, String title, Integer status) {
        StudentTask task = new StudentTask();
        task.setId(id);
        task.setStudentId(TEST_STUDENT_ID);
        task.setTaskType("training");
        task.setRefId(100L);
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setPriority(2);
        task.setStatus(status);
        task.setDueDate(OffsetDateTime.now().plusDays(7));
        task.setCreatedAt(OffsetDateTime.now());
        return task;
    }

    @Test
    void getRecommendations_shouldReturnAllRecommendationsWhenTypeIsAll() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock recommendations of different types
            StudentRecommendation rec1 = createMockRecommendation(1L, "project", 101L, new BigDecimal("95.5"));
            StudentRecommendation rec2 = createMockRecommendation(2L, "job", 201L, new BigDecimal("88.0"));
            StudentRecommendation rec3 = createMockRecommendation(3L, "course", 301L, new BigDecimal("92.3"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(rec1, rec2, rec3));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("all");

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:recommendations:" + TEST_USER_ID + ":all"), 
                    eq(15L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getRecommendations_shouldReturnOnlyProjectsWhenTypeIsProject() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock only project recommendations
            StudentRecommendation rec1 = createMockRecommendation(1L, "project", 101L, new BigDecimal("95.5"));
            StudentRecommendation rec2 = createMockRecommendation(2L, "project", 102L, new BigDecimal("88.0"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(rec1, rec2));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("project");

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify all are project type
            for (RecommendationDTO rec : result) {
                assertEquals("project", rec.getRecType());
            }

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:recommendations:" + TEST_USER_ID + ":project"), 
                    eq(15L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getRecommendations_shouldReturnOnlyJobsWhenTypeIsJob() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock only job recommendations
            StudentRecommendation rec1 = createMockRecommendation(1L, "job", 201L, new BigDecimal("90.0"));
            StudentRecommendation rec2 = createMockRecommendation(2L, "job", 202L, new BigDecimal("85.5"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(rec1, rec2));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("job");

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());

            // Verify all are job type
            for (RecommendationDTO rec : result) {
                assertEquals("job", rec.getRecType());
            }
        }
    }

    @Test
    void getRecommendations_shouldReturnOnlyCoursesWhenTypeIsCourse() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock only course recommendations
            StudentRecommendation rec1 = createMockRecommendation(1L, "course", 301L, new BigDecimal("92.0"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(rec1));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("course");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("course", result.get(0).getRecType());
        }
    }

    @Test
    void getRecommendations_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student not found
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("all");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getRecommendations_shouldReturnEmptyWhenNoRecommendations() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock no recommendations
            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("all");

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getRecommendations_shouldIncludeAllRecommendationFields() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock recommendation with all fields
            StudentRecommendation rec = new StudentRecommendation();
            rec.setId(1L);
            rec.setStudentId(TEST_STUDENT_ID);
            rec.setRecType("project");
            rec.setRefId(101L);
            rec.setScore(new BigDecimal("95.50"));
            rec.setReason("Based on your Java skills and interest in web development");
            rec.setCreatedAt(OffsetDateTime.now());

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(rec));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("all");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());

            RecommendationDTO dto = result.get(0);
            assertEquals(1L, dto.getId());
            assertEquals("project", dto.getRecType());
            assertEquals(101L, dto.getRefId());
            assertEquals(new BigDecimal("95.50"), dto.getScore());
            assertEquals("Based on your Java skills and interest in web development", dto.getReason());
            assertNotNull(dto.getCreatedAt());
        }
    }

    @Test
    void getRecommendations_shouldHandleCaseInsensitiveType() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            StudentRecommendation rec1 = createMockRecommendation(1L, "project", 101L, new BigDecimal("95.5"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(rec1));

            // When - using uppercase
            List<RecommendationDTO> result = studentPortalService.getRecommendations("PROJECT");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    void getRecommendations_shouldOrderByScoreAndCreatedAt() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock recommendations with different scores (already ordered by mapper)
            StudentRecommendation rec1 = createMockRecommendation(1L, "project", 101L, new BigDecimal("95.5"));
            StudentRecommendation rec2 = createMockRecommendation(2L, "job", 201L, new BigDecimal("90.0"));
            StudentRecommendation rec3 = createMockRecommendation(3L, "course", 301L, new BigDecimal("85.0"));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(rec1, rec2, rec3));

            // When
            List<RecommendationDTO> result = studentPortalService.getRecommendations("all");

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());

            // Verify order (highest score first)
            assertTrue(result.get(0).getScore().compareTo(result.get(1).getScore()) >= 0);
            assertTrue(result.get(1).getScore().compareTo(result.get(2).getScore()) >= 0);
        }
    }

    @Test
    void getRecommendations_shouldUseCacheWithCorrectTTL() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            when(studentRecommendationMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // When
            studentPortalService.getRecommendations("all");

            // Then - verify cache was called with 15 minutes TTL
            verify(cacheService).getOrSet(
                    eq("student:recommendations:" + TEST_USER_ID + ":all"),
                    eq(15L),
                    eq(TimeUnit.MINUTES),
                    any()
            );
        }
    }

    /**
     * Helper method to create mock StudentRecommendation
     */
    private StudentRecommendation createMockRecommendation(Long id, String recType, Long refId, BigDecimal score) {
        StudentRecommendation rec = new StudentRecommendation();
        rec.setId(id);
        rec.setStudentId(TEST_STUDENT_ID);
        rec.setRecType(recType);
        rec.setRefId(refId);
        rec.setScore(score);
        rec.setReason("Recommended based on your profile");
        rec.setCreatedAt(OffsetDateTime.now());
        return rec;
    }

    // ==================== Tests for Training Projects ====================

    @Test
    void getTrainingProjects_shouldReturnProjectsWithEnrollmentStatus() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock query results
            when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong(), anyInt(), anyInt()))
                    .thenAnswer(invocation -> {
                        RowMapper<?> mapper = invocation.getArgument(1);
                        // Simulate result set with one enrolled and one not enrolled project
                        return Arrays.asList(
                                // Project 1 - enrolled
                                mapper.mapRow(mockResultSet(1L, "Project 1", "[\"Java\",\"Spring\"]", 1L), 0),
                                // Project 2 - not enrolled
                                mapper.mapRow(mockResultSet(2L, "Project 2", "[\"Python\",\"Django\"]", null), 1)
                        );
                    });

            when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class)))
                    .thenReturn(2L);

            // When
            PageResult result = studentPortalService.getTrainingProjects(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getRecords().size());

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:projects:list:1:10"), eq(5L), eq(TimeUnit.MINUTES), any());
        }
    }

    @Test
    void getProjectBoard_shouldReturnTasksGroupedByStatus() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock student ID lookup
            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock enrollment check - student is enrolled
            when(jdbcTemplate.queryForObject(contains("project_enrollment"), eq(Integer.class), anyLong(), anyLong()))
                    .thenReturn(1);

            // Mock task query - return empty list for simplicity
            when(jdbcTemplate.query(contains("project_task"), any(RowMapper.class), anyLong()))
                    .thenReturn(Collections.emptyList());

            // When
            var result = studentPortalService.getProjectBoard(1L);

            // Then
            assertNotNull(result);
            assertNotNull(result.getTodo());
            assertNotNull(result.getInProgress());
            assertNotNull(result.getDone());
        }
    }

    @Test
    void getProjectBoard_shouldThrowExceptionWhenNotEnrolled() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock enrollment check - student is NOT enrolled
            when(jdbcTemplate.queryForObject(contains("project_enrollment"), eq(Integer.class), anyLong(), anyLong()))
                    .thenReturn(0);

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                studentPortalService.getProjectBoard(1L);
            });
        }
    }

    // ==================== Tests for Internship Jobs ====================

    @Test
    void getInternshipJobs_shouldReturnJobsWithApplicationStatus() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            // Mock query results - return empty for simplicity
            when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class)))
                    .thenReturn(0L);

            // When
            PageResult result = studentPortalService.getInternshipJobs(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());

            // Verify cache was used
            verify(cacheService).getOrSet(eq("student:jobs:list:1:10"), eq(5L), eq(TimeUnit.MINUTES), any());
        }
    }

    // ==================== Tests for Weekly Reports ====================

    @Test
    void getMyReports_shouldReturnReportsOrderedByDate() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock query results - return empty for simplicity
            when(jdbcTemplate.query(contains("weekly_report"), any(RowMapper.class), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class), anyLong()))
                    .thenReturn(0L);

            // When
            PageResult result = studentPortalService.getMyReports(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getMyReports_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult result = studentPortalService.getMyReports(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }

    // ==================== Tests for Evaluation Summary ====================

    @Test
    void getEvaluationSummary_shouldCalculateAverageScore() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock query results - return empty for simplicity
            when(jdbcTemplate.query(contains("evaluation_record"), any(RowMapper.class), anyLong()))
                    .thenReturn(Collections.emptyList());

            // When
            var result = studentPortalService.getEvaluationSummary();

            // Then
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getAverageScore());
            assertTrue(result.getEvaluations().isEmpty());
        }
    }

    @Test
    void getEvaluationSummary_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            var result = studentPortalService.getEvaluationSummary();

            // Then
            assertNotNull(result);
            assertEquals(BigDecimal.ZERO, result.getAverageScore());
            assertTrue(result.getEvaluations().isEmpty());
        }
    }

    // ==================== Tests for Certificates ====================

    @Test
    void getMyCertificates_shouldReturnCertificatesWithDownloadUrl() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock query results - return empty for simplicity
            when(jdbcTemplate.query(contains("growth_badge"), any(RowMapper.class), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class), anyLong()))
                    .thenReturn(0L);

            // When
            PageResult result = studentPortalService.getMyCertificates(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getMyCertificates_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult result = studentPortalService.getMyCertificates(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }

    // ==================== Tests for Badges ====================

    @Test
    void getMyBadges_shouldReturnBadgesOrderedByDate() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.singletonList(TEST_STUDENT_ID));

            // Mock query results - return empty for simplicity
            when(jdbcTemplate.query(contains("growth_badge"), any(RowMapper.class), anyLong(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class), anyLong()))
                    .thenReturn(0L);

            // When
            PageResult result = studentPortalService.getMyBadges(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertEquals(1, result.getPage());
            assertEquals(10, result.getSize());
        }
    }

    @Test
    void getMyBadges_shouldReturnEmptyWhenStudentNotFound() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Given
            mockedUserContext.when(UserContext::getUserId).thenReturn(TEST_USER_ID);

            when(jdbcTemplate.query(contains("student_svc.student_info"), any(RowMapper.class), eq(TEST_USER_ID)))
                    .thenReturn(Collections.emptyList());

            // When
            PageResult result = studentPortalService.getMyBadges(1, 10);

            // Then
            assertNotNull(result);
            assertEquals(0L, result.getTotal());
            assertTrue(result.getRecords().isEmpty());
        }
    }

    /**
     * Helper method to create mock ResultSet for training projects
     */
    private java.sql.ResultSet mockResultSet(Long id, String projectName, String techStack, Long enrollmentId) throws Exception {
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.getLong("id")).thenReturn(id);
        when(rs.getString("project_name")).thenReturn(projectName);
        when(rs.getString("description")).thenReturn("Description for " + projectName);
        when(rs.getString("tech_stack")).thenReturn(techStack);
        when(rs.getString("industry")).thenReturn("互联网");
        when(rs.getInt("max_teams")).thenReturn(5);
        when(rs.getInt("max_members")).thenReturn(4);
        when(rs.getObject("start_date", java.time.LocalDate.class)).thenReturn(java.time.LocalDate.now());
        when(rs.getObject("end_date", java.time.LocalDate.class)).thenReturn(java.time.LocalDate.now().plusMonths(3));
        when(rs.getInt("status")).thenReturn(1);
        when(rs.getObject("created_at", java.time.LocalDateTime.class)).thenReturn(java.time.LocalDateTime.now());
        when(rs.getObject("enrollment_id", Long.class)).thenReturn(enrollmentId);
        return rs;
    }
}
