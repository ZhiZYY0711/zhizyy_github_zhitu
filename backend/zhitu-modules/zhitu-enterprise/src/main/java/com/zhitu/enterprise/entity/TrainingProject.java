package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 实训项目实体 - training_svc.training_project
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "training_svc", value = "training_project")
public class TrainingProject extends BaseEntity {

    private Long enterpriseId;
    private String projectName;
    private String description;
    private String techStack;
    private String industry;
    private Integer maxTeams;
    private Integer maxMembers;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer auditStatus;
    private Integer status;
}
