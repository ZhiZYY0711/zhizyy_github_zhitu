package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.service.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.ok(platformService.getDashboardStats());
    }

    // ── Tenants ───────────────────────────────────────────────────────────────

    @GetMapping("/tenants/colleges")
    public Result<List<Map<String, Object>>> getTenantList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.ok(platformService.getTenantList(type, status));
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

    @GetMapping("/logs/operation")
    public Result<List<Map<String, Object>>> getOperationLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.ok(platformService.getOperationLogs(userId, module, result, startTime, endTime));
    }

    @GetMapping("/logs/security")
    public Result<List<Map<String, Object>>> getSecurityLogs(
            @RequestParam(required = false) String level) {
        return Result.ok(platformService.getSecurityLogs(level));
    }
}
