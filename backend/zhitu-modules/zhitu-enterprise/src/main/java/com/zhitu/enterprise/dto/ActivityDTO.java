package com.zhitu.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 企业活动动态DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动类型: application, interview, report_submitted, evaluation
     */
    private String activityType;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 关联类型: job, application, intern
     */
    private String refType;

    /**
     * 关联ID
     */
    private Long refId;

    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
