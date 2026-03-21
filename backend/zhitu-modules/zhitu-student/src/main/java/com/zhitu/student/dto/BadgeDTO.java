package com.zhitu.student.dto;

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
public class BadgeDTO {
    private Long id;
    private String type; // badge
    private String name;
    private LocalDate issueDate;
    private String imageUrl;
    private LocalDateTime createdAt;
}
