package com.zhitu.enterprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.enterprise.dto.CreateJobRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EnterpriseJobController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnterpriseJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testUserId = 1001L;
    private Long testTenantId = 2001L;

    @BeforeEach
    void setUp() {
        // 设置用户上下文
        UserContext.set(UserContext.LoginUser.builder()
            .userId(testUserId)
            .username("test-enterprise-user")
            .role("ENTERPRISE")
            .tenantId(testTenantId)
            .build());

        // 确保测试租户存在
        jdbcTemplate.update(
            "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status) " +
            "VALUES (?, 'Test Enterprise', 'enterprise', 1) " +
            "ON CONFLICT (id) DO NOTHING",
            testTenantId
        );

        // 确保测试用户存在并关联租户
        jdbcTemplate.update(
            "INSERT INTO auth_center.sys_user (id, username, password, tenant_id, status) " +
            "VALUES (?, 'test-enterprise-user', 'password', ?, 1) " +
            "ON CONFLICT (id) DO UPDATE SET tenant_id = ?",
            testUserId, testTenantId, testTenantId
        );
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testGetJobs_Success() throws Exception {
        // 创建测试岗位
        createTestJob("Java Developer", 1);
        createTestJob("Python Developer", 1);

        mockMvc.perform(get("/api/internship/v1/enterprise/jobs")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].jobTitle").exists());
    }

    @Test
    void testGetJobs_WithStatusFilter() throws Exception {
        // 创建测试岗位
        createTestJob("Open Job", 1);
        createTestJob("Closed Job", 0);

        mockMvc.perform(get("/api/internship/v1/enterprise/jobs")
                .param("status", "1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].status").value(1));
    }

    @Test
    void testCreateJob_Success() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setJobTitle("Senior Java Developer");
        request.setJobType("full_time");
        request.setDescription("We are looking for a senior Java developer");
        request.setRequirements("5+ years of Java experience");
        request.setTechStack(List.of("Java", "Spring Boot", "MySQL"));
        request.setCity("Beijing");
        request.setSalaryMin(15000);
        request.setSalaryMax(25000);
        request.setHeadcount(3);
        request.setStartDate(LocalDate.of(2024, 4, 1));
        request.setEndDate(LocalDate.of(2024, 6, 30));

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void testCreateJob_MissingRequiredFields() throws Exception {
        CreateJobRequest request = new CreateJobRequest();
        request.setJobTitle("Test Job");
        // 缺少必填字段

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testCloseJob_Success() throws Exception {
        // 创建测试岗位
        Long jobId = createTestJob("Test Job", 1);

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs/{id}/close", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCloseJob_NotFound() throws Exception {
        Long nonExistentJobId = 999999L;

        mockMvc.perform(post("/api/internship/v1/enterprise/jobs/{id}/close", nonExistentJobId))
                .andExpect(status().is5xxServerError());
    }

    /**
     * 辅助方法：创建测试岗位
     */
    private Long createTestJob(String title, int status) {
        String sql = "INSERT INTO internship_svc.internship_job " +
            "(enterprise_id, job_title, job_type, description, requirements, city, " +
            "salary_min, salary_max, headcount, start_date, end_date, status) " +
            "VALUES (?, ?, 'full_time', 'Test description', 'Test requirements', 'Beijing', " +
            "10000, 20000, 5, '2024-04-01', '2024-06-30', ?) " +
            "RETURNING id";

        return jdbcTemplate.queryForObject(sql, Long.class, testTenantId, title, status);
    }
}
