package com.zhitu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.system.entity.SysTenant;
import com.zhitu.system.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/system/v1")
@RequiredArgsConstructor
public class TenantController {

    private final SysTenantMapper tenantMapper;

    /**
     * 开通高校租户
     */
    @PostMapping("/tenants/colleges")
    public Result<SysTenant> createCollege(@RequestBody SysTenant tenant) {
        tenant.setType(1);
        tenant.setStatus(1);
        tenant.setCreatedAt(OffsetDateTime.now());
        tenant.setUpdatedAt(OffsetDateTime.now());
        tenantMapper.insert(tenant);
        return Result.ok(tenant);
    }

    /**
     * 高校列表
     */
    @GetMapping("/tenants/colleges")
    public Result<PageResult<SysTenant>> listColleges(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<SysTenant> p = tenantMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysTenant>().eq(SysTenant::getType, 1));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords(), page, size));
    }

    /**
     * 待审核企业列表
     */
    @GetMapping("/audits/enterprises")
    public Result<PageResult<SysTenant>> listPendingEnterprises(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<SysTenant> p = tenantMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SysTenant>()
                        .eq(SysTenant::getType, 2)
                        .eq(SysTenant::getStatus, 0));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords(), page, size));
    }

    /**
     * 审核企业
     */
    @PostMapping("/audits/enterprises/{id}")
    public Result<Void> auditEnterprise(@PathVariable Long id,
                                         @RequestParam Integer status) {
        SysTenant tenant = tenantMapper.selectById(id);
        if (tenant != null) {
            tenant.setStatus(status);
            tenant.setUpdatedAt(OffsetDateTime.now());
            tenantMapper.updateById(tenant);
        }
        return Result.ok();
    }
}
