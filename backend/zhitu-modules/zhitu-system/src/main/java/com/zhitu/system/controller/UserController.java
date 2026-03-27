package com.zhitu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhitu.common.core.result.Result;
import com.zhitu.system.entity.SysUser;
import com.zhitu.system.mapper.SysUserMapper;
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

@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RestController
@RequestMapping("/api/system/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    @Operation(
        summary = "获取用户列表",
        description = "分页查询系统用户列表，支持按用户名、角色、状态筛选\n\n" +
            "**测试前置条件：**\n" +
            "- 需要管理员权限（platform 或 college 角色）\n" +
            "- 必须携带有效的 JWT token\n\n" +
            "**数据依赖：**\n" +
            "- 查询 sys_user 表\n" +
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
    public Result<Page<SysUser>> list(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页记录数", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "用户名，支持模糊查询", example = "admin")
            @RequestParam(required = false) String username,
            @Parameter(description = "角色类型：student/enterprise/college/platform", example = "platform")
            @RequestParam(required = false) String role,
            @Parameter(description = "状态：1-正常，2-锁定，3-注销", example = "1")
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size), wrapper);
        return Result.ok(page);
    }

    @Operation(
        summary = "根据ID获取用户详情",
        description = "通过用户ID查询用户的详细信息\n\n" +
            "**测试前置条件：**\n" +
            "- 用户ID必须存在于 sys_user 表中\n" +
            "- 需要管理员权限或查询自己的信息\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_user 表中的用户记录"
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
            description = "用户不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"用户不存在\", \"data\": null}"
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
    public Result<SysUser> getById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.ok(user);
    }

    @Operation(
        summary = "创建用户",
        description = "创建新的系统用户，用户名必须唯一\n\n" +
            "**测试前置条件：**\n" +
            "- 租户ID（tenantId）必须存在于 sys_tenant 表中\n" +
            "- 用户名不能与现有用户重复\n" +
            "- 需要管理员权限\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_tenant 表中的租户记录\n" +
            "- 密码需使用 BCrypt 加密后传入 passwordHash 字段",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysUser.class),
                examples = {
                    @ExampleObject(
                        name = "创建平台管理员",
                        summary = "创建平台管理员用户",
                        value = "{\"tenantId\": 1001, \"username\": \"admin_zhang\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138001\", \"role\": \"platform\", \"subRole\": \"admin\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建高校用户",
                        summary = "创建高校辅导员用户",
                        value = "{\"tenantId\": 1002, \"username\": \"teacher_wang\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138002\", \"role\": \"college\", \"subRole\": \"counselor\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "创建企业HR",
                        summary = "创建企业HR用户",
                        value = "{\"tenantId\": 2001, \"username\": \"hr_li\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138003\", \"role\": \"enterprise\", \"subRole\": \"hr\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-最小租户ID",
                        summary = "最小租户ID（1）",
                        value = "{\"tenantId\": 1, \"username\": \"user_min\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13000000000\", \"role\": \"student\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-最大租户ID",
                        summary = "最大租户ID（Long.MAX_VALUE）",
                        value = "{\"tenantId\": 9223372036854775807, \"username\": \"user_max\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"19999999999\", \"role\": \"student\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中文用户名",
                        summary = "包含中文字符的用户名",
                        value = "{\"tenantId\": 1001, \"username\": \"张三_老师\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138888\", \"role\": \"college\", \"subRole\": \"teacher\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-邮箱用户名",
                        summary = "邮箱格式的用户名",
                        value = "{\"tenantId\": 1001, \"username\": \"user@example.com\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13900139000\", \"role\": \"platform\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-空手机号",
                        summary = "手机号为空（可选字段）",
                        value = "{\"tenantId\": 1001, \"username\": \"no_phone_user\", \"passwordHash\": \"$2a$10$...\", \"role\": \"student\", \"status\": 1}"
                    ),
                    @ExampleObject(
                        name = "边界值-状态边界",
                        summary = "状态值为3（注销状态）",
                        value = "{\"tenantId\": 1001, \"username\": \"deactivated_user\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138000\", \"role\": \"student\", \"status\": 3}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-特殊符号用户名",
                        summary = "包含下划线、点号的用户名",
                        value = "{\"tenantId\": 1001, \"username\": \"user.name_123\", \"passwordHash\": \"$2a$10$...\", \"phone\": \"13800138000\", \"role\": \"enterprise\", \"subRole\": \"hr\", \"status\": 1}"
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
            description = "参数校验失败或用户名已存在",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "参数校验失败",
                        value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "用户名已存在",
                        value = "{\"code\": 400, \"message\": \"用户名已存在\", \"data\": null}"
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
    public Result<SysUser> create(
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUser user) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())
        );
        if (count > 0) {
            return Result.fail(400, "用户名已存在");
        }

        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        userMapper.insert(user);
        return Result.ok(user);
    }

    @Operation(
        summary = "更新用户信息",
        description = "根据用户ID更新用户信息\n\n" +
            "**测试前置条件：**\n" +
            "- 用户ID必须存在于 sys_user 表中\n" +
            "- 需要管理员权限\n" +
            "- 如果更新用户名，新用户名不能与其他用户重复\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_user 表中的现有用户记录",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SysUser.class),
                examples = {
                    @ExampleObject(
                        name = "更新用户状态",
                        summary = "更新用户状态为锁定",
                        value = "{\"status\": 2}"
                    ),
                    @ExampleObject(
                        name = "更新用户信息",
                        summary = "更新用户手机号和子角色",
                        value = "{\"phone\": \"13900139000\", \"subRole\": \"dean\"}"
                    ),
                    @ExampleObject(
                        name = "完整更新",
                        summary = "更新用户完整信息",
                        value = "{\"username\": \"admin_zhang\", \"phone\": \"13800138001\", \"role\": \"platform\", \"subRole\": \"admin\", \"status\": 1}"
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
            description = "用户不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"用户不存在\", \"data\": null}"
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
    public Result<SysUser> update(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUser user) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "用户不存在");
        }

        user.setId(id);
        user.setUpdatedAt(OffsetDateTime.now());
        userMapper.updateById(user);
        return Result.ok(user);
    }

    @Operation(
        summary = "删除用户",
        description = "根据用户ID删除用户（逻辑删除）\n\n" +
            "**测试前置条件：**\n" +
            "- 用户ID必须存在于 sys_user 表中\n" +
            "- 需要管理员权限\n" +
            "- 不能删除当前登录的用户\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_user 表中的用户记录\n" +
            "- 删除后会影响该用户的所有关联数据访问"
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
            description = "用户不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 404, \"message\": \"用户不存在\", \"data\": null}"
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
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.fail(404, "用户不存在");
        }

        userMapper.deleteById(id);
        return Result.ok();
    }
}
