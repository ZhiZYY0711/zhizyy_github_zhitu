package com.zhitu.college.controller;

import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 高校门户补充接口
 * 覆盖前端 college/services/api.ts 中 PORTAL_API / INTERNSHIP_API 路径下的接口
 */
@RestController
@RequestMapping("/api/portal-college/v1")
@RequiredArgsConstructor
public class CollegePortalController {

    private final CollegeService collegeService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String year) {
        return Result.ok(collegeService.getEmploymentStats(year));
    }

    @GetMapping("/dashboard/trends")
    public Result<Map<String, Object>> getTrends(
            @RequestParam(defaultValue = "month") String dimension) {
        return Result.ok(collegeService.getTrends(dimension));
    }

    // ── Warning Stats ─────────────────────────────────────────────────────────

    @GetMapping("/warnings/stats")
    public Result<Map<String, Object>> getWarningStats() {
        return Result.ok(collegeService.getWarningStats());
    }

    // ── CRM ───────────────────────────────────────────────────────────────────

    @GetMapping("/crm/enterprises")
    public Result<List<Map<String, Object>>> getCrmEnterprises(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String industry) {
        return Result.ok(collegeService.getCrmEnterprises(level, industry));
    }

    @GetMapping("/crm/audits")
    public Result<List<Map<String, Object>>> getCrmAudits(
            @RequestParam(required = false) String status) {
        return Result.ok(collegeService.getCrmAudits(status));
    }

    @PostMapping("/crm/audits/{id}")
    public Result<Void> auditEnterprise(@PathVariable Long id,
                                        @RequestBody Map<String, Object> req) {
        collegeService.auditEnterprise(id, req);
        return Result.ok();
    }

    @PutMapping("/crm/enterprises/{id}/level")
    public Result<Void> updateEnterpriseLevel(@PathVariable Long id,
                                              @RequestBody Map<String, Object> req) {
        collegeService.updateEnterpriseLevel(id, req);
        return Result.ok();
    }

    @GetMapping("/crm/visits")
    public Result<List<Map<String, Object>>> getVisitRecords(
            @RequestParam(required = false) Long enterpriseId) {
        return Result.ok(collegeService.getVisitRecords(enterpriseId));
    }

    @PostMapping("/crm/visits")
    public Result<Void> createVisitRecord(@RequestBody Map<String, Object> req) {
        collegeService.createVisitRecord(req);
        return Result.ok();
    }
}
