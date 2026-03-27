package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 企业资质审核实体 - college_svc.enterprise_audit
 */
@Schema(description = "企业资质审核实体")
@Data
@TableName(schema = "college_svc", value = "enterprise_audit")
public class EnterpriseAudit {

    @Schema(description = "审核记录ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "企业租户ID", example = "2001")
    private Long enterpriseTenantId;

    @Schema(description = "审核类型：registration-注册审核，qualification-资质审核，annual-年审", example = "registration")
    /** registration / qualification / annual */
    private String auditType;

    @Schema(description = "审核状态：0-待审核，1-通过，2-拒绝", example = "1")
    /** 0=pending, 1=passed, 2=rejected */
    private Integer status;

    @Schema(description = "审核人ID", example = "3001")
    private Long auditorId;

    @Schema(description = "审核意见", example = "企业资质符合要求")
    private String auditComment;

    @Schema(description = "审核时间")
    private OffsetDateTime auditedAt;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
