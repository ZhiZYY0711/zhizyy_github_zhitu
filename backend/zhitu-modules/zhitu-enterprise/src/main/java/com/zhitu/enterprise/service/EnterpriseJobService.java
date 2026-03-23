package com.zhitu.enterprise.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.enterprise.dto.CreateJobRequest;
import com.zhitu.enterprise.dto.JobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业岗位管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseJobService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 获取企业岗位列表
     * 
     * @param status 岗位状态 (可选: 1=招募中, 0=已关闭)
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页的岗位列表
     */
    public PageResult<JobDTO> getJobs(Integer status, Integer page, Integer size) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return PageResult.of(0L, List.of(), page, size);
        }

        log.debug("Getting jobs for enterprise: {}, status: {}, page: {}, size: {}", tenantId, status, page, size);

        // 计算偏移量
        int offset = (page - 1) * size;

        // 构建查询SQL
        StringBuilder sql = new StringBuilder(
            "SELECT id, job_title, job_type, description, requirements, tech_stack, " +
            "city, salary_min, salary_max, headcount, start_date, end_date, status, created_at " +
            "FROM internship_svc.internship_job " +
            "WHERE enterprise_id = ? AND is_deleted IS FALSE"
        );

        List<Object> params = new ArrayList<>();
        params.add(tenantId);

        // 添加状态过滤
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        // 查询岗位列表
        List<JobDTO> jobs = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            JobDTO job = new JobDTO();
            job.setId(rs.getLong("id"));
            job.setJobTitle(rs.getString("job_title"));
            job.setJobType(rs.getString("job_type"));
            job.setDescription(rs.getString("description"));
            job.setRequirements(rs.getString("requirements"));
            
            // 解析技术栈JSON数组
            String techStackJson = rs.getString("tech_stack");
            if (techStackJson != null) {
                try {
                    job.setTechStack(objectMapper.readValue(techStackJson, List.class));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse tech_stack JSON: {}", techStackJson, e);
                    job.setTechStack(List.of());
                }
            }
            
            job.setCity(rs.getString("city"));
            job.setSalaryMin(rs.getInt("salary_min"));
            job.setSalaryMax(rs.getInt("salary_max"));
            job.setHeadcount(rs.getInt("headcount"));
            job.setStartDate(rs.getObject("start_date", LocalDate.class));
            job.setEndDate(rs.getObject("end_date", LocalDate.class));
            job.setStatus(rs.getInt("status"));
            job.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            
            return job;
        }, params.toArray());

        // 查询总数
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM internship_svc.internship_job " +
            "WHERE enterprise_id = ? AND is_deleted IS FALSE"
        );
        
        List<Object> countParams = new ArrayList<>();
        countParams.add(tenantId);
        
        if (status != null) {
            countSql.append(" AND status = ?");
            countParams.add(status);
        }

        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());

        return PageResult.of(total != null ? total : 0L, jobs, page, size);
    }

    /**
     * 创建岗位
     * 
     * @param request 创建岗位请求
     * @return 创建的岗位ID
     */
    public Long createJob(CreateJobRequest request) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            throw new IllegalStateException("Tenant not found for user: " + userId);
        }

        log.debug("Creating job for enterprise: {}, title: {}", tenantId, request.getJobTitle());

        // 验证必填字段
        validateCreateJobRequest(request);

        // 验证日期范围
        if (request.getEndDate() != null && request.getStartDate() != null 
            && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        // 转换技术栈为JSON
        String techStackJson = null;
        if (request.getTechStack() != null && !request.getTechStack().isEmpty()) {
            try {
                techStackJson = objectMapper.writeValueAsString(request.getTechStack());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize tech_stack", e);
                throw new IllegalArgumentException("Invalid tech stack format");
            }
        }

        // 插入岗位
        String sql = "INSERT INTO internship_svc.internship_job " +
            "(enterprise_id, job_title, job_type, description, requirements, tech_stack, " +
            "city, salary_min, salary_max, headcount, start_date, end_date, status) " +
            "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, 1)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        final String finalTechStackJson = techStackJson;
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            ps.setString(2, request.getJobTitle());
            ps.setString(3, request.getJobType());
            ps.setString(4, request.getDescription());
            ps.setString(5, request.getRequirements());
            ps.setString(6, finalTechStackJson);
            ps.setString(7, request.getCity());
            ps.setObject(8, request.getSalaryMin());
            ps.setObject(9, request.getSalaryMax());
            ps.setObject(10, request.getHeadcount());
            ps.setObject(11, request.getStartDate());
            ps.setObject(12, request.getEndDate());
            return ps;
        }, keyHolder);

        Long jobId = keyHolder.getKey().longValue();
        log.info("Created job with ID: {} for enterprise: {}", jobId, tenantId);
        
        return jobId;
    }

    /**
     * 关闭岗位
     * 
     * @param jobId 岗位ID
     */
    public void closeJob(Long jobId) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            throw new IllegalStateException("Tenant not found for user: " + userId);
        }

        log.debug("Closing job: {} for enterprise: {}", jobId, tenantId);

        // 更新岗位状态为关闭（0）
        String sql = "UPDATE internship_svc.internship_job " +
            "SET status = 0, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = ? AND enterprise_id = ? AND is_deleted IS FALSE";

        int updated = jdbcTemplate.update(sql, jobId, tenantId);
        
        if (updated == 0) {
            throw new IllegalArgumentException("Job not found or not owned by enterprise: " + jobId);
        }

        log.info("Closed job: {} for enterprise: {}", jobId, tenantId);
        
        // 通知申请者（简化实现 - 仅记录日志）
        log.info("Notification: Job {} has been closed. Applicants should be notified.", jobId);
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
     * 验证创建岗位请求
     */
    private void validateCreateJobRequest(CreateJobRequest request) {
        if (request.getJobTitle() == null || request.getJobTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (request.getJobType() == null || request.getJobType().trim().isEmpty()) {
            throw new IllegalArgumentException("Job type is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (request.getRequirements() == null || request.getRequirements().trim().isEmpty()) {
            throw new IllegalArgumentException("Requirements is required");
        }
        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
    }
}
