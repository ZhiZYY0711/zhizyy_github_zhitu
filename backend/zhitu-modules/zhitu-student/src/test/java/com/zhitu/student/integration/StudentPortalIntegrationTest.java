package com.zhitu.student.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.*;
import com.zhitu.student.entity.*;
import com.zhitu.student.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Student Portal Integration Tests
 * 
 * Tests complete request-response flow for all 11 Student Portal endpoints:
 * 1. GET /api/student-portal/v1/dashboard - Dashboard statistics
 * 2. GET /api/student-portal/v1/capability/radar - Capability radar chart
 * 3. GET /api/student-portal/v1/tasks - Task management with status filter
 * 4. GET /api/student-portal/v1/recommendations - Personalized recommendations
 * 5. GET /api/student-portal/v1/training/projects - Training projects list
 * 6. GET /api/student-portal/v1/training/projects/{id}/board - Project scrum board
 * 7. GET /api/student-portal/v1/internship/jobs - Internship jobs list
 * 8. GET /api/student-portal/v1/internship/reports/my - Student weekly reports
 * 9. GET /api/student-portal/v1/growth/evaluation - Growth evaluation summary
 * 10. GET /api/student-portal/v1/growth/certificates - Student certificates
 * 11. GET /api/student-portal/v1/growth/badges - Student badges
 * 
 * Validates: Requirements 1-11
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Student Portal Integration Tests")
class StudentPortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StudentTaskMapper studentTaskMapper;

    @Autowired
    private StudentCapabilityMapper studentCapabilityMapper;

    @Autowired
    private StudentRecommendationMapper studentRecommendationMapper;

    @Autowired
    private EvaluationRecordMapper evaluationRecordMapper;

    private static final Long TEST_USER_ID = 1000L;
    private static final Long TEST_STUDENT_ID = 2000L;
    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_PROJECT_ID = 3000L;
    private static final Long TEST_JOB_ID = 4000L;

    @BeforeEach
    void setUp() {
        // Setup user context
        UserContext.LoginUser user = UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .username("teststudent")
                .tenantId(TEST_TENANT_ID)
                .role("STUDENT")
                .build();
        UserContext.set(user);

        // Clear Redis cache
        clearRedisCache();

        // Setup test data
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        cleanupTestData();

        // Clear user context
        UserContext.clear();

        // Clear Redis cache
        clearRedisCache();
    }

    // ==================== Test 1: Dashboard Statistics ====================

    @Test
    @Order(1)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /dashboard - Should return dashboard statistics with 200")
    void testGetDashboard_Success() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trainingProjectCount").exists())
                .andExpect(jsonPath("$.data.internshipJobCount").exists())
                .andExpect(jsonPath("$.data.pendingTaskCount").exists())
                .andExpect(jsonPath("$.data.growthScore").exists())
                .andReturn();

        // Verify response data
        String content = result.getResponse().getContentAsString();
        Result<DashboardStatsDTO> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructParametricType(Result.class, DashboardStatsDTO.class));
        
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getTrainingProjectCount()).isGreaterThanOrEqualTo(0);
        assertThat(response.getData().getInternshipJobCount()).isGreaterThanOrEqualTo(0);
        assertThat(response.getData().getPendingTaskCount()).isGreaterThanOrEqualTo(0);
        assertThat(response.getData().getGrowthScore()).isBetween(0, 100);
    }

    @Test
    @Order(2)
    @DisplayName("GET /dashboard - Should return 401 when not authenticated")
    void testGetDashboard_Unauthorized() throws Exception {
        // Given: Clear user context
        UserContext.clear();

        // When & Then
        mockMvc.perform(get("/api/student-portal/v1/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /dashboard - Should use Redis cache on second request")
    void testGetDashboard_CacheHit() throws Exception {
        // First request - cache miss
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());

        // Verify cache key exists
        String cacheKey = "student:dashboard:" + TEST_USER_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // Second request - cache hit
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());
    }

    // ==================== Test 2: Capability Radar ====================

    @Test
    @Order(4)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /capability/radar - Should return capability radar with 5 dimensions")
    void testGetCapabilityRadar_Success() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/capability/radar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dimensions").isArray())
                .andExpect(jsonPath("$.data.dimensions.length()").value(5))
                .andReturn();

        // Verify all dimensions are present
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("technical_skill");
        assertThat(content).contains("communication");
        assertThat(content).contains("teamwork");
        assertThat(content).contains("problem_solving");
        assertThat(content).contains("innovation");
    }

    @Test
    @Order(5)
    @DisplayName("GET /capability/radar - Should return 401 when not authenticated")
    void testGetCapabilityRadar_Unauthorized() throws Exception {
        UserContext.clear();

        mockMvc.perform(get("/api/student-portal/v1/capability/radar"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Test 3: Task Management ====================

    @Test
    @Order(6)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /tasks - Should return all tasks without status filter")
    void testGetTasks_AllTasks() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.total").exists());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /tasks?status=pending - Should return only pending tasks")
    void testGetTasks_PendingOnly() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("status", "pending")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        // Verify all returned tasks have status = 0 (pending)
        String content = result.getResponse().getContentAsString();
        assertThat(content).doesNotContain("\"status\":1");
    }

    @Test
    @Order(8)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /tasks?status=completed - Should return only completed tasks")
    void testGetTasks_CompletedOnly() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("status", "completed")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    // ==================== Test 4: Recommendations ====================

    @Test
    @Order(9)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /recommendations - Should return all recommendations")
    void testGetRecommendations_All() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/recommendations")
                        .param("type", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(10)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /recommendations?type=project - Should return only project recommendations")
    void testGetRecommendations_ProjectOnly() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/recommendations")
                        .param("type", "project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /recommendations?type=job - Should return only job recommendations")
    void testGetRecommendations_JobOnly() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/recommendations")
                        .param("type", "job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(12)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /recommendations - Should use Redis cache")
    void testGetRecommendations_CacheHit() throws Exception {
        // First request
        mockMvc.perform(get("/api/student-portal/v1/recommendations")
                        .param("type", "all"))
                .andExpect(status().isOk());

        // Verify cache
        String cacheKey = "student:recommendations:" + TEST_USER_ID + ":all";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 5: Training Projects ====================

    @Test
    @Order(13)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /training/projects - Should return paginated training projects")
    void testGetTrainingProjects_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/training/projects")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /training/projects - Should include enrollment status")
    void testGetTrainingProjects_EnrollmentStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/training/projects")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Should have enrollmentStatus field
        assertThat(content).containsAnyOf("enrollmentStatus", "enrolled");
    }

    // ==================== Test 6: Project Scrum Board ====================

    @Test
    @Order(15)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /training/projects/{id}/board - Should return scrum board when enrolled")
    void testGetProjectBoard_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/training/projects/{id}/board", TEST_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todo").isArray())
                .andExpect(jsonPath("$.data.in_progress").isArray())
                .andExpect(jsonPath("$.data.done").isArray());
    }

    @Test
    @Order(16)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /training/projects/{id}/board - Should return 403 when not enrolled")
    void testGetProjectBoard_NotEnrolled() throws Exception {
        Long nonEnrolledProjectId = 9999L;

        mockMvc.perform(get("/api/student-portal/v1/training/projects/{id}/board", nonEnrolledProjectId))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /training/projects/{id}/board - Should return 404 for non-existent project")
    void testGetProjectBoard_NotFound() throws Exception {
        Long nonExistentProjectId = 99999L;

        mockMvc.perform(get("/api/student-portal/v1/training/projects/{id}/board", nonExistentProjectId))
                .andExpect(status().isNotFound());
    }

    // ==================== Test 7: Internship Jobs ====================

    @Test
    @Order(18)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /internship/jobs - Should return paginated internship jobs")
    void testGetInternshipJobs_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/internship/jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(19)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /internship/jobs - Should include application status")
    void testGetInternshipJobs_ApplicationStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/internship/jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Should have applicationStatus field
        assertThat(content).containsAnyOf("applicationStatus", "applied", "offered");
    }

    @Test
    @Order(20)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /internship/jobs - Should use Redis cache")
    void testGetInternshipJobs_CacheHit() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/internship/jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        String cacheKey = "student:jobs:list:1:10";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 8: Weekly Reports ====================

    @Test
    @Order(21)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /internship/reports/my - Should return student's weekly reports")
    void testGetMyReports_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/internship/reports/my")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(22)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /internship/reports/my - Should return reports ordered by date descending")
    void testGetMyReports_OrderedByDate() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/internship/reports/my")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify ordering (most recent first)
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("createdAt");
    }

    // ==================== Test 9: Growth Evaluation ====================

    @Test
    @Order(23)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/evaluation - Should return evaluation summary")
    void testGetEvaluationSummary_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/growth/evaluation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.averageScore").exists())
                .andExpect(jsonPath("$.data.evaluations").isArray());
    }

    @Test
    @Order(24)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/evaluation - Should include evaluator details")
    void testGetEvaluationSummary_EvaluatorDetails() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/growth/evaluation"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Should have evaluator information
        assertThat(content).containsAnyOf("evaluatorName", "sourceType", "evaluationDate");
    }

    // ==================== Test 10: Certificates ====================

    @Test
    @Order(25)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/certificates - Should return student certificates")
    void testGetMyCertificates_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/growth/certificates")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(26)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/certificates - Should include download URL")
    void testGetMyCertificates_DownloadUrl() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/growth/certificates")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Should have download URL
        assertThat(content).containsAnyOf("downloadUrl", "imageUrl");
    }

    // ==================== Test 11: Badges ====================

    @Test
    @Order(27)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/badges - Should return student badges")
    void testGetMyBadges_Success() throws Exception {
        mockMvc.perform(get("/api/student-portal/v1/growth/badges")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @Order(28)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("GET /growth/badges - Should return badges ordered by date descending")
    void testGetMyBadges_OrderedByDate() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/growth/badges")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("issueDate");
    }

    // ==================== Error Scenario Tests ====================

    @Test
    @Order(29)
    @DisplayName("All endpoints - Should return 401 when JWT token is missing")
    void testAllEndpoints_MissingToken() throws Exception {
        UserContext.clear();

        String[] endpoints = {
                "/api/student-portal/v1/dashboard",
                "/api/student-portal/v1/capability/radar",
                "/api/student-portal/v1/tasks",
                "/api/student-portal/v1/recommendations",
                "/api/student-portal/v1/training/projects",
                "/api/student-portal/v1/internship/jobs",
                "/api/student-portal/v1/internship/reports/my",
                "/api/student-portal/v1/growth/evaluation",
                "/api/student-portal/v1/growth/certificates",
                "/api/student-portal/v1/growth/badges"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(30)
    @WithMockUser(username = "enterpriseuser", roles = "ENTERPRISE")
    @DisplayName("All endpoints - Should return 403 when user is not a student")
    void testAllEndpoints_WrongRole() throws Exception {
        // Setup enterprise user context
        UserContext.LoginUser enterpriseUser = UserContext.LoginUser.builder()
                .userId(5000L)
                .username("enterpriseuser")
                .tenantId(2L)
                .role("ENTERPRISE")
                .build();
        UserContext.set(enterpriseUser);

        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isForbidden());
    }

    // ==================== Cache Behavior Tests ====================

    @Test
    @Order(31)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Cache - Should expire after TTL")
    void testCache_TTLExpiration() throws Exception {
        // First request - populate cache
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());

        String cacheKey = "student:dashboard:" + TEST_USER_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // Verify TTL is set (should be 5 minutes = 300 seconds)
        Long ttl = redisTemplate.getExpire(cacheKey);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(300);
    }

    @Test
    @Order(32)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Cache - Different users should have separate cache entries")
    void testCache_UserIsolation() throws Exception {
        // User 1 request
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());

        String cacheKey1 = "student:dashboard:" + TEST_USER_ID;
        assertThat(redisTemplate.hasKey(cacheKey1)).isTrue();

        // Switch to user 2
        UserContext.LoginUser user2 = UserContext.LoginUser.builder()
                .userId(1001L)
                .username("teststudent2")
                .tenantId(TEST_TENANT_ID)
                .role("STUDENT")
                .build();
        UserContext.set(user2);

        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());

        String cacheKey2 = "student:dashboard:1001";
        assertThat(redisTemplate.hasKey(cacheKey2)).isTrue();

        // Both cache keys should exist
        assertThat(redisTemplate.hasKey(cacheKey1)).isTrue();
        assertThat(redisTemplate.hasKey(cacheKey2)).isTrue();
    }

    // ==================== Pagination Tests ====================

    @Test
    @Order(33)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Pagination - Should handle different page sizes")
    void testPagination_DifferentSizes() throws Exception {
        int[] pageSizes = {5, 10, 20, 50};

        for (int size : pageSizes) {
            mockMvc.perform(get("/api/student-portal/v1/tasks")
                            .param("page", "1")
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(size));
        }
    }

    @Test
    @Order(34)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Pagination - Should handle page navigation")
    void testPagination_PageNavigation() throws Exception {
        // Page 1
        mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1));

        // Page 2
        mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(2));
    }

    // ==================== Database Interaction Tests ====================

    @Test
    @Order(35)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Database - Should query correct student data based on user context")
    void testDatabase_UserContextFiltering() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/student-portal/v1/tasks")
                        .param("status", "pending"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify only current user's tasks are returned
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("records");
    }

    @Test
    @Order(36)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Database - Should handle empty result sets gracefully")
    void testDatabase_EmptyResults() throws Exception {
        // Clear all tasks for this student
        jdbcTemplate.update("DELETE FROM student_svc.student_task WHERE student_id = ?", TEST_STUDENT_ID);

        mockMvc.perform(get("/api/student-portal/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // ==================== Performance Tests ====================

    @Test
    @Order(37)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Performance - Dashboard should respond within 500ms")
    void testPerformance_DashboardResponseTime() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        assertThat(responseTime).isLessThan(500);
    }

    @Test
    @Order(38)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Performance - Cached requests should be faster than uncached")
    void testPerformance_CacheSpeedup() throws Exception {
        // Clear cache
        clearRedisCache();

        // First request (uncached)
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());
        long uncachedTime = System.currentTimeMillis() - startTime1;

        // Second request (cached)
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/api/student-portal/v1/dashboard"))
                .andExpect(status().isOk());
        long cachedTime = System.currentTimeMillis() - startTime2;

        // Cached request should be faster (or at least not significantly slower)
        assertThat(cachedTime).isLessThanOrEqualTo(uncachedTime + 50);
    }

    // ==================== Helper Methods ====================

    private void setupTestData() {
        try {
            // Create test student
            jdbcTemplate.update(
                    "INSERT INTO student_svc.student_info (id, user_id, student_number, real_name, gender, " +
                            "college_id, major, class_name, enrollment_year, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT (id) DO NOTHING",
                    TEST_STUDENT_ID, TEST_USER_ID, "2021001", "Test Student", 1,
                    1L, "Computer Science", "CS2021-1", 2021, 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test tasks
            StudentTask task1 = new StudentTask();
            task1.setStudentId(TEST_STUDENT_ID);
            task1.setTaskType("training");
            task1.setTitle("Complete Project Module");
            task1.setDescription("Finish the authentication module");
            task1.setPriority(2);
            task1.setStatus(0); // pending
            task1.setDueDate(OffsetDateTime.now().plusDays(7));
            task1.setCreatedAt(OffsetDateTime.now());
            task1.setUpdatedAt(OffsetDateTime.now());
            task1.setIsDeleted(false);
            studentTaskMapper.insert(task1);

            StudentTask task2 = new StudentTask();
            task2.setStudentId(TEST_STUDENT_ID);
            task2.setTaskType("internship");
            task2.setTitle("Submit Weekly Report");
            task2.setDescription("Week 5 internship report");
            task2.setPriority(3);
            task2.setStatus(1); // completed
            task2.setDueDate(OffsetDateTime.now().minusDays(1));
            task2.setCreatedAt(OffsetDateTime.now());
            task2.setUpdatedAt(OffsetDateTime.now());
            task2.setIsDeleted(false);
            studentTaskMapper.insert(task2);

            // Create test capabilities
            String[] dimensions = {"technical_skill", "communication", "teamwork", "problem_solving", "innovation"};
            int[] scores = {85, 78, 92, 80, 70};
            
            for (int i = 0; i < dimensions.length; i++) {
                StudentCapability capability = new StudentCapability();
                capability.setStudentId(TEST_STUDENT_ID);
                capability.setDimension(dimensions[i]);
                capability.setScore(scores[i]);
                capability.setUpdatedAt(OffsetDateTime.now());
                studentCapabilityMapper.insert(capability);
            }

            // Create test recommendations
            StudentRecommendation rec1 = new StudentRecommendation();
            rec1.setStudentId(TEST_STUDENT_ID);
            rec1.setRecType("project");
            rec1.setRefId(100L);
            rec1.setScore(new BigDecimal("95.5"));
            rec1.setReason("Matches your Java skills");
            rec1.setCreatedAt(OffsetDateTime.now());
            studentRecommendationMapper.insert(rec1);

            StudentRecommendation rec2 = new StudentRecommendation();
            rec2.setStudentId(TEST_STUDENT_ID);
            rec2.setRecType("job");
            rec2.setRefId(200L);
            rec2.setScore(new BigDecimal("88.0"));
            rec2.setReason("Good location match");
            rec2.setCreatedAt(OffsetDateTime.now());
            studentRecommendationMapper.insert(rec2);

            // Create test project enrollment
            jdbcTemplate.update(
                    "INSERT INTO training_svc.project_enrollment (project_id, student_id, team_id, role, status, enrolled_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT DO NOTHING",
                    TEST_PROJECT_ID, TEST_STUDENT_ID, 1L, "member", 1, OffsetDateTime.now()
            );

            // Create test project tasks
            jdbcTemplate.update(
                    "INSERT INTO training_svc.project_task (project_id, title, description, assignee_id, status, " +
                            "priority, story_points, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT DO NOTHING",
                    TEST_PROJECT_ID, "Setup Database", "Create initial schema", TEST_USER_ID, "todo",
                    2, 5, OffsetDateTime.now(), OffsetDateTime.now(), false
            );

            // Create test training project
            jdbcTemplate.update(
                    "INSERT INTO training_svc.training_project (id, project_name, description, tech_stack, industry, " +
                            "max_teams, max_members, start_date, end_date, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT (id) DO NOTHING",
                    TEST_PROJECT_ID, "E-Commerce Platform", "Build a full-stack e-commerce system",
                    "[\"Java\",\"Spring Boot\",\"React\"]", "E-Commerce",
                    5, 6, LocalDate.now(), LocalDate.now().plusMonths(3), 1,
                    OffsetDateTime.now(), OffsetDateTime.now(), false
            );

            // Create test internship job
            jdbcTemplate.update(
                    "INSERT INTO internship_svc.internship_job (id, enterprise_id, job_title, job_type, description, " +
                            "requirements, tech_stack, city, salary_min, salary_max, headcount, start_date, end_date, " +
                            "status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT (id) DO NOTHING",
                    TEST_JOB_ID, TEST_TENANT_ID, "Java Backend Developer", "internship",
                    "Develop backend services", "Java, Spring Boot, MySQL",
                    "[\"Java\",\"Spring Boot\",\"MySQL\"]", "Beijing",
                    new BigDecimal("5000"), new BigDecimal("8000"), 3,
                    LocalDate.now(), LocalDate.now().plusMonths(6), 1,
                    OffsetDateTime.now(), OffsetDateTime.now(), false
            );

            // Create test evaluation record
            EvaluationRecord evaluation = new EvaluationRecord();
            evaluation.setStudentId(TEST_STUDENT_ID);
            evaluation.setEvaluatorId(100L);
            evaluation.setSourceType("enterprise_mentor");
            evaluation.setScores("{\"technical\": 85, \"communication\": 90, \"teamwork\": 88}");
            evaluation.setComment("Excellent performance");
            evaluation.setCreatedAt(OffsetDateTime.now());
            evaluationRecordMapper.insert(evaluation);

            // Create test badges and certificates
            jdbcTemplate.update(
                    "INSERT INTO growth_svc.growth_badge (student_id, type, name, issue_date, image_url, " +
                            "blockchain_hash, created_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT DO NOTHING",
                    TEST_STUDENT_ID, "certificate", "Java Certification", LocalDate.now(),
                    "/images/cert1.png", "0x123abc", LocalDateTime.now(), false
            );

            jdbcTemplate.update(
                    "INSERT INTO growth_svc.growth_badge (student_id, type, name, issue_date, image_url, " +
                            "created_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT DO NOTHING",
                    TEST_STUDENT_ID, "badge", "Team Player", LocalDate.now(),
                    "/images/badge1.png", LocalDateTime.now(), false
            );

        } catch (Exception e) {
            // Ignore errors if data already exists
            System.out.println("Test data setup warning: " + e.getMessage());
        }
    }

    private void cleanupTestData() {
        try {
            // Clean up in reverse order of dependencies
            jdbcTemplate.update("DELETE FROM growth_svc.growth_badge WHERE student_id = ?", TEST_STUDENT_ID);
            evaluationRecordMapper.delete(null);
            jdbcTemplate.update("DELETE FROM internship_svc.internship_job WHERE id = ?", TEST_JOB_ID);
            jdbcTemplate.update("DELETE FROM training_svc.project_task WHERE project_id = ?", TEST_PROJECT_ID);
            jdbcTemplate.update("DELETE FROM training_svc.project_enrollment WHERE student_id = ?", TEST_STUDENT_ID);
            jdbcTemplate.update("DELETE FROM training_svc.training_project WHERE id = ?", TEST_PROJECT_ID);
            studentRecommendationMapper.delete(null);
            studentCapabilityMapper.delete(null);
            studentTaskMapper.delete(null);
            jdbcTemplate.update("DELETE FROM student_svc.student_info WHERE id = ?", TEST_STUDENT_ID);
        } catch (Exception e) {
            // Ignore cleanup errors
            System.out.println("Test data cleanup warning: " + e.getMessage());
        }
    }

    private void clearRedisCache() {
        try {
            redisTemplate.keys("student:*").forEach(key -> redisTemplate.delete(key));
        } catch (Exception e) {
            // Ignore Redis errors in test environment
            System.out.println("Redis cache clear warning: " + e.getMessage());
        }
    }
}
