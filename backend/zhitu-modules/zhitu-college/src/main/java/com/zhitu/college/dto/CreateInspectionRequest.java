package com.zhitu.college.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Create Inspection Request DTO
 */
@Schema(description = "创建巡查记录请求")
@Data
public class CreateInspectionRequest {
    
    @Schema(description = "实习记录ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实习ID不能为空")
    private Long internshipId;
    
    @Schema(description = "巡查日期", example = "2024-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "巡查日期不能为空")
    private LocalDate inspectionDate;
    
    @Schema(description = "巡查地点", example = "某某科技有限公司")
    private String location;
    
    @Schema(description = "巡查发现", example = "学生工作状态良好，企业管理规范")
    private String findings;
    
    @Schema(description = "存在问题", example = "部分学生加班时间较长")
    private String issues;
    
    @Schema(description = "改进建议", example = "建议企业合理安排工作时间")
    private String recommendations;
}
