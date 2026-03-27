package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "创建实训计划请求")
@Data
public class CreateTrainingPlanRequest {
    @Schema(description = "计划名称", example = "2024春季Java实训计划", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String planName;
    
    @Schema(description = "实训项目ID", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long projectId;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "指导教师ID", example = "3001")
    private Long teacherId;
}
