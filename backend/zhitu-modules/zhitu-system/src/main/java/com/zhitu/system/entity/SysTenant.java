package com.zhitu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_tenant")
@Schema(description = "系统租户实体")
public class SysTenant {

    @TableId(type = IdType.AUTO)
    @Schema(description = "租户ID", example = "1")
    private Long id;

    @Schema(description = "租户名称", example = "清华大学", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "租户类型：0-平台，1-高校，2-企业", example = "1")
    private Integer type;

    @Schema(description = "状态：0-待审核，1-正常，2-禁用", example = "1")
    private Integer status;

    @Schema(description = "租户配置（JSON格式）", example = "{\"maxUsers\": 1000}")
    private String config;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @TableLogic(value = "false", delval = "true")
    @Schema(description = "是否删除", example = "false")
    private Boolean isDeleted;
}
