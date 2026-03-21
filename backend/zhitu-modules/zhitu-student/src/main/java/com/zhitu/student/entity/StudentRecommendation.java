package com.zhitu.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 学生个性化推荐实体 - student_svc.student_recommendation
 */
@Data
@TableName(schema = "student_svc", value = "student_recommendation")
public class StudentRecommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** project / job / course */
    private String recType;

    private Long refId;

    private BigDecimal score;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
