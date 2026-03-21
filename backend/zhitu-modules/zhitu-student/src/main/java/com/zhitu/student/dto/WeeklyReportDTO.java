package com.zhitu.student.dto;

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
public class WeeklyReportDTO {
    private Long id;
    private Long internshipId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String content;
    private BigDecimal workHours;
    private Integer status; // 0=草稿 1=已提交 2=已批阅
    private String reviewComment;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
