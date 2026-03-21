package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 安全日志实体 - platform_service.security_log
 */
@Data
@TableName(schema = "platform_service", value = "security_log")
public class SecurityLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** info / warning / critical */
    private String level;

    /** login_failed / permission_denied / suspicious_activity / data_breach_attempt */
    private String eventType;

    private Long userId;

    private String ipAddress;

    private String description;

    /** JSON with additional context */
    private String details;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
