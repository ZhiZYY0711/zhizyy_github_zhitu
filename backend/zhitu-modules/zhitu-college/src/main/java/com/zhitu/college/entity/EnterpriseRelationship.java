package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 校企合作关系实体 - college_svc.enterprise_relationship
 */
@Schema(description = "校企合作关系实体")
@Data
@TableName(schema = "college_svc", value = "enterprise_relationship")
public class EnterpriseRelationship {

    @Schema(description = "合作关系ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "高校租户ID", example = "1001")
    private Long collegeTenantId;

    @Schema(description = "企业租户ID", example = "2001")
    private Long enterpriseTenantId;

    @Schema(description = "合作等级：1-普通，2-重点，3-战略", example = "2")
    /** 1=normal, 2=key, 3=strategic */
    private Integer cooperationLevel;

    @Schema(description = "状态：1-活跃，0-停用", example = "1")
    /** 1=active, 0=inactive */
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @Schema(description = "是否删除", example = "false")
    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
