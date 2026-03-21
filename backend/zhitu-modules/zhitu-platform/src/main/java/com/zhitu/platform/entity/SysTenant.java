package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName(schema = "auth_center", value = "sys_tenant")
public class SysTenant {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;   // 0:平台 1:高校 2:企业
    private Integer status; // 0:待审核 1:正常 2:禁用
    private String config;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @TableLogic(value = "false", delval = "true")
    private Boolean isDeleted;
}
