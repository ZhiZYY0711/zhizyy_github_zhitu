package com.zhitu.college.controller;

import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.service.CollegeService;
import com.zhitu.college.service.CollegePortalService;
import com.zhitu.college.service.CollegeWarningService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private final CollegePortalService collegePortalService;
    private final CollegeWarningService collegeWarningService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String year) {
        return Result.ok(collegePortalService.getDashboardStats(year));
    }

    @GetMapping("/dashboard/trends")
    public Result<Map<String, Object>> getTrends(
            @RequestParam(defaultValue = "month") String dimension) {
        return Result.ok(collegePortalService.getEmploymentTrends(dimension));
    }

    // ── Warning Stats ─────────────────────────────────────────────────────────

    @GetMapping("/warnings")
    public Result<Map<String, Object>> getWarnings(
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        var result = collegeWarningService.getWarnings(level, type, status, page, size);
        return Result.ok(Map.of(
            "records", result.getRecords(),
            "total", result.getTotal(),
            "page", page,
            "size", size
        ));
    }

    @GetMapping("/warnings/stats")
    public Result<Map<String, Object>> getWarningStats() {
        return Result.ok(collegeWarningService.getWarningStats());
    }

    @PostMapping("/warnings/{id}/intervene")
    public Result<Void> interveneWarning(@PathVariable(value = "id") Long id,
                                         @RequestBody InterveneRequest request) {
        collegeWarningService.intervene(id, request);
        return Result.ok();
    }

    // ── CRM ───────────────────────────────────────────────────────────────────

    @GetMapping("/crm/enterprises")
    public Result<List<Map<String, Object>>> getCrmEnterprises(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String industry) {
        return Result.ok(collegePortalService.getCrmEnterprises(level, industry));
    }

    @GetMapping("/crm/audits")
    public Result<List<Map<String, Object>>> getCrmAudits(
            @RequestParam(required = false) Integer status) {
        return Result.ok(collegePortalService.getCrmAudits(status));
    }

    @PostMapping("/crm/audits/{id}")
    public Result<Void> auditEnterprise(@PathVariable Long id,
                                        @RequestBody Map<String, Object> req) {
        String action = (String) req.get("action");
        String comment = (String) req.get("comment");
        collegePortalService.auditEnterprise(id, action, comment);
        return Result.ok();
    }

    @PutMapping("/crm/enterprises/{id}/level")
    public Result<Void> updateEnterpriseLevel(@PathVariable Long id,
                                              @RequestBody Map<String, Object> req) {
        Integer level = (Integer) req.get("level");
        String reason = (String) req.get("reason");
        collegePortalService.updateEnterpriseLevel(id, level, reason);
        return Result.ok();
    }

    @GetMapping("/crm/visits")
    public Result<List<Map<String, Object>>> getVisitRecords(
            @RequestParam(required = false) Long enterpriseId) {
        return Result.ok(collegePortalService.getVisitRecords(enterpriseId));
    }

    @PostMapping("/crm/visits")
    public Result<Void> createVisitRecord(@RequestBody Map<String, Object> req) {
        Long enterpriseTenantId = ((Number) req.get("enterpriseTenantId")).longValue();
        LocalDate visitDate = LocalDate.parse((String) req.get("visitDate"));
        String visitor = (String) req.get("visitor");
        String purpose = (String) req.get("purpose");
        String outcome = (String) req.get("outcome");
        String nextAction = (String) req.get("nextAction");
        
        collegePortalService.createVisitRecord(enterpriseTenantId, visitDate, visitor, 
            purpose, outcome, nextAction);
        return Result.ok();
    }
}
