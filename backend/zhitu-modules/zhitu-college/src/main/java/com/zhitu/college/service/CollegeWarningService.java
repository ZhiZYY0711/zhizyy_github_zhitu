package com.zhitu.college.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.WarningRecord;
import com.zhitu.college.mapper.WarningRecordMapper;
import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.redis.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * College Warning Service
 * Handles warning records, statistics, and interventions
 * Requirements: 27.1-27.8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeWarningService {

    private final WarningRecordMapper warningRecordMapper;
    private final CacheService cacheService;

    /**
     * Get warnings with filtering and pagination
     * Requirements: 27.1, 27.4
     */
    public IPage<WarningRecord> getWarnings(Integer level, String type, Integer status, 
                                            Integer page, Integer size) {
        Long tenantId = UserContext.getTenantId();
        
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<WarningRecord>()
            .eq(WarningRecord::getTenantId, tenantId)
            .orderByDesc(WarningRecord::getCreatedAt);
        
        // Apply filters
        if (level != null) {
            wrapper.eq(WarningRecord::getWarningLevel, level);
        }
        
        if (type != null && !type.isBlank()) {
            wrapper.eq(WarningRecord::getWarningType, type);
        }
        
        if (status != null) {
            wrapper.eq(WarningRecord::getStatus, status);
        }
        
        return warningRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * Get warning statistics
     * Requirements: 27.2
     * Cache: 10 minutes TTL
     */
    public Map<String, Object> getWarningStats() {
        Long tenantId = UserContext.getTenantId();
        String cacheKey = "college:warnings:stats:" + tenantId;
        
        return cacheService.getOrSet(cacheKey, 600, () -> {
            Map<String, Object> stats = new HashMap<>();
            
            // Total count
            long totalCount = warningRecordMapper.selectCount(
                new LambdaQueryWrapper<WarningRecord>()
                    .eq(WarningRecord::getTenantId, tenantId)
            );
            stats.put("totalCount", totalCount);
            
            // By level (1=low, 2=medium, 3=high/critical)
            Map<String, Long> byLevel = new HashMap<>();
            byLevel.put("low", countByLevel(tenantId, 1));
            byLevel.put("medium", countByLevel(tenantId, 2));
            byLevel.put("high", countByLevel(tenantId, 3));
            stats.put("byLevel", byLevel);
            
            // By type
            Map<String, Long> byType = new HashMap<>();
            byType.put("attendance", countByType(tenantId, "attendance"));
            byType.put("report", countByType(tenantId, "report"));
            byType.put("evaluation", countByType(tenantId, "evaluation"));
            stats.put("byType", byType);
            
            // By status (0=pending, 1=intervened, 2=closed)
            Map<String, Long> byStatus = new HashMap<>();
            byStatus.put("pending", countByStatus(tenantId, 0));
            byStatus.put("intervened", countByStatus(tenantId, 1));
            stats.put("byStatus", byStatus);
            
            log.debug("Warning stats calculated for tenant: {}", tenantId);
            return stats;
        });
    }

    /**
     * Record intervention for a warning
     * Requirements: 27.3, 27.7, 27.8
     */
    @Transactional
    public void intervene(Long id, InterveneRequest request) {
        Long userId = UserContext.getUserId();
        Long tenantId = UserContext.getTenantId();
        
        // Validate warning exists and belongs to tenant
        WarningRecord record = warningRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("预警记录不存在");
        }
        
        if (!record.getTenantId().equals(tenantId)) {
            throw new RuntimeException("无权操作此预警记录");
        }
        
        if (record.getStatus() != 0) {
            throw new RuntimeException("该预警已处理");
        }
        
        // Update warning record
        record.setStatus(1); // Set to "intervened"
        record.setInterveneNote(request.getInterveneNote());
        record.setIntervenedBy(userId);
        record.setIntervenedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        
        warningRecordMapper.updateById(record);
        
        // Invalidate cache
        cacheService.invalidate("college:warnings:stats:" + tenantId);
        
        log.info("Warning intervention recorded: id={}, userId={}, tenantId={}", 
            id, userId, tenantId);
    }

    // Helper methods for statistics

    private long countByLevel(Long tenantId, Integer level) {
        return warningRecordMapper.selectCount(
            new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getTenantId, tenantId)
                .eq(WarningRecord::getWarningLevel, level)
        );
    }

    private long countByType(Long tenantId, String type) {
        return warningRecordMapper.selectCount(
            new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getTenantId, tenantId)
                .eq(WarningRecord::getWarningType, type)
        );
    }

    private long countByStatus(Long tenantId, Integer status) {
        return warningRecordMapper.selectCount(
            new LambdaQueryWrapper<WarningRecord>()
                .eq(WarningRecord::getTenantId, tenantId)
                .eq(WarningRecord::getStatus, status)
        );
    }
}
