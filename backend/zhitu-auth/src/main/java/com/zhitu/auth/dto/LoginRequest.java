package com.zhitu.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "登录请求信息")
@Data
public class LoginRequest {

    @Schema(description = "用户角色", example = "student", allowableValues = {"student", "enterprise", "college", "platform"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色不能为空")
    private String role;        // student / enterprise / college / platform

    @Schema(description = "登录类型（预留）", example = "password", allowableValues = {"password", "phone"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String loginType;   // password / phone（预留）

    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 50)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 6, maxLength = 128)
    @NotBlank(message = "密码不能为空")
    private String password;
}
