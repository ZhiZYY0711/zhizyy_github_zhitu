package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_user")
@Schema(description = "系统用户实体")
public class SysUser {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;

    @Schema(description = "用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 50)
    private String username;

    @Schema(description = "密码哈希", hidden = true, maxLength = 255)
    private String passwordHash;

    @Schema(description = "手机号", example = "13800138000", pattern = "^1[3-9]\\d{9}$", minLength = 11, maxLength = 11)
    private String phone;

    @Schema(description = "角色：student/enterprise/college/platform", example = "platform")
    private String role;

    @Schema(description = "子角色：hr/mentor/admin / counselor/dean/admin", example = "admin")
    private String subRole;

    @Schema(description = "状态：1-正常，2-锁定，3-注销", example = "1", minimum = "1", maximum = "3")
    private Integer status;

    @Schema(description = "最后登录时间")
    private OffsetDateTime lastLoginAt;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    @Schema(description = "是否删除", example = "false")
    private Boolean isDeleted;
}
