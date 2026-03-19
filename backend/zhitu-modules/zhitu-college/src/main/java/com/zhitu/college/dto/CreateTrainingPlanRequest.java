package com.zhitu.college.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTrainingPlanRequest {
    @NotBlank
    private String planName;
    @NotNull
    private Long projectId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long teacherId;
}
