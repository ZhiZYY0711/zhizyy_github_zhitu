package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 平台监控接口
 * GET /api/monitor/v1/health           - 系统健康状态
 * GET /api/monitor/v1/users/online-trend - 在线用户趋势
 */
@RestController
@RequestMapping("/api/monitor/v1")
@RequiredArgsConstructor
public class PlatformMonitorController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("status", "UP", "service", "zhitu-platform"));
    }

    @GetMapping("/users/online-trend")
    public Result<Object> onlineTrend() {
        // TODO: 接入 Redis 统计在线用户数趋势
        return Result.ok(Map.of("message", "online trend data"));
    }

    @GetMapping("/services")
    public Result<List<Map<String, Object>>> getServiceStatuses() {
        List<Map<String, Object>> services = List.of(
                Map.of("name", "zhitu-auth", "status", "UP"),
                Map.of("name", "zhitu-gateway", "status", "UP"),
                Map.of("name", "zhitu-enterprise", "status", "UP"),
                Map.of("name", "zhitu-college", "status", "UP"),
                Map.of("name", "zhitu-platform", "status", "UP"),
                Map.of("name", "zhitu-student", "status", "UP"),
                Map.of("name", "zhitu-system", "status", "UP")
        );
        return Result.ok(services);
    }
}
