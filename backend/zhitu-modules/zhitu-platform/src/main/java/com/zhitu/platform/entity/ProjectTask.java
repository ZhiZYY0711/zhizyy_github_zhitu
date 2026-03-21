package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 项目任务看板实体 - training_svc.project_task
 */
@Data
@TableName(schema = "training_svc", value = "project_task")
public class ProjectTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long teamId;

    private String title;

    private String description;

    private Long assigneeId;

    /** todo / in_progress / done */
    private String status;

    /** 1=low, 2=medium, 3=high */
    private Integer priority;

    private Integer storyPoints;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
