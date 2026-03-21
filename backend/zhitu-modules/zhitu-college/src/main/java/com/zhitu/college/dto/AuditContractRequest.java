package com.zhitu.college.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Audit Contract Request DTO
 */
@Data
public class AuditContractRequest {
    
    @NotNull(message = "审核动作不能为空")
    @NotBlank(message = "审核动作不能为空")
    private String action; // "pass" or "reject"
    
    private String comment;
}
