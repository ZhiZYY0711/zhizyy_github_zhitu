package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实习岗位实体 - internship_svc.internship_job
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "internship_svc", value = "internship_job")
public class InternshipJob extends BaseEntity {

    private Long enterpriseId;
    private String jobTitle;
    private String jobType;
    private String description;
    private String requirements;
    private String techStack;       // JSON 数组
    private String industry;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer headcount;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 1=招募中 0=已关闭 */
    private Integer status;
}
