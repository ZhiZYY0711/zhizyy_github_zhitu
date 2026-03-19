package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.AuditRequest;
import com.zhitu.platform.entity.EnterpriseInfo;
import com.zhitu.platform.entity.TrainingProject;
import com.zhitu.platform.service.PlatformService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 平台审核接口
 * GET  /api/portal-platform/v1/audits/projects      - 待审核实训项目
 * POST /api/portal-platform/v1/audits/projects/{id} - 审核实训项目
 * GET  /api/system/v1/audits/enterprises             - 待审核企业
 * POST /api/system/v1/audits/enterprises/{id}        - 审核企业
 */
@RestController
@RequiredArgsConstructor
public class PlatformAuditController {

    private final PlatformService platformService;

    @GetMapping("/api/portal-platform/v1/audits/projects")
    public Result<IPage<TrainingProject>> getPendingProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(platformService.getPendingProjects(page, size));
    }

    @PostMapping("/api/portal-platform/v1/audits/projects/{id}")
    public Result<Void> auditProject(@PathVariable Long id,
                                     @Valid @RequestBody AuditRequest req) {
        platformService.auditProject(id, req);
        return Result.ok();
    }

    @GetMapping("/api/system/v1/audits/enterprises")
    public Result<IPage<EnterpriseInfo>> getPendingEnterprises(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(platformService.getPendingEnterprises(page, size));
    }

    @PostMapping("/api/system/v1/audits/enterprises/{id}")
    public Result<Void> auditEnterprise(@PathVariable Long id,
                                        @Valid @RequestBody AuditRequest req) {
        platformService.auditEnterprise(id, req);
        return Result.ok();
    }
}
