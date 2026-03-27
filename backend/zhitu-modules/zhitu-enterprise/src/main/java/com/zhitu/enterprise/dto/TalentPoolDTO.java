package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "人才库信息")
public class TalentPoolDTO {
    @Schema(description = "人才库记录ID", example = "1")
    private Long id;
    
    @Schema(description = "学生ID", example = "2001")
    private Long studentId;
    
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;
    
    @Schema(description = "学号", example = "2021001")
    private String studentNo;
    
    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;
    
    @Schema(description = "年级", example = "2021级")
    private String grade;
    
    @Schema(description = "技能标签（JSON数组）", example = "[\"Java\", \"Spring Boot\", \"MySQL\"]")
    private String skills;
    
    @Schema(description = "备注", example = "技术能力强，适合Java开发岗位")
    private String remark;
    
    @Schema(description = "收藏时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime collectedAt;
}
