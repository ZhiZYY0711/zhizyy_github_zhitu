package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实训项目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实训项目信息")
public class TrainingProjectDTO {
    
    @Schema(description = "项目ID", example = "1")
    private Long id;
    
    @Schema(description = "项目名称", example = "智慧校园管理系统")
    private String projectName;
    
    @Schema(description = "项目描述", example = "基于Spring Cloud的微服务架构项目")
    private String description;
    
    @Schema(description = "技术栈", example = "[\"Java\", \"Spring Cloud\", \"Vue.js\"]")
    private List<String> techStack;
    
    @Schema(description = "行业领域", example = "教育")
    private String industry;
    
    @Schema(description = "最大团队数", example = "10")
    private Integer maxTeams;
    
    @Schema(description = "每队最大人数", example = "5")
    private Integer maxMembers;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "状态", example = "1", allowableValues = {"1", "2", "3"})
    private Integer status;
    
    @Schema(description = "报名状态", example = "enrolled")
    private String enrollmentStatus;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
