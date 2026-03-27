package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 高校信息实体 - college_svc.college_info
 */
@Schema(description = "高校信息实体")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "college_svc", value = "college_info")
public class CollegeInfo extends BaseEntity {

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;
    
    @Schema(description = "高校名称", example = "某某大学")
    private String collegeName;
    
    @Schema(description = "高校代码", example = "10001")
    private String collegeCode;
    
    @Schema(description = "省份", example = "广东省")
    private String province;
    
    @Schema(description = "城市", example = "深圳市")
    private String city;
    
    @Schema(description = "详细地址", example = "南山区学苑大道1088号")
    private String address;
    
    @Schema(description = "Logo URL", example = "https://example.com/logo.png")
    private String logoUrl;
    
    @Schema(description = "联系人姓名", example = "张老师")
    private String contactName;
    
    @Schema(description = "联系电话", example = "0755-12345678")
    private String contactPhone;
    
    @Schema(description = "联系邮箱", example = "contact@college.edu.cn")
    private String contactEmail;
    
    @Schema(description = "合作等级：1-普通，2-重点，3-战略", example = "2")
    /** 1=普通 2=重点 3=战略 */
    private Integer cooperationLevel;
    
    @Schema(description = "状态：1-正常，0-停用", example = "1")
    private Integer status;
}
