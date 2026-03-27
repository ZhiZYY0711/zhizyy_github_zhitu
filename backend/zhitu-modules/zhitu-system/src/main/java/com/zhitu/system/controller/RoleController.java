package com.zhitu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.Result;
import com.zhitu.system.entity.SysRole;
import com.zhitu.system.mapper.SysRoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Tag(name = "角色管理", description = "角色权限管理相关接口")
@RestController
@RequestMapping("/api/system/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleMapper roleMapper;

    @Operation(
        summary = "获取角色列表",
        description = "分页查询系统角色列表，支持按角色名称、状态筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要管理员权限\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 sys_role 表\n" +
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
    @GetMapping
    public Result<Page<SysRole>> list(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "角色名称，支持模糊查询", example = "管理员")
            @RequestParam(required = false) String roleName,
            @Parameter(description = "状态：1-启用，0-禁用", example = "1")
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);

        Page<SysRole> page = roleMapper.selectPage(new Page<>(current, size), wrapper);
        return Result.ok(page);
    }

    @Operation(
        summary = "根据ID获取角色详情",
        description = "通过角色ID查询角色的详细信息\n\n" +
            "**测试前置条件：**\n" +
            "- 角色ID必须存在于 sys_role 表中\n" +
            "- 需要管理员权限\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_role 表中的角色记录"
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
            responseCode = "404",
            description = "角色不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"角色不存在\", \"data\": null}"
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
    @GetMapping("/{id}")
    public Result<SysRole> getById(
            @Parameter(description = "角色ID", required = true, example = "1")
            @PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return Result.fail(404, "角色不存在");
        }
        return Result.ok(role);
    }

    @Operation(
        summary = "创建角色",
        description = "创建新的系统角色，角色编码必须唯一\n\n" +
            "**测试前置条件：**\n" +
            "- 租户ID（tenantId）必须存在于 sys_tenant 表中\n" +
            "- 角色编码（roleCode）不能与现有角色重复\n" +
            "- 需要管理员权限\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_tenant 表中的租户记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "角色信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysRole.class),
                examples = {
                    @ExampleObject(
                        name = "创建管理员角色",
                        summary = "创建系统管理员角色",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_ADMIN\", \"roleName\": \"系统管理员\", \"description\": \"拥有系统所有权限\", \"status\": 1, \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建教师角色",
                        summary = "创建高校教师角色",
                        value = "{\"tenantId\": 1002, \"roleCode\": \"ROLE_TEACHER\", \"roleName\": \"教师\", \"description\": \"高校教师，可管理学生和课程\", \"status\": 1, \"sortOrder\": 10}"
                    ),
                    @ExampleObject(
                        name = "创建HR角色",
                        summary = "创建企业HR角色",
                        value = "{\"tenantId\": 2001, \"roleCode\": \"ROLE_HR\", \"roleName\": \"人力资源\", \"description\": \"企业HR，可发布岗位和管理招聘\", \"status\": 1, \"sortOrder\": 20}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小排序值",
                        summary = "排序值为0（最小值）",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_TOP\", \"roleName\": \"顶级角色\", \"description\": \"排序最前的角色\", \"status\": 1, \"sortOrder\": 0}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大排序值",
                        summary = "排序值为999（最大值）",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_BOTTOM\", \"roleName\": \"底部角色\", \"description\": \"排序最后的角色\", \"status\": 1, \"sortOrder\": 999}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中文角色编码",
                        summary = "包含中文的角色编码",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_管理员\", \"roleName\": \"中文管理员\", \"description\": \"测试中文字符\", \"status\": 1, \"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号",
                        summary = "包含下划线和点号的角色编码",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_SUPER.ADMIN_V2\", \"roleName\": \"超级管理员V2\", \"description\": \"包含特殊符号的角色编码\", \"status\": 1, \"sortOrder\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-空描述",
                        summary = "描述字段为空（可选字段）",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_SIMPLE\", \"roleName\": \"简单角色\", \"status\": 1, \"sortOrder\": 10}"
                    ),
                    @ExampleObject(
                        name = "边界值-禁用状态",
                        summary = "创建时即为禁用状态",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_DISABLED\", \"roleName\": \"禁用角色\", \"description\": \"创建时即禁用\", \"status\": 0, \"sortOrder\": 100}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-长描述",
                        summary = "包含很长描述的角色",
                        value = "{\"tenantId\": 1001, \"roleCode\": \"ROLE_LONG_DESC\", \"roleName\": \"长描述角色\", \"description\": \"这是一个非常长的描述文本，用于测试系统对长文本的处理能力。描述可以包含多种信息，如角色的职责、权限范围、使用场景等详细内容。\", \"status\": 1, \"sortOrder\": 50}"
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
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数校验失败或角色编码已存在",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "参数校验失败",
                        value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "角色编码已存在",
                        value = "{\"code\": 400, \"message\": \"角色编码已存在\", \"data\": null}"
                    )
                }
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
    @PostMapping
    public Result<SysRole> create(
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRole role) {
        // 检查角色编码是否已存在
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, role.getRoleCode())
        );
        if (count > 0) {
            return Result.fail(400, "角色编码已存在");
        }

        role.setCreatedAt(OffsetDateTime.now());
        role.setUpdatedAt(OffsetDateTime.now());
        roleMapper.insert(role);
        return Result.ok(role);
    }

    @Operation(
        summary = "更新角色信息",
        description = "根据角色ID更新角色信息\n\n" +
            "**测试前置条件：**\n" +
            "- 角色ID必须存在于 sys_role 表中\n" +
            "- 需要管理员权限\n" +
            "- 如果更新角色编码，新编码不能与其他角色重复\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_role 表中的现有角色记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "角色信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysRole.class),
                examples = {
                    @ExampleObject(
                        name = "更新角色状态",
                        summary = "禁用角色",
                        value = "{\"status\": 0}"
                    ),
                    @ExampleObject(
                        name = "更新角色描述",
                        summary = "更新角色描述和排序",
                        value = "{\"description\": \"更新后的角色描述\", \"sortOrder\": 5}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新角色完整信息",
                        value = "{\"roleCode\": \"ROLE_ADMIN\", \"roleName\": \"超级管理员\", \"description\": \"拥有所有权限\", \"status\": 1, \"sortOrder\": 1}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "角色不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"角色不存在\", \"data\": null}"
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
    @PutMapping("/{id}")
    public Result<SysRole> update(
            @Parameter(description = "角色ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRole role) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "角色不存在");
        }

        role.setId(id);
        role.setUpdatedAt(OffsetDateTime.now());
        roleMapper.updateById(role);
        return Result.ok(role);
    }

    @Operation(
        summary = "删除角色",
        description = "根据角色ID删除角色（逻辑删除）\n\n" +
            "**测试前置条件：**\n" +
            "- 角色ID必须存在于 sys_role 表中\n" +
            "- 需要管理员权限\n" +
            "- 角色未被任何用户使用\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_role 表中的角色记录\n" +
            "- 删除前应检查 sys_user_role 关联表"
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
            description = "角色不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"角色不存在\", \"data\": null}"
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
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "角色ID", required = true, example = "1")
            @PathVariable Long id) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "角色不存在");
        }

        roleMapper.deleteById(id);
        return Result.ok();
    }
}
