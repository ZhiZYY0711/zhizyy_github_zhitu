package com.zhitu.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {
    /** 1=通过 2=拒绝 */
    @NotNull
    private Integer auditStatus;
    private String auditRemark;
}
