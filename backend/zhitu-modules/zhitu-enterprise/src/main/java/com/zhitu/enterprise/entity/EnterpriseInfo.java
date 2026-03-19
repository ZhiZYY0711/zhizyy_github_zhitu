package com.zhitu.enterprise.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业信息实体 - enterprise_svc.enterprise_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "enterprise_svc", value = "enterprise_info")
public class EnterpriseInfo extends BaseEntity {

    private Long tenantId;
    private String enterpriseName;
    private String enterpriseCode;   // 统一社会信用代码
    private String industry;
    private String scale;
    private String province;
    private String city;
    private String address;
    private String logoUrl;
    private String website;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    /** 0=待审核 1=通过 2=拒绝 */
    private Integer auditStatus;
    private String auditRemark;
    /** 1=正常 0=禁用 */
    private Integer status;
}
