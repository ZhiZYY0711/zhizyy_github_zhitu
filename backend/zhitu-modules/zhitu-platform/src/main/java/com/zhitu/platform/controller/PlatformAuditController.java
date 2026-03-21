package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.AuditEnterpriseRequest;
import com.zhitu.platform.dto.AuditProjectRequest;
import com.zhitu.platform.dto.EnterpriseAuditDTO;
import com.zhitu.platform.dto.ProjectAuditDTO;
import com.zhitu.platform.service.PlatformAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 平台审核接口
 * Platform Audit Controller
 * 
 * GET  /api/portal-platform/v1/audits/projects      - 项目审核列表
 * POST /api/portal-platform/v1/audits/projects/{id} - 审核项目
 * GET  /api/system/v1/audits/enterprises             - 企业审核列表
 * POST /api/system/v1/audits/enterprises/{id}        - 审核企业
 */
@RestController
@RequiredArgsConstructor
public class PlatformAuditController {

    private final PlatformAuditService platformAuditService;

    /**
     * 获取项目审核列表
     * Get project audits
     * 
     * Requirements: 32.1, 32.3, 32.7
     * - 32.1: Expose GET /api/portal-platform/v1/audits/projects endpoint
     * - 32.3: Support filtering audits by status query parameter
     * - 32.7: Include project name, creator, submission date, and description in audit record
     */
    @GetMapping("/api/portal-platform/v1/audits/projects")
    public Result<IPage<ProjectAuditDTO>> getPendingProjects(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.ok(platformAuditService.getProjectAudits(status, page, size));
    }

    /**
     * 审核实训项目
     * Audit training project
     * 
     * Requirements: 32.2, 32.4-32.6
     * - 32.2: Expose POST /api/portal-platform/v1/audits/projects/{id} endpoint
     * - 32.4: Accept action parameter with values "pass" or "reject"
     * - 32.5: Support optional quality_rating parameter on approval
     * - 32.6: Require reject_reason when rejecting
     */
    @PostMapping("/api/portal-platform/v1/audits/projects/{id}")
    public Result<Void> auditProject(@PathVariable Long id,
                                     @Valid @RequestBody AuditProjectRequest req) {
        platformAuditService.auditProject(id, req);
        return Result.ok();
    }

    /**
     * 获取企业审核列表
     * Get enterprise audits
     * 
     * Requirements: 31.1, 31.3
     * - 31.1: Expose GET /api/system/v1/audits/enterprises endpoint
     * - 31.3: Support filtering audits by status query parameter
     */
    @GetMapping("/api/system/v1/audits/enterprises")
    public Result<IPage<EnterpriseAuditDTO>> getEnterpriseAudits(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return Result.ok(platformAuditService.getEnterpriseAudits(status, page, size));
    }

    /**
     * 审核企业注册
     * Audit enterprise registration
     * 
     * Requirements: 31.2, 31.4-31.6
     * - 31.2: Expose POST /api/system/v1/audits/enterprises/{id} endpoint
     * - 31.4: Accept action parameter with values "pass" or "reject"
     * - 31.5: Require reject_reason parameter when rejecting
     * - 31.6: Activate enterprise account and send notification on approval
     */
    @PostMapping("/api/system/v1/audits/enterprises/{id}")
    public Result<Void> auditEnterprise(
            @PathVariable Long id,
            @Valid @RequestBody AuditEnterpriseRequest request) {
        platformAuditService.auditEnterprise(id, request);
        return Result.ok();
    }
}
