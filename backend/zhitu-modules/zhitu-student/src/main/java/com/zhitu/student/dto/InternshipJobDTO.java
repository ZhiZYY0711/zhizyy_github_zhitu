package com.zhitu.student.dto;

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
public class InternshipJobDTO {
    private Long id;
    private String jobTitle;
    private String enterpriseName;
    private String jobType;
    private String description;
    private String requirements;
    private List<String> techStack;
    private String city;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Integer headcount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 1=开放 2=关闭
    private String applicationStatus; // null=未申请, "applied"=已申请, "interviewed"=已面试, "offered"=已录用
    private LocalDateTime createdAt;
}
