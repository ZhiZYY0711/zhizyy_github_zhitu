package com.zhitu.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhitu.common.core.result.PageResult;
import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.OperationLogDTO;
import com.zhitu.platform.dto.PlatformDashboardStatsDTO;
import com.zhitu.platform.dto.SecurityLogDTO;
import com.zhitu.platform.service.OperationLogService;
import com.zhitu.platform.service.PlatformService;
import com.zhitu.platform.service.SecurityLogService;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 平台系统管理接口（/api/system/v1）
 */
@Tag(name = "平台系统", description = "平台系统管理相关接口")
@RestController
@RequestMapping("/api/system/v1")
@RequiredArgsConstructor
public class PlatformSystemController {

    private final PlatformService platformService;
    private final OperationLogService operationLogService;
    private final SecurityLogService securityLogService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /**
     * 获取平台仪表板统计数据
     * GET /api/system/v1/dashboard/stats
     * 
     * Requirements: 28.1-28.7
     */
    @Operation(
        summary = "获取平台仪表板统计数据",
        description = "获取平台整体统计数据，包括用户数、企业数、项目数等关键指标\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 统计数据来自多个表：sys_user, enterprise_info, college, training_project, internship_job\n" +
            "- 无需特定数据预先存在，零值也是有效结果"
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
    @GetMapping("/dashboard/stats")
    public Result<PlatformDashboardStatsDTO> getDashboardStats() {
        return Result.ok(platformService.getDashboardStats());
    }

    // ── Tenants ───────────────────────────────────────────────────────────────

    /**
     * 获取租户列表
     * GET /api/system/v1/tenants/colleges
     * 
     * Requirements: 30.1-30.6
     */
    @Operation(
        summary = "获取租户列表",
        description = "分页查询租户（高校）列表，支持按类型和状态筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 sys_tenant 表\n" +
            "- 无需其他数据预先存在，空列表也是有效结果"
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
    @GetMapping("/tenants/colleges")
    public Result<IPage<com.zhitu.platform.dto.TenantDTO>> getTenantList(
            @Parameter(description = "租户类型", example = "college")
            @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "租户状态：active-活跃，inactive-停用", example = "active")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return Result.ok(platformService.getTenantList(type, status, page, size));
    }

    // ── Tags ──────────────────────────────────────────────────────────────────

    @Operation(
        summary = "获取标签列表",
        description = "获取系统标签列表，支持按分类筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 system_tag 表\n" +
            "- 标签用于技能、行业、项目类型等分类"
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
    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> getTags(
            @Parameter(description = "标签分类", example = "skill")
            @RequestParam(required = false) String category) {
        return Result.ok(platformService.getTags(category));
    }

    @Operation(
        summary = "创建标签",
        description = "创建新的系统标签\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 标签名称在同一分类下不能重复\n\n" +
            "**数据依赖：**\n" +
            "- 无强制依赖，但建议标签分类标准化",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "标签信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "创建技能标签",
                        summary = "创建技能类标签",
                        value = "{\"category\": \"skill\", \"name\": \"Java\", \"description\": \"Java编程语言\", \"color\": \"#FF5722\"}"
                    ),
                    @ExampleObject(
                        name = "创建行业标签",
                        summary = "创建行业类标签",
                        value = "{\"category\": \"industry\", \"name\": \"互联网\", \"description\": \"互联网行业\", \"color\": \"#2196F3\"}"
                    ),
                    @ExampleObject(
                        name = "创建项目标签",
                        summary = "创建项目类标签",
                        value = "{\"category\": \"project\", \"name\": \"Web开发\", \"description\": \"Web应用开发项目\", \"color\": \"#4CAF50\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
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
    @PostMapping("/tags")
    public Result<Void> createTag(
            @Parameter(description = "标签信息", required = true)
            @RequestBody Map<String, Object> req) {
        platformService.createTag(req);
        return Result.ok();
    }

    @Operation(
        summary = "删除标签",
        description = "删除指定的系统标签\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 标签ID必须存在于 system_tag 表中\n" +
            "- 标签未被任何实体引用（项目、岗位、学生等）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 system_tag 表中的标签记录\n" +
            "- 删除前应检查标签的使用情况"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "删除成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "标签不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"标签不存在\", \"data\": null}"
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
    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID", required = true, example = "1")
            @PathVariable Long id) {
        platformService.deleteTag(id);
        return Result.ok();
    }

    // ── Skill Tree ────────────────────────────────────────────────────────────

    @Operation(
        summary = "获取技能树",
        description = "获取完整的技能树结构\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 skill_tree 表中的技能树数据\n" +
            "- 技能之间通过 parent_id 建立父子关系"
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
    @GetMapping("/skills/tree")
    public Result<List<Map<String, Object>>> getSkillTree() {
        return Result.ok(platformService.getSkillTree());
    }

    // ── Certificate Templates ─────────────────────────────────────────────────

    @Operation(
        summary = "获取证书模板列表",
        description = "获取所有可用的证书模板\n\n" +
            "**测试前置条件：**\n" +
            "- 需要已登录用户\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 certificate_template 表\n" +
            "- 模板用于颁发学生证书"
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
    @GetMapping("/certificates/templates")
    public Result<List<Map<String, Object>>> getCertificateTemplates() {
        return Result.ok(platformService.getCertificateTemplates());
    }

    // ── Contract Templates ────────────────────────────────────────────────────

    @Operation(
        summary = "获取合同模板列表",
        description = "获取所有可用的合同模板\n\n" +
            "**测试前置条件：**\n" +
            "- 需要企业或高校管理员权限\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 contract_template 表\n" +
            "- 模板用于生成实习合同"
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
    @GetMapping("/contracts/templates")
    public Result<List<Map<String, Object>>> getContractTemplates() {
        return Result.ok(platformService.getContractTemplates());
    }

    // ── Logs ──────────────────────────────────────────────────────────────────

    /**
     * 获取操作日志列表
     * GET /api/system/v1/logs/operation
     * 
     * Requirements: 39.1, 39.2, 39.3, 39.6
     */
    @Operation(
        summary = "获取操作日志列表",
        description = "分页查询操作日志，支持按用户、模块、结果和时间范围筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 operation_log 表\n" +
            "- 日志由系统自动记录，无需手动创建"
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
    @GetMapping("/logs/operation")
    public Result<PageResult<OperationLogDTO>> getOperationLogs(
            @Parameter(description = "用户ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "模块名称", example = "user")
            @RequestParam(required = false) String module,
            @Parameter(description = "操作结果：success-成功，failure-失败", example = "success")
            @RequestParam(required = false) String result,
            @Parameter(description = "开始时间", example = "2024-01-01T00:00:00Z")
            @RequestParam(required = false) OffsetDateTime startTime,
            @Parameter(description = "结束时间", example = "2024-12-31T23:59:59Z")
            @RequestParam(required = false) OffsetDateTime endTime,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(operationLogService.getLogs(userId, module, result, startTime, endTime, page, size));
    }

    /**
     * 获取安全日志列表
     * GET /api/system/v1/logs/security
     * 
     * Requirements: 40.1, 40.2, 40.3, 40.6
     */
    @Operation(
        summary = "获取安全日志列表",
        description = "分页查询安全日志，支持按级别筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须以平台管理员角色登录\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 security_log 表\n" +
            "- 日志记录登录失败、权限异常等安全事件"
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
    @GetMapping("/logs/security")
    public Result<PageResult<SecurityLogDTO>> getSecurityLogs(
            @Parameter(description = "日志级别：info-信息，warning-警告，error-错误", example = "warning")
            @RequestParam(required = false) String level,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页记录数", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(securityLogService.getSecurityLogs(level, page, size));
    }
}
