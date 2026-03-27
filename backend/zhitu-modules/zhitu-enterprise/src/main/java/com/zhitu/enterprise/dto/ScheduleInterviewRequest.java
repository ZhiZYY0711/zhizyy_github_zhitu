package com.zhitu.enterprise.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 安排面试请求
 */
@Data
@Schema(description = "安排面试请求")
public class ScheduleInterviewRequest {
    @Schema(description = "申请ID", example = "1")
    private Long applicationId;
    
    @Schema(description = "学生ID", example = "2001")
    private Long studentId;
    
    @Schema(description = "面试时间", example = "2024-01-20T14:00:00+08:00")
    private OffsetDateTime interviewTime;
    
    @Schema(description = "面试地点", example = "公司会议室A")
    private String location;
    
    @Schema(description = "面试官ID", example = "3001")
    private Long interviewerId;
    
    @Schema(description = "面试类型", example = "technical", allowableValues = {"phone", "video", "onsite", "technical"})
    private String interviewType;
    
    @Schema(description = "备注", example = "请提前准备技术问题")
    private String notes;
}
