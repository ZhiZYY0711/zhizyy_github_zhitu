package com.zhitu.platform.entity;

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
    /** 0=待审核 1=通过 2=拒绝 */
    private Integer auditStatus;
    /** 1=招募中 2=进行中 3=已结束 */
    private Integer status;
}
