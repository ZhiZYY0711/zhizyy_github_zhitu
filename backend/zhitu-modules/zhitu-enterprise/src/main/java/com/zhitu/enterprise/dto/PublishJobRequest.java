package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PublishJobRequest {
    @NotBlank
    private String jobTitle;
    private String description;
    private String requirements;
    private String techStack;
    private String industry;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    @NotNull
    private Integer headcount;
    private LocalDate startDate;
    private LocalDate endDate;
}
