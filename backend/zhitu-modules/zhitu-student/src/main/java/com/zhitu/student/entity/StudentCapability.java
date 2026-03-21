package com.zhitu.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 学生能力雷达图数据实体 - student_svc.student_capability
 */
@Data
@TableName(schema = "student_svc", value = "student_capability")
public class StudentCapability {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** technical / communication / teamwork / problem_solving / leadership */
    private String dimension;

    /** 0-100 */
    private Integer score;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
