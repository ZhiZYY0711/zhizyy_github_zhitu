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

            // 1. 计算转化率数据
            AnalyticsDTO.ConversionRate conversionRate = calculateConversionRate(tenantId);

            // 2. 计算转化率趋势
            List<AnalyticsDTO.ConversionTrend> conversionTrend = calculateConversionTrend(tenantId, range);

            // 3. 计算贡献度数据
            AnalyticsDTO.Contribution contribution = calculateContribution(tenantId);

            // 4. 计算招聘漏斗
            List<AnalyticsDTO.RecruitmentFunnel> recruitmentFunnel = calculateRecruitmentFunnel(tenantId);

            // 5. 计算申请趋势
            List<AnalyticsDTO.TrendDataPoint> applicationTrends = calculateApplicationTrends(tenantId, range);

            // 6. 计算实习生绩效
            AnalyticsDTO.InternPerformanceMetrics internPerformance = calculateInternPerformance(tenantId);

            // 7. 计算实训项目完成率
            Double projectCompletionRate = calculateProjectCompletionRate(tenantId);

            // 8. 计算导师满意度
            Double mentorSatisfaction = calculateMentorSatisfaction(tenantId);

            AnalyticsDTO dto = new AnalyticsDTO();
            dto.setConversionRate(conversionRate);
            dto.setConversionTrend(conversionTrend);
            dto.setContribution(contribution);
            dto.setRecruitmentFunnel(recruitmentFunnel);
            dto.setApplicationTrends(applicationTrends);
            dto.setInternPerformance(internPerformance);
            dto.setProjectCompletionRate(projectCompletionRate);
            dto.setMentorSatisfaction(mentorSatisfaction);
            
            return dto;
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
        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setConversionRate(new AnalyticsDTO.ConversionRate(0.0, 0.0));
        dto.setConversionTrend(new ArrayList<>());
        dto.setContribution(new AnalyticsDTO.Contribution(0.0, new ArrayList<>()));
        dto.setRecruitmentFunnel(new ArrayList<>());
        dto.setApplicationTrends(new ArrayList<>());
        dto.setInternPerformance(new AnalyticsDTO.InternPerformanceMetrics(0.0, 0, 0));
        dto.setProjectCompletionRate(0.0);
        dto.setMentorSatisfaction(0.0);
        return dto;
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
                sql = "SELECT TO_CHAR(DATE_TRUNC('week', ja.applied_at), 'IYYY-\"W\"IW') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.applied_at >= CURRENT_DATE - INTERVAL '12 weeks' " +
                      "GROUP BY DATE_TRUNC('week', ja.applied_at) " +
                      "ORDER BY DATE_TRUNC('week', ja.applied_at)";
                periodsBack = 12;
                break;
                
            case "month":
                // 最近12个月
                sql = "SELECT TO_CHAR(DATE_TRUNC('month', ja.applied_at), 'YYYY-MM') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.applied_at >= CURRENT_DATE - INTERVAL '12 months' " +
                      "GROUP BY DATE_TRUNC('month', ja.applied_at) " +
                      "ORDER BY DATE_TRUNC('month', ja.applied_at)";
                periodsBack = 12;
                break;
                
            case "quarter":
                // 最近8个季度
                sql = "SELECT TO_CHAR(DATE_TRUNC('quarter', ja.applied_at), 'YYYY-\"Q\"Q') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.applied_at >= CURRENT_DATE - INTERVAL '2 years' " +
                      "GROUP BY DATE_TRUNC('quarter', ja.applied_at) " +
                      "ORDER BY DATE_TRUNC('quarter', ja.applied_at)";
                periodsBack = 8;
                break;
                
            case "year":
                // 最近5年
                sql = "SELECT TO_CHAR(DATE_TRUNC('year', ja.applied_at), 'YYYY') as period, " +
                      "COUNT(*) as count " +
                      "FROM internship_svc.job_application ja " +
                      "INNER JOIN internship_svc.internship_job ij ON ja.job_id = ij.id " +
                      "WHERE ij.enterprise_id = ? " +
                      "AND ja.applied_at >= CURRENT_DATE - INTERVAL '5 years' " +
                      "GROUP BY DATE_TRUNC('year', ja.applied_at) " +
                      "ORDER BY DATE_TRUNC('year', ja.applied_at)";
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
        // scores 是 JSON 格式，如 {"technical":85,"attitude":90}，使用 PostgreSQL 的 JSON 函数提取并计算平均值
        String evalSql = "SELECT " +
                "COUNT(DISTINCT er.student_id) as evaluated_count, " +
                "AVG((er.scores::json->>'technical')::numeric) as avg_tech_score, " +
                "AVG((er.scores::json->>'attitude')::numeric) as avg_attitude_score " +
                "FROM growth_svc.evaluation_record er " +
                "INNER JOIN internship_svc.internship_record ir ON er.student_id = ir.student_id " +
                "WHERE ir.enterprise_id = ? " +
                "AND er.source_type = 'enterprise' " +
                "AND er.is_deleted IS FALSE " +
                "AND er.scores IS NOT NULL";

        List<AnalyticsDTO.InternPerformanceMetrics> results = jdbcTemplate.query(evalSql, (rs, rowNum) -> {
            Integer evaluatedCount = rs.getInt("evaluated_count");
            Double avgTechScore = rs.getDouble("avg_tech_score");
            Double avgAttitudeScore = rs.getDouble("avg_attitude_score");
            if (rs.wasNull()) {
                avgTechScore = 0.0;
            }
            // 取技术分和态度分的平均值
            double avgScore = (avgTechScore + avgAttitudeScore) / 2.0;
            return new AnalyticsDTO.InternPerformanceMetrics(
                    Math.round(avgScore * 10.0) / 10.0,
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
                "AND pt.is_deleted IS FALSE " +
                "AND tp.is_deleted IS FALSE";

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
        // scores 是 JSON 格式，需要解析并计算平均值
        String sql = "SELECT " +
                "CASE " +
                "  WHEN COUNT(*) = 0 THEN 0 " +
                "  ELSE AVG((er.scores::jsonb->>'technical')::numeric + (er.scores::jsonb->>'attitude')::numeric) / 2 " +
                "END as avg_score " +
                "FROM growth_svc.evaluation_record er " +
                "INNER JOIN enterprise_svc.enterprise_staff es ON er.evaluator_id = es.user_id " +
                "WHERE es.tenant_id = ? " +
                "AND es.is_mentor = true " +
                "AND er.source_type = 'enterprise' " +
                "AND er.is_deleted IS FALSE " +
                "AND es.is_deleted IS FALSE " +
                "AND er.scores IS NOT NULL " +
                "AND er.scores != ''";

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

    /**
     * 计算转化率数据
     */
    private AnalyticsDTO.ConversionRate calculateConversionRate(Long tenantId) {
        // 查询实习转正人数和总实习人数
        String sql = "SELECT " +
                "COUNT(CASE WHEN es.employment_type = 'full_time' AND es.previous_intern = true THEN 1 END) as converted, " +
                "COUNT(CASE WHEN es.previous_intern = true THEN 1 END) as total_interns " +
                "FROM enterprise_svc.enterprise_staff es " +
                "WHERE es.tenant_id = ? AND es.is_deleted IS FALSE";
        
        List<AnalyticsDTO.ConversionRate> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int converted = rs.getInt("converted");
            int totalInterns = rs.getInt("total_interns");
            
            double rate = totalInterns > 0 ? (double) converted / totalInterns : 0.0;
            // 假设每个转正员工节省招聘成本 5000 元
            double costSaving = converted * 5000.0;
            
            return new AnalyticsDTO.ConversionRate(rate, costSaving);
        }, tenantId);
        
        return results.isEmpty() ? new AnalyticsDTO.ConversionRate(0.0, 0.0) : results.get(0);
    }
    
    /**
     * 计算转化率趋势（最近6个月）
     */
    private List<AnalyticsDTO.ConversionTrend> calculateConversionTrend(Long tenantId, String range) {
        // 简化实现：返回模拟数据
        List<AnalyticsDTO.ConversionTrend> trends = new ArrayList<>();
        java.time.LocalDate now = java.time.LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate month = now.minusMonths(i);
            String monthStr = month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            // 这里应该查询实际数据，暂时返回模拟值
            trends.add(new AnalyticsDTO.ConversionTrend(monthStr, 0.6 + (Math.random() * 0.2)));
        }
        
        return trends;
    }
    
    /**
     * 计算贡献度数据
     */
    private AnalyticsDTO.Contribution calculateContribution(Long tenantId) {
        // 查询各部门实习生数量
        String sql = "SELECT " +
                "COALESCE(es.department, '未分配') as department, " +
                "COUNT(*) as intern_count " +
                "FROM enterprise_svc.enterprise_staff es " +
                "WHERE es.tenant_id = ? " +
                "AND es.previous_intern = true " +
                "AND es.is_deleted IS FALSE " +
                "GROUP BY es.department";
        
        List<AnalyticsDTO.DepartmentContribution> byDepartment = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String department = rs.getString("department");
            int count = rs.getInt("intern_count");
            // 假设每个实习生贡献价值 10000 元
            double value = count * 10000.0;
            return new AnalyticsDTO.DepartmentContribution(department, value);
        }, tenantId);
        
        double totalValue = byDepartment.stream()
                .mapToDouble(AnalyticsDTO.DepartmentContribution::getValue)
                .sum();
        
        return new AnalyticsDTO.Contribution(totalValue, byDepartment);
    }
    
    /**
     * 计算招聘漏斗
     */
    private List<AnalyticsDTO.RecruitmentFunnel> calculateRecruitmentFunnel(Long tenantId) {
        List<AnalyticsDTO.RecruitmentFunnel> funnel = new ArrayList<>();
        
        // 1. 简历投递
        String sql1 = "SELECT COUNT(*) FROM student_svc.internship_application ia " +
                "INNER JOIN student_svc.internship_position ip ON ia.position_id = ip.id " +
                "WHERE ip.tenant_id = ? AND ia.is_deleted IS FALSE";
        Integer totalApplications = jdbcTemplate.queryForObject(sql1, Integer.class, tenantId);
        funnel.add(new AnalyticsDTO.RecruitmentFunnel("简历投递", totalApplications != null ? totalApplications : 0));
        
        // 2. 简历筛选通过
        String sql2 = "SELECT COUNT(*) FROM student_svc.internship_application ia " +
                "INNER JOIN student_svc.internship_position ip ON ia.position_id = ip.id " +
                "WHERE ip.tenant_id = ? AND ia.status IN ('interview_scheduled', 'interviewed', 'offered', 'accepted') " +
                "AND ia.is_deleted IS FALSE";
        Integer screened = jdbcTemplate.queryForObject(sql2, Integer.class, tenantId);
        funnel.add(new AnalyticsDTO.RecruitmentFunnel("简历筛选", screened != null ? screened : 0));
        
        // 3. 面试
        String sql3 = "SELECT COUNT(*) FROM student_svc.internship_application ia " +
                "INNER JOIN student_svc.internship_position ip ON ia.position_id = ip.id " +
                "WHERE ip.tenant_id = ? AND ia.status IN ('interviewed', 'offered', 'accepted') " +
                "AND ia.is_deleted IS FALSE";
        Integer interviewed = jdbcTemplate.queryForObject(sql3, Integer.class, tenantId);
        funnel.add(new AnalyticsDTO.RecruitmentFunnel("面试", interviewed != null ? interviewed : 0));
        
        // 4. 录用
        String sql4 = "SELECT COUNT(*) FROM student_svc.internship_application ia " +
                "INNER JOIN student_svc.internship_position ip ON ia.position_id = ip.id " +
                "WHERE ip.tenant_id = ? AND ia.status IN ('offered', 'accepted') " +
                "AND ia.is_deleted IS FALSE";
        Integer offered = jdbcTemplate.queryForObject(sql4, Integer.class, tenantId);
        funnel.add(new AnalyticsDTO.RecruitmentFunnel("录用", offered != null ? offered : 0));
        
        // 5. 入职
        String sql5 = "SELECT COUNT(*) FROM student_svc.internship_application ia " +
                "INNER JOIN student_svc.internship_position ip ON ia.position_id = ip.id " +
                "WHERE ip.tenant_id = ? AND ia.status = 'accepted' " +
                "AND ia.is_deleted IS FALSE";
        Integer accepted = jdbcTemplate.queryForObject(sql5, Integer.class, tenantId);
        funnel.add(new AnalyticsDTO.RecruitmentFunnel("入职", accepted != null ? accepted : 0));
        
        return funnel;
    }

}
