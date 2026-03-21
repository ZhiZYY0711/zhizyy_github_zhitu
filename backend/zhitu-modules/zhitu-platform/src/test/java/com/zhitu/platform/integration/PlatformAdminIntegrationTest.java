package com.zhitu.platform.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Platform Administration Integration Tests
 * 
 * Tests complete request-response flow for all Platform Administration endpoints:
 * 1. GET /api/system/v1/dashboard/stats - Platform dashboard statistics
 * 2. GET /api/monitor/v1/health - System health monitoring
 * 3. GET /api/monitor/v1/users/online-trend - Online user trend
 * 4. GET /api/monitor/v1/services - Service health status
 * 5. GET /api/system/v1/tenants/colleges - Tenant management
 * 6. GET /api/system/v1/audits/enterprises - Enterprise audit list
 * 7. POST /api/system/v1/audits/enterprises/{id} - Audit enterprise
 * 8. GET /api/portal-platform/v1/audits/projects - Project audit list
 * 9. POST /api/portal-platform/v1/audits/projects/{id} - Audit project
 * 10. GET /api/portal-platform/v1/recommendations/banner - Get banners
 * 11. POST /api/portal-platform/v1/recommendations/banner - Save banner
 * 12. GET /api/portal-platform/v1/recommendations/top-list - Get top list
 * 13. POST /api/portal-platform/v1/recommendations/top-list - Save top list
 * 
 * Validates: Requirements 28-40
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Platform Administration Integration Tests")
class PlatformAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ServiceHealthMapper serviceHealthMapper;

    @Autowired
    private OnlineUserTrendMapper onlineUserTrendMapper;

    @Autowired
    private RecommendationBannerMapper recommendationBannerMapper;

    @Autowired
    private RecommendationTopListMapper recommendationTopListMapper;

    private static final Long TEST_USER_ID = 1000L;
    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_ENTERPRISE_ID = 2000L;
    private static final Long TEST_PROJECT_ID = 3000L;

    @BeforeEach
    void setUp() {
        // Setup admin user context
        UserContext.LoginUser user = UserContext.LoginUser.builder()
                .userId(TEST_USER_ID)
                .username("platformadmin")
                .tenantId(TEST_TENANT_ID)
                .role("PLATFORM_ADMIN")
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
    }

    private void clearRedisCache() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    private void setupTestData() {
        // Insert service health records
        ServiceHealth health1 = new ServiceHealth();
        health1.setServiceName("zhitu-student");
        health1.setStatus("healthy");
        health1.setResponseTime(50);
        health1.setErrorRate(new BigDecimal("0.01"));
        health1.setCpuUsage(new BigDecimal("45.5"));
        health1.setMemoryUsage(new BigDecimal("60.0"));
        health1.setCheckedAt(OffsetDateTime.now());
        serviceHealthMapper.insert(health1);

        ServiceHealth health2 = new ServiceHealth();
        health2.setServiceName("zhitu-enterprise");
        health2.setStatus("healthy");
        health2.setResponseTime(60);
        health2.setErrorRate(new BigDecimal("0.02"));
        health2.setCpuUsage(new BigDecimal("50.0"));
        health2.setMemoryUsage(new BigDecimal("65.0"));
        health2.setCheckedAt(OffsetDateTime.now());
        serviceHealthMapper.insert(health2);

        // Insert online user trend records
        for (int i = 0; i < 24; i++) {
            OnlineUserTrend trend = new OnlineUserTrend();
            trend.setTimestamp(OffsetDateTime.now().minusHours(23 - i));
            trend.setOnlineCount(100 + i * 5);
            trend.setStudentCount(50 + i * 2);
            trend.setEnterpriseCount(30 + i);
            trend.setCollegeCount(20 + i);
            onlineUserTrendMapper.insert(trend);
        }

        // Insert recommendation banner
        RecommendationBanner banner = new RecommendationBanner();
        banner.setTitle("Test Banner");
        banner.setImageUrl("https://example.com/banner.jpg");
        banner.setLinkUrl("https://example.com/link");
        banner.setTargetPortal("student");
        banner.setStartDate(LocalDate.now().minusDays(1));
        banner.setEndDate(LocalDate.now().plusDays(30));
        banner.setSortOrder(1);
        banner.setStatus(1);
        recommendationBannerMapper.insert(banner);

        // Insert recommendation top list
        RecommendationTopList topList = new RecommendationTopList();
        topList.setListType("mentor");
        topList.setItemIds("[1,2,3,4,5]");
        topList.setUpdatedAt(OffsetDateTime.now());
        recommendationTopListMapper.insert(topList);
    }

    private void cleanupTestData() {
        jdbcTemplate.execute("DELETE FROM platform_service.service_health WHERE service_name IN ('zhitu-student', 'zhitu-enterprise')");
        jdbcTemplate.execute("DELETE FROM platform_service.online_user_trend WHERE timestamp >= NOW() - INTERVAL '24 hours'");
        jdbcTemplate.execute("DELETE FROM platform_service.recommendation_banner WHERE title = 'Test Banner'");
        jdbcTemplate.execute("DELETE FROM platform_service.recommendation_top_list WHERE list_type = 'mentor'");
    }

    // ==================== Dashboard Statistics Tests ====================

    @Test
    @Order(1)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 1: GET /api/system/v1/dashboard/stats - Success")
    void testGetDashboardStats_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalTenantCount").exists())
                .andExpect(jsonPath("$.data.totalUserCount").exists())
                .andExpect(jsonPath("$.data.activeUserCount").exists())
                .andExpect(jsonPath("$.data.totalEnterpriseCount").exists())
                .andExpect(jsonPath("$.data.pendingAuditCount").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<PlatformDashboardStatsDTO> response = objectMapper.readValue(content, 
                objectMapper.getTypeFactory().constructParametricType(Result.class, PlatformDashboardStatsDTO.class));
        
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getTotalTenantCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: GET /api/system/v1/dashboard/stats - Unauthorized")
    void testGetDashboardStats_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ==================== System Health Monitoring Tests ====================

    @Test
    @Order(3)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 3: GET /api/monitor/v1/health - Success")
    void testGetSystemHealth_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/monitor/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.overallStatus").exists())
                .andExpect(jsonPath("$.data.services").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("zhitu-student");
        assertThat(content).contains("zhitu-enterprise");
    }

    @Test
    @Order(4)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 4: GET /api/monitor/v1/users/online-trend - Success")
    void testGetOnlineUserTrend_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/monitor/v1/users/online-trend")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Result<List> response = objectMapper.readValue(content, 
                objectMapper.getTypeFactory().constructParametricType(Result.class, List.class));
        
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData().size()).isLessThanOrEqualTo(24);
    }

    @Test
    @Order(5)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 5: GET /api/monitor/v1/services - Success")
    void testGetServices_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/monitor/v1/services")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("zhitu-student");
        assertThat(content).contains("healthy");
    }

    // ==================== Tenant Management Tests ====================

    @Test
    @Order(6)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 6: GET /api/system/v1/tenants/colleges - Success")
    void testGetTenantList_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andReturn();
    }

    @Test
    @Order(7)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 7: GET /api/system/v1/tenants/colleges - With Filters")
    void testGetTenantList_WithFilters() throws Exception {
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("type", "college")
                        .param("status", "1")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Enterprise Audit Tests ====================

    @Test
    @Order(8)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 8: GET /api/system/v1/audits/enterprises - Success")
    void testGetEnterpriseAudits_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();
    }

    @Test
    @Order(9)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 9: GET /api/system/v1/audits/enterprises - Filter by Status")
    void testGetEnterpriseAudits_FilterByStatus() throws Exception {
        mockMvc.perform(get("/api/system/v1/audits/enterprises")
                        .param("status", "0")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Project Audit Tests ====================

    @Test
    @Order(10)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 10: GET /api/portal-platform/v1/audits/projects - Success")
    void testGetProjectAudits_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-platform/v1/audits/projects")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();
    }

    // ==================== Recommendation Banner Tests ====================

    @Test
    @Order(11)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 11: GET /api/portal-platform/v1/recommendations/banner - Success")
    void testGetBanners_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("Test Banner");
    }

    @Test
    @Order(12)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 12: POST /api/portal-platform/v1/recommendations/banner - Success")
    void testSaveBanner_Success() throws Exception {
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("New Banner");
        request.setImageUrl("https://example.com/new-banner.jpg");
        request.setLinkUrl("https://example.com/new-link");
        request.setTargetPortal("enterprise");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setSortOrder(1);

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(13)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 13: POST /api/portal-platform/v1/recommendations/banner - Validation Error")
    void testSaveBanner_ValidationError() throws Exception {
        SaveBannerRequest request = new SaveBannerRequest();
        // Missing required fields

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Recommendation Top List Tests ====================

    @Test
    @Order(14)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 14: GET /api/portal-platform/v1/recommendations/top-list - Success")
    void testGetTopList_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.listType").value("mentor"))
                .andExpect(jsonPath("$.data.itemIds").isArray())
                .andReturn();
    }

    @Test
    @Order(15)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 15: POST /api/portal-platform/v1/recommendations/top-list - Success")
    void testSaveTopList_Success() throws Exception {
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(List.of(1L, 2L, 3L, 4L, 5L));

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(16)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 16: POST /api/portal-platform/v1/recommendations/top-list - Exceeds Size Limit")
    void testSaveTopList_ExceedsSizeLimit() throws Exception {
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("project");
        request.setItemIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)); // 11 items, exceeds limit of 10

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Cache Tests ====================

    @Test
    @Order(17)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 17: Verify Redis Caching for Dashboard Stats")
    void testDashboardStatsCache() throws Exception {
        // First request - should hit database
        mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify cache key exists
        String cacheKey = "platform:dashboard:stats";
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        assertThat(hasKey).isTrue();

        // Second request - should hit cache
        mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(18)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 18: Verify Cache Invalidation on Banner Save")
    void testBannerCacheInvalidation() throws Exception {
        // Get banners to populate cache
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Save new banner - should invalidate cache
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Cache Test Banner");
        request.setImageUrl("https://example.com/cache-test.jpg");
        request.setLinkUrl("https://example.com/cache-link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setSortOrder(1);

        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify cache was invalidated
        String cacheKey = "platform:banners:student";
        Boolean hasKey = redisTemplate.hasKey(cacheKey);
        assertThat(hasKey).isFalse();
    }

    // ==================== Error Scenario Tests ====================

    @Test
    @Order(19)
    @WithMockUser(roles = "STUDENT")
    @DisplayName("Test 19: Access Denied for Non-Admin User")
    void testAccessDenied_NonAdminUser() throws Exception {
        mockMvc.perform(get("/api/system/v1/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(20)
    @WithMockUser(roles = "PLATFORM_ADMIN")
    @DisplayName("Test 20: Invalid Query Parameters")
    void testInvalidQueryParameters() throws Exception {
        mockMvc.perform(get("/api/system/v1/tenants/colleges")
                        .param("page", "-1")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
