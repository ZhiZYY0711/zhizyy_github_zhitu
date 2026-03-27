package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生仪表板统计数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生仪表板统计数据")
public class DashboardStatsDTO {
    
    @Schema(description = "实训项目数量", example = "5")
    private Integer trainingProjectCount;
    
    @Schema(description = "实习岗位数量", example = "3")
    private Integer internshipJobCount;
    
    @Schema(description = "待办任务数量", example = "8")
    private Integer pendingTaskCount;
    
    @Schema(description = "成长分数", example = "850")
    private Integer growthScore;
}
