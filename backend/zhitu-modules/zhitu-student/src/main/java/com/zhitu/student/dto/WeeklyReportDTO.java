package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周报DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "周报信息")
public class WeeklyReportDTO {
    
    @Schema(description = "周报ID", example = "1")
    private Long id;
    
    @Schema(description = "实习ID", example = "2001")
    private Long internshipId;
    
    @Schema(description = "周开始日期", example = "2024-01-15")
    private LocalDate weekStart;
    
    @Schema(description = "周结束日期", example = "2024-01-21")
    private LocalDate weekEnd;
    
    @Schema(description = "周报内容", example = "本周完成了用户模块的开发")
    private String content;
    
    @Schema(description = "工作时长（小时）", example = "40.0")
    private BigDecimal workHours;
    
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;
    
    @Schema(description = "批阅意见", example = "表现良好，继续保持")
    private String reviewComment;
    
    @Schema(description = "批阅人姓名", example = "李导师")
    private String reviewerName;
    
    @Schema(description = "批阅时间", example = "2024-01-22T14:30:00")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "创建时间", example = "2024-01-21T18:00:00")
    private LocalDateTime createdAt;
}
