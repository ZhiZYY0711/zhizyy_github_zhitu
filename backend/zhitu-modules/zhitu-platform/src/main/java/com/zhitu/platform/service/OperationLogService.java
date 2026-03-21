package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.platform.dto.OperationLogDTO;
import com.zhitu.platform.entity.OperationLog;
import com.zhitu.platform.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务
 * Requirements: 39.1-39.7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 异步保存操作日志
     * Requirements: 39.4, 39.5
     * 
     * @param operationLog 操作日志实体
     */
    @Async("asyncExecutor")
    public void saveLogAsync(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
            log.debug("Operation log saved: user={}, module={}, operation={}, result={}", 
                     operationLog.getUserId(), operationLog.getModule(), 
                     operationLog.getOperation(), operationLog.getResult());
        } catch (Exception e) {
            log.error("Failed to save operation log", e);
        }
    }

    /**
     * 查询操作日志列表（带过滤和分页）
     * Requirements: 39.1, 39.2, 39.3, 39.6
     * 
     * @param userId 用户ID（可选）
     * @param module 模块（可选）
     * @param result 结果（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页的操作日志列表
     */
    public PageResult<OperationLogDTO> getLogs(Long userId, String module, String result,
                                                OffsetDateTime startTime, OffsetDateTime endTime,
                                                int page, int size) {
        log.debug("Querying operation logs: userId={}, module={}, result={}, startTime={}, endTime={}, page={}, size={}",
                 userId, module, result, startTime, endTime, page, size);

        // 构建查询条件
        LambdaQueryWrapper<OperationLog> query = new LambdaQueryWrapper<>();
        
        // 过滤条件
        if (userId != null) {
            query.eq(OperationLog::getUserId, userId);
        }
        if (module != null && !module.isEmpty()) {
            query.eq(OperationLog::getModule, module);
        }
        if (result != null && !result.isEmpty()) {
            query.eq(OperationLog::getResult, result);
        }
        if (startTime != null) {
            query.ge(OperationLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            query.le(OperationLog::getCreatedAt, endTime);
        }
        
        // 按时间倒序排序
        query.orderByDesc(OperationLog::getCreatedAt);

        // 分页查询
        Page<OperationLog> pageRequest = new Page<>(page, size);
        Page<OperationLog> pageResult = operationLogMapper.selectPage(pageRequest, query);

        // 转换为 DTO
        List<OperationLogDTO> dtoList = pageResult.getRecords().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        log.debug("Found {} operation logs (total: {})", dtoList.size(), pageResult.getTotal());

        return PageResult.of(
            pageResult.getTotal(),
            dtoList,
            (int) pageResult.getCurrent(),
            (int) pageResult.getSize()
        );
    }

    /**
     * 清理过期日志（保留90天）
     * Requirements: 39.7
     * 
     * @return 删除的日志数量
     */
    @Async("asyncExecutor")
    public void cleanupExpiredLogs() {
        try {
            OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(90);
            
            LambdaQueryWrapper<OperationLog> query = new LambdaQueryWrapper<>();
            query.lt(OperationLog::getCreatedAt, cutoffDate);
            
            int deleted = operationLogMapper.delete(query);
            
            log.info("Cleaned up {} expired operation logs (older than {})", deleted, cutoffDate);
        } catch (Exception e) {
            log.error("Failed to cleanup expired operation logs", e);
        }
    }

    // ── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 将 OperationLog 实体转换为 DTO
     */
    private OperationLogDTO toDTO(OperationLog entity) {
        return new OperationLogDTO(
            entity.getId(),
            entity.getUserId(),
            entity.getUserName(),
            entity.getTenantId(),
            entity.getModule(),
            entity.getOperation(),
            entity.getRequestParams(),
            entity.getResponseStatus(),
            entity.getResult(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getExecutionTime(),
            entity.getCreatedAt()
        );
    }
}
