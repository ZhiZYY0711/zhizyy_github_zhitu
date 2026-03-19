package com.zhitu.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportReviewRequest {
    @NotBlank
    private String reviewComment;
}
