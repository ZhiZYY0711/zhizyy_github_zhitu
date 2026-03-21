package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 操作日志实体 - platform_service.operation_log
 */
@Data
@TableName(schema = "platform_service", value = "operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private Long tenantId;

    /** student / enterprise / college / platform */
    private String module;

    /** create_job / audit_enterprise / etc */
    private String operation;

    private String requestParams;

    private Integer responseStatus;

    /** success / failure */
    private String result;

    private String ipAddress;

    private String userAgent;

    /** milliseconds */
    private Integer executionTime;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
