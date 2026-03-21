package com.zhitu.enterprise.dto;

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
public class JobDTO {
    private Long id;
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
    private Integer status; // 1=招募中 0=已关闭
    private OffsetDateTime createdAt;
}
