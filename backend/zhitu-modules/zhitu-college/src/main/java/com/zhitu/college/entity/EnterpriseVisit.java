package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 企业走访记录实体 - college_svc.enterprise_visit
 */
@Data
@TableName(schema = "college_svc", value = "enterprise_visit")
public class EnterpriseVisit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeTenantId;

    private Long enterpriseTenantId;

    private LocalDate visitDate;

    private Long visitorId;

    private String visitorName;

    private String purpose;

    private String outcome;

    private String nextAction;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
