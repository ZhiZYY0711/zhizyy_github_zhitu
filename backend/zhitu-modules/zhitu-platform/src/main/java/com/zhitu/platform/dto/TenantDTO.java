package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Tenant DTO for platform tenant management
 * Requirements: 30.4, 30.5
 */
@Schema(description = "租户信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    
    @Schema(description = "租户ID", example = "1")
    private Long id;
    
    /**
     * Tenant name
     * Requirement 30.4
     */
    @Schema(description = "租户名称", example = "浙江大学")
    private String name;
    
    /**
     * Tenant type: 0=Platform, 1=College, 2=Enterprise
     * Requirement 30.4
     */
    @Schema(description = "租户类型", example = "1", allowableValues = {"0", "1", "2"})
    private Integer type;
    
    /**
     * Tenant status: 0=Pending, 1=Active, 2=Disabled
     * Requirement 30.4
     */
    @Schema(description = "租户状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;
    
    /**
     * Creation date
     * Requirement 30.4
     */
    @Schema(description = "创建时间", example = "2024-01-01T00:00:00+08:00")
    private OffsetDateTime createdAt;
    
    /**
     * User count for this tenant
     * Requirement 30.4
     */
    @Schema(description = "用户数量", example = "500")
    private Long userCount;
    
    /**
     * Subscription plan (e.g., "Basic", "Premium", "Enterprise")
     * Requirement 30.5
     * Note: Currently stored in config JSON field, extracted during mapping
     */
    @Schema(description = "订阅计划", example = "Premium", allowableValues = {"Basic", "Premium", "Enterprise"})
    private String subscriptionPlan;
    
    /**
     * Subscription expiration date
     * Requirement 30.5
     * Note: Currently stored in config JSON field, extracted during mapping
     */
    @Schema(description = "订阅到期日期", example = "2024-12-31T23:59:59+08:00")
    private OffsetDateTime expirationDate;
}
