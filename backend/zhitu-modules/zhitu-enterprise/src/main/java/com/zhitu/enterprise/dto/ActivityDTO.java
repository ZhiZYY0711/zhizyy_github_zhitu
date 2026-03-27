package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "企业活动动态信息")
public class ActivityDTO {
    /**
     * 活动ID
     */
    @Schema(description = "活动ID", example = "1")
    private Long id;

    /**
     * 活动类型: application, interview, report_submitted, evaluation
     */
    @Schema(description = "活动类型", example = "application", allowableValues = {"application", "interview", "report_submitted", "evaluation"})
    private String activityType;

    /**
     * 活动描述
     */
    @Schema(description = "活动描述", example = "学生张三申请了Java开发实习岗位")
    private String description;

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
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00+08:00")
    private OffsetDateTime createdAt;
}
