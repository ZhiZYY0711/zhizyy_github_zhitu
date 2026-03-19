package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实训排期计划 - training_svc.training_plan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "training_svc", value = "training_plan")
public class TrainingPlan extends BaseEntity {

    private Long tenantId;
    private Long projectId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long teacherId;
    private Integer status;
}
