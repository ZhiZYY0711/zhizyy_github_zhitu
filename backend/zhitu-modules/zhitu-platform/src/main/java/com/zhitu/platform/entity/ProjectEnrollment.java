package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 项目报名实体 - training_svc.project_enrollment
 */
@Data
@TableName(schema = "training_svc", value = "project_enrollment")
public class ProjectEnrollment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long studentId;

    private Long teamId;

    /** member / leader */
    private String role;

    /** 1=active, 2=completed, 3=withdrawn */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime enrolledAt;
}
