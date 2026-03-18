package com.zhitu.common.core.context;

import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 由网关解析 Token 后写入请求头，各服务从请求头中提取并存入 ThreadLocal
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getRole() : null;
    }

    public static Long getTenantId() {
        LoginUser user = HOLDER.get();
        return user != null ? user.getTenantId() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @Builder
    public static class LoginUser {
        private Long userId;
        private String username;
        private String role;
        private String subRole;
        private Long tenantId;
    }
}
