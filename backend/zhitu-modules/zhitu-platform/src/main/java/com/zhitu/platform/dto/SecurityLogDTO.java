package com.zhitu.platform.dto;

import java.time.OffsetDateTime;

/**
 * 安全日志 DTO
 * Requirements: 40.4, 40.5
 */
public record SecurityLogDTO(
    Long id,
    String level,
    String eventType,
    Long userId,
    String ipAddress,
    String description,
    String details,
    OffsetDateTime createdAt
) {
}
