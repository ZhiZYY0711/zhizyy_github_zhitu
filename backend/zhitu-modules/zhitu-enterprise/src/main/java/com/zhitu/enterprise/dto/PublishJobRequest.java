package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "发布岗位请求")
public class PublishJobRequest {
    @NotBlank
    @Schema(description = "岗位标题", example = "Java开发实习生", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobTitle;
    
    @Schema(description = "岗位描述", example = "负责后端开发工作")
    private String description;
    
    @Schema(description = "岗位要求", example = "熟悉Java、Spring Boot框架")
    private String requirements;
    
    @Schema(description = "技术栈", example = "Java,Spring Boot,MySQL")
    private String techStack;
    
    @Schema(description = "所属行业", example = "互联网")
    private String industry;
    
    @Schema(description = "工作城市", example = "北京")
    private String city;
    
    @Schema(description = "最低薪资", example = "3000")
    private Integer salaryMin;
    
    @Schema(description = "最高薪资", example = "5000")
    private Integer salaryMax;
    
    @NotNull
    @Schema(description = "招聘人数", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer headcount;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
}
