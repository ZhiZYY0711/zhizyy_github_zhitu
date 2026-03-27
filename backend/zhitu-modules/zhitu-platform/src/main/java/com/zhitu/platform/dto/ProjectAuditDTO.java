package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 项目审核DTO
 * Project Audit DTO
 * 
 * Requirements: 32.7
 * - Include project name, creator, submission date, and description in audit record
 */
@Schema(description = "项目审核信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAuditDTO {
    
    /** Project ID */
    @Schema(description = "项目ID", example = "1")
    private Long id;
    
    /** Project name */
    @Schema(description = "项目名称", example = "智慧校园管理系统")
    private String projectName;
    
    /** Project description */
    @Schema(description = "项目描述", example = "基于微服务架构的智慧校园管理平台")
    private String description;
    
    /** Creator enterprise ID */
    @Schema(description = "创建企业ID", example = "100")
    private Long enterpriseId;
    
    /** Creator enterprise name */
    @Schema(description = "创建企业名称", example = "阿里巴巴集团")
    private String enterpriseName;
    
    /** Technology stack */
    @Schema(description = "技术栈", example = "Java, Spring Boot, Vue.js")
    private String techStack;
    
    /** Industry */
    @Schema(description = "行业", example = "教育")
    private String industry;
    
    /** Start date */
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    /** End date */
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    /** Audit status: 0=pending, 1=passed, 2=rejected */
    @Schema(description = "审核状态", example = "0", allowableValues = {"0", "1", "2"})
    private Integer auditStatus;
    
    /** Quality rating (1-5, only set on approval) */
    @Schema(description = "质量评分（1-5分，审核通过时设置）", example = "4", minimum = "1", maximum = "5")
    private Integer qualityRating;
    
    /** Submission date */
    @Schema(description = "提交日期", example = "2024-03-15T10:30:00+08:00")
    private OffsetDateTime submissionDate;
}
