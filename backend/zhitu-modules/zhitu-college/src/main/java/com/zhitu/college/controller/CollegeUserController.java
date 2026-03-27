package com.zhitu.college.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.college.entity.StudentInfo;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * College User Management Controller
 * Handles student management endpoints
 */
@Tag(name = "学生管理", description = "学生信息管理相关接口")
@RestController
@RequestMapping("/api/user/v1/college")
@RequiredArgsConstructor
public class CollegeUserController {

    private final CollegeService collegeService;

    /**
     * Get student list with filtering and pagination
     * Requirements: 22.1-22.6
     */
    @Operation(
        summary = "获取学生列表",
        description = "分页查询学生列表，支持按关键字、班级、状态筛选"
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
    public Result<IPage<StudentInfo>> getStudents(
            @Parameter(description = "搜索关键字，支持学号、姓名模糊查询", example = "张三")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "班级ID", example = "1")
            @RequestParam(value = "classId", required = false) Long classId,
            @Parameter(description = "学生状态：active-在校，graduated-已毕业，suspended-休学", example = "active")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        IPage<StudentInfo> students = collegeService.getStudentList(keyword, classId, page, size);
        return Result.ok(students);
    }
}
