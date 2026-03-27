package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "企业审核请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEnterpriseRequest {
    
    /** Audit action: "pass" or "reject" */
    @Schema(description = "审核操作", example = "pass", allowableValues = {"pass", "reject"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "action is required")
    @Pattern(regexp = "^(pass|reject)$", message = "action must be 'pass' or 'reject'")
    private String action;
    
    /** Rejection reason (required when action is "reject") */
    @Schema(description = "拒绝原因（当action为reject时必填）", example = "企业资质不符合要求")
    private String rejectReason;
}
