package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 预警记录实体 - growth_svc.warning_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "growth_svc", value = "warning_record")
public class WarningRecord extends BaseEntity {

    private Long tenantId;
    private Long studentId;
    /** attendance / report / evaluation */
    private String warningType;
    /** 1=轻微 2=一般 3=严重 */
    private Integer warningLevel;
    private String description;
    /** 0=待处理 1=已干预 2=已关闭 */
    private Integer status;
    private String interveneNote;
    private Long intervenedBy;
    private LocalDateTime intervenedAt;
}
