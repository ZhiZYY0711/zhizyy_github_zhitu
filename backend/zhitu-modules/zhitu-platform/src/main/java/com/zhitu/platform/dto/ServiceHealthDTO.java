package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 服务健康状态 DTO
 * Requirements: 29.4, 29.5
 */
@Schema(description = "服务健康状态")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDTO {

    /** 服务名称 */
    @Schema(description = "服务名称", example = "zhitu-auth")
    private String name;

    /** 状态：healthy, degraded, down */
    @Schema(description = "状态", example = "healthy", allowableValues = {"healthy", "degraded", "down"})
    private String status;

    /** 响应时间（毫秒） */
    @Schema(description = "响应时间（毫秒）", example = "150")
    private Integer responseTime;

    /** 错误率（百分比） */
    @Schema(description = "错误率（百分比）", example = "0.5")
    private BigDecimal errorRate;

    /** CPU 使用率（百分比） */
    @Schema(description = "CPU使用率（百分比）", example = "45.2")
    private BigDecimal cpuUsage;

    /** 内存使用率（百分比） */
    @Schema(description = "内存使用率（百分比）", example = "68.5")
    private BigDecimal memoryUsage;
}
