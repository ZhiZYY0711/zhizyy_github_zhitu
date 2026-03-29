package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.ActivityDTO;
import com.zhitu.enterprise.dto.CodeReviewDTO;
import com.zhitu.enterprise.dto.MentorDashboardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 企业导师服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseMentorService {

    private final CacheService cacheService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取导师仪表板数据
     * 包括：分配的实习生数量、待批阅周报数量、待审核代码评审数量、最近的实习生活动
     * 
     * 实现逻辑：
     * 1. 从UserContext获取当前导师用户ID
     * 2. 查询internship_record表统计分配给该导师的实习生数量（mentor_id = 当前用户 AND status = 1）
     * 3. 查询weekly_report表统计待批阅周报数量（通过internship_record关联，status = 1表示已提交待批阅）
     * 4. 查询code_review表统计待审核代码评审数量
     * 5. 查询最近10条实习生活动（周报提交、考勤等）
     * 6. 使用Redis缓存，TTL为5分钟
     * 
     * @return 导师仪表板数据
     */
    public MentorDashboardDTO getDashboard() {
        Long mentorId = UserContext.getUserId();
        
        String cacheKey = "mentor:dashboard:" + mentorId;

        return cacheService.getOrSet(cacheKey, 5, TimeUnit.MINUTES, () -> {
            log.debug("Computing mentor dashboard for mentor: {}", mentorId);

            // 1. 统计分配的实习生数量（status=1表示实习中）
            Integer assignedInternCount = countAssignedInterns(mentorId);

            // 2. 统计待批阅周报数量（status=1表示已提交待批阅）
            Integer pendingReportCount = countPendingReports(mentorId);

            // 3. 统计待审核代码评审数量
            Integer pendingCodeReviewCount = countPendingCodeReviews(mentorId);

            // 4. 查询最近的实习生活动（最近10条）
            List<ActivityDTO> recentActivities = getRecentInternActivities(mentorId);

            return new MentorDashboardDTO(
                    assignedInternCount,
                    pendingReportCount,
                    pendingCodeReviewCount,
                    recentActivities
            );
        });
    }

    /**
     * 统计分配给导师的实习生数量
     * 查询internship_record表，条件：
     * - mentor_id = 当前导师ID
     * - status = 1（实习中）
     */
    private Integer countAssignedInterns(Long mentorId) {
        String sql = "SELECT COUNT(*) FROM internship_svc.internship_record " +
                "WHERE mentor_id = ? AND status = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class, mentorId);
    }

    /**
     * 统计待批阅周报数量
     * 查询weekly_report表，条件：
     * - 通过internship_record关联，找到该导师负责的实习生
     * - status = 1（已提交待批阅）
     */
    private Integer countPendingReports(Long mentorId) {
        String sql = "SELECT COUNT(*) FROM internship_svc.weekly_report wr " +
                "INNER JOIN internship_svc.internship_record ir ON wr.internship_id = ir.id " +
                "WHERE ir.mentor_id = ? AND wr.status = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class, mentorId);
    }

    /**
     * 统计待审核代码评审数量
     * 查询code_review表，条件：
     * - mentor_id = 当前导师ID
     * - status = 'pending'
     */
    private Integer countPendingCodeReviews(Long mentorId) {
        try {
            String sql = "SELECT COUNT(*) FROM training_svc.code_review " +
                    "WHERE mentor_id = ? AND status = 'pending'";
            return jdbcTemplate.queryForObject(sql, Integer.class, mentorId);
        } catch (Exception e) {
            log.warn("Failed to count pending code reviews, table may not exist: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 查询最近的实习生活动
     * 包括：周报提交、考勤打卡等
     * 
     * 实现逻辑：
     * 1. 查询该导师负责的实习生的最近活动
     * 2. 从weekly_report表查询最近提交的周报（最近10条）
     * 3. 按时间降序排序
     * 
     * @param mentorId 导师ID
     * @return 最近的活动列表
     */
    private List<ActivityDTO> getRecentInternActivities(Long mentorId) {
        String sql = "SELECT wr.id, wr.created_at, si.real_name " +
                "FROM internship_svc.weekly_report wr " +
                "INNER JOIN internship_svc.internship_record ir ON wr.internship_id = ir.id " +
                "INNER JOIN student_svc.student_info si ON wr.student_id = si.id " +
                "WHERE ir.mentor_id = ? " +
                "ORDER BY wr.created_at DESC " +
                "LIMIT 10";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long reportId = rs.getLong("id");
            OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
            String studentName = rs.getString("real_name");
            
            String description = studentName + " 提交了周报";
            
            return new ActivityDTO(
                    reportId,
                    "report_submitted",
                    description,
                    "weekly_report",
                    reportId,
                    createdAt
            );
        }, mentorId);
    }

    /**
     * 获取代码评审列表
     * 查询code_review表，按创建时间降序排序
     * 
     * @param status 状态过滤（可选）
     * @return 代码评审列表
     */
    public List<CodeReviewDTO> getCodeReviews(String status) {
        Long mentorId = UserContext.getUserId();
        log.debug("Getting code reviews for mentor: {} with status filter: {}", mentorId, status);
        
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT cr.id, cr.project_id, tp.project_name, cr.student_id, si.real_name as student_name, " +
                    "cr.file_path as file, cr.line_number as line, cr.code_snippet, cr.comment, cr.status, " +
                    "cr.created_at, cr.resolved_at " +
                    "FROM training_svc.code_review cr " +
                    "LEFT JOIN training_svc.training_project tp ON cr.project_id = tp.id " +
                    "LEFT JOIN student_svc.student_info si ON cr.student_id = si.id " +
                    "WHERE cr.mentor_id = ? "
            );
            
            List<Object> params = new ArrayList<>();
            params.add(mentorId);
            
            if (status != null && !status.isEmpty()) {
                sql.append("AND cr.status = ? ");
                params.add(status);
            }
            
            sql.append("ORDER BY cr.created_at DESC LIMIT 100");
            
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                CodeReviewDTO dto = new CodeReviewDTO();
                dto.setId(rs.getLong("id"));
                dto.setProjectId(rs.getLong("project_id"));
                dto.setProjectName(rs.getString("project_name"));
                dto.setStudentId(rs.getLong("student_id"));
                dto.setStudentName(rs.getString("student_name"));
                dto.setFile(rs.getString("file"));
                dto.setLine(rs.getInt("line"));
                dto.setCodeSnippet(rs.getString("code_snippet"));
                dto.setComment(rs.getString("comment"));
                dto.setStatus(rs.getString("status"));
                dto.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                dto.setResolvedAt(rs.getObject("resolved_at", OffsetDateTime.class));
                return dto;
            }, params.toArray());
        } catch (Exception e) {
            log.warn("Failed to get code reviews, table may not exist: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 提交代码评审意见
     * 更新code_review表的comment和status字段
     * 
     * @param id 评审ID
     * @param comment 评审意见
     */
    public void submitCodeReview(Long id, String comment) {
        Long mentorId = UserContext.getUserId();
        log.debug("Submitting code review for id: {}, mentor: {}, comment: {}", id, mentorId, comment);
        
        try {
            String sql = "UPDATE training_svc.code_review SET comment = ?, status = 'resolved', " +
                    "resolved_by = ?, resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND mentor_id = ?";
            jdbcTemplate.update(sql, comment, mentorId, id, mentorId);
        } catch (Exception e) {
            log.warn("Failed to submit code review, table may not exist: {}", e.getMessage());
        }
    }
}
