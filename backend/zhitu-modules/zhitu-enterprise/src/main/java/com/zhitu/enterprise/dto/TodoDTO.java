package com.zhitu.enterprise.dto;

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
public class TodoDTO {
    /**
     * 待办事项ID
     */
    private Long id;

    /**
     * 待办类型: application_review, interview_schedule, report_review, evaluation_pending
     */
    private String todoType;

    /**
     * 关联类型: job, application, intern
     */
    private String refType;

    /**
     * 关联ID
     */
    private Long refId;

    /**
     * 待办标题
     */
    private String title;

    /**
     * 优先级: 1=low, 2=medium, 3=high
     */
    private Integer priority;

    /**
     * 截止日期
     */
    private OffsetDateTime dueDate;

    /**
     * 状态: 0=pending, 1=completed
     */
    private Integer status;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
