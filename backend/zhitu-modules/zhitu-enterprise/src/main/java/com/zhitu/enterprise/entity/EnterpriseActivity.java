package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 企业活动动态实体 - enterprise_svc.enterprise_activity
 */
@Data
@TableName(schema = "enterprise_svc", value = "enterprise_activity")
public class EnterpriseActivity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** application / interview / report_submitted / evaluation */
    private String activityType;

    private String description;

    /** job / application / intern */
    private String refType;

    private Long refId;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
