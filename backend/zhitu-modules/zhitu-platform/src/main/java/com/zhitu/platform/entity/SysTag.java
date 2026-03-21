package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 系统标签实体 - platform_service.sys_tag
 */
@Data
@TableName(schema = "platform_service", value = "sys_tag")
public class SysTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** skill / industry / job_type / project_type / course_type */
    private String category;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private Integer usageCount;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
