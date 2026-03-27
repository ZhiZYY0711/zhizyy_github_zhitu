package com.zhitu.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

/**
 * 安全日志 DTO
 * Requirements: 40.4, 40.5
 */
@Schema(description = "安全日志")
public record SecurityLogDTO(
    @Schema(description = "日志ID", example = "1")
    Long id,
    
    @Schema(description = "日志级别", example = "WARNING", allowableValues = {"INFO", "WARNING", "ERROR", "CRITICAL"})
    String level,
    
    @Schema(description = "事件类型", example = "LOGIN_FAILED")
    String eventType,
    
    @Schema(description = "用户ID", example = "100")
    Long userId,
    
    @Schema(description = "IP地址", example = "192.168.1.100")
    String ipAddress,
    
    @Schema(description = "描述", example = "用户登录失败")
    String description,
    
    @Schema(description = "详细信息", example = "{\"reason\":\"密码错误\"}")
    String details,
    
    @Schema(description = "创建时间", example = "2024-03-15T10:30:00+08:00")
    OffsetDateTime createdAt
) {
}
