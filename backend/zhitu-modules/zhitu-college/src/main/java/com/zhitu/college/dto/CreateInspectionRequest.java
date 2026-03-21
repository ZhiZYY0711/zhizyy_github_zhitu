package com.zhitu.college.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Create Inspection Request DTO
 */
@Data
public class CreateInspectionRequest {
    
    @NotNull(message = "实习ID不能为空")
    private Long internshipId;
    
    @NotNull(message = "巡查日期不能为空")
    private LocalDate inspectionDate;
    
    private String location;
    
    private String findings;
    
    private String issues;
    
    private String recommendations;
}
