package com.zhitu.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 服务健康监控实体 - platform_service.service_health
 */
@Data
@TableName(schema = "platform_service", value = "service_health")
public class ServiceHealth {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** zhitu-student / zhitu-enterprise / etc */
    private String serviceName;

    /** healthy / degraded / down */
    private String status;

    /** milliseconds */
    private Integer responseTime;

    /** percentage */
    private BigDecimal errorRate;

    /** percentage */
    private BigDecimal cpuUsage;

    /** percentage */
    private BigDecimal memoryUsage;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime checkedAt;
}
