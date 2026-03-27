package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "考勤审核请求")
public class AttendanceAuditRequest {
    @NotNull
    @Schema(description = "考勤记录ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long attendanceId;
    
    /** 1=正常 2=异常 */
    @NotNull
    @Schema(description = "考勤状态：1=正常 2=异常", example = "1", allowableValues = {"1", "2"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
    
    @Schema(description = "审核备注", example = "考勤正常")
    private String auditRemark;
}
