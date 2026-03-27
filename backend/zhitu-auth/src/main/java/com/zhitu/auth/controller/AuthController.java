package com.zhitu.auth.controller;

import com.zhitu.auth.dto.LoginRequest;
import com.zhitu.auth.dto.LoginResponse;
import com.zhitu.auth.dto.RefreshTokenRequest;
import com.zhitu.auth.service.AuthService;
import com.zhitu.common.core.result.Result;
import com.zhitu.common.security.util.JwtUtils;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户认证、登录、Token 管理相关接口")
@RestController
@RequestMapping("/api/auth/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    /**
     * 统一登录
     */
    @Operation(
        summary = "${api.auth.login.summary}",
        description = "${api.auth.login.description}\n\n" +
            "**测试前置条件：**\n" +
            "- 用户账号必须已在对应租户的用户表中存在\n" +
            "- 租户（tenant）必须已创建且状态为启用\n" +
            "- 用户状态必须为正常（status=1），非锁定或注销状态\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 sys_user 表中的用户记录\n" +
            "- 依赖 sys_tenant 表中的租户记录\n" +
            "- 密码需使用 BCrypt 加密存储",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "登录请求信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = {
                    @ExampleObject(
                        name = "学生登录",
                        summary = "学生角色登录示例",
                        value = "{\"role\": \"student\", \"username\": \"zhangsan\", \"password\": \"password123\"}"
                    ),
                    @ExampleObject(
                        name = "企业登录",
                        summary = "企业角色登录示例",
                        value = "{\"role\": \"enterprise\", \"username\": \"company_hr\", \"password\": \"password123\"}"
                    ),
                    @ExampleObject(
                        name = "高校登录",
                        summary = "高校角色登录示例",
                        value = "{\"role\": \"college\", \"username\": \"teacher_wang\", \"password\": \"password123\"}"
                    ),
                    @ExampleObject(
                        name = "平台管理员登录",
                        summary = "平台管理员登录示例",
                        value = "{\"role\": \"platform\", \"username\": \"admin\", \"password\": \"admin123\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最短用户名",
                        summary = "最短用户名（3个字符）",
                        value = "{\"role\": \"student\", \"username\": \"abc\", \"password\": \"pass123\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-最长用户名",
                        summary = "最长用户名（50个字符）",
                        value = "{\"role\": \"student\", \"username\": \"abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmn\", \"password\": \"password123\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-中文用户名",
                        summary = "包含中文字符的用户名",
                        value = "{\"role\": \"student\", \"username\": \"张三_student\", \"password\": \"密码123\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-邮箱格式",
                        summary = "邮箱格式的用户名",
                        value = "{\"role\": \"enterprise\", \"username\": \"user@example.com\", \"password\": \"P@ssw0rd!\"}"
                    ),
                    @ExampleObject(
                        name = "特殊字符-下划线和数字",
                        summary = "包含下划线和数字的用户名",
                        value = "{\"role\": \"college\", \"username\": \"user_123_test\", \"password\": \"Test@2024\"}"
                    ),
                    @ExampleObject(
                        name = "边界值-空字符串测试",
                        summary = "空字符串（应该失败）",
                        value = "{\"role\": \"\", \"username\": \"\", \"password\": \"\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "${api.auth.login.response.200}",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "${api.auth.login.response.400}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "${api.auth.login.response.401}",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "用户不存在",
                        value = "{\"code\": 1001, \"message\": \"用户不存在\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "密码错误",
                        value = "{\"code\": 1002, \"message\": \"密码错误\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "账号已被禁用",
                        value = "{\"code\": 1003, \"message\": \"账号已被禁用\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "租户不存在",
                        value = "{\"code\": 1007, \"message\": \"租户不存在\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "租户已被禁用",
                        value = "{\"code\": 1008, \"message\": \"租户已被禁用\", \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "${api.auth.login.response.500}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/login")
    public Result<LoginResponse> login(
        @Parameter(description = "${api.auth.login.param.request}", required = true)
        @Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    /**
     * 刷新 Token
     */
    @Operation(
        summary = "${api.auth.refresh.summary}",
        description = "${api.auth.refresh.description}\n\n" +
            "**测试前置条件：**\n" +
            "- 必须先通过 /login 接口获取有效的 refreshToken\n" +
            "- refreshToken 必须未过期且未被撤销\n" +
            "- 用户账号必须仍然有效（未被删除或禁用）\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 Redis 中存储的 refreshToken\n" +
            "- 依赖 sys_user 表中的用户状态",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "刷新令牌请求信息",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RefreshTokenRequest.class),
                examples = @ExampleObject(
                    name = "刷新令牌",
                    summary = "使用刷新令牌获取新的访问令牌",
                    value = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "${api.auth.refresh.response.200}",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Result.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "${api.auth.login.response.400}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 422, \"message\": \"参数校验失败\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "${api.auth.refresh.response.401}",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "刷新令牌无效",
                        value = "{\"code\": 1006, \"message\": \"Refresh Token 无效或已过期\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "Token已过期",
                        value = "{\"code\": 1005, \"message\": \"Token 已过期\", \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "${api.auth.login.response.500}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/token/refresh")
    public Result<LoginResponse> refresh(
        @Parameter(description = "${api.auth.refresh.param.request}", required = true)
        @Valid @RequestBody RefreshTokenRequest req) {
        return Result.ok(authService.refresh(req));
    }

    /**
     * 登出
     */
    @Operation(
        summary = "${api.auth.logout.summary}",
        description = "${api.auth.logout.description}\n\n" +
            "**测试前置条件：**\n" +
            "- 用户必须已登录并持有有效的 JWT token\n" +
            "- Authorization header 格式：Bearer {token}\n\n" +
            "**数据依赖：**\n" +
            "- 依赖 Redis 中存储的 token 和 refreshToken\n" +
            "- 清除操作不依赖数据库，仅清除缓存"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "${api.auth.logout.response.200}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"操作成功\", \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "${api.auth.logout.response.401}",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Token无效",
                        value = "{\"code\": 1004, \"message\": \"Token 无效\", \"data\": null}"
                    ),
                    @ExampleObject(
                        name = "Token已过期",
                        value = "{\"code\": 1005, \"message\": \"Token 已过期\", \"data\": null}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "${api.auth.login.response.500}",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 500, \"message\": \"服务器内部错误\", \"data\": null}"
                )
            )
        )
    })
    @PostMapping("/logout")
    public Result<Void> logout(
            @Parameter(description = "${api.auth.logout.param.authorization}", required = false)
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "${api.auth.logout.param.request}", required = false)
            @RequestBody(required = false) RefreshTokenRequest req) {

        Long userId = null;
        String refreshToken = req != null ? req.getRefreshToken() : null;

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                userId = jwtUtils.getUserId(token);
            } catch (Exception ignored) {}
        }

        if (userId != null) {
            authService.logout(userId, refreshToken);
        }
        return Result.ok();
    }
}
