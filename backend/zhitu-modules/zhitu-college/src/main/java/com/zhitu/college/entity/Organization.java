package com.zhitu.college.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.zhitu.common.core.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院/专业/班级组织树 - college_svc.organization
 */
@Schema(description = "组织架构实体（学院/专业/班级）")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(schema = "college_svc", value = "organization")
public class Organization extends BaseEntity {

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;
    
    @Schema(description = "父组织ID", example = "100")
    private Long parentId;
    
    @Schema(description = "组织类型：1-学院，2-专业，3-班级", example = "1")
    /** 1=学院 2=专业 3=班级 */
    private Integer orgType;
    
    @Schema(description = "组织名称", example = "计算机学院")
    private String orgName;
    
    @Schema(description = "组织代码", example = "CS001")
    private String orgCode;
    
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}
