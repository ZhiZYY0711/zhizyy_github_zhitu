package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 实习记录实体 - internship_svc.internship_record
 */
@Data
@TableName(schema = "internship_svc", value = "internship_record")
public class InternshipRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long enterpriseId;
    private Long jobId;
    private Long mentorId;
    private Long teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 1=实习中 2=已结束 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
