package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.OperationLogDTO;
import com.zhitu.platform.dto.PlatformDashboardStatsDTO;
import com.zhitu.platform.dto.SecurityLogDTO;
import com.zhitu.platform.service.OperationLogService;
import com.zhitu.platform.service.PlatformService;
import com.zhitu.platform.service.SecurityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 平台系统管理接口（/api/system/v1）
 */
@RestController
@RequestMapping("/api/system/v1")
@RequiredArgsConstructor
public class PlatformSystemController {

    private final PlatformService platformService;
    private final OperationLogService operationLogService;
    private final SecurityLogService securityLogService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /**
     * 获取平台仪表板统计数据
     * GET /api/system/v1/dashboard/stats
     * 
     * Requirements: 28.1-28.7
     */
    @GetMapping("/dashboard/stats")
    public Result<PlatformDashboardStatsDTO> getDashboardStats() {
        return Result.ok(platformService.getDashboardStats());
    }

    // ── Tenants ───────────────────────────────────────────────────────────────

    /**
     * 获取租户列表
     * GET /api/system/v1/tenants/colleges
     * 
     * Requirements: 30.1-30.6
     */
    @GetMapping("/tenants/colleges")
    public Result<IPage<com.zhitu.platform.dto.TenantDTO>> getTenantList(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Result.ok(platformService.getTenantList(type, status, page, size));
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> getTags(
            @RequestParam(required = false) String category) {
        return Result.ok(platformService.getTags(category));
    }

    @PostMapping("/tags")
    public Result<Void> createTag(@RequestBody Map<String, Object> req) {
        platformService.createTag(req);
        return Result.ok();
    }

    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        platformService.deleteTag(id);
        return Result.ok();
    }

    // ── Skill Tree ────────────────────────────────────────────────────────────

    @GetMapping("/skills/tree")
    public Result<List<Map<String, Object>>> getSkillTree() {
        return Result.ok(platformService.getSkillTree());
    }

    // ── Certificate Templates ─────────────────────────────────────────────────

    @GetMapping("/certificates/templates")
    public Result<List<Map<String, Object>>> getCertificateTemplates() {
        return Result.ok(platformService.getCertificateTemplates());
    }

    // ── Contract Templates ────────────────────────────────────────────────────

    @GetMapping("/contracts/templates")
    public Result<List<Map<String, Object>>> getContractTemplates() {
        return Result.ok(platformService.getContractTemplates());
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    /**
     * 获取操作日志列表
     * GET /api/system/v1/logs/operation
     * 
     * Requirements: 39.1, 39.2, 39.3, 39.6
     */
    @GetMapping("/logs/operation")
    public Result<PageResult<OperationLogDTO>> getOperationLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) OffsetDateTime startTime,
            @RequestParam(required = false) OffsetDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(operationLogService.getLogs(userId, module, result, startTime, endTime, page, size));
    }

    /**
     * 获取安全日志列表
     * GET /api/system/v1/logs/security
     * 
     * Requirements: 40.1, 40.2, 40.3, 40.6
     */
    @GetMapping("/logs/security")
    public Result<PageResult<SecurityLogDTO>> getSecurityLogs(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(securityLogService.getSecurityLogs(level, page, size));
    }
}
