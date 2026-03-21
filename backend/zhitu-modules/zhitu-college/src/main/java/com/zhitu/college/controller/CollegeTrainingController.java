package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.service.CollegeTrainingService;
import com.zhitu.common.core.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * College Training Controller
 * Handles training plan management endpoints
 */
@RestController
@RequestMapping("/api/training/v1/college")
@RequiredArgsConstructor
public class CollegeTrainingController {

    private final CollegeTrainingService collegeTrainingService;

    /**
     * Get training plans with filtering and pagination
     * Requirements: 23.1-23.6
     */
    @GetMapping("/plans")
    public Result<IPage<TrainingPlan>> getPlans(
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<TrainingPlan> plans = collegeTrainingService.getPlans(semester, page, size);
        return Result.ok(plans);
    }

    /**
     * Create a new training plan
     * Requirements: 23.1-23.6
     */
    @PostMapping("/plans")
    public Result<Long> createPlan(@RequestBody TrainingPlan plan) {
        Long planId = collegeTrainingService.createPlan(plan);
        return Result.ok(planId);
    }

    /**
     * Assign mentor to training plan
     * Requirements: 29.1-29.6
     */
    @PostMapping("/mentors/assign")
    public Result<Void> assignMentor(
            @RequestParam(value = "planId") Long planId,
            @RequestParam(value = "teacherId") Long teacherId) {
        
        collegeTrainingService.assignMentor(planId, teacherId);
        return Result.ok();
    }
}
