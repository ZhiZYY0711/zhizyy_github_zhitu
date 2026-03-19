package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceAuditRequest {
    @NotNull
    private Long attendanceId;
    /** 1=正常 2=异常 */
    @NotNull
    private Integer status;
    private String auditRemark;
}
