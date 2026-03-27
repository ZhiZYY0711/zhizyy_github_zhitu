package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徽章DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "徽章信息")
public class BadgeDTO {
    
    @Schema(description = "徽章ID", example = "1")
    private Long id;
    
    @Schema(description = "类型", example = "badge")
    private String type;
    
    @Schema(description = "徽章名称", example = "优秀学员")
    private String name;
    
    @Schema(description = "颁发日期", example = "2024-01-15")
    private LocalDate issueDate;
    
    @Schema(description = "徽章图片URL", example = "https://example.com/badge.png")
    private String imageUrl;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
