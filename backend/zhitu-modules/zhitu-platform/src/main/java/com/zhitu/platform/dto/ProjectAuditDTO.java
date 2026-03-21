package com.zhitu.platform.dto;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAuditDTO {
    
    /** Project ID */
    private Long id;
    
    /** Project name */
    private String projectName;
    
    /** Project description */
    private String description;
    
    /** Creator enterprise ID */
    private Long enterpriseId;
    
    /** Creator enterprise name */
    private String enterpriseName;
    
    /** Technology stack */
    private String techStack;
    
    /** Industry */
    private String industry;
    
    /** Start date */
    private LocalDate startDate;
    
    /** End date */
    private LocalDate endDate;
    
    /** Audit status: 0=pending, 1=passed, 2=rejected */
    private Integer auditStatus;
    
    /** Quality rating (1-5, only set on approval) */
    private Integer qualityRating;
    
    /** Submission date */
    private OffsetDateTime submissionDate;
}
