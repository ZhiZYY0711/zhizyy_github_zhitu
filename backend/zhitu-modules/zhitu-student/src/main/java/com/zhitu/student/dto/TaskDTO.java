package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 学生任务DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String taskType;
    private Long refId;
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private OffsetDateTime dueDate;
    private OffsetDateTime createdAt;
}
