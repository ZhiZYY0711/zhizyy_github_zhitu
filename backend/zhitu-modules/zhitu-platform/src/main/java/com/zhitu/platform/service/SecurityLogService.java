package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.SecurityLogDTO;
import com.zhitu.platform.entity.SecurityLog;
import com.zhitu.platform.mapper.SecurityLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全日志服务
 * Requirements: 40.1-40.7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogMapper securityLogMapper;

    /**
     * 记录认证失败事件
     * Requirements: 40.4, 40.5
     * 
     * @param userId 用户ID（可能为null）
     * @param ipAddress IP地址
     * @param description 描述信息
     * @param details 详细信息（JSON格式）
     */
    @Async("asyncExecutor")
    public void logAuthenticationFailure(Long userId, String ipAddress, String description, String details) {
        logSecurityEvent("warning", "login_failed", userId, ipAddress, description, details);
    }

    /**
     * 记录权限拒绝事件
     * Requirements: 40.4, 40.5
     * 
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param description 描述信息
     * @param details 详细信息（JSON格式）
     */
    @Async("asyncExecutor")
    public void logPermissionDenied(Long userId, String ipAddress, String description, String details) {
        logSecurityEvent("warning", "permission_denied", userId, ipAddress, description, details);
    }

    /**
     * 记录可疑活动事件
     * Requirements: 40.4, 40.5
     * 
     * @param userId 用户ID（可能为null）
     * @param ipAddress IP地址
     * @param description 描述信息
     * @param details 详细信息（JSON格式）
     */
    @Async("asyncExecutor")
    public void logSuspiciousActivity(Long userId, String ipAddress, String description, String details) {
        logSecurityEvent("critical", "suspicious_activity", userId, ipAddress, description, details);
    }

    /**
     * 记录数据泄露尝试事件
     * Requirements: 40.4, 40.5
     * 
     * @param userId 用户ID（可能为null）
     * @param ipAddress IP地址
     * @param description 描述信息
     * @param details 详细信息（JSON格式）
     */
    @Async("asyncExecutor")
    public void logDataBreachAttempt(Long userId, String ipAddress, String description, String details) {
        logSecurityEvent("critical", "data_breach_attempt", userId, ipAddress, description, details);
    }

    /**
     * 记录通用安全事件
     * Requirements: 40.4, 40.5
     * 
     * @param level 日志级别（info/warning/critical）
     * @param eventType 事件类型
     * @param userId 用户ID（可能为null）
     * @param ipAddress IP地址
     * @param description 描述信息
     * @param details 详细信息（JSON格式）
     */
    @Async("asyncExecutor")
    public void logSecurityEvent(String level, String eventType, Long userId, String ipAddress, 
                                  String description, String details) {
        try {
            SecurityLog securityLog = new SecurityLog();
            securityLog.setLevel(level);
            securityLog.setEventType(eventType);
            securityLog.setUserId(userId);
            securityLog.setIpAddress(ipAddress);
            securityLog.setDescription(description);
            securityLog.setDetails(details);
            
            securityLogMapper.insert(securityLog);
            
            log.debug("Security log saved: level={}, eventType={}, userId={}, ipAddress={}", 
                     level, eventType, userId, ipAddress);
        } catch (Exception e) {
            log.error("Failed to save security log", e);
        }
    }

    /**
     * 查询安全日志列表（带过滤和分页）
     * Requirements: 40.1, 40.2, 40.3, 40.6
     * 
     * @param level 日志级别（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页的安全日志列表
     */
    public PageResult<SecurityLogDTO> getSecurityLogs(String level, int page, int size) {
        log.debug("Querying security logs: level={}, page={}, size={}", level, page, size);

        // 构建查询条件
        LambdaQueryWrapper<SecurityLog> query = new LambdaQueryWrapper<>();
        
        // 过滤条件
        if (level != null && !level.isEmpty()) {
            query.eq(SecurityLog::getLevel, level);
        }
        
        // 按时间倒序排序
        query.orderByDesc(SecurityLog::getCreatedAt);

        // 分页查询
        Page<SecurityLog> pageRequest = new Page<>(page, size);
        Page<SecurityLog> pageResult = securityLogMapper.selectPage(pageRequest, query);

        // 转换为 DTO
        List<SecurityLogDTO> dtoList = pageResult.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        log.debug("Found {} security logs (total: {})", dtoList.size(), pageResult.getTotal());

        return PageResult.of(
            pageResult.getTotal(),
            dtoList,
            (int) pageResult.getCurrent(),
            (int) pageResult.getSize()
        );
    }

    /**
     * 清理过期日志（保留180天）
     * Requirements: 40.7
     * 
     * @return 删除的日志数量
     */
    @Async("asyncExecutor")
    public void cleanupExpiredLogs() {
        try {
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(180);
            
            LambdaQueryWrapper<SecurityLog> query = new LambdaQueryWrapper<>();
            query.lt(SecurityLog::getCreatedAt, cutoffDate);
            
            int deleted = securityLogMapper.delete(query);
            
            log.info("Cleaned up {} expired security logs (older than {})", deleted, cutoffDate);
        } catch (Exception e) {
            log.error("Failed to cleanup expired security logs", e);
        }
    }

    // ── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 将 SecurityLog 实体转换为 DTO
     */
    private SecurityLogDTO toDTO(SecurityLog entity) {
        return new SecurityLogDTO(
            entity.getId(),
            entity.getLevel(),
            entity.getEventType(),
            entity.getUserId(),
            entity.getIpAddress(),
            entity.getDescription(),
            entity.getDetails(),
            entity.getCreatedAt()
        );
    }
}
