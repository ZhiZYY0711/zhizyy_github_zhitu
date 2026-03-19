package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业信息（平台视角只读）- enterprise_svc.enterprise_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "enterprise_svc", value = "enterprise_info")
public class EnterpriseInfo extends BaseEntity {

    private Long tenantId;
    private String enterpriseName;
    private String enterpriseCode;
    private String industry;
    private String city;
    private String contactName;
    private String contactPhone;
    /** 0=待审核 1=通过 2=拒绝 */
    private Integer auditStatus;
    private String auditRemark;
    private Integer status;
}
