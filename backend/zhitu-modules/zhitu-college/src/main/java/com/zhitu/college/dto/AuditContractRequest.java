package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Audit Contract Request DTO
 */
@Schema(description = "审核合同请求")
@Data
public class AuditContractRequest {
    
    @Schema(description = "审核动作：pass-通过，reject-拒绝", example = "pass", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核动作不能为空")
    @NotBlank(message = "审核动作不能为空")
    private String action; // "pass" or "reject"
    
    @Schema(description = "审核意见", example = "合同条款符合要求")
    private String comment;
}
