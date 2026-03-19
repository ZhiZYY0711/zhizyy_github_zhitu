package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.service.EnterpriseService;
import com.zhitu.enterprise.service.RecruitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 企业门户补充接口（portal-enterprise）
 * 覆盖前端 api.ts 中 PORTAL_API 路径下的接口
 */
@RestController
@RequestMapping("/api/portal-enterprise/v1")
@RequiredArgsConstructor
public class EnterprisePortalController {

    private final EnterpriseService enterpriseService;
    private final RecruitService recruitService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.ok(enterpriseService.getDashboardStats());
    }

    @GetMapping("/todos")
    public Result<List<Map<String, Object>>> getTodos() {
        return Result.ok(enterpriseService.getTodos());
    }

    @GetMapping("/activities")
    public Result<List<Map<String, Object>>> getActivities() {
        return Result.ok(enterpriseService.getActivities());
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics")
    public Result<Map<String, Object>> getAnalytics(
            @RequestParam(required = false, defaultValue = "month") String range) {
        return Result.ok(enterpriseService.getAnalytics(range));
    }

    // ── Talent Pool ───────────────────────────────────────────────────────────

    @GetMapping("/talent-pool")
    public Result<List<Map<String, Object>>> getTalentPool() {
        return Result.ok(recruitService.getTalentPool());
    }

    @DeleteMapping("/talent-pool/{id}")
    public Result<Void> removeFromTalentPool(@PathVariable Long id) {
        recruitService.removeFromTalentPool(id);
        return Result.ok();
    }

    // ── Mentor Dashboard ──────────────────────────────────────────────────────

    @GetMapping("/mentor/dashboard")
    public Result<Map<String, Object>> getMentorDashboard() {
        return Result.ok(enterpriseService.getMentorDashboard());
    }
}
