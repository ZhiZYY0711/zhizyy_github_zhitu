package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "项目审核请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditProjectRequest {
    
    /** Audit action: "pass" or "reject" */
    @Schema(description = "审核操作", example = "pass", allowableValues = {"pass", "reject"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "action is required")
    @Pattern(regexp = "^(pass|reject)$", message = "action must be 'pass' or 'reject'")
    private String action;
    
    /** Quality rating (1-5, optional, only used when action is "pass") */
    @Schema(description = "质量评分（1-5分，通过时可选）", example = "4", minimum = "1", maximum = "5")
    @Min(value = 1, message = "quality_rating must be between 1 and 5")
    @Max(value = 5, message = "quality_rating must be between 1 and 5")
    private Integer qualityRating;
    
    /** Rejection reason (required when action is "reject") */
    @Schema(description = "拒绝原因（当action为reject时必填）", example = "项目描述不完整")
    private String rejectReason;
}
