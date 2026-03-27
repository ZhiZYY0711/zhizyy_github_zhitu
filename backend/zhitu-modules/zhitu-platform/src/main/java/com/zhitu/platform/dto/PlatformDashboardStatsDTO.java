package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台仪表板统计数据 DTO
 * Platform Dashboard Statistics DTO
 */
@Schema(description = "平台仪表板统计数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDashboardStatsDTO {
    
    /**
     * 租户总数
     */
    @Schema(description = "租户总数", example = "150")
    private Long totalTenantCount;
    
    /**
     * 用户总数
     */
    @Schema(description = "用户总数", example = "5000")
    private Long totalUserCount;
    
    /**
     * 活跃用户数（最近30天）
     */
    @Schema(description = "活跃用户数（最近30天）", example = "3500")
    private Long activeUserCount;
    
    /**
     * 企业总数
     */
    @Schema(description = "企业总数", example = "80")
    private Long totalEnterpriseCount;
    
    /**
     * 待审核数量
     */
    @Schema(description = "待审核数量", example = "12")
    private Long pendingAuditCount;
}
