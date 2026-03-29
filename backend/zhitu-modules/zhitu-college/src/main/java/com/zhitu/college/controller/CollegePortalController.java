package com.zhitu.college.controller;

import com.zhitu.college.dto.DashboardStatsDTO;
import com.zhitu.common.core.result.Result;
import com.zhitu.college.service.CollegePortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * College Portal Controller
 * Handles dashboard statistics, employment trends, and CRM operations
 * 
 * @author Zhitu Team
 */
@Slf4j
@Tag(name = "高校门户", description = "高校门户仪表盘和CRM相关接口")
@RestController
@RequestMapping("/api/portal-college/v1")
@RequiredArgsConstructor
public class CollegePortalController {

    private final CollegePortalService collegePortalService;

    /**
     * Get dashboard statistics
     * Requirements: 20.1-20.7
     */
    @Operation(summary = "获取仪表盘统计数据", description = "获取就业统计、实习参与率等数据")
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<DashboardStatsDTO> getDashboardStats(
            @Parameter(description = "年份，如2024") @RequestParam(required = false) String year) {
        log.info("Getting dashboard stats for year: {}", year);
        DashboardStatsDTO stats = collegePortalService.getDashboardStats(year);
        return Result.ok(stats);
    }

    /**
     * Get employment trends
     * Requirements: 20.8
     */
    @Operation(summary = "获取就业趋势数据", description = "按月度、季度或年度获取就业趋势")
    @GetMapping("/dashboard/trends")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<Map<String, Object>> getEmploymentTrends(
            @Parameter(description = "维度: month, quarter, year") @RequestParam(required = false, defaultValue = "month") String dimension) {
        log.info("Getting employment trends with dimension: {}", dimension);
        Map<String, Object> trends = collegePortalService.getEmploymentTrends(dimension);
        return Result.ok(trends);
    }

    /**
     * Get CRM enterprises
     * Requirements: 24.1-24.3
     */
    @Operation(summary = "获取企业列表", description = "获取合作企业列表，支持按等级和行业筛选")
    @GetMapping("/crm/enterprises")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<List<Map<String, Object>>> getCrmEnterprises(
            @Parameter(description = "合作等级") @RequestParam(required = false) Integer level,
            @Parameter(description = "行业") @RequestParam(required = false) String industry,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Getting CRM enterprises with level: {}, industry: {}", level, industry);
        List<Map<String, Object>> enterprises = collegePortalService.getCrmEnterprises(level, industry);
        return Result.ok(enterprises);
    }

    /**
     * Get enterprise audits
     * Requirements: 24.4
     */
    @Operation(summary = "获取企业审核列表", description = "获取待审核的企业列表")
    @GetMapping("/crm/audits")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<List<Map<String, Object>>> getCrmAudits(
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Getting CRM audits with status: {}", status);
        List<Map<String, Object>> audits = collegePortalService.getCrmAudits(status);
        return Result.ok(audits);
    }

    /**
     * Audit enterprise
     * Requirements: 24.5
     */
    @Operation(summary = "审核企业", description = "通过或拒绝企业审核")
    @PostMapping("/crm/audits/{id}")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<Void> auditEnterprise(
            @Parameter(description = "审核ID") @PathVariable Long id,
            @Parameter(description = "操作: pass, reject") @RequestParam String action,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        log.info("Auditing enterprise with id: {}, action: {}", id, action);
        collegePortalService.auditEnterprise(id, action, comment);
        return Result.ok();
    }

    /**
     * Update enterprise level
     * Requirements: 24.6
     */
    @Operation(summary = "更新企业等级", description = "调整企业合作等级")
    @PutMapping("/crm/enterprises/{id}/level")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<Void> updateEnterpriseLevel(
            @Parameter(description = "企业ID") @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Integer level = (Integer) request.get("level");
        String reason = (String) request.get("reason");
        log.info("Updating enterprise level for id: {}, level: {}", id, level);
        collegePortalService.updateEnterpriseLevel(id, level, reason);
        return Result.ok();
    }

    /**
     * Get visit records
     * Requirements: 25.1-25.2
     */
    @Operation(summary = "获取走访记录", description = "获取企业走访记录列表")
    @GetMapping("/crm/visits")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<List<Map<String, Object>>> getVisitRecords(
            @Parameter(description = "企业ID") @RequestParam(required = false) Long enterpriseId,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Getting visit records for enterprise: {}", enterpriseId);
        List<Map<String, Object>> visits = collegePortalService.getVisitRecords(enterpriseId);
        return Result.ok(visits);
    }

    /**
     * Create visit record
     * Requirements: 25.3
     */
    @Operation(summary = "创建走访记录", description = "记录企业走访信息")
    @PostMapping("/crm/visits")
    @PreAuthorize("hasRole('COLLEGE')")
    public Result<Void> createVisitRecord(@RequestBody Map<String, Object> request) {
        Long enterpriseId = ((Number) request.get("enterpriseId")).longValue();
        LocalDate visitDate = LocalDate.parse((String) request.get("visitDate"));
        String visitor = (String) request.get("visitor");
        String purpose = (String) request.get("purpose");
        String outcome = (String) request.get("outcome");
        String nextAction = (String) request.get("nextAction");
        
        log.info("Creating visit record for enterprise: {}", enterpriseId);
        collegePortalService.createVisitRecord(enterpriseId, visitDate, visitor, purpose, outcome, nextAction);
        return Result.ok();
    }
}
