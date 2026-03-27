package com.zhitu.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "项目看板数据")
public class ScrumBoardDTO {
    
    @Schema(description = "待办任务列表")
    private List<TaskItemDTO> todo;
    
    @Schema(description = "进行中任务列表")
    private List<TaskItemDTO> inProgress;
    
    @Schema(description = "已完成任务列表")
    private List<TaskItemDTO> done;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "任务条目")
    public static class TaskItemDTO {
        
        @Schema(description = "任务ID", example = "1")
        private Long id;
        
        @Schema(description = "任务标题", example = "实现用户登录功能")
        private String title;
        
        @Schema(description = "任务描述", example = "完成用户登录接口开发")
        private String description;
        
        @Schema(description = "负责人姓名", example = "张三")
        private String assigneeName;
        
        @Schema(description = "负责人ID", example = "1001")
        private Long assigneeId;
        
        @Schema(description = "优先级", example = "1")
        private Integer priority;
        
        @Schema(description = "故事点数", example = "5")
        private Integer storyPoints;
    }
}
