package com.zhitu.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 学生任务实体 - student_svc.student_task
 */
@Data
@TableName(schema = "student_svc", value = "student_task")
public class StudentTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** training / internship / evaluation */
    private String taskType;

    private Long refId;

    private String title;

    private String description;

    /** 1=low, 2=medium, 3=high */
    private Integer priority;

    /** 0=pending, 1=completed */
    private Integer status;

    private OffsetDateTime dueDate;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
