package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建岗位请求
 */
@Data
@Schema(description = "创建岗位请求")
public class CreateJobRequest {
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
}
