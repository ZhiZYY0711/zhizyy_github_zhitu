package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 人才库实体 - enterprise_svc.talent_pool
 */
@Data
@TableName(schema = "enterprise_svc", value = "talent_pool")
public class TalentPool {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long studentId;
    private Long collectedBy;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableLogic
    private Boolean isDeleted;
}
