package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 企业资质审核实体 - college_svc.enterprise_audit
 */
@Data
@TableName(schema = "college_svc", value = "enterprise_audit")
public class EnterpriseAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long enterpriseTenantId;

    /** registration / qualification / annual */
    private String auditType;

    /** 0=pending, 1=passed, 2=rejected */
    private Integer status;

    private Long auditorId;

    private String auditComment;

    private OffsetDateTime auditedAt;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
