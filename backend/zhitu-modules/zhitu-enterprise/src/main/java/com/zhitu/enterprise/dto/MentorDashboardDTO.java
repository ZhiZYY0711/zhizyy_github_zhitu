package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导师仪表板DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导师仪表板数据")
public class MentorDashboardDTO {
    /**
     * 分配的实习生数量
     */
    @Schema(description = "分配的实习生数量", example = "8")
    private Integer assignedInternCount;

    /**
     * 待批阅周报数量
     */
    @Schema(description = "待批阅周报数量", example = "5")
    private Integer pendingReportCount;

    /**
     * 待审核代码评审数量
     */
    @Schema(description = "待审核代码评审数量", example = "3")
    private Integer pendingCodeReviewCount;

    /**
     * 最近的实习生活动列表
     */
    @Schema(description = "最近的实习生活动列表")
    private List<ActivityDTO> recentActivities;
}
