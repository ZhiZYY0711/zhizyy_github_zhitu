package com.zhitu.platform.dto;

import java.time.OffsetDateTime;

/**
 * 操作日志 DTO
 * Requirements: 39.4, 39.5
 */
public record OperationLogDTO(
    Long id,
    Long userId,
    String userName,
    Long tenantId,
    String module,
    String operation,
    String requestParams,
    Integer responseStatus,
    String result,
    String ipAddress,
    String userAgent,
    Integer executionTime,
    OffsetDateTime createdAt
) {
}
