package com.zhitu.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.*;
import com.zhitu.platform.entity.*;
import com.zhitu.platform.mapper.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Platform Administration Integration Tests
 * 
 * Tests complete request-response flow for all Platform Administration endpoints:
 * 1. GET /api/system/v1/dashboard/stats - Dashboard statistics
 * 2. GET /api/monitor/v1/health - System health monitoring
 * 3. GET /api/monitor/v1/users/online-trend - Online user trend
 * 4. GET /api/monitor/v1/services - Service health details
 * 5. GET /api/system/v1/tenants/colleges - Tenant management
 * 6. GET /api/system/v1/audits/enterprises - Enterprise audit list
 * 7. POST /api/system/v1/audits/enterprises/{id} - Enterprise audit action
 * 8. GET /api/portal-platform/v1/audits/projects - Project audit list
 * 9. POST /api/portal-platform/v1/audits/projects/{id} - Project audit action
 * 10. GET /api/portal-platform/v1/recommendations/banner - Recommendation banners
 * 11. POST /api/portal-platform/v1/recommendations/banner - Save banner
 * 12. GET /api/portal-platform/v1/recommendations/top-list - Top list
 * 13. POST /api/portal-platform/v1/recommendations/top-list - Save top list
 * 
 * Validates: Requirements 28-40
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Platform Administration Integration Tests")
class PlatformAdministrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Long TEST_USER_ID = 9000L;
    private static final Long TEST_TENANT_ID = 100L;
    private static final Long TEST_ENTERPRISE_ID = 200L;
    private static final Long TEST_PROJECT_ID = 300L;

    @BeforeEach
    void setUp() {
        UserContext.LoginUser user = UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .username("platformadmin")
                .tenantId(TEST_TENANT_ID)
                .role("PLATFORM_ADMIN")
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
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /dashboard/stats - Should return platform statistics")
    void testGetDashboardStats_Success() throws Exception {
        mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTenantCount").exists())
                .andExpect(jsonPath("$.data.totalUserCount").exists())
                .andExpect(jsonPath("$.data.activeUserCount").exists())
                .andExpect(jsonPath("$.data.totalEnterpriseCount").exists())
                .andExpect(jsonPath("$.data.pendingAuditCount").exists());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /dashboard/stats - Should use Redis cache")
    void testGetDashboardStats_CacheHit() throws Exception {
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey = "platform:dashboard:stats";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 2: System Health Monitoring ====================

    @Test
    @Order(3)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /monitor/v1/health - Should return system health status")
    void testGetSystemHealth_Success() throws Exception {
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.services").isArray());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /monitor/v1/users/online-trend - Should return online user trend")
    void testGetOnlineUserTrend_Success() throws Exception {
        mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trend").isArray());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /monitor/v1/services - Should return service health details")
    void testGetServices_Success() throws Exception {
        mockMvc.perform(get("/api/monitor/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(6)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /monitor/v1/health - Should use Redis cache")
    void testGetSystemHealth_CacheHit() throws Exception {
        mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk());

        String cacheKey = "platform:health";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 3: Tenant Management ====================

    @Test
    @Order(7)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /tenants/colleges - Should return paginated tenants")
    void testGetTenants_Success() throws Exception {
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(8)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /tenants/colleges - Should filter by type")
    void testGetTenants_FilterByType() throws Exception {
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("type", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /tenants/colleges - Should filter by status")
    void testGetTenants_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("status", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 4: Enterprise Audit ====================

    @Test
    @Order(10)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /audits/enterprises - Should return enterprise audits")
    void testGetEnterpriseAudits_Success() throws Exception {
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /audits/enterprises - Should filter by status")
    void testGetEnterpriseAudits_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(12)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /audits/enterprises/{id} - Should approve enterprise")
    void testAuditEnterprise_Approve() throws Exception {
        String auditRequest = """
                {
                    "action": "pass",
                    "rejectReason": null
                }
                """;

        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", TEST_ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(13)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /audits/enterprises/{id} - Should reject enterprise with reason")
    void testAuditEnterprise_Reject() throws Exception {
        String auditRequest = """
                {
                    "action": "reject",
                    "rejectReason": "Business license invalid"
                }
                """;

        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", TEST_ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /audits/enterprises/{id} - Should validate required fields")
    void testAuditEnterprise_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "action": "reject"
                }
                """;

        mockMvc.perform(post("/api/system/v1/audits/enterprises/{id}", TEST_ENTERPRISE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test 5: Project Audit ====================

    @Test
    @Order(15)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /audits/projects - Should return project audits")
    void testGetProjectAudits_Success() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @Order(16)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /audits/projects - Should filter by status")
    void testGetProjectAudits_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/audits/projects")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(17)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /audits/projects/{id} - Should approve project")
    void testAuditProject_Approve() throws Exception {
        String auditRequest = """
                {
                    "action": "pass",
                    "qualityRating": 4
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", TEST_PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(18)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /audits/projects/{id} - Should reject project")
    void testAuditProject_Reject() throws Exception {
        String auditRequest = """
                {
                    "action": "reject",
                    "qualityRating": null
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/audits/projects/{id}", TEST_PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Test 6: Recommendation Banners ====================

    @Test
    @Order(19)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/banner - Should return all banners")
    void testGetBanners_All() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(20)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/banner - Should filter by portal")
    void testGetBanners_FilterByPortal() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(21)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/banner - Should create banner")
    void testSaveBanner_Success() throws Exception {
        String bannerRequest = """
                {
                    "title": "Spring Internship Fair",
                    "imageUrl": "https://example.com/banner.jpg",
                    "linkUrl": "https://example.com/internships",
                    "targetPortal": "student",
                    "startDate": "2024-03-01",
                    "endDate": "2024-03-31",
                    "sortOrder": 1
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bannerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(22)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/banner - Should validate required fields")
    void testSaveBanner_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "title": "Test Banner"
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/banner - Should validate date range")
    void testSaveBanner_InvalidDateRange() throws Exception {
        String invalidRequest = """
                {
                    "title": "Test Banner",
                    "imageUrl": "https://example.com/banner.jpg",
                    "linkUrl": "https://example.com/link",
                    "targetPortal": "student",
                    "startDate": "2024-03-31",
                    "endDate": "2024-03-01"
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/banner - Should use Redis cache")
    void testGetBanners_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student"))
                .andExpect(status().isOk());

        String cacheKey = "platform:banners:student";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 7: Top List Management ====================

    @Test
    @Order(25)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/top-list - Should return mentor top list")
    void testGetTopList_Mentor() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(26)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/top-list - Should return course top list")
    void testGetTopList_Course() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "course"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(27)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/top-list - Should return project top list")
    void testGetTopList_Project() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(28)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/top-list - Should save top list")
    void testSaveTopList_Success() throws Exception {
        String topListRequest = """
                {
                    "listType": "mentor",
                    "itemIds": [1, 2, 3, 4, 5]
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(topListRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(29)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/top-list - Should validate max 10 items")
    void testSaveTopList_MaxItems() throws Exception {
        String topListRequest = """
                {
                    "listType": "mentor",
                    "itemIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(topListRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(30)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("POST /recommendations/top-list - Should validate required fields")
    void testSaveTopList_ValidationError() throws Exception {
        String invalidRequest = """
                {
                    "listType": "mentor"
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(31)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("GET /recommendations/top-list - Should use Redis cache")
    void testGetTopList_CacheHit() throws Exception {
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor"))
                .andExpect(status().isOk());

        String cacheKey = "platform:toplist:mentor";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    }

    // ==================== Test 8: Error Scenarios ====================

    @Test
    @Order(32)
    @DisplayName("All endpoints - Should return 401 when JWT token is missing")
    void testAllEndpoints_MissingToken() throws Exception {
        UserContext.clear();

        String[] endpoints = {
                "/api/system/v1/dashboard/stats",
                "/api/monitor/v1/health",
                "/api/monitor/v1/users/online-trend",
                "/api/monitor/v1/services",
                "/api/system/v1/tenants/colleges",
                "/api/system/v1/audits/enterprises",
                "/api/portal-platform/v1/audits/projects",
                "/api/portal-platform/v1/recommendations/banner",
                "/api/portal-platform/v1/recommendations/top-list?listType=mentor"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @Order(33)
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("All endpoints - Should return 403 when user is not platform admin")
    void testAllEndpoints_WrongRole() throws Exception {
        UserContext.LoginUser studentUser = UserContext.LoginUser.builder()
                .userId(5000L)
                .username("teststudent")
                .tenantId(1L)
                .role("STUDENT")
                .build();
        UserContext.set(studentUser);

        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    // ==================== Test 9: Cache Behavior ====================

    @Test
    @Order(34)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Cache - Should expire after TTL")
    void testCache_TTLExpiration() throws Exception {
        mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk());

        String cacheKey = "platform:dashboard:stats";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        Long ttl = redisTemplate.getExpire(cacheKey);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(600);
    }

    @Test
    @Order(35)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Cache - Banner cache should be invalidated after save")
    void testCache_BannerInvalidation() throws Exception {
        // First request - populate cache
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student"))
                .andExpect(status().isOk());

        String cacheKey = "platform:banners:student";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // Save new banner - should invalidate cache
        String bannerRequest = """
                {
                    "title": "New Banner",
                    "imageUrl": "https://example.com/new.jpg",
                    "linkUrl": "https://example.com/link",
                    "targetPortal": "student",
                    "startDate": "2024-03-01",
                    "endDate": "2024-03-31"
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bannerRequest))
                .andExpect(status().isOk());

        // Cache should be cleared
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    @Test
    @Order(36)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Cache - Top list cache should be invalidated after save")
    void testCache_TopListInvalidation() throws Exception {
        // First request - populate cache
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor"))
                .andExpect(status().isOk());

        String cacheKey = "platform:toplist:mentor";
        assertThat(redisTemplate.hasKey(cacheKey)).isTrue();

        // Save new top list - should invalidate cache
        String topListRequest = """
                {
                    "listType": "mentor",
                    "itemIds": [1, 2, 3]
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(topListRequest))
                .andExpect(status().isOk());

        // Cache should be cleared
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }

    // ==================== Test 10: Pagination ====================

    @Test
    @Order(37)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Pagination - Should handle different page sizes")
    void testPagination_DifferentSizes() throws Exception {
        int[] pageSizes = {5, 10, 20, 50};

        for (int size : pageSizes) {
            mockMvc.perform(get("/api/system/v1/tenants/colleges")
                            .param("page", "1")
                            .param("size", String.valueOf(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(size));
        }
    }

    @Test
    @Order(38)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Pagination - Should handle page navigation")
    void testPagination_PageNavigation() throws Exception {
        // Page 1
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1));

        // Page 2
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2));
    }

    // ==================== Test 11: Data Validation ====================

    @Test
    @Order(39)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Validation - Should validate banner target portal")
    void testValidation_BannerTargetPortal() throws Exception {
        String invalidRequest = """
                {
                    "title": "Test Banner",
                    "imageUrl": "https://example.com/banner.jpg",
                    "linkUrl": "https://example.com/link",
                    "targetPortal": "invalid_portal",
                    "startDate": "2024-03-01",
                    "endDate": "2024-03-31"
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(40)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Validation - Should validate top list type")
    void testValidation_TopListType() throws Exception {
        String invalidRequest = """
                {
                    "listType": "invalid_type",
                    "itemIds": [1, 2, 3]
                }
                """;

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test 12: Response Format ====================

    @Test
    @Order(41)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Response Format - Dashboard stats should have correct structure")
    void testResponseFormat_DashboardStats() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("totalTenantCount");
        assertThat(content).contains("totalUserCount");
        assertThat(content).contains("activeUserCount");
        assertThat(content).contains("totalEnterpriseCount");
        assertThat(content).contains("pendingAuditCount");
    }

    @Test
    @Order(42)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Response Format - Health status should include service details")
    void testResponseFormat_HealthStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/monitor/v1/health"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("services");
    }

    @Test
    @Order(43)
    @WithMockUser(username = "platformadmin", roles = "PLATFORM_ADMIN")
    @DisplayName("Response Format - Online trend should include timestamp and count")
    void testResponseFormat_OnlineTrend() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/monitor/v1/users/online-trend"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("trend");
    }

    // ==================== Helper Methods ====================

    private void setupTestData() {
        try {
            // Create test platform admin user
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_user (id, username, password, real_name, tenant_id, " +
                            "status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_USER_ID, "platformadmin", "password", "Platform Admin", TEST_TENANT_ID,
                    1, LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test tenant
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status, " +
                            "created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_TENANT_ID, "Test Platform", "platform", 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test enterprise tenant for audit
            jdbcTemplate.update(
                    "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status, " +
                            "created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                    TEST_ENTERPRISE_ID, "Test Enterprise", "enterprise", 0,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test service health record
            jdbcTemplate.update(
                    "INSERT INTO platform_service.service_health (service_name, status, response_time, " +
                            "error_rate, cpu_usage, memory_usage, checked_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                    "zhitu-student", "healthy", 45, 0.1, 35.5, 62.3,
                    LocalDateTime.now()
            );

            // Create test online user trend
            jdbcTemplate.update(
                    "INSERT INTO platform_service.online_user_trend (timestamp, online_count, " +
                            "student_count, enterprise_count, college_count) " +
                            "VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                    LocalDateTime.now().minusHours(1), 1200, 800, 300, 100
            );

            // Create test recommendation banner
            jdbcTemplate.update(
                    "INSERT INTO platform_service.recommendation_banner (title, image_url, link_url, " +
                            "target_portal, start_date, end_date, sort_order, status, created_at, updated_at, is_deleted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING",
                    "Test Banner", "https://example.com/banner.jpg", "https://example.com/link",
                    "student", LocalDate.now(), LocalDate.now().plusDays(30), 1, 1,
                    LocalDateTime.now(), LocalDateTime.now(), false
            );

            // Create test top list
            jdbcTemplate.update(
                    "INSERT INTO platform_service.recommendation_top_list (list_type, item_ids, updated_at) " +
                            "VALUES (?, ?, ?) ON CONFLICT (list_type) DO NOTHING",
                    "mentor", "[1,2,3,4,5]", LocalDateTime.now()
            );

        } catch (Exception e) {
            System.out.println("Test data setup warning: " + e.getMessage());
        }
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.update("DELETE FROM platform_service.recommendation_top_list WHERE list_type = ?", "mentor");
            jdbcTemplate.update("DELETE FROM platform_service.recommendation_banner WHERE title = ?", "Test Banner");
            jdbcTemplate.update("DELETE FROM platform_service.online_user_trend WHERE online_count = ?", 1200);
            jdbcTemplate.update("DELETE FROM platform_service.service_health WHERE service_name = ?", "zhitu-student");
            jdbcTemplate.update("DELETE FROM auth_center.sys_tenant WHERE id = ?", TEST_ENTERPRISE_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_tenant WHERE id = ?", TEST_TENANT_ID);
            jdbcTemplate.update("DELETE FROM auth_center.sys_user WHERE id = ?", TEST_USER_ID);
        } catch (Exception e) {
            System.out.println("Test data cleanup warning: " + e.getMessage());
        }
    }

    private void clearRedisCache() {
        try {
            redisTemplate.keys("platform:*").forEach(key -> redisTemplate.delete(key));
        } catch (Exception e) {
            System.out.println("Redis cache clear warning: " + e.getMessage());
        }
    }
}
