package com.zhitu.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 评价记录实体 - growth_svc.evaluation_record
 */
@Data
@TableName(schema = "growth_svc", value = "evaluation_record")
public class EvaluationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    /** 评价人 ID */
    private Long evaluatorId;

    /** 来源类型: enterprise / school / peer */
    private String sourceType;

    /** 关联类型: project / internship */
    private String refType;

    /** 关联 ID */
    private Long refId;

    /** 评分 JSON，如 {"technical":85,"attitude":90} */
    private String scores;

    private String comment;

    /** strongly_recommend / recommend / not_recommend */
    private String hireRecommendation;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
