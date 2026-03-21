package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习巡查记录实体 - college_svc.internship_inspection
 */
@Data
@TableName(schema = "college_svc", value = "internship_inspection")
public class InternshipInspection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeTenantId;

    private Long internshipId;

    private Long inspectorId;

    private LocalDate inspectionDate;

    private String location;

    private String findings;

    private String issues;

    private String recommendations;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
