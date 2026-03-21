package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 合同模板实体 - platform_service.contract_template
 */
@Data
@TableName(schema = "platform_service", value = "contract_template")
public class ContractTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;

    private String description;

    /** internship / training / employment */
    private String contractType;

    /** Template content with placeholders */
    private String content;

    /** JSON array: ["student_name", "enterprise_name", "position", "duration", "salary"] */
    private String variables;

    private String legalTerms;

    private Integer usageCount;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
