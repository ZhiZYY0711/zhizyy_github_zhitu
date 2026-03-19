package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院/专业/班级组织树 - college_svc.organization
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "college_svc", value = "organization")
public class Organization extends BaseEntity {

    private Long tenantId;
    private Long parentId;
    /** 1=学院 2=专业 3=班级 */
    private Integer orgType;
    private String orgName;
    private String orgCode;
    private Integer sortOrder;
}
