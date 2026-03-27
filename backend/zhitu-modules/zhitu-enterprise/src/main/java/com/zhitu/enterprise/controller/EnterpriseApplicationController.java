package com.zhitu.enterprise.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.enterprise.dto.ApplicationDTO;
import com.zhitu.enterprise.dto.InterviewDTO;
import com.zhitu.enterprise.dto.ScheduleInterviewRequest;
import com.zhitu.enterprise.service.EnterpriseApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 企业申请管理控制器
 */
@Tag(name = "申请管理", description = "企业申请管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/internship/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseApplicationController {

    private final EnterpriseApplicationService applicationService;

    /**
     * 获取岗位申请列表
     * 
     * @param jobId 岗位ID (可选)
     * @param status 申请状态 (可选)
     * @param page 页码
     * @param size 每页大小
     * @return 分页的申请列表
     */
    @Operation(
        summary = "获取岗位申请列表",
        description = "分页查询企业收到的学生岗位申请，支持按岗位和状态筛选"
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
    @GetMapping("/applications")
    public Result<PageResult<ApplicationDTO>> getApplications(
            @Parameter(description = "岗位ID，用于筛选特定岗位的申请", example = "1001")
            @RequestParam(required = false) Long jobId,
            @Parameter(description = "申请状态：0-待处理，1-已通过，2-已拒绝", example = "0")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("Getting applications - jobId: {}, status: {}, page: {}, size: {}", 
            jobId, status, page, size);
        
        PageResult<ApplicationDTO> result = applicationService.getApplications(jobId, status, page, size);
        return Result.ok(result);
    }

    /**
     * 安排面试
     * 
     * @param request 面试安排请求
     * @return 面试ID
     */
    @Operation(
        summary = "安排面试",
        description = "为学生申请安排面试时间和地点"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "安排成功",
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
            responseCode = "404",
            description = "申请不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"申请不存在\", \"data\": null}"
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
    @PostMapping("/interviews")
    public Result<InterviewDTO> scheduleInterview(
            @Parameter(description = "面试安排请求", required = true)
            @RequestBody ScheduleInterviewRequest request) {
        log.info("Scheduling interview for application: {}", request.getApplicationId());
        
        InterviewDTO result = applicationService.scheduleInterview(request);
        return Result.ok(result);
    }
}
