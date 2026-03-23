package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.ApplicationDTO;
import com.zhitu.enterprise.dto.InterviewDTO;
import com.zhitu.enterprise.dto.ScheduleInterviewRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业申请管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseApplicationService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取企业岗位申请列表
     * 
     * @param jobId 岗位ID (可选)
     * @param status 申请状态 (可选: 0=待处理 1=面试 2=Offer 3=拒绝 4=录用)
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的申请列表
     */
    public PageResult<ApplicationDTO> getApplications(Long jobId, Integer status, Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return PageResult.of(0L, List.of(), page, size);
        }

        log.debug("Getting applications for enterprise: {}, jobId: {}, status: {}, page: {}, size: {}", 
            tenantId, jobId, status, page, size);

        // 计算偏移量
        int offset = (page - 1) * size;

        // 构建查询SQL - 通过job表关联过滤企业的申请
        StringBuilder sql = new StringBuilder(
            "SELECT a.id, a.job_id, j.job_title, a.student_id, s.real_name as student_name, " +
            "a.status, a.applied_at " +
            "FROM internship_svc.job_application a " +
            "INNER JOIN internship_svc.internship_job j ON a.job_id = j.id " +
            "INNER JOIN student_svc.student_info s ON a.student_id = s.id " +
            "WHERE j.enterprise_id = ? AND j.is_deleted IS FALSE AND s.is_deleted IS FALSE"
        );

        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        // 添加岗位过滤
        if (jobId != null) {
            sql.append(" AND a.job_id = ?");
            params.add(jobId);
        }

        // 添加状态过滤
        if (status != null) {
            sql.append(" AND a.status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY a.applied_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        // 查询申请列表
        List<ApplicationDTO> applications = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            ApplicationDTO app = new ApplicationDTO();
            app.setId(rs.getLong("id"));
            app.setJobId(rs.getLong("job_id"));
            app.setJobTitle(rs.getString("job_title"));
            app.setStudentId(rs.getLong("student_id"));
            app.setStudentName(rs.getString("student_name"));
            app.setStatus(rs.getInt("status"));
            app.setAppliedAt(rs.getObject("applied_at", OffsetDateTime.class));
            return app;
        }, params.toArray());

        // 查询总数
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM internship_svc.job_application a " +
            "INNER JOIN internship_svc.internship_job j ON a.job_id = j.id " +
            "WHERE j.enterprise_id = ? AND j.is_deleted IS FALSE"
        );
        
        List<Object> countParams = new ArrayList<>();
        countParams.add(tenantId);
        
        if (jobId != null) {
            countSql.append(" AND a.job_id = ?");
            countParams.add(jobId);
        }
        
        if (status != null) {
            countSql.append(" AND a.status = ?");
            countParams.add(status);
        }

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());

        return PageResult.of(total != null ? total : 0L, applications, page, size);
    }

    /**
     * 安排面试
     * 
     * @param request 面试安排请求
     * @return 面试ID
     */
    public InterviewDTO scheduleInterview(ScheduleInterviewRequest request) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            throw new IllegalStateException("Tenant not found for user: " + userId);
        }

        log.debug("Scheduling interview for enterprise: {}, application: {}", tenantId, request.getApplicationId());

        // 验证必填字段
        validateScheduleInterviewRequest(request);

        // 验证面试时间必须是未来时间
        if (request.getInterviewTime().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Interview time must be in the future");
        }

        // 验证申请是否属于该企业
        String checkSql = "SELECT COUNT(*) FROM internship_svc.job_application a " +
            "INNER JOIN internship_svc.internship_job j ON a.job_id = j.id " +
            "WHERE a.id = ? AND j.enterprise_id = ?";
        
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, 
            request.getApplicationId(), tenantId);
        
        if (count == null || count == 0) {
            throw new IllegalArgumentException("Application not found or not owned by enterprise: " 
                + request.getApplicationId());
        }

        // 插入面试安排
        String sql = "INSERT INTO enterprise_svc.interview_schedule " +
            "(application_id, student_id, enterprise_id, interview_time, location, " +
            "interviewer_id, interview_type, notes, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getApplicationId());
            ps.setLong(2, request.getStudentId());
            ps.setLong(3, tenantId);
            ps.setObject(4, request.getInterviewTime());
            ps.setString(5, request.getLocation());
            ps.setObject(6, request.getInterviewerId());
            ps.setString(7, request.getInterviewType());
            ps.setString(8, request.getNotes());
            return ps;
        }, keyHolder);

        Long interviewId = keyHolder.getKey().longValue();
        log.info("Created interview schedule with ID: {} for application: {}", interviewId, request.getApplicationId());
        
        // 发送通知给学生（简化实现 - 仅记录日志）
        log.info("Notification: Interview scheduled for student {} at {}", 
            request.getStudentId(), request.getInterviewTime());

        InterviewDTO response = new InterviewDTO();
        response.setId(interviewId);
        return response;
    }

    /**
     * 根据用户ID获取租户ID（企业ID）
     */
    private Long getTenantIdByUserId(Long userId) {
        String sql = "SELECT tenant_id FROM auth_center.sys_user WHERE id = ?";
        List<Long> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("tenant_id"), userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 验证面试安排请求
     */
    private void validateScheduleInterviewRequest(ScheduleInterviewRequest request) {
        if (request.getApplicationId() == null) {
            throw new IllegalArgumentException("Application ID is required");
        }
        if (request.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID is required");
        }
        if (request.getInterviewTime() == null) {
            throw new IllegalArgumentException("Interview time is required");
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (request.getInterviewerId() == null) {
            throw new IllegalArgumentException("Interviewer ID is required");
        }
    }
}
