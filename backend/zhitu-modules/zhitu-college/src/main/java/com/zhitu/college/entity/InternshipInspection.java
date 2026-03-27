package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习巡查记录实体 - college_svc.internship_inspection
 */
@Schema(description = "实习巡查记录实体")
@Data
@TableName(schema = "college_svc", value = "internship_inspection")
public class InternshipInspection {

    @Schema(description = "巡查记录ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "高校租户ID", example = "1001")
    private Long collegeTenantId;

    @Schema(description = "实习记录ID", example = "5001")
    private Long internshipId;

    @Schema(description = "巡查人ID", example = "3001")
    private Long inspectorId;

    @Schema(description = "巡查日期", example = "2024-03-15")
    private LocalDate inspectionDate;

    @Schema(description = "巡查地点", example = "某某科技有限公司")
    private String location;

    @Schema(description = "巡查发现", example = "学生工作状态良好，企业管理规范")
    private String findings;

    @Schema(description = "存在问题", example = "部分学生加班时间较长")
    private String issues;

    @Schema(description = "改进建议", example = "建议企业合理安排工作时间")
    private String recommendations;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
