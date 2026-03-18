package com.zhitu.auth.controller;

import com.zhitu.auth.dto.LoginRequest;
import com.zhitu.auth.dto.LoginResponse;
import com.zhitu.auth.dto.RefreshTokenRequest;
import com.zhitu.auth.service.AuthService;
import com.zhitu.common.core.result.Result;
import com.zhitu.common.security.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    /**
     * 统一登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/token/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return Result.ok(authService.refresh(req));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
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
