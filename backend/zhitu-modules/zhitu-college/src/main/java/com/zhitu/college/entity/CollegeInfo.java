package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 高校信息实体 - college_svc.college_info
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "college_svc", value = "college_info")
public class CollegeInfo extends BaseEntity {

    private Long tenantId;
    private String collegeName;
    private String collegeCode;
    private String province;
    private String city;
    private String address;
    private String logoUrl;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    /** 1=普通 2=重点 3=战略 */
    private Integer cooperationLevel;
    private Integer status;
}
