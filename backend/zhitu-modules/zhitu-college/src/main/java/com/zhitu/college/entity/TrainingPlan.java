package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实训排期计划 - training_svc.training_plan
 */
@Schema(description = "实训排期计划实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "training_svc", value = "training_plan")
public class TrainingPlan extends BaseEntity {

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;
    
    @Schema(description = "实训项目ID", example = "2001")
    private Long projectId;
    
    @Schema(description = "计划名称", example = "2024春季Java实训计划")
    private String planName;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "指导教师ID", example = "3001")
    private Long teacherId;
    
    @Schema(description = "状态：1-进行中，2-已结束", example = "1")
    private Integer status;
}
