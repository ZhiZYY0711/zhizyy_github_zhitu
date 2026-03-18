package com.zhitu.common.core.result;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(422, "参数校验失败"),
    SERVER_ERROR(500, "服务器内部错误"),

    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    TOKEN_INVALID(1004, "Token 无效"),
    TOKEN_EXPIRED(1005, "Token 已过期"),
    REFRESH_TOKEN_INVALID(1006, "Refresh Token 无效或已过期"),
    TENANT_NOT_FOUND(1007, "租户不存在"),
    TENANT_DISABLED(1008, "租户已被禁用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
