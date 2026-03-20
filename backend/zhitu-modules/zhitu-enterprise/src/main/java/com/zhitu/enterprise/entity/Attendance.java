package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 考勤记录实体 - internship_svc.attendance
 */
@Data
@TableName(schema = "internship_svc", value = "attendance")
public class Attendance {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long internshipId;
    private Long studentId;
    private OffsetDateTime clockInTime;
    private OffsetDateTime clockOutTime;
    private BigDecimal clockInLat;
    private BigDecimal clockInLng;
    /** 0=待审核 1=正常 2=异常 */
    private Integer status;
    private String auditRemark;
    private Long auditedBy;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
