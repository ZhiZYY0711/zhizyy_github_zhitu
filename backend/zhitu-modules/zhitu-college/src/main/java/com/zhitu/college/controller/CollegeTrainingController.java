package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.entity.TrainingPlan;
import com.zhitu.college.service.CollegeTrainingService;
import com.zhitu.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * College Training Controller
 * Handles training plan management endpoints
 */
@Tag(name = "实训管理", description = "实训计划管理相关接口")
@RestController
@RequestMapping("/api/training/v1/college")
@RequiredArgsConstructor
public class CollegeTrainingController {

    private final CollegeTrainingService collegeTrainingService;

    /**
     * Get training plans with filtering and pagination
     * Requirements: 23.1-23.6
     */
    @Operation(
        summary = "获取培养方案列表",
        description = "分页查询培养方案列表，支持按学期筛选"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @GetMapping("/plans")
    public Result<IPage<TrainingPlan>> getPlans(
            @Parameter(description = "学期，格式：2024-1", example = "2024-1")
            @RequestParam(value = "semester", required = false) String semester,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<TrainingPlan> plans = collegeTrainingService.getPlans(semester, page, size);
        return Result.ok(plans);
    }

    /**
     * Create a new training plan
     * Requirements: 23.1-23.6
     */
    @Operation(
        summary = "创建培养方案",
        description = "创建新的培养方案"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/plans")
    public Result<Long> createPlan(
            @Parameter(description = "培养方案信息", required = true)
            @RequestBody TrainingPlan plan) {
        Long planId = collegeTrainingService.createPlan(plan);
        return Result.ok(planId);
    }

    /**
     * Assign mentor to training plan
     * Requirements: 29.1-29.6
     */
    @Operation(
        summary = "分配导师",
        description = "为培养方案分配指导教师"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "分配成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "培养方案或教师不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"培养方案或教师不存在\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/mentors/assign")
    public Result<Void> assignMentor(
            @Parameter(description = "培养方案ID", required = true, example = "1")
            @RequestParam(value = "planId") Long planId,
            @Parameter(description = "教师ID", required = true, example = "1")
            @RequestParam(value = "teacherId") Long teacherId) {
        
        collegeTrainingService.assignMentor(planId, teacherId);
        return Result.ok();
    }
}
