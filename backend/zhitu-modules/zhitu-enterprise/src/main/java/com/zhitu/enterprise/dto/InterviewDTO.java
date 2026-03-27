package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 面试响应DTO
 */
@Data
@Schema(description = "面试信息")
public class InterviewDTO {
    @Schema(description = "面试ID", example = "1")
    private Long id;
}
