package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "发送Offer请求")
public class SendOfferRequest {
    @NotNull
    @Schema(description = "申请ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long applicationId;
    
    @Schema(description = "薪资", example = "4000")
    private Integer salary;
    
    @Schema(description = "开始日期", example = "2024-03-01")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2024-06-30")
    private LocalDate endDate;
}
