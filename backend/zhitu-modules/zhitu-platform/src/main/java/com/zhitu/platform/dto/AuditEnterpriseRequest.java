package com.zhitu.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业审核请求DTO
 * Audit Enterprise Request DTO
 * 
 * Requirements: 31.4-31.5
 * - 31.4: Accept action parameter with values "pass" or "reject"
 * - 31.5: Require reject_reason parameter when rejecting
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEnterpriseRequest {
    
    /** Audit action: "pass" or "reject" */
    @NotBlank(message = "action is required")
    @Pattern(regexp = "^(pass|reject)$", message = "action must be 'pass' or 'reject'")
    private String action;
    
    /** Rejection reason (required when action is "reject") */
    private String rejectReason;
}
