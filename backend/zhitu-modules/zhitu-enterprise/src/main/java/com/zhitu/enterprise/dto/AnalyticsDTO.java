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
