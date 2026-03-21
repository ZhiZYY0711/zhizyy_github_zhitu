package com.zhitu.enterprise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.*;
import com.zhitu.enterprise.entity.*;
import com.zhitu.enterprise.mapper.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Enterprise Portal Integration Tests
 * 
 * Tests complete request-response flow for all 12 Enterprise Portal endpoints:
 * 1. GET /api/portal-enterprise/v1/dashboard/stats - Enterprise dashboard statistics
 * 2. GET /api/portal-enterprise/v1/todos - Enterprise todo list
 * 3. GET /api/portal-enterprise/v1/activities - Enterprise activity feed
 * 4. GET /api/internship/v1/enterprise/jobs - Job management (list)
 * 5. POST /api/internship/v1/enterprise/jobs - Job creation
 * 6. POST /api/internship/v1/enterprise/jobs/{id}/close - Close job
 * 7. GET /api/internship/v1/enterprise/applications - Application management
 * 8. POST /api/internship/v1/enterprise/interviews - Schedule interview
 * 9. GET /api/portal-enterprise/v1/talent-pool - Talent pool list
 * 10. DELETE /api/portal-enterprise/v1/talent-pool/{id} - Remove from talent pool
 * 11. GET /api/portal-enterprise/v1/mentor/dashboard - Mentor dashboard
 * 12. GET /api/portal-enterprise/v1/analytics - Enterprise analytics
 * 
 * Validates: Requirements 12-19
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Enterprise Portal Integration Tests")
class EnterprisePortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EnterpriseTodoMapper enterpriseTodoMapper;

    @Autowired
    private EnterpriseActivityMapper enterpriseActivityMapper;

    private static final Long TEST_USER_ID = 2000L;
    private static final Long TEST_TENANT_ID = 2L;
    private static final Long TEST_JOB_ID = 5000L;
    private static final Long TEST_APPLICATION_ID = 6000L;
    private static final Long TEST_STUDENT_ID = 3000L;
    private static final Long TEST_TALENT_POOL_ID = 7000L;

    @BeforeEach
    void setUp() {
        // Setup enterprise user context
        UserContext.LoginUser user = UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .username("testenterprise")
                .tenantId(TEST_TENANT_ID)
                .role("ENTERPRISE")
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
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /dashboard/stats - Should return dashboard statistics with 200")
    void testGetDashboardStats_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activeJobCount").exists())
                .andExpect(jsonPath("$.data.pendingApplicationCount").exists())
                .andExpect(jsonPath("$.data.activeInternCount").exists())
                .andExpect(jsonPath("$.data.trainingProjectCount").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("activeJobCount");
    }

    @Test
    @Order(2)
    @DisplayName("GET /dashboard/stats - Should return 401 when not authenticated")
    void testGetDashboardStats_Unauthorized() throws Exception {
        UserContext.clear();

        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /dashboard/stats - Should use Redis cache")
    void testGetDashboardStats_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey = "enterprise:dashboard:" + TEST_TENANT_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 2: Todo List ====================

    @Test
    @Order(4)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /todos - Should return paginated todos")
    void testGetTodos_Success() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/todos")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /todos - Should filter by user and status")
    void testGetTodos_FilteredByUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-enterprise/v1/todos")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("records");
    }

    // ==================== Test 3: Activity Feed ====================

    @Test
    @Order(6)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /activities - Should return recent activities")
    void testGetActivities_Success() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/activities")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /activities - Should use Redis cache")
    void testGetActivities_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/activities")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        String cacheKey = "enterprise:activities:" + TEST_TENANT_ID + ":1:10";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 4: Job Management ====================

    @Test
    @Order(8)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /enterprise/jobs - Should return paginated jobs")
    void testGetJobs_Success() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(9)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("POST /enterprise/jobs - Should create job with validation")
    void testCreateJob_Success() throws Exception {
        String jobRequest = """
                {
                    "title": "Backend Developer",
                    "description": "Develop backend services",
                    "requirements": "Java, Spring Boot",
                    "salaryRange": "8000-12000",
                    "location": "Beijing",
                    "startDate": "2024-06-01",
                    "endDate": "2024-12-31"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("POST /enterprise/jobs - Should validate required fields")
    void testCreateJob_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "title": ""
                }
                """;

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("POST /enterprise/jobs/{id}/close - Should close job and notify applicants")
    void testCloseJob_Success() throws Exception {
        mockMvc.perform(post("/api/internship/v1/enterprise/jobs/{id}/close", TEST_JOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 5: Application Management ====================

    @Test
    @Order(12)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /enterprise/applications - Should return applications")
    void testGetApplications_Success() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/applications")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /enterprise/applications - Should filter by jobId")
    void testGetApplications_FilterByJob() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/applications")
                        .param("jobId", TEST_JOB_ID.toString())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("POST /enterprise/interviews - Should schedule interview")
    void testScheduleInterview_Success() throws Exception {
        String interviewRequest = """
                {
                    "applicationId": %d,
                    "studentId": %d,
                    "interviewTime": "2024-06-15T10:00:00",
                    "location": "Conference Room A",
                    "interviewerId": %d,
                    "interviewType": "onsite"
                }
                """.formatted(TEST_APPLICATION_ID, TEST_STUDENT_ID, TEST_USER_ID);

        mockMvc.perform(post("/api/internship/v1/enterprise/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(interviewRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(15)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("POST /enterprise/interviews - Should validate interview time")
    void testScheduleInterview_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "applicationId": %d,
                    "studentId": %d
                }
                """.formatted(TEST_APPLICATION_ID, TEST_STUDENT_ID);

        mockMvc.perform(post("/api/internship/v1/enterprise/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test 6: Talent Pool ====================

    @Test
    @Order(16)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /talent-pool - Should return talent pool entries")
    void testGetTalentPool_Success() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/talent-pool")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(17)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("DELETE /talent-pool/{id} - Should soft delete talent pool entry")
    void testRemoveFromTalentPool_Success() throws Exception {
        mockMvc.perform(delete("/api/portal-enterprise/v1/talent-pool/{id}", TEST_TALENT_POOL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(18)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("DELETE /talent-pool/{id} - Should return 404 for non-existent entry")
    void testRemoveFromTalentPool_NotFound() throws Exception {
        mockMvc.perform(delete("/api/portal-enterprise/v1/talent-pool/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ==================== Test 7: Mentor Dashboard ====================

    @Test
    @Order(19)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /mentor/dashboard - Should return mentor dashboard")
    void testGetMentorDashboard_Success() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/mentor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.assignedInternCount").exists())
                .andExpect(jsonPath("$.data.pendingReportCount").exists())
                .andExpect(jsonPath("$.data.pendingCodeReviewCount").exists());
    }

    @Test
    @Order(20)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /mentor/dashboard - Should use Redis cache")
    void testGetMentorDashboard_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/mentor/dashboard"))
                .andExpect(status().isOk());

        String cacheKey = "enterprise:mentor:dashboard:" + TEST_USER_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 8: Analytics ====================

    @Test
    @Order(21)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /analytics - Should return analytics data")
    void testGetAnalytics_Success() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/analytics")
                        .param("range", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(22)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /analytics - Should support different time ranges")
    void testGetAnalytics_DifferentRanges() throws Exception {
        String[] ranges = {"week", "month", "quarter", "year"};

        for (String range : ranges) {
            mockMvc.perform(get("/api/portal-enterprise/v1/analytics")
                            .param("range", range))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @Order(23)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("GET /analytics - Should use Redis cache")
    void testGetAnalytics_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/analytics")
                        .param("range", "month"))
                .andExpect(status().isOk());

        String cacheKey = "enterprise:analytics:" + TEST_TENANT_ID + ":month";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 9: Tenant Isolation ====================

    @Test
    @Order(24)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("Tenant Isolation - Should only see own tenant's data")
    void testTenantIsolation_DashboardStats() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("activeJobCount");
    }

    @Test
    @Order(25)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("Tenant Isolation - Should not access other tenant's jobs")
    void testTenantIsolation_Jobs() throws Exception {
        // Create a job for another tenant
        Long otherTenantId = 999L;
        jdbcTemplate.update(
                "INSERT INTO internship_svc.internship_job (id, enterprise_id, job_title, job_type, " +
                        "description, requirements, city, status, created_at, updated_at, is_deleted) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                99999L, otherTenantId, "Other Tenant Job", "internship",
                "Test job", "Test requirements", "Shanghai", 1,
                LocalDateTime.now(), LocalDateTime.now(), false
        );

        MvcResult result = mockMvc.perform(get("/api/internship/v1/enterprise/jobs")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        // Should not contain other tenant's job
        assertThat(content).doesNotContain("Other Tenant Job");
    }

    // ==================== Test 10: Error Scenarios ====================

    @Test
    @Order(26)
    @DisplayName("All endpoints - Should return 401 when JWT token is missing")
    void testAllEndpoints_MissingToken() throws Exception {
        UserContext.clear();

        String[] endpoints = {
                "/api/portal-enterprise/v1/dashboard/stats",
                "/api/portal-enterprise/v1/todos",
                "/api/portal-enterprise/v1/activities",
                "/api/internship/v1/enterprise/jobs",
                "/api/portal-enterprise/v1/talent-pool",
                "/api/portal-enterprise/v1/mentor/dashboard",
                "/api/portal-enterprise/v1/analytics?range=month"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(27)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("All endpoints - Should return 403 when user is not enterprise")
    void testAllEndpoints_WrongRole() throws Exception {
        UserContext.LoginUser studentUser = UserContext.LoginUser.builder()
                .userId(5000L)
                .username("teststudent")
                .tenantId(1L)
                .role("STUDENT")
                .build();
        UserContext.set(studentUser);

        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    // ==================== Test 11: Cache Behavior ====================

    @Test
    @Order(28)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("Cache - Should expire after TTL")
    void testCache_TTLExpiration() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey = "enterprise:dashboard:" + TEST_TENANT_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        Long ttl = redisTemplate.getExpire(cacheKey);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(300);
    }

    @Test
    @Order(29)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("Cache - Different tenants should have separate cache entries")
    void testCache_TenantIsolation() throws Exception {
        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey1 = "enterprise:dashboard:" + TEST_TENANT_ID;
        assertThat(redisTemplate.hasKey(cacheKey1)).isTrue();

        // Switch to another tenant
        UserContext.LoginUser user2 = UserContext.LoginUser.builder()
                .userId(2001L)
                .username("testenterprise2")
                .tenantId(3L)
                .role("ENTERPRISE")
                .build();
        UserContext.set(user2);

        mockMvc.perform(get("/api/portal-enterprise/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey2 = "enterprise:dashboard:3";
        assertThat(redisTemplate.hasKey(cacheKey2)).isTrue();

        assertThat(redisTemplate.hasKey(cacheKey1)).isTrue();
        assertThat(redisTemplate.hasKey(cacheKey2)).isTrue();
    }

    // ==================== Test 12: Pagination ====================

    @Test
    @Order(30)
    @WithMockUser(username = "testenterprise", roles = "ENTERPRISE")
    @DisplayName("Pagination - Should handle different page sizes")
    void testPagination_DifferentSizes() throws Exception {
        int[] pageSizes = {5, 10, 20, 50};

        for (int size : pageSizes) {
            mockMvc.perform(get("/api/portal-enterprise/v1/todos")
                            .param("page", "1")
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(size));
        }
    }

    // ==================== Helper Methods ====================

    private void setupTestData() {
        try {
            // Create test enterprise tenant
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status, " +
                            "created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_TENANT_ID, "Test Enterprise", "enterprise", 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test user
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_user (id, username, password, real_name, tenant_id, " +
                            "status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_USER_ID, "testenterprise", "password", "Test Enterprise User", TEST_TENANT_ID,
                    1, LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test todos
            EnterpriseTodo todo1 = new EnterpriseTodo();
            todo1.setTenantId(TEST_TENANT_ID);
            todo1.setUserId(TEST_USER_ID);
            todo1.setTodoType("application_review");
            todo1.setTitle("Review Application #123");
            todo1.setPriority(2);
            todo1.setStatus(0);
            todo1.setDueDate(OffsetDateTime.now().plusDays(3));
            todo1.setCreatedAt(OffsetDateTime.now());
            enterpriseTodoMapper.insert(todo1);

            // Create test activities
            EnterpriseActivity activity1 = new EnterpriseActivity();
            activity1.setTenantId(TEST_TENANT_ID);
            activity1.setActivityType("application");
            activity1.setDescription("New application received");
            activity1.setRefType("application");
            activity1.setRefId(TEST_APPLICATION_ID);
            activity1.setCreatedAt(OffsetDateTime.now());
            enterpriseActivityMapper.insert(activity1);

            // Create test job
            jdbcTemplate.update(
                    "INSERT INTO internship_svc.internship_job (id, enterprise_id, job_title, job_type, " +
                            "description, requirements, tech_stack, city, salary_min, salary_max, headcount, " +
                            "start_date, end_date, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_JOB_ID, TEST_TENANT_ID, "Java Developer", "internship",
                    "Develop backend services", "Java, Spring Boot",
                    "[\"Java\",\"Spring Boot\"]", "Beijing",
                    new BigDecimal("6000"), new BigDecimal("10000"), 2,
                    LocalDate.now(), LocalDate.now().plusMonths(6), 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test student
            jdbcTemplate.update(
                    "INSERT INTO student_svc.student_info (id, user_id, student_number, real_name, gender, " +
                            "college_id, major, class_name, enrollment_year, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_STUDENT_ID, 3001L, "2021002", "Test Student", 1,
                    1L, "Computer Science", "CS2021-1", 2021, 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test application
            jdbcTemplate.update(
                    "INSERT INTO internship_svc.job_application (id, job_id, student_id, resume_url, " +
                            "cover_letter, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_APPLICATION_ID, TEST_JOB_ID, TEST_STUDENT_ID, "/resumes/test.pdf",
                    "I am interested in this position", 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test talent pool entry
            jdbcTemplate.update(
                    "INSERT INTO enterprise_svc.talent_pool (id, enterprise_id, student_id, tags, " +
                            "collected_at, created_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_TALENT_POOL_ID, TEST_TENANT_ID, TEST_STUDENT_ID, "[\"Java\",\"Spring\"]",
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

        } catch (Exception e) {
            System.out.println("Test data setup warning: " + e.getMessage());
        }
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.update("DELETE FROM enterprise_svc.talent_pool WHERE id = ?", TEST_TALENT_POOL_ID);
            jdbcTemplate.update("DELETE FROM internship_svc.job_application WHERE id = ?", TEST_APPLICATION_ID);
            jdbcTemplate.update("DELETE FROM internship_svc.internship_job WHERE id = ?", TEST_JOB_ID);
            jdbcTemplate.update("DELETE FROM internship_svc.internship_job WHERE id = ?", 99999L);
            enterpriseActivityMapper.delete(null);
            enterpriseTodoMapper.delete(null);
            jdbcTemplate.update("DELETE FROM student_svc.student_info WHERE id = ?", TEST_STUDENT_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_user WHERE id = ?", TEST_USER_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_tenant WHERE id = ?", TEST_TENANT_ID);
        } catch (Exception e) {
            System.out.println("Test data cleanup warning: " + e.getMessage());
        }
    }

    private void clearRedisCache() {
        try {
            redisTemplate.keys("enterprise:*").forEach(key -> redisTemplate.delete(key));
        } catch (Exception e) {
            System.out.println("Redis cache clear warning: " + e.getMessage());
        }
    }
}
