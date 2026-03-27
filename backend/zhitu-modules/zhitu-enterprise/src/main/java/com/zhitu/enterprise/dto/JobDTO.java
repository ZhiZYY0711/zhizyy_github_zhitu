package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 岗位DTO - 用于企业端岗位管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "岗位信息")
public class JobDTO {
    @Schema(description = "岗位ID", example = "1")
    private Long id;
    
    @Schema(description = "岗位标题", example = "Java开发实习生")
    private String jobTitle;
    
    @Schema(description = "岗位类型", example = "技术类")
    private String jobType;
    
    @Schema(description = "岗位描述", example = "负责后端开发工作")
    private String description;
    
    @Schema(description = "岗位要求", example = "熟悉Java、Spring Boot框架")
    private String requirements;
    
    @Schema(description = "技术栈", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
    private List<String> techStack;
    
    @Schema(description = "工作城市", example = "北京")
    private String city;
    
    @Schema(description = "最低薪资", example = "3000")
    private Integer salaryMin;
    
    @Schema(description = "最高薪资", example = "5000")
    private Integer salaryMax;
    
    @Schema(description = "招聘人数", example = "5")
    private Integer headcount;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
    
    @Schema(description = "岗位状态：1=招募中 0=已关闭", example = "1", allowableValues = {"0", "1"})
    private Integer status;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime createdAt;
}
