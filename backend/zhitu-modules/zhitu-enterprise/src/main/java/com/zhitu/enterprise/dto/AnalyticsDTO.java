package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 企业分析数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业分析数据")
public class AnalyticsDTO {
    
    /**
     * 转化率数据
     */
    @Schema(description = "转化率数据")
    private ConversionRate conversionRate;
    
    /**
     * 转化率趋势
     */
    @Schema(description = "转化率趋势")
    private List<ConversionTrend> conversionTrend;
    
    /**
     * 贡献度数据
     */
    @Schema(description = "贡献度数据")
    private Contribution contribution;
    
    /**
     * 招聘漏斗数据
     */
    @Schema(description = "招聘漏斗数据")
    private List<RecruitmentFunnel> recruitmentFunnel;
    
    /**
     * 申请趋势数据
     */
    @Schema(description = "申请趋势数据")
    private List<TrendDataPoint> applicationTrends;
    
    /**
     * 实习生绩效指标
     */
    @Schema(description = "实习生绩效指标")
    private InternPerformanceMetrics internPerformance;
    
    /**
     * 实训项目完成率
     */
    @Schema(description = "实训项目完成率", example = "0.85")
    private Double projectCompletionRate;
    
    /**
     * 导师满意度评分
     */
    @Schema(description = "导师满意度评分", example = "4.5")
    private Double mentorSatisfaction;
    
    /**
     * 简化构造器 - 用于测试和简单场景
     */
    public AnalyticsDTO(List<TrendDataPoint> applicationTrends, 
                       InternPerformanceMetrics internPerformance,
                       Double projectCompletionRate,
                       Double mentorSatisfaction) {
        this.applicationTrends = applicationTrends;
        this.internPerformance = internPerformance;
        this.projectCompletionRate = projectCompletionRate;
        this.mentorSatisfaction = mentorSatisfaction;
    }
    
    /**
     * 转化率数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "转化率数据")
    public static class ConversionRate {
        @Schema(description = "实习转正率", example = "0.65")
        private Double internshipToFulltime;
        
        @Schema(description = "招聘成本节省", example = "50000")
        private Double costSaving;
    }
    
    /**
     * 转化率趋势
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "转化率趋势")
    public static class ConversionTrend {
        @Schema(description = "月份", example = "2024-01")
        private String month;
        
        @Schema(description = "转化率", example = "0.65")
        private Double rate;
    }
    
    /**
     * 贡献度数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "贡献度数据")
    public static class Contribution {
        @Schema(description = "总价值", example = "500000")
        private Double totalValue;
        
        @Schema(description = "按部门分布")
        private List<DepartmentContribution> byDepartment;
    }
    
    /**
     * 部门贡献度
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "部门贡献度")
    public static class DepartmentContribution {
        @Schema(description = "部门名称", example = "技术部")
        private String department;
        
        @Schema(description = "贡献值", example = "200000")
        private Double value;
    }
    
    /**
     * 招聘漏斗
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "招聘漏斗")
    public static class RecruitmentFunnel {
        @Schema(description = "阶段", example = "简历筛选")
        private String stage;
        
        @Schema(description = "人数", example = "100")
        private Integer count;
    }
    
    /**
     * 趋势数据点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势数据点")
    public static class TrendDataPoint {
        /**
         * 时间段标签（如"2024-W01", "2024-01", "2024-Q1"）
         */
        @Schema(description = "时间段标签", example = "2024-W01")
        private String period;
        
        /**
         * 申请数量
         */
        @Schema(description = "申请数量", example = "25")
        private Integer count;
    }
    
    /**
     * 实习生绩效指标
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "实习生绩效指标")
    public static class InternPerformanceMetrics {
        /**
         * 平均评价分数
         */
        @Schema(description = "平均评价分数", example = "4.2")
        private Double averageScore;
        
        /**
         * 实习生总数
         */
        @Schema(description = "实习生总数", example = "50")
        private Integer totalInterns;
        
        /**
         * 已评价实习生数
         */
        @Schema(description = "已评价实习生数", example = "45")
        private Integer evaluatedInterns;
    }
}
