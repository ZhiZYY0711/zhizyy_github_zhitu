package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求职申请实体 - internship_svc.job_application
 */
@Data
@TableName(schema = "internship_svc", value = "job_application")
public class JobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long studentId;
    private String resumeUrl;
    private String coverLetter;
    /** 0=待处理 1=面试 2=Offer 3=拒绝 4=录用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime appliedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
