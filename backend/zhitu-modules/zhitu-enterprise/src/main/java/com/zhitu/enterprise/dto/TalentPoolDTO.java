package com.zhitu.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 人才库DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TalentPoolDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String major;
    private String grade;
    private String skills; // JSON array of skill tags
    private String remark;
    private OffsetDateTime collectedAt;
}
