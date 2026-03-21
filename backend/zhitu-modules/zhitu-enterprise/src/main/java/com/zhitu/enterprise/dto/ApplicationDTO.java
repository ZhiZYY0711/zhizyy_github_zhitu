package com.zhitu.enterprise.dto;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 岗位申请DTO
 */
@Data
public class ApplicationDTO {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long studentId;
    private String studentName;
    private Integer status; // 0=待处理 1=面试 2=Offer 3=拒绝 4=录用
    private OffsetDateTime appliedAt;
}
