package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.redis.constants.CacheConstants;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.OnlineUserTrendDTO;
import com.zhitu.platform.dto.OnlineUserTrendResponseDTO;
import com.zhitu.platform.dto.ServiceHealthDTO;
import com.zhitu.platform.dto.SystemHealthDTO;
import com.zhitu.platform.entity.OnlineUserTrend;
import com.zhitu.platform.entity.ServiceHealth;
import com.zhitu.platform.mapper.OnlineUserTrendMapper;
import com.zhitu.platform.mapper.ServiceHealthMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 平台监控服务
 * Requirements: 29.1-29.7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformMonitorService {

    private final ServiceHealthMapper serviceHealthMapper;
    private final OnlineUserTrendMapper onlineUserTrendMapper;
    private final CacheService cacheService;

    /**
     * 获取系统健康状态
     * Requirements: 29.1, 29.4, 29.5
     * Cache: Redis key platform:health, TTL 1 minute
     * 
     * @return 系统健康状态，包含所有微服务的状态信息
     */
    public SystemHealthDTO getHealth() {
        return cacheService.getOrSet(
            CacheConstants.KEY_PLATFORM_HEALTH,
            CacheConstants.TTL_HEALTH,
            CacheConstants.TTL_HEALTH_UNIT,
            () -> {
                log.debug("Computing system health status");

                // 查询所有微服务的最新健康状态
                // 按服务名分组，取每个服务的最新记录
                List<ServiceHealth> healthRecords = serviceHealthMapper.selectList(
                    new LambdaQueryWrapper<ServiceHealth>()
                        .orderByDesc(ServiceHealth::getCheckedAt)
                );

                // 转换为 DTO
                List<ServiceHealthDTO> services = healthRecords.stream()
                    .collect(Collectors.groupingBy(ServiceHealth::getServiceName))
                    .values()
                    .stream()
                    .map(list -> list.get(0)) // 取每组的第一条（最新）
                    .map(this::toServiceHealthDTO)
                    .collect(Collectors.toList());

                log.debug("System health - {} services monitored", services.size());

                return new SystemHealthDTO(services);
            }
        );
    }

    /**
     * 获取在线用户趋势（过去24小时）
     * Requirements: 29.2, 29.6
     * Cache: Redis key platform:online:trend, TTL 5 minutes
     * 
     * @return 过去24小时的在线用户趋势数据
     */
    public OnlineUserTrendResponseDTO getOnlineUserTrend() {
        return cacheService.getOrSet(
            CacheConstants.KEY_PLATFORM_ONLINE_TREND,
            5,
            TimeUnit.MINUTES,
            () -> {
                log.debug("Computing online user trend for past 24 hours");

                // 查询过去24小时的在线用户趋势数据
                OffsetDateTime twentyFourHoursAgo = OffsetDateTime.now().minusHours(24);
                
                List<OnlineUserTrend> trendRecords = onlineUserTrendMapper.selectList(
                    new LambdaQueryWrapper<OnlineUserTrend>()
                        .ge(OnlineUserTrend::getTimestamp, twentyFourHoursAgo)
                        .orderByAsc(OnlineUserTrend::getTimestamp)
                );

                // 转换为 DTO
                List<OnlineUserTrendDTO> trend = trendRecords.stream()
                    .map(this::toOnlineUserTrendDTO)
                    .collect(Collectors.toList());

                log.debug("Online user trend - {} data points in past 24 hours", trend.size());

                return new OnlineUserTrendResponseDTO(trend);
            }
        );
    }

    /**
     * 获取所有微服务健康状态详情
     * Requirements: 29.3, 29.4, 29.5
     * 
     * @return 所有微服务的健康状态列表
     */
    public List<ServiceHealthDTO> getServices() {
        log.debug("Fetching all service health details");

        // 查询所有微服务的最新健康状态
        List<ServiceHealth> healthRecords = serviceHealthMapper.selectList(
            new LambdaQueryWrapper<ServiceHealth>()
                .orderByDesc(ServiceHealth::getCheckedAt)
        );

        // 按服务名分组，取每个服务的最新记录
        List<ServiceHealthDTO> services = healthRecords.stream()
            .collect(Collectors.groupingBy(ServiceHealth::getServiceName))
            .values()
            .stream()
            .map(list -> list.get(0)) // 取每组的第一条（最新）
            .map(this::toServiceHealthDTO)
            .collect(Collectors.toList());

        log.debug("Retrieved health status for {} services", services.size());

        return services;
    }

    // ── 私有辅助方法 ──────────────────────────────────────────────────────────

    /**
     * 将 ServiceHealth 实体转换为 DTO
     */
    private ServiceHealthDTO toServiceHealthDTO(ServiceHealth entity) {
        return new ServiceHealthDTO(
            entity.getServiceName(),
            entity.getStatus(),
            entity.getResponseTime(),
            entity.getErrorRate(),
            entity.getCpuUsage(),
            entity.getMemoryUsage()
        );
    }

    /**
     * 将 OnlineUserTrend 实体转换为 DTO
     */
    private OnlineUserTrendDTO toOnlineUserTrendDTO(OnlineUserTrend entity) {
        return new OnlineUserTrendDTO(
            entity.getTimestamp(),
            entity.getOnlineCount(),
            entity.getStudentCount(),
            entity.getEnterpriseCount(),
            entity.getCollegeCount()
        );
    }
}
