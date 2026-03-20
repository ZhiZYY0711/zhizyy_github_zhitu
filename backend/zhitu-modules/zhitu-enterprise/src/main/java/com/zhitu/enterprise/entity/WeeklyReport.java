package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习周报实体 - internship_svc.weekly_report
 */
@Data
@TableName(schema = "internship_svc", value = "weekly_report")
public class WeeklyReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long internshipId;
    private Long studentId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String content;
    private BigDecimal workHours;
    /** 0=草稿 1=已提交 2=已批阅 */
    private Integer status;
    private String reviewComment;
    private Long reviewedBy;
    private OffsetDateTime reviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
