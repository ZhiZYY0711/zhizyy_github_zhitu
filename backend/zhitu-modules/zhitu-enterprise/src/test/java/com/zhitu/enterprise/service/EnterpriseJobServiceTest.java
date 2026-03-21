package com.zhitu.enterprise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.CreateJobRequest;
import com.zhitu.enterprise.dto.JobDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnterpriseJobService 单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnterpriseJobServiceTest {

    @Autowired
    private EnterpriseJobService enterpriseJobService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testGetJobs_WithoutStatusFilter() {
        // 创建测试岗位
        createTestJob("Java Developer", 1);
        createTestJob("Python Developer", 1);
        createTestJob("Closed Job", 0);

        // 查询所有岗位
        PageResult<JobDTO> result = enterpriseJobService.getJobs(null, 1, 10);

        assertNotNull(result);
        assertEquals(3, result.getTotal());
        assertEquals(3, result.getRecords().size());
    }

    @Test
    void testGetJobs_WithStatusFilter() {
        // 创建测试岗位
        createTestJob("Java Developer", 1);
        createTestJob("Python Developer", 1);
        createTestJob("Closed Job", 0);

        // 查询开放岗位
        PageResult<JobDTO> result = enterpriseJobService.getJobs(1, 1, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertTrue(result.getRecords().stream().allMatch(job -> job.getStatus() == 1));
    }

    @Test
    void testGetJobs_Pagination() {
        // 创建多个测试岗位
        for (int i = 1; i <= 15; i++) {
            createTestJob("Job " + i, 1);
        }

        // 第一页
        PageResult<JobDTO> page1 = enterpriseJobService.getJobs(null, 1, 10);
        assertEquals(15, page1.getTotal());
        assertEquals(10, page1.getRecords().size());

        // 第二页
        PageResult<JobDTO> page2 = enterpriseJobService.getJobs(null, 2, 10);
        assertEquals(15, page2.getTotal());
        assertEquals(5, page2.getRecords().size());
    }

    @Test
    void testCreateJob_Success() {
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

        Long jobId = enterpriseJobService.createJob(request);

        assertNotNull(jobId);
        assertTrue(jobId > 0);

        // 验证岗位已创建
        PageResult<JobDTO> result = enterpriseJobService.getJobs(null, 1, 10);
        assertTrue(result.getRecords().stream().anyMatch(job -> job.getId().equals(jobId)));
    }

    @Test
    void testCreateJob_MissingRequiredFields() {
        CreateJobRequest request = new CreateJobRequest();
        request.setJobTitle("Test Job");
        // 缺少必填字段

        assertThrows(IllegalArgumentException.class, () -> {
            enterpriseJobService.createJob(request);
        });
    }

    @Test
    void testCreateJob_InvalidDateRange() {
        CreateJobRequest request = new CreateJobRequest();
        request.setJobTitle("Test Job");
        request.setJobType("full_time");
        request.setDescription("Description");
        request.setRequirements("Requirements");
        request.setCity("Beijing");
        request.setStartDate(LocalDate.of(2024, 6, 30));
        request.setEndDate(LocalDate.of(2024, 4, 1)); // 结束日期早于开始日期

        assertThrows(IllegalArgumentException.class, () -> {
            enterpriseJobService.createJob(request);
        });
    }

    @Test
    void testCloseJob_Success() {
        // 创建测试岗位
        Long jobId = createTestJob("Test Job", 1);

        // 关闭岗位
        enterpriseJobService.closeJob(jobId);

        // 验证岗位已关闭
        PageResult<JobDTO> result = enterpriseJobService.getJobs(0, 1, 10);
        assertTrue(result.getRecords().stream().anyMatch(job -> 
            job.getId().equals(jobId) && job.getStatus() == 0
        ));
    }

    @Test
    void testCloseJob_NotFound() {
        Long nonExistentJobId = 999999L;

        assertThrows(IllegalArgumentException.class, () -> {
            enterpriseJobService.closeJob(nonExistentJobId);
        });
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
