package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实习岗位DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实习岗位信息")
public class InternshipJobDTO {
    
    @Schema(description = "岗位ID", example = "1")
    private Long id;
    
    @Schema(description = "岗位名称", example = "Java后端开发实习生")
    private String jobTitle;
    
    @Schema(description = "企业名称", example = "阿里巴巴")
    private String enterpriseName;
    
    @Schema(description = "岗位类型", example = "技术类")
    private String jobType;
    
    @Schema(description = "岗位描述", example = "负责后端服务开发")
    private String description;
    
    @Schema(description = "岗位要求", example = "熟悉Java、Spring Boot")
    private String requirements;
    
    @Schema(description = "技术栈", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
    private List<String> techStack;
    
    @Schema(description = "工作城市", example = "杭州")
    private String city;
    
    @Schema(description = "最低薪资", example = "3000.00")
    private BigDecimal salaryMin;
    
    @Schema(description = "最高薪资", example = "5000.00")
    private BigDecimal salaryMax;
    
    @Schema(description = "招聘人数", example = "5")
    private Integer headcount;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "状态", example = "1", allowableValues = {"1", "2"})
    private Integer status;
    
    @Schema(description = "申请状态", example = "applied", 
            allowableValues = {"applied", "interviewed", "offered"})
    private String applicationStatus;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
