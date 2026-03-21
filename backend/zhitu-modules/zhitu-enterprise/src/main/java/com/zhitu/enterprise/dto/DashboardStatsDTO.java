package com.zhitu.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业仪表板统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    /**
     * 活跃岗位数量
     */
    private Integer activeJobCount;

    /**
     * 待处理申请数量
     */
    private Integer pendingApplicationCount;

    /**
     * 活跃实习生数量
     */
    private Integer activeInternCount;

    /**
     * 实训项目数量
     */
    private Integer trainingProjectCount;
}
