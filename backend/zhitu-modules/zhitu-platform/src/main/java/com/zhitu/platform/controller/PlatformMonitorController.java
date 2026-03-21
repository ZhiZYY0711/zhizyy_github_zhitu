package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.OnlineUserTrendResponseDTO;
import com.zhitu.platform.dto.ServiceHealthDTO;
import com.zhitu.platform.dto.SystemHealthDTO;
import com.zhitu.platform.service.PlatformMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台监控接口（/api/monitor/v1）
 * Requirements: 29.1-29.7
 */
@RestController
@RequestMapping("/api/monitor/v1")
@RequiredArgsConstructor
public class PlatformMonitorController {

    private final PlatformMonitorService platformMonitorService;

    /**
     * 获取系统健康状态
     * GET /api/monitor/v1/health
     * 
     * Requirements: 29.1, 29.4, 29.5
     * - 29.4: Include status (healthy, degraded, down) for each microservice
     * - 29.5: Include response time, error rate, and CPU usage for each microservice
     */
    @GetMapping("/health")
    public Result<SystemHealthDTO> health() {
        return Result.ok(platformMonitorService.getHealth());
    }

    /**
     * 获取在线用户趋势（过去24小时）
     * GET /api/monitor/v1/users/online-trend
     * 
     * Requirements: 29.2, 29.6
     * - 29.6: Include online user count trend for the past 24 hours
     */
    @GetMapping("/users/online-trend")
    public Result<OnlineUserTrendResponseDTO> onlineTrend() {
        return Result.ok(platformMonitorService.getOnlineUserTrend());
    }

    /**
     * 获取所有微服务健康状态详情
     * GET /api/monitor/v1/services
     * 
     * Requirements: 29.3, 29.4, 29.5
     */
    @GetMapping("/services")
    public Result<List<ServiceHealthDTO>> getServiceStatuses() {
        return Result.ok(platformMonitorService.getServices());
    }
}
