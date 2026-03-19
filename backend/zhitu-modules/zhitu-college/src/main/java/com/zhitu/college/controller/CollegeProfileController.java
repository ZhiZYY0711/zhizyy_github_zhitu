package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.dto.CreateTrainingPlanRequest;
import com.zhitu.college.entity.CollegeInfo;
import com.zhitu.college.entity.Organization;
import com.zhitu.college.entity.StudentInfo;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 高校基础信息 & 教学教务接口
 * GET  /api/user/v1/college/profile              - 获取教职工档案
 * GET  /api/user/v1/basic/organization-tree      - 组织树
 * GET  /api/user/v1/college/students             - 学生列表
 * GET  /api/user/v1/college/students/{id}/full-view - 学生360视图
 * GET  /api/training/v1/college/plans            - 实训排期列表
 * POST /api/training/v1/college/plans            - 发布实训计划
 */
@RestController
@RequiredArgsConstructor
public class CollegeProfileController {

    private final CollegeService collegeService;

    @GetMapping("/api/user/v1/college/profile")
    public Result<CollegeInfo> getProfile() {
        return Result.ok(collegeService.getProfile());
    }

    @GetMapping("/api/user/v1/basic/organization-tree")
    public Result<List<Organization>> getOrganizationTree() {
        return Result.ok(collegeService.getOrganizationTree());
    }

    @GetMapping("/api/user/v1/college/students")
    public Result<IPage<StudentInfo>> getStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collegeService.getStudentList(keyword, classId, page, size));
    }

    @GetMapping("/api/user/v1/college/students/{id}/full-view")
    public Result<StudentInfo> getStudentFullView(@PathVariable Long id) {
        return Result.ok(collegeService.getStudentFullView(id));
    }

    @GetMapping("/api/training/v1/college/plans")
    public Result<IPage<TrainingPlan>> getTrainingPlans(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collegeService.getTrainingPlans(page, size));
    }

    @PostMapping("/api/training/v1/college/plans")
    public Result<Void> createTrainingPlan(@Valid @RequestBody CreateTrainingPlanRequest req) {
        collegeService.createTrainingPlan(req);
        return Result.ok();
    }
}
