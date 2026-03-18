package com.zhitu.common.security.util;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.exception.BusinessException;
import com.zhitu.common.core.result.ResultCode;

/**
 * Security 工具类
 */
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static String getCurrentRole() {
        return UserContext.getRole();
    }

    public static Long getCurrentTenantId() {
        return UserContext.getTenantId();
    }

    public static UserContext.LoginUser getCurrentUser() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    public static boolean hasRole(String role) {
        return role.equals(UserContext.getRole());
    }
}
