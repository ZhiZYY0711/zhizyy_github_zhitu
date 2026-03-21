package com.zhitu.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 服务健康状态 DTO
 * Requirements: 29.4, 29.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDTO {

    /** 服务名称 */
    private String name;

    /** 状态：healthy, degraded, down */
    private String status;

    /** 响应时间（毫秒） */
    private Integer responseTime;

    /** 错误率（百分比） */
    private BigDecimal errorRate;

    /** CPU 使用率（百分比） */
    private BigDecimal cpuUsage;

    /** 内存使用率（百分比） */
    private BigDecimal memoryUsage;
}
