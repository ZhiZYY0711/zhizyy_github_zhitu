package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.dto.InterveneRequest;
import com.zhitu.college.entity.WarningRecord;
import com.zhitu.college.service.CollegeService;
import com.zhitu.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 高校预警管理接口
 * GET  /api/portal-college/v1/warnings           - 预警学生列表
 * POST /api/portal-college/v1/warnings/{id}/intervene - 预警干预
 */
@Tag(name = "预警管理", description = "学生预警管理相关接口")
@RestController
@RequestMapping("/api/portal-college/v1/warnings")
@RequiredArgsConstructor
public class CollegeWarningController {

    private final CollegeService collegeService;

    @Operation(
        summary = "获取预警学生列表",
        description = "分页查询预警学生列表，支持按状态筛选"
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
    @GetMapping
    public Result<IPage<WarningRecord>> getWarnings(
            @Parameter(description = "预警状态：0-待处理，1-已处理", example = "0")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collegeService.getWarnings(status, page, size));
    }

    @Operation(
        summary = "预警干预",
        description = "对预警学生进行干预处理"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "干预成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "预警记录不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"预警记录不存在\", \"data\": null}"
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
    @PostMapping("/{id}/intervene")
    public Result<Void> intervene(
            @Parameter(description = "预警记录ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "干预请求信息", required = true)
            @Valid @RequestBody InterveneRequest req) {
        collegeService.interveneWarning(id, req);
        return Result.ok();
    }
}
