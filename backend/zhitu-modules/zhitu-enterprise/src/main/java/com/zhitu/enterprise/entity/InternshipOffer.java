package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Offer 实体 - internship_svc.internship_offer
 */
@Data
@TableName(schema = "internship_svc", value = "internship_offer")
public class InternshipOffer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long studentId;
    private Long enterpriseId;
    private Long jobId;
    private Integer salary;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 0=待确认 1=已接受 2=已拒绝 */
    private Integer status;
    /** 0=待审核 1=通过 2=拒绝 */
    private Integer collegeAudit;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
