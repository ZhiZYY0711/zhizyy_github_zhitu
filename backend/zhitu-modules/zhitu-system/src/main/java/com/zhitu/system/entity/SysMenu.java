package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_menu")
@Schema(description = "系统菜单实体")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    @Schema(description = "菜单ID", example = "1")
    private Long id;

    @Schema(description = "父菜单ID，0表示根菜单", example = "0")
    private Long parentId;

    @Schema(description = "菜单名称", example = "用户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;

    @Schema(description = "菜单路径", example = "/system/user")
    private String menuPath;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "菜单类型：1-目录，2-菜单，3-按钮", example = "2")
    private Integer menuType;

    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

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
