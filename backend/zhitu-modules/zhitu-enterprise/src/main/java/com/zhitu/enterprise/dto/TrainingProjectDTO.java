package com.zhitu.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 实训项目DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProjectDTO {

    private Long id;
    private String name;
    private String description;
    private Integer difficulty;
    private List<String> techStack;
    private Integer maxTeams;
    private Integer currentTeams;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private OffsetDateTime createdAt;
}
