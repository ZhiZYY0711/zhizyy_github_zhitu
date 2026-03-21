package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 技能树实体 - platform_service.skill_tree
 */
@Data
@TableName(schema = "platform_service", value = "skill_tree")
public class SkillTree {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String skillName;

    /** technical / soft_skill / domain_knowledge */
    private String skillCategory;

    private Long parentId;

    private Integer level;

    private String description;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
