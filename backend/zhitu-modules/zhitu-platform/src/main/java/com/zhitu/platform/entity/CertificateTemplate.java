package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 证书模板实体 - platform_service.certificate_template
 */
@Data
@TableName(schema = "platform_service", value = "certificate_template")
public class CertificateTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;

    private String description;

    /** JSON configuration */
    private String layoutConfig;

    private String backgroundUrl;

    /** JSON array of signature image URLs */
    private String signatureUrls;

    /** JSON array: ["student_name", "certificate_type", "issue_date", "issuer_name"] */
    private String variables;

    private Integer usageCount;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
