package com.zhitu.enterprise.service;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.enterprise.dto.AnalyticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 企业分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseAnalyticsService {

    private final CacheService cacheService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取企业分析数据
     * 
     * @param range 时间范围: "week", "month", "quarter", "year"
     * @return 分析数据
     */
    public AnalyticsDTO getAnalytics(String range) {
        Long userId = UserContext.getUserId();
        
        // 获取企业租户ID
        Long tenantId = getTenantIdByUserId(userId);
        if (tenantId == null) {
            log.warn("Tenant not found for user: {}", userId);
            return createEmptyAnalytics();
        }

        String cacheKey = "enterprise:analytics:" + tenantId + ":" + range;

        return cacheService.getOrSet(cacheKey, 30, TimeUnit.MINUTES, () -> {
            log.debug("Computing analytics for tenant: {}, range: {}", tenantId, range);

            // 1. 计算申请趋势
            List<AnalyticsDTO.TrendDataPoint> applicationTrends = calculateApplicationTrends(tenantId, range);

            // 2. 计算实习生绩效
            AnalyticsDTO.InternPerformanceMetrics internPerformance = calculateInternPerformance(tenantId);

            // 3. 计算实训项目完成率
            Double projectCompletionRate = calculateProjectCompletionRate(tenantId);

            // 4. 计算导师满意度
            Double mentorSatisfaction = calculateMentorSatisfaction(tenantId);

            return new AnalyticsDTO(
                    applicationTrends,
                    internPerformance,
                    projectCompletionRate,
                    mentorSatisfaction
            );
        });
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
     * 创建空的分析数据
     */
    private AnalyticsDTO createEmptyAnalytics() {
        return new AnalyticsDTO(
                new ArrayList<>(),
                new AnalyticsDTO.InternPerformanceMetrics(0.0, 0, 0),
                0.0,
                0.0
        );
    }

    /**
     * 计算申请趋势
     * 按时间范围聚合申请数量
     */
    private List<AnalyticsDTO.TrendDataPoint> calculateApplicationTrends(Long tenantId, String range) {
        String sql;
        String periodFormat;
        int periodsBack;

        switch (range.toLowerCase()) {
            case "week":
                // 最近12周
                sql = "SELECT TO_CHAR(DATE_TRUNC('week', ja.created_at), 'IYYY-\"W\"IW') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.created_at >= CURRENT_DATE - INTERVAL '12 weeks' " +
                      "GROUP BY DATE_TRUNC('week', ja.created_at) " +
                      "ORDER BY DATE_TRUNC('week', ja.created_at')";
                periodsBack = 12;
                break;
                
            case "month":
                // 最近12个月
                sql = "SELECT TO_CHAR(DATE_TRUNC('month', ja.created_at), 'YYYY-MM') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.created_at >= CURRENT_DATE - INTERVAL '12 months' " +
                      "GROUP BY DATE_TRUNC('month', ja.created_at) " +
                      "ORDER BY DATE_TRUNC('month', ja.created_at')";
                periodsBack = 12;
                break;
                
            case "quarter":
                // 最近8个季度
                sql = "SELECT TO_CHAR(DATE_TRUNC('quarter', ja.created_at), 'YYYY-\"Q\"Q') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.created_at >= CURRENT_DATE - INTERVAL '2 years' " +
                      "GROUP BY DATE_TRUNC('quarter', ja.created_at) " +
                      "ORDER BY DATE_TRUNC('quarter', ja.created_at')";
                periodsBack = 8;
                break;
                
            case "year":
                // 最近5年
                sql = "SELECT TO_CHAR(DATE_TRUNC('year', ja.created_at), 'YYYY') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.created_at >= CURRENT_DATE - INTERVAL '5 years' " +
                      "GROUP BY DATE_TRUNC('year', ja.created_at) " +
                      "ORDER BY DATE_TRUNC('year', ja.created_at')";
                periodsBack = 5;
                break;
                
            default:
                log.warn("Invalid range parameter: {}, defaulting to month", range);
                return calculateApplicationTrends(tenantId, "month");
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AnalyticsDTO.TrendDataPoint(
                rs.getString("period"),
                rs.getInt("count")
        ), tenantId);
    }

    /**
     * 计算实习生绩效指标
     * 基于评价记录计算平均分数
     */
    private AnalyticsDTO.InternPerformanceMetrics calculateInternPerformance(Long tenantId) {
        // 查询该企业的实习生总数
        String countSql = "SELECT COUNT(DISTINCT student_id) " +
                "FROM internship_svc.internship_record " +
                "WHERE enterprise_id = ?";
        Integer totalInternsCount = jdbcTemplate.queryForObject(countSql, Integer.class, tenantId);
        final Integer totalInterns = (totalInternsCount != null) ? totalInternsCount : 0;

        // 查询该企业对实习生的评价数据
        String evalSql = "SELECT " +
                "COUNT(DISTINCT er.student_id) as evaluated_count, " +
                "AVG(er.score) as avg_score " +
                "FROM growth_svc.evaluation_record er " +
                "INNER JOIN internship_svc.internship_record ir ON er.student_id = ir.student_id " +
                "WHERE ir.enterprise_id = ? " +
                "AND er.source_type = 'enterprise' " +
                "AND er.is_deleted = false";

        List<AnalyticsDTO.InternPerformanceMetrics> results = jdbcTemplate.query(evalSql, (rs, rowNum) -> {
            Integer evaluatedCount = rs.getInt("evaluated_count");
            Double avgScore = rs.getDouble("avg_score");
            if (rs.wasNull()) {
                avgScore = 0.0;
            }
            return new AnalyticsDTO.InternPerformanceMetrics(
                    Math.round(avgScore * 10.0) / 10.0, // 保留1位小数
                    totalInterns,
                    evaluatedCount
            );
        }, tenantId);

        return results.isEmpty() 
                ? new AnalyticsDTO.InternPerformanceMetrics(0.0, totalInterns, 0)
                : results.get(0);
    }

    /**
     * 计算实训项目完成率
     * 完成率 = 已完成任务数 / 总任务数
     */
    private Double calculateProjectCompletionRate(Long tenantId) {
        String sql = "SELECT " +
                "COUNT(CASE WHEN pt.status = 'done' THEN 1 END) as completed_tasks, " +
                "COUNT(*) as total_tasks " +
                "FROM training_svc.project_task pt " +
                "INNER JOIN training_svc.training_project tp ON pt.project_id = tp.id " +
                "WHERE tp.enterprise_id = ? " +
                "AND pt.is_deleted = false " +
                "AND tp.is_deleted = false";

        List<Double> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int completedTasks = rs.getInt("completed_tasks");
            int totalTasks = rs.getInt("total_tasks");
            
            if (totalTasks == 0) {
                return 0.0;
            }
            
            double rate = (completedTasks * 100.0) / totalTasks;
            return Math.round(rate * 10.0) / 10.0; // 保留1位小数
        }, tenantId);

        return results.isEmpty() ? 0.0 : results.get(0);
    }

    /**
     * 计算导师满意度
     * 基于导师对实习生的评价分数
     */
    private Double calculateMentorSatisfaction(Long tenantId) {
        // 查询该企业导师对实习生的评价平均分
        String sql = "SELECT AVG(er.score) as avg_score " +
                "FROM growth_svc.evaluation_record er " +
                "INNER JOIN enterprise_svc.enterprise_staff es ON er.evaluator_id = es.user_id " +
                "WHERE es.tenant_id = ? " +
                "AND es.is_mentor = true " +
                "AND er.source_type = 'enterprise' " +
                "AND er.is_deleted = false " +
                "AND es.is_deleted = false";

        List<Double> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Double avgScore = rs.getDouble("avg_score");
            if (rs.wasNull()) {
                return 0.0;
            }
            // 将100分制转换为5分制
            double satisfaction = (avgScore / 100.0) * 5.0;
            return Math.round(satisfaction * 10.0) / 10.0; // 保留1位小数
        }, tenantId);

        return results.isEmpty() ? 0.0 : results.get(0);
    }
}
