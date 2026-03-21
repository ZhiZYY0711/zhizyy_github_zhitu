package com.zhitu.enterprise.dto;

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
public class AnalyticsDTO {
    
    /**
     * 申请趋势数据
     */
    private List<TrendDataPoint> applicationTrends;
    
    /**
     * 实习生绩效指标
     */
    private InternPerformanceMetrics internPerformance;
    
    /**
     * 实训项目完成率
     */
    private Double projectCompletionRate;
    
    /**
     * 导师满意度评分
     */
    private Double mentorSatisfaction;
    
    /**
     * 趋势数据点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        /**
         * 时间段标签（如"2024-W01", "2024-01", "2024-Q1"）
         */
        private String period;
        
        /**
         * 申请数量
         */
        private Integer count;
    }
    
    /**
     * 实习生绩效指标
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternPerformanceMetrics {
        /**
         * 平均评价分数
         */
        private Double averageScore;
        
        /**
         * 实习生总数
         */
        private Integer totalInterns;
        
        /**
         * 已评价实习生数
         */
        private Integer evaluatedInterns;
    }
}
