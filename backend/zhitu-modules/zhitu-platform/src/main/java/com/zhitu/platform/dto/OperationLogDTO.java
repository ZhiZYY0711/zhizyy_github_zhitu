package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 操作日志 DTO
 * Requirements: 39.4, 39.5
 */
@Schema(description = "操作日志")
public record OperationLogDTO(
    @Schema(description = "日志ID", example = "1")
    Long id,
    
    @Schema(description = "用户ID", example = "100")
    Long userId,
    
    @Schema(description = "用户名", example = "张三")
    String userName,
    
    @Schema(description = "租户ID", example = "1")
    Long tenantId,
    
    @Schema(description = "模块", example = "用户管理")
    String module,
    
    @Schema(description = "操作", example = "创建用户")
    String operation,
    
    @Schema(description = "请求参数", example = "{\"username\":\"test\"}")
    String requestParams,
    
    @Schema(description = "响应状态码", example = "200")
    Integer responseStatus,
    
    @Schema(description = "操作结果", example = "成功")
    String result,
    
    @Schema(description = "IP地址", example = "192.168.1.100")
    String ipAddress,
    
    @Schema(description = "用户代理", example = "Mozilla/5.0")
    String userAgent,
    
    @Schema(description = "执行时间（毫秒）", example = "150")
    Integer executionTime,
    
    @Schema(description = "创建时间", example = "2024-03-15T10:30:00+08:00")
    OffsetDateTime createdAt
) {
}
