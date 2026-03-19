package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实习证明实体 - internship_svc.internship_certificate
 */
@Data
@TableName(schema = "internship_svc", value = "internship_certificate")
public class InternshipCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long internshipId;
    private Long studentId;
    private Long enterpriseId;
    private String certNo;
    private String certUrl;
    private Long issuedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime issuedAt;
}
