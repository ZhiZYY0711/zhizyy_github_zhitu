package com.zhitu.student.controller;

import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.*;
import com.zhitu.student.service.StudentPortalService;
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

import java.util.List;

/**
 * 学生门户接口
 * GET /api/student-portal/v1/dashboard - 获取学生仪表板统计数据
 * GET /api/student-portal/v1/capability/radar - 获取学生能力雷达图数据
 * GET /api/student-portal/v1/tasks - 获取学生任务列表（支持状态过滤和分页）
 * GET /api/student-portal/v1/recommendations - 获取学生个性化推荐
 */
@Tag(name = "学生门户", description = "学生门户相关接口")
@RestController
@RequestMapping("/api/student-portal/v1")
@RequiredArgsConstructor
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    /**
     * 获取学生仪表板统计数据
     * 包括：实训项目数、实习岗位数、待办任务数、成长分数
     *
     * @return 仪表板统计数据
     */
    @Operation(
        summary = "获取学生仪表板统计数据",
        description = "获取学生的实训项目数、实习岗位数、待办任务数、成长分数等统计信息\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 必须携带有效的 JWT token\n" +
            "- 学生信息必须已在 student_profile 表中存在\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生记录\n" +
            "- 统计数据来自多个表：project_enrollment, job_application, student_task, growth_evaluation"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/dashboard")
    public Result<DashboardStatsDTO> getDashboard() {
        return Result.ok(studentPortalService.getDashboardStats());
    }

    /**
     * 获取学生能力雷达图数据
     * 包括五个维度：technical_skill, communication, teamwork, problem_solving, innovation
     * 
     * @return 能力雷达图数据
     */
    @Operation(
        summary = "获取学生能力雷达图数据",
        description = "获取学生在技术能力、沟通能力、团队协作、问题解决、创新能力五个维度的评分\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 学生必须有至少一条成长评价记录\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 growth_evaluation 表中的评价记录\n" +
            "- 计算各维度的平均分数"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/capability/radar")
    public Result<CapabilityRadarDTO> getCapabilityRadar() {
        return Result.ok(studentPortalService.getCapabilityRadar());
    }

    /**
     * 获取学生任务列表
     * 支持按状态过滤（pending/completed）和分页
     * 
     * @param status 任务状态：pending 或 completed（可选）
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的任务列表
     */
    @Operation(
        summary = "获取学生任务列表",
        description = "分页查询学生任务列表，支持按状态过滤（pending/completed）\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 student_task 表\n" +
            "- 任务可能来自项目、实习或系统分配"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/tasks")
    public Result<PageResult<TaskDTO>> getTasks(
            @Parameter(description = "任务状态：pending（待办）或 completed（已完成）", example = "pending")
            @RequestParam(required = false) String status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getTasks(status, page, size));
    }

    /**
     * 获取学生个性化推荐
     * 支持按类型过滤：all / project / job / course
     * 使用Redis缓存，TTL为15分钟
     * 
     * @param type 推荐类型：all, project, job, course（默认all）
     * @return 推荐列表
     */
    @Operation(
        summary = "获取学生个性化推荐",
        description = "获取学生的个性化推荐内容，支持按类型过滤（all/project/job/course），使用Redis缓存\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 学生档案信息应已完善（包括技能、兴趣等）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 student_profile 表中的学生档案\n" +
            "- 推荐算法基于学生的技能标签、历史行为等\n" +
            "- 使用 Redis 缓存推荐结果，提高性能"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/recommendations")
    public Result<List<RecommendationDTO>> getRecommendations(
            @Parameter(description = "推荐类型：all（全部）、project（项目）、job（岗位）、course（课程）", example = "all")
            @RequestParam(defaultValue = "all") String type) {
        return Result.ok(studentPortalService.getRecommendations(type));
    }

    /**
     * 获取实训项目列表
     * 查询开放状态的实训项目，并标注学生的报名状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的实训项目列表
     */
    @Operation(
        summary = "获取实训项目列表",
        description = "分页查询开放状态的实训项目，并标注学生的报名状态，使用Redis缓存\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 无需其他前置条件，未报名项目也可查看\n\n" +
            "**数据依赖：**\n" +
            "- 查询 training_project 表（状态为开放）\n" +
            "- 关联 project_enrollment 表查询学生报名状态\n" +
            "- 使用 Redis 缓存项目列表"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/training/projects")
    public Result<PageResult<TrainingProjectDTO>> getTrainingProjects(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getTrainingProjects(page, size));
    }

    /**
     * 获取项目看板（Scrum Board）
     * 验证学生是否已报名该项目，未报名返回403错误
     * 
     * @param id 项目ID
     * @return 看板数据
     */
    @Operation(
        summary = "获取项目看板",
        description = "获取指定实训项目的Scrum看板数据，需要学生已报名该项目\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以学生角色登录\n" +
            "- 项目ID必须存在于 training_project 表中\n" +
            "- 学生必须已报名该项目（project_enrollment 表）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 training_project 表中的项目记录\n" +
            "- 依赖 project_enrollment 表中的报名记录\n" +
            "- 依赖 project_task 表中的任务数据"
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
            responseCode = "403",
            description = "未报名该项目",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 403, \"message\": \"未报名该项目\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "项目不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"项目不存在\", \"data\": null}"
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
    @GetMapping("/training/projects/{id}/board")
    public Result<ScrumBoardDTO> getProjectBoard(
            @Parameter(description = "项目ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.ok(studentPortalService.getProjectBoard(id));
    }

    /**
     * 获取实习岗位列表
     * 查询开放状态的实习岗位，并标注学生的申请状态
     * 使用Redis缓存，TTL为5分钟
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的实习岗位列表
     */
    @Operation(
        summary = "获取实习岗位列表",
        description = "分页查询开放状态的实习岗位，并标注学生的申请状态，使用Redis缓存"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/internship/jobs")
    public Result<PageResult<InternshipJobDTO>> getInternshipJobs(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getInternshipJobs(page, size));
    }

    /**
     * 获取学生的周报列表
     * 按提交日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的周报列表
     */
    @Operation(
        summary = "获取我的周报列表",
        description = "分页查询学生的周报列表，按提交日期降序排列"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/internship/reports/my")
    public Result<PageResult<WeeklyReportDTO>> getMyReports(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyReports(page, size));
    }

    /**
     * 获取学生的成长评价汇总
     * 查询所有评价记录并计算平均分
     * 
     * @return 评价汇总数据
     */
    @Operation(
        summary = "获取成长评价汇总",
        description = "查询学生的所有评价记录并计算平均分"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/growth/evaluation")
    public Result<EvaluationSummaryDTO> getEvaluationSummary() {
        return Result.ok(studentPortalService.getEvaluationSummary());
    }

    /**
     * 获取学生的证书列表
     * 按颁发日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的证书列表
     */
    @Operation(
        summary = "获取我的证书列表",
        description = "分页查询学生的证书列表，按颁发日期降序排列"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/growth/certificates")
    public Result<PageResult<CertificateDTO>> getMyCertificates(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyCertificates(page, size));
    }

    /**
     * 获取学生的徽章列表
     * 按获得日期降序排列
     * 
     * @param page 页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页的徽章列表
     */
    @Operation(
        summary = "获取我的徽章列表",
        description = "分页查询学生的徽章列表，按获得日期降序排列"
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
            responseCode = "401",
            description = "未授权访问",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 401, \"message\": \"未授权访问\", \"data\": null}"
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
    @GetMapping("/growth/badges")
    public Result<PageResult<BadgeDTO>> getMyBadges(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(studentPortalService.getMyBadges(page, size));
    }
}
