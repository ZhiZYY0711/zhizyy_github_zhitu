package com.zhitu.enterprise.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建岗位请求
 */
@Data
public class CreateJobRequest {
    private String jobTitle;
    private String jobType;
    private String description;
    private String requirements;
    private List<String> techStack;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer headcount;
    private LocalDate startDate;
    private LocalDate endDate;
}
