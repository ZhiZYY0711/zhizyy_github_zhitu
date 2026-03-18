package com.zhitu.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "角色不能为空")
    private String role;        // student / enterprise / college / platform

    private String loginType;   // password / phone（预留）

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
