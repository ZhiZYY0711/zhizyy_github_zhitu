package com.zhitu.enterprise.dto;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 安排面试请求
 */
@Data
public class ScheduleInterviewRequest {
    private Long applicationId;
    private Long studentId;
    private OffsetDateTime interviewTime;
    private String location;
    private Long interviewerId;
    private String interviewType; // phone, video, onsite, technical
    private String notes;
}
