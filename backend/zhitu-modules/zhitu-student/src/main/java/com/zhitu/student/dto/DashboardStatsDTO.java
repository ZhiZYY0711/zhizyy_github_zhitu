package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生仪表板统计数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    /**
     * 实训项目数量
     */
    private Integer trainingProjectCount;
    
    /**
     * 实习岗位数量
     */
    private Integer internshipJobCount;
    
    /**
     * 待办任务数量
     */
    private Integer pendingTaskCount;
    
    /**
     * 成长分数
     */
    private Integer growthScore;
}
