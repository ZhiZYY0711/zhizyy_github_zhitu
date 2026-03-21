package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 校企合作关系实体 - college_svc.enterprise_relationship
 */
@Data
@TableName(schema = "college_svc", value = "enterprise_relationship")
public class EnterpriseRelationship {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeTenantId;

    private Long enterpriseTenantId;

    /** 1=normal, 2=key, 3=strategic */
    private Integer cooperationLevel;

    /** 1=active, 0=inactive */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
