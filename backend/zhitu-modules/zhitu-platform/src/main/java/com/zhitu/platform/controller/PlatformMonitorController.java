package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.OnlineUserTrendResponseDTO;
import com.zhitu.platform.dto.ServiceHealthDTO;
import com.zhitu.platform.dto.SystemHealthDTO;
import com.zhitu.platform.service.PlatformMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台监控接口（/api/monitor/v1）
 * Requirements: 29.1-29.7
 */
@Tag(name = "监控管理", description = "系统监控管理相关接口")
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
    @Operation(
        summary = "获取系统健康状态",
        description = "获取整个系统的健康状态，包括所有微服务的状态、响应时间、错误率和CPU使用率\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 通过 Nacos 服务发现获取微服务列表\n" +
            "- 通过健康检查端点获取各服务状态\n" +
            "- 监控数据来自系统监控组件"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
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
    @Operation(
        summary = "获取在线用户趋势",
        description = "获取过去24小时的在线用户数量趋势数据"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
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
    @Operation(
        summary = "获取微服务状态列表",
        description = "获取所有微服务的详细健康状态信息"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/services")
    public Result<List<ServiceHealthDTO>> getServiceStatuses() {
        return Result.ok(platformMonitorService.getServices());
    }
}
