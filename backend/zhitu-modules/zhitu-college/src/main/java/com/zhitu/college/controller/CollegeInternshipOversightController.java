package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.dto.AuditContractRequest;
import com.zhitu.college.dto.CreateInspectionRequest;
import com.zhitu.college.entity.InternshipOffer;
import com.zhitu.college.entity.InternshipRecord;
import com.zhitu.college.service.CollegeInternshipService;
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
 * College Internship Oversight Controller
 * Handles college internship oversight endpoints
 */
@Tag(name = "实习监管", description = "高校实习监管相关接口")
@RestController
@RequestMapping("/api/internship/v1/college")
@RequiredArgsConstructor
public class CollegeInternshipOversightController {

    private final CollegeInternshipService collegeInternshipService;

    /**
     * Get internship students with filtering and pagination
     * Requirements: 24.1-24.7
     */
    @Operation(
        summary = "获取实习学生列表",
        description = "分页查询实习学生列表，支持按状态筛选"
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
    @GetMapping("/students")
    public Result<IPage<InternshipRecord>> getInternshipStudents(
            @Parameter(description = "实习状态：pending-待审核，approved-已通过，ongoing-进行中，completed-已完成", example = "ongoing")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<InternshipRecord> students = collegeInternshipService.getInternshipStudents(status, page, size);
        return Result.ok(students);
    }

    /**
     * Get pending contracts for audit
     * Requirements: 24.1-24.7
     */
    @Operation(
        summary = "获取待审核合同列表",
        description = "分页查询待审核的实习合同列表"
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
    @GetMapping("/contracts/pending")
    public Result<IPage<InternshipOffer>> getPendingContracts(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<InternshipOffer> contracts = collegeInternshipService.getPendingContracts(page, size);
        return Result.ok(contracts);
    }

    /**
     * Audit internship contract
     * Requirements: 24.1-24.7
     */
    @Operation(
        summary = "审核实习合同",
        description = "审核实习合同，可以通过或拒绝"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "审核成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "合同不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"合同不存在\", \"data\": null}"
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
    @PostMapping("/contracts/{id}/audit")
    public Result<Void> auditContract(
            @Parameter(description = "合同ID", required = true, example = "1")
            @PathVariable(value = "id") Long id,
            @Parameter(description = "审核请求信息", required = true)
            @RequestBody AuditContractRequest request) {
        
        collegeInternshipService.auditContract(id, request.getAction(), request.getComment());
        return Result.ok();
    }

    /**
     * Create inspection record
     * Requirements: 24.1-24.7
     */
    @Operation(
        summary = "创建巡查记录",
        description = "创建实习巡查记录"
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
    @PostMapping("/inspections")
    public Result<Long> createInspection(
            @Parameter(description = "巡查记录信息", required = true)
            @RequestBody CreateInspectionRequest request) {
        Long inspectionId = collegeInternshipService.createInspection(request);
        return Result.ok(inspectionId);
    }
}
