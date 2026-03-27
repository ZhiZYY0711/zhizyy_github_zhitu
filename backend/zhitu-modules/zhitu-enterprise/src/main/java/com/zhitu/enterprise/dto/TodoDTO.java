package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 企业待办事项DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业待办事项")
public class TodoDTO {
    /**
     * 待办事项ID
     */
    @Schema(description = "待办事项ID", example = "1")
    private Long id;

    /**
     * 待办类型: application_review, interview_schedule, report_review, evaluation_pending
     */
    @Schema(description = "待办类型", example = "application_review", allowableValues = {"application_review", "interview_schedule", "report_review", "evaluation_pending"})
    private String todoType;

    /**
     * 关联类型: job, application, intern
     */
    @Schema(description = "关联类型", example = "application", allowableValues = {"job", "application", "intern"})
    private String refType;

    /**
     * 关联ID
     */
    @Schema(description = "关联ID", example = "100")
    private Long refId;

    /**
     * 待办标题
     */
    @Schema(description = "待办标题", example = "审核张三的岗位申请")
    private String title;

    /**
     * 优先级: 1=low, 2=medium, 3=high
     */
    @Schema(description = "优先级：1=低 2=中 3=高", example = "2", allowableValues = {"1", "2", "3"})
    private Integer priority;

    /**
     * 截止日期
     */
    @Schema(description = "截止日期", example = "2024-01-20T18:00:00+08:00")
    private OffsetDateTime dueDate;

    /**
     * 状态: 0=pending, 1=completed
     */
    @Schema(description = "状态：0=待处理 1=已完成", example = "0", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime createdAt;
}
