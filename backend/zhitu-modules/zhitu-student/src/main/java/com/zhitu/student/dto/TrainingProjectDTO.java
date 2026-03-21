package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实训项目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProjectDTO {
    private Long id;
    private String projectName;
    private String description;
    private List<String> techStack;
    private String industry;
    private Integer maxTeams;
    private Integer maxMembers;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 1=招募中 2=进行中 3=已结束
    private String enrollmentStatus; // null=未报名, "enrolled"=已报名
    private LocalDateTime createdAt;
}
