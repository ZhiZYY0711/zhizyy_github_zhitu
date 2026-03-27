package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "审核请求")
@Data
public class AuditRequest {
    /** 1=通过 2=拒绝 */
    @Schema(description = "审核状态", example = "1", allowableValues = {"1", "2"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer auditStatus;
    
    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;
}
