package com.zhitu.enterprise.dto;

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
public class MentorDashboardDTO {
    /**
     * 分配的实习生数量
     */
    private Integer assignedInternCount;

    /**
     * 待批阅周报数量
     */
    private Integer pendingReportCount;

    /**
     * 待审核代码评审数量
     */
    private Integer pendingCodeReviewCount;

    /**
     * 最近的实习生活动列表
     */
    private List<ActivityDTO> recentActivities;
}
