package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业员工实体 - enterprise_svc.enterprise_staff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "enterprise_svc", value = "enterprise_staff")
public class EnterpriseStaff extends BaseEntity {

    private Long tenantId;
    private Long userId;
    private String department;
    private String position;
    private Boolean isMentor;
}
