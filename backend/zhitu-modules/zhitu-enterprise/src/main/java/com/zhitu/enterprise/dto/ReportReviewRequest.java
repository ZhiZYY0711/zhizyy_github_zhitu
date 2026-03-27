package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "周报批阅请求")
public class ReportReviewRequest {
    @NotBlank
    @Schema(description = "批阅意见", example = "本周工作完成良好，继续保持", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewComment;
}
