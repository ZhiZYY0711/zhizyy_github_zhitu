package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 面试安排实体 - enterprise_svc.interview_schedule
 */
@Data
@TableName(schema = "enterprise_svc", value = "interview_schedule")
public class InterviewSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long studentId;

    private Long enterpriseId;

    private OffsetDateTime interviewTime;

    private String location;

    private Long interviewerId;

    /** phone / video / onsite */
    private String interviewType;

    /** 0=scheduled, 1=completed, 2=cancelled */
    private Integer status;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
