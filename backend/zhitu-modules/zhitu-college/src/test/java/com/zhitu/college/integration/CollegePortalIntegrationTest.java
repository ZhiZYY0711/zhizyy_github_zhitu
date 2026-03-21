package com.zhitu.college.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.Result;
import com.zhitu.college.dto.*;
import com.zhitu.college.entity.*;
import com.zhitu.college.mapper.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * College Portal Integration Tests
 * 
 * Tests complete request-response flow for all 19 College Portal endpoints covering:
 * - Dashboard statistics and employment trends
 * - Student management and search
 * - Training plan management
 * - Internship oversight and contract auditing
 * - CRM (enterprise relationships, visits, audits)
 * - Warning system and interventions
 * 
 * Validates: Requirements 20-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("College Portal Integration Tests")
class CollegePortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long TEST_USER_ID = 3000L;
    private static final Long TEST_TENANT_ID = 3L;
    private static final Long TEST_STUDENT_ID = 4000L;
    private static final Long TEST_ENTERPRISE_ID = 5L;
    private static final Long TEST_WARNING_ID = 6000L;
    private static final Long TEST_CONTRACT_ID = 7000L;

    @BeforeEach
    void setUp() {
        UserContext.LoginUser user = UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .username("testcollege")
                .tenantId(TEST_TENANT_ID)
                .role("COLLEGE")
                .build();
        UserContext.set(user);

        clearRedisCache();
        setupTestData();
    }

    @AfterEach
    void tearDown() {
        cleanupTestData();
        UserContext.clear();
        clearRedisCache();
    }

    // ==================== Test 1: Dashboard Statistics ====================

    @Test
    @Order(1)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /dashboard/stats - Should return employment statistics")
    void testGetDashboardStats_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalStudentCount").exists())
                .andExpect(jsonPath("$.data.internshipParticipationRate").exists())
                .andExpect(jsonPath("$.data.employmentRate").exists())
                .andExpect(jsonPath("$.data.averageSalary").exists());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /dashboard/stats - Should use Redis cache")
    void testGetDashboardStats_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats")
                        .param("year", "2024"))
                .andExpect(status().isOk());

        String cacheKey = "college:dashboard:stats:" + TEST_TENANT_ID + ":2024";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 2: Employment Trends ====================

    @Test
    @Order(3)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /dashboard/trends - Should return employment trends")
    void testGetEmploymentTrends_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/dashboard/trends")
                        .param("dimension", "month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /dashboard/trends - Should support different dimensions")
    void testGetEmploymentTrends_DifferentDimensions() throws Exception {
        String[] dimensions = {"month", "quarter", "year"};

        for (String dimension : dimensions) {
            mockMvc.perform(get("/api/portal-college/v1/dashboard/trends")
                            .param("dimension", dimension))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    // ==================== Test 3: Student Management ====================

    @Test
    @Order(5)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /college/students - Should return paginated students")
    void testGetStudents_Success() throws Exception {
        mockMvc.perform(get("/api/user/v1/college/students")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /college/students - Should filter by keyword")
    void testGetStudents_FilterByKeyword() throws Exception {
        mockMvc.perform(get("/api/user/v1/college/students")
                        .param("keyword", "Test")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 4: Training Plan Management ====================

    @Test
    @Order(7)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /college/plans - Should return training plans")
    void testGetTrainingPlans_Success() throws Exception {
        mockMvc.perform(get("/api/training/v1/college/plans")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(8)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /college/plans - Should create training plan")
    void testCreateTrainingPlan_Success() throws Exception {
        String planRequest = """
                {
                    "name": "Summer Training 2024",
                    "semester": "2024-1",
                    "startDate": "2024-07-01",
                    "endDate": "2024-08-31",
                    "targetStudents": "CS2021"
                }
                """;

        mockMvc.perform(post("/api/training/v1/college/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /college/mentors/assign - Should assign mentor to plan")
    void testAssignMentor_Success() throws Exception {
        String assignRequest = """
                {
                    "planId": 1000,
                    "teacherId": 2000
                }
                """;

        mockMvc.perform(post("/api/training/v1/college/mentors/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 5: Internship Oversight ====================

    @Test
    @Order(10)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /college/students - Should return internship students")
    void testGetInternshipStudents_Success() throws Exception {
        mockMvc.perform(get("/api/internship/v1/college/students")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /college/contracts/pending - Should return pending contracts")
    void testGetPendingContracts_Success() throws Exception {
        mockMvc.perform(get("/api/internship/v1/college/contracts/pending")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /college/contracts/{id}/audit - Should audit contract")
    void testAuditContract_Success() throws Exception {
        String auditRequest = """
                {
                    "action": "pass",
                    "comment": "Contract approved"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/contracts/{id}/audit", TEST_CONTRACT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(13)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /college/inspections - Should create inspection record")
    void testCreateInspection_Success() throws Exception {
        String inspectionRequest = """
                {
                    "internshipId": 8000,
                    "inspectionDate": "2024-06-15",
                    "location": "Enterprise Office",
                    "findings": "Good working environment",
                    "issues": "None",
                    "recommendations": "Continue monitoring"
                }
                """;

        mockMvc.perform(post("/api/internship/v1/college/inspections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inspectionRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 6: CRM - Enterprise Management ====================

    @Test
    @Order(14)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /crm/enterprises - Should return enterprises")
    void testGetEnterprises_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/crm/enterprises")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(15)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /crm/enterprises - Should filter by level")
    void testGetEnterprises_FilterByLevel() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/crm/enterprises")
                        .param("level", "2")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(16)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /crm/audits - Should return enterprise audits")
    void testGetEnterpriseAudits_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/crm/audits")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(17)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /crm/audits/{id} - Should audit enterprise")
    void testAuditEnterprise_Success() throws Exception {
        String auditRequest = """
                {
                    "action": "pass",
                    "comment": "Enterprise verified"
                }
                """;

        mockMvc.perform(post("/api/portal-college/v1/crm/audits/{id}", 9000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(18)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("PUT /crm/enterprises/{id}/level - Should update enterprise level")
    void testUpdateEnterpriseLevel_Success() throws Exception {
        String levelRequest = """
                {
                    "level": 3,
                    "reason": "Excellent cooperation"
                }
                """;

        mockMvc.perform(put("/api/portal-college/v1/crm/enterprises/{id}/level", TEST_ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(levelRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 7: CRM - Visit Records ====================

    @Test
    @Order(19)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /crm/visits - Should return visit records")
    void testGetVisits_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/crm/visits")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(20)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /crm/visits - Should filter by enterpriseId")
    void testGetVisits_FilterByEnterprise() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/crm/visits")
                        .param("enterpriseId", TEST_ENTERPRISE_ID.toString())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(21)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /crm/visits - Should create visit record")
    void testCreateVisit_Success() throws Exception {
        String visitRequest = """
                {
                    "enterpriseId": %d,
                    "visitDate": "2024-06-20",
                    "visitor": "Prof. Wang",
                    "purpose": "Discuss cooperation",
                    "outcome": "Positive discussion"
                }
                """.formatted(TEST_ENTERPRISE_ID);

        mockMvc.perform(post("/api/portal-college/v1/crm/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(visitRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(22)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /crm/visits - Should validate required fields")
    void testCreateVisit_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "enterpriseId": %d
                }
                """.formatted(TEST_ENTERPRISE_ID);

        mockMvc.perform(post("/api/portal-college/v1/crm/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test 8: Warning System ====================

    @Test
    @Order(23)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /warnings - Should return warnings")
    void testGetWarnings_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/warnings")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(24)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /warnings - Should filter by level")
    void testGetWarnings_FilterByLevel() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/warnings")
                        .param("level", "high")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(25)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /warnings - Should filter by type")
    void testGetWarnings_FilterByType() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/warnings")
                        .param("type", "attendance_low")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(26)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /warnings/stats - Should return warning statistics")
    void testGetWarningStats_Success() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/warnings/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(27)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("GET /warnings/stats - Should use Redis cache")
    void testGetWarningStats_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/warnings/stats"))
                .andExpect(status().isOk());

        String cacheKey = "college:warnings:stats:" + TEST_TENANT_ID;
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    @Test
    @Order(28)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("POST /warnings/{id}/intervene - Should create intervention")
    void testInterveneWarning_Success() throws Exception {
        String interventionRequest = """
                {
                    "interventionType": "counseling",
                    "actionTaken": "Met with student",
                    "expectedOutcome": "Improved attendance"
                }
                """;

        mockMvc.perform(post("/api/portal-college/v1/warnings/{id}/intervene", TEST_WARNING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(interventionRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 9: Error Scenarios ====================

    @Test
    @Order(29)
    @DisplayName("All endpoints - Should return 401 when JWT token is missing")
    void testAllEndpoints_MissingToken() throws Exception {
        UserContext.clear();

        String[] endpoints = {
                "/api/portal-college/v1/dashboard/stats?year=2024",
                "/api/portal-college/v1/dashboard/trends?dimension=month",
                "/api/user/v1/college/students",
                "/api/training/v1/college/plans",
                "/api/internship/v1/college/students",
                "/api/portal-college/v1/crm/enterprises",
                "/api/portal-college/v1/warnings"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(30)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("All endpoints - Should return 403 when user is not college")
    void testAllEndpoints_WrongRole() throws Exception {
        UserContext.LoginUser studentUser = UserContext.LoginUser.builder()
                .userId(5000L)
                .username("teststudent")
                .tenantId(1L)
                .role("STUDENT")
                .build();
        UserContext.set(studentUser);

        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats?year=2024"))
                .andExpect(status().isForbidden());
    }

    // ==================== Test 10: Cache Behavior ====================

    @Test
    @Order(31)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("Cache - Should expire after TTL")
    void testCache_TTLExpiration() throws Exception {
        mockMvc.perform(get("/api/portal-college/v1/dashboard/stats")
                        .param("year", "2024"))
                .andExpect(status().isOk());

        String cacheKey = "college:dashboard:stats:" + TEST_TENANT_ID + ":2024";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        Long ttl = redisTemplate.getExpire(cacheKey);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(3600);
    }

    // ==================== Test 11: Pagination ====================

    @Test
    @Order(32)
    @WithMockUser(username = "testcollege", roles = "COLLEGE")
    @DisplayName("Pagination - Should handle different page sizes")
    void testPagination_DifferentSizes() throws Exception {
        int[] pageSizes = {5, 10, 20, 50};

        for (int size : pageSizes) {
            mockMvc.perform(get("/api/user/v1/college/students")
                            .param("page", "1")
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(size));
        }
    }

    // ==================== Helper Methods ====================

    private void setupTestData() {
        try {
            // Create test college tenant
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status, " +
                            "created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_TENANT_ID, "Test College", "college", 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test user
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_user (id, username, password, real_name, tenant_id, " +
                            "status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_USER_ID, "testcollege", "password", "Test College User", TEST_TENANT_ID,
                    1, LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test student
            jdbcTemplate.update(
                    "INSERT INTO student_svc.student_info (id, user_id, student_number, real_name, gender, " +
                            "college_id, major, class_name, enrollment_year, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_STUDENT_ID, 4001L, "2021003", "Test Student", 1,
                    TEST_TENANT_ID, "Computer Science", "CS2021-1", 2021, 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test enterprise tenant
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status, " +
                            "created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_ENTERPRISE_ID, "Test Enterprise", "enterprise", 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test enterprise relationship
            jdbcTemplate.update(
                    "INSERT INTO college_svc.enterprise_relationship (college_tenant_id, enterprise_tenant_id, " +
                            "cooperation_level, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                    TEST_TENANT_ID, TEST_ENTERPRISE_ID, 2, 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test warning
            jdbcTemplate.update(
                    "INSERT INTO college_svc.student_warning (id, student_id, college_tenant_id, warning_type, " +
                            "warning_level, description, status, created_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_WARNING_ID, TEST_STUDENT_ID, TEST_TENANT_ID, "attendance_low",
                    "medium", "Low attendance rate", 0,
                    LocalDateTime.now(), false
            );

            // Create test contract
            jdbcTemplate.update(
                    "INSERT INTO internship_svc.internship_contract (id, student_id, enterprise_id, " +
                            "position, start_date, end_date, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_CONTRACT_ID, TEST_STUDENT_ID, TEST_ENTERPRISE_ID,
                    "Software Engineer Intern", LocalDate.now(), LocalDate.now().plusMonths(6), 0,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

        } catch (Exception e) {
            System.out.println("Test data setup warning: " + e.getMessage());
        }
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.update("DELETE FROM internship_svc.internship_contract WHERE id = ?", TEST_CONTRACT_ID);
            jdbcTemplate.update("DELETE FROM college_svc.student_warning WHERE id = ?", TEST_WARNING_ID);
            jdbcTemplate.update("DELETE FROM college_svc.enterprise_relationship WHERE college_tenant_id = ?", TEST_TENANT_ID);
            jdbcTemplate.update("DELETE FROM student_svc.student_info WHERE id = ?", TEST_STUDENT_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_tenant WHERE id = ?", TEST_ENTERPRISE_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_user WHERE id = ?", TEST_USER_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_tenant WHERE id = ?", TEST_TENANT_ID);
        } catch (Exception e) {
            System.out.println("Test data cleanup warning: " + e.getMessage());
        }
    }

    private void clearRedisCache() {
        try {
            redisTemplate.keys("college:*").forEach(key -> redisTemplate.delete(key));
        } catch (Exception e) {
            System.out.println("Redis cache clear warning: " + e.getMessage());
        }
    }
}
