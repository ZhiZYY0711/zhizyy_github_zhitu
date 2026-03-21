package com.zhitu.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目审核请求DTO
 * Audit Project Request DTO
 * 
 * Requirements: 32.4-32.6
 * - 32.4: Accept action parameter with values "pass" or "reject"
 * - 32.5: Support optional quality_rating parameter on approval
 * - 32.6: Require reject_reason when rejecting
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditProjectRequest {
    
    /** Audit action: "pass" or "reject" */
    @NotBlank(message = "action is required")
    @Pattern(regexp = "^(pass|reject)$", message = "action must be 'pass' or 'reject'")
    private String action;
    
    /** Quality rating (1-5, optional, only used when action is "pass") */
    @Min(value = 1, message = "quality_rating must be between 1 and 5")
    @Max(value = 5, message = "quality_rating must be between 1 and 5")
    private Integer qualityRating;
    
    /** Rejection reason (required when action is "reject") */
    private String rejectReason;
}
