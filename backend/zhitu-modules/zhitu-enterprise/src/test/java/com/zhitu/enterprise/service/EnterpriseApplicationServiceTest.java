package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.ApplicationDTO;
import com.zhitu.enterprise.dto.InterviewDTO;
import com.zhitu.enterprise.dto.ScheduleInterviewRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnterpriseApplicationService 单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnterpriseApplicationServiceTest {

    @Autowired
    private EnterpriseApplicationService applicationService;

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
    void testGetApplications_WithoutFilters() {
        // 创建更多测试申请
        createTestApplication(testJobId, testStudentId + 1, 0);
        createTestApplication(testJobId, testStudentId + 2, 1);

        // 查询所有申请
        PageResult<ApplicationDTO> result = applicationService.getApplications(null, null, 1, 10);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 3);
        assertTrue(result.getRecords().size() >= 3);
    }

    @Test
    void testGetApplications_WithJobIdFilter() {
        // 创建另一个岗位和申请
        Long anotherJobId = createTestJob("Python Developer", 1);
        createTestApplication(anotherJobId, testStudentId + 1, 0);

        // 查询特定岗位的申请
        PageResult<ApplicationDTO> result = applicationService.getApplications(testJobId, null, 1, 10);

        assertNotNull(result);
        assertTrue(result.getRecords().stream().allMatch(app -> app.getJobId().equals(testJobId)));
    }

    @Test
    void testGetApplications_WithStatusFilter() {
        // 创建不同状态的申请
        createTestApplication(testJobId, testStudentId + 1, 0); // 待处理
        createTestApplication(testJobId, testStudentId + 2, 1); // 面试
        createTestApplication(testJobId, testStudentId + 3, 2); // Offer

        // 查询待处理的申请
        PageResult<ApplicationDTO> result = applicationService.getApplications(null, 0, 1, 10);

        assertNotNull(result);
        assertTrue(result.getRecords().stream().allMatch(app -> app.getStatus() == 0));
    }

    @Test
    void testGetApplications_Pagination() {
        // 创建多个测试申请
        for (int i = 1; i <= 15; i++) {
            createTestApplication(testJobId, testStudentId + i, 0);
        }

        // 第一页
        PageResult<ApplicationDTO> page1 = applicationService.getApplications(null, null, 1, 10);
        assertEquals(10, page1.getRecords().size());

        // 第二页
        PageResult<ApplicationDTO> page2 = applicationService.getApplications(null, null, 2, 10);
        assertTrue(page2.getRecords().size() > 0);
    }

    @Test
    void testScheduleInterview_Success() {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        request.setStudentId(testStudentId);
        request.setInterviewTime(OffsetDateTime.now().plusDays(1));
        request.setLocation("Conference Room A");
        request.setInterviewerId(testUserId);
        request.setInterviewType("technical");
        request.setNotes("First round technical interview");

        InterviewDTO result = applicationService.scheduleInterview(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0);

        // 验证面试已创建
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM enterprise_svc.interview_schedule WHERE id = ?",
            Integer.class, result.getId()
        );
        assertEquals(1, count);
    }

    @Test
    void testScheduleInterview_MissingRequiredFields() {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        // 缺少必填字段

        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.scheduleInterview(request);
        });
    }

    @Test
    void testScheduleInterview_PastInterviewTime() {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(testApplicationId);
        request.setStudentId(testStudentId);
        request.setInterviewTime(OffsetDateTime.now().minusDays(1)); // 过去的时间
        request.setLocation("Conference Room A");
        request.setInterviewerId(testUserId);

        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.scheduleInterview(request);
        });
    }

    @Test
    void testScheduleInterview_InvalidApplication() {
        ScheduleInterviewRequest request = new ScheduleInterviewRequest();
        request.setApplicationId(999999L); // 不存在的申请
        request.setStudentId(testStudentId);
        request.setInterviewTime(OffsetDateTime.now().plusDays(1));
        request.setLocation("Conference Room A");
        request.setInterviewerId(testUserId);

        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.scheduleInterview(request);
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

    /**
     * 辅助方法：创建测试申请
     */
    private Long createTestApplication(Long jobId, Long studentId, int status) {
        // 确保学生信息存在
        jdbcTemplate.update(
            "INSERT INTO student_svc.student_info (id, user_id, tenant_id, student_no, real_name) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO NOTHING",
            studentId, studentId, 4001L, "S" + studentId, "Student " + studentId
        );

        String sql = "INSERT INTO internship_svc.job_application " +
            "(job_id, student_id, status) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (job_id, student_id) DO UPDATE SET status = ? " +
            "RETURNING id";

        return jdbcTemplate.queryForObject(sql, Long.class, jobId, studentId, status, status);
    }
}
