package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.AddStaffRequest;
import com.zhitu.enterprise.dto.EnterpriseProfileUpdateRequest;
import com.zhitu.enterprise.entity.EnterpriseInfo;
import com.zhitu.enterprise.entity.EnterpriseStaff;
import com.zhitu.enterprise.service.EnterpriseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业基础信息接口
 * GET  /api/user/v1/enterprise/profile  - 获取企业详情
 * PUT  /api/user/v1/enterprise/profile  - 更新企业信息
 * GET  /api/user/v1/enterprise/staff    - 员工列表
 * POST /api/user/v1/enterprise/staff    - 添加员工
 */
@RestController
@RequestMapping("/api/user/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseProfileController {

    private final EnterpriseService enterpriseService;

    @GetMapping("/profile")
    public Result<EnterpriseInfo> getProfile() {
        return Result.ok(enterpriseService.getProfile());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody EnterpriseProfileUpdateRequest req) {
        enterpriseService.updateProfile(req);
        return Result.ok();
    }

    @GetMapping("/staff")
    public Result<List<EnterpriseStaff>> getStaff() {
        return Result.ok(enterpriseService.getStaffList());
    }

    @PostMapping("/staff")
    public Result<Void> addStaff(@Valid @RequestBody AddStaffRequest req) {
        enterpriseService.addStaff(req);
        return Result.ok();
    }
}
