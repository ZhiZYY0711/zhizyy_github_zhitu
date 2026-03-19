package com.zhitu.college.controller;

import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 高校实习管理接口
 * GET  /api/internship/v1/college/students        - 实习学生列表
 * GET  /api/internship/v1/college/contracts/pending - 待审核合同
 * POST /api/internship/v1/college/contracts/{id}/audit - 审核合同
 * POST /api/internship/v1/college/inspections     - 创建巡查记录
 * POST /api/training/v1/college/mentors/assign    - 分配导师
 */
@RestController
@RequiredArgsConstructor
public class CollegeInternshipController {

    private final CollegeService collegeService;

    @GetMapping("/api/internship/v1/college/students")
    public Result<List<Map<String, Object>>> getInternshipStudents(
            @RequestParam(required = false) String status) {
        return Result.ok(collegeService.getInternshipStudents(status));
    }

    @GetMapping("/api/internship/v1/college/contracts/pending")
    public Result<List<Map<String, Object>>> getPendingContracts() {
        return Result.ok(collegeService.getPendingContracts());
    }

    @PostMapping("/api/internship/v1/college/contracts/{id}/audit")
    public Result<Void> auditContract(@PathVariable Long id,
                                      @RequestBody Map<String, Object> req) {
        collegeService.auditContract(id, req);
        return Result.ok();
    }

    @PostMapping("/api/internship/v1/college/inspections")
    public Result<Void> createInspection(@RequestBody Map<String, Object> req) {
        collegeService.createInspection(req);
        return Result.ok();
    }

    @PostMapping("/api/training/v1/college/mentors/assign")
    public Result<Void> assignMentor(@RequestBody Map<String, Object> req) {
        collegeService.assignMentor(req);
        return Result.ok();
    }
}
