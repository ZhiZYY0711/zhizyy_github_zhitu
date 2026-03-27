package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业仪表板统计数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业仪表板统计数据")
public class DashboardStatsDTO {
    /**
     * 活跃岗位数量
     */
    @Schema(description = "活跃岗位数量", example = "10")
    private Integer activeJobCount;

    /**
     * 待处理申请数量
     */
    @Schema(description = "待处理申请数量", example = "25")
    private Integer pendingApplicationCount;

    /**
     * 活跃实习生数量
     */
    @Schema(description = "活跃实习生数量", example = "15")
    private Integer activeInternCount;

    /**
     * 实训项目数量
     */
    @Schema(description = "实训项目数量", example = "8")
    private Integer trainingProjectCount;
}
