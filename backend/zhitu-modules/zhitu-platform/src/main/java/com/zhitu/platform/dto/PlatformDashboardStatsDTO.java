package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台仪表板统计数据 DTO
 * Platform Dashboard Statistics DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDashboardStatsDTO {
    
    /**
     * 租户总数
     */
    private Long totalTenantCount;
    
    /**
     * 用户总数
     */
    private Long totalUserCount;
    
    /**
     * 活跃用户数（最近30天）
     */
    private Long activeUserCount;
    
    /**
     * 企业总数
     */
    private Long totalEnterpriseCount;
    
    /**
     * 待审核数量
     */
    private Long pendingAuditCount;
}
