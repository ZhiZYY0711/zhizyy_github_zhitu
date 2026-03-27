package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_role")
@Schema(description = "系统角色实体")
public class SysRole {

    @TableId(type = IdType.AUTO)
    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;

    @Schema(description = "角色编码", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    @Schema(description = "角色名称", example = "系统管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @Schema(description = "角色描述", example = "拥有系统所有权限")
    private String description;

    @Schema(description = "状态：1-启用，0-禁用", example = "1")
    private Integer status;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    @Schema(description = "是否删除", example = "false")
    private Boolean isDeleted;
}
