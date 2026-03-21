package com.zhitu.enterprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.enterprise.dto.ScheduleInterviewRequest;
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

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EnterpriseApplicationController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EnterpriseApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testUserId = 1002L;
    private Long testTenantId = 2002L;
    private Long testStudentId = 3001L;
    private Long testJobId;
    private Long testApplicationId;

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

        // 创建测试学生租户
        jdbcTemplate.update(
            "INSERT INTO auth_center.sys_tenant (id, tenant_name, tenant_type, status) " +
            "VALUES (?, 'Test College', 'college', 1) " +
            "ON CONFLICT (id) DO NOTHING",
            4001L
        );

        // 创建测试学生用户
        jdbcTemplate.update(
            "INSERT INTO auth_center.sys_user (id, username, password, tenant_id, status) " +
            "VALUES (?, 'test-student', 'password', ?, 1) " +
            "ON CONFLICT (id) DO NOTHING",
            testStudentId, 4001L
        );

        // 创建测试学生信息
        jdbcTemplate.update(
            "INSERT INTO student_svc.student_info (id, user_id, tenant_id, student_no, real_name) " +
            "VALUES (?, ?, ?, 'S001', 'Test Student') " +
            "ON CONFLICT (id) DO UPDATE SET real_name = 'Test Student'",
            testStudentId, testStudentId, 4001L
        );

        // 创建测试岗位
        testJobId = createTestJob("Java Developer", 1);

        // 创建测试申请
        testApplicationId = createTestApplication(testJobId, testStudentId, 0);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void testGetApplications_Success() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/applications")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").exists())
                .andExpect(jsonPath("$.data.records[0].jobTitle").exists())
                .andExpect(jsonPath("$.data.records[0].studentName").exists());
    }

    @Test
    void testGetApplications_WithJobIdFilter() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/applications")
                .param("jobId", testJobId.toString())
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].jobId").value(testJobId));
    }

    @Test
    void testGetApplications_WithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/internship/v1/enterprise/applications")
                .param("status", "0")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].status").value(0));
    }

    @Test
    void testScheduleInterview_Success() throws Exception {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        request.setStudentId(testStudentId);
        request.setInterviewTime(OffsetDateTime.now().plusDays(1));
        request.setLocation("Conference Room A");
        request.setInterviewerId(testUserId);
        request.setInterviewType("technical");
        request.setNotes("First round technical interview");

        mockMvc.perform(post("/api/internship/v1/enterprise/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void testScheduleInterview_MissingRequiredFields() throws Exception {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        // 缺少必填字段

        mockMvc.perform(post("/api/internship/v1/enterprise/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testScheduleInterview_PastInterviewTime() throws Exception {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        request.setStudentId(testStudentId);
        request.setInterviewTime(OffsetDateTime.now().minusDays(1)); // 过去的时间
        request.setLocation("Conference Room A");
        request.setInterviewerId(testUserId);

        mockMvc.perform(post("/api/internship/v1/enterprise/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
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

    /**
     * 辅助方法：创建测试申请
     */
    private Long createTestApplication(Long jobId, Long studentId, int status) {
        String sql = "INSERT INTO internship_svc.job_application " +
            "(job_id, student_id, status) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (job_id, student_id) DO UPDATE SET status = ? " +
            "RETURNING id";

        return jdbcTemplate.queryForObject(sql, Long.class, jobId, studentId, status, status);
    }
}
