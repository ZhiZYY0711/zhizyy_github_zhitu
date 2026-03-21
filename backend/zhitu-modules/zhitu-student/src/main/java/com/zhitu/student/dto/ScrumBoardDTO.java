package com.zhitu.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目看板DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrumBoardDTO {
    private List<TaskItemDTO> todo;
    private List<TaskItemDTO> inProgress;
    private List<TaskItemDTO> done;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskItemDTO {
        private Long id;
        private String title;
        private String description;
        private String assigneeName;
        private Long assigneeId;
        private Integer priority;
        private Integer storyPoints;
    }
}
