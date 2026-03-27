package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 学生任务DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生任务信息")
public class TaskDTO {

    @Schema(description = "任务ID", example = "1")
    private Long id;
    
    @Schema(description = "任务类型", example = "project")
    private String taskType;
    
    @Schema(description = "关联ID", example = "2001")
    private Long refId;
    
    @Schema(description = "任务标题", example = "完成项目文档")
    private String title;
    
    @Schema(description = "任务描述", example = "编写项目需求文档和设计文档")
    private String description;
    
    @Schema(description = "优先级", example = "1")
    private Integer priority;
    
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    @Schema(description = "截止日期", example = "2024-02-01T18:00:00+08:00")
    private OffsetDateTime dueDate;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime createdAt;
}
