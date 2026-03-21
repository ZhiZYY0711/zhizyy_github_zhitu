package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Tenant DTO for platform tenant management
 * Requirements: 30.4, 30.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    
    private Long id;
    
    /**
     * Tenant name
     * Requirement 30.4
     */
    private String name;
    
    /**
     * Tenant type: 0=Platform, 1=College, 2=Enterprise
     * Requirement 30.4
     */
    private Integer type;
    
    /**
     * Tenant status: 0=Pending, 1=Active, 2=Disabled
     * Requirement 30.4
     */
    private Integer status;
    
    /**
     * Creation date
     * Requirement 30.4
     */
    private OffsetDateTime createdAt;
    
    /**
     * User count for this tenant
     * Requirement 30.4
     */
    private Long userCount;
    
    /**
     * Subscription plan (e.g., "Basic", "Premium", "Enterprise")
     * Requirement 30.5
     * Note: Currently stored in config JSON field, extracted during mapping
     */
    private String subscriptionPlan;
    
    /**
     * Subscription expiration date
     * Requirement 30.5
     * Note: Currently stored in config JSON field, extracted during mapping
     */
    private OffsetDateTime expirationDate;
}
