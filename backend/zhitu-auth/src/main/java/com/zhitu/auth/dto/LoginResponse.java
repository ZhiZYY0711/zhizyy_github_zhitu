package com.zhitu.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;     // access_token 有效期（秒）
    private UserInfo userInfo;

    @Data
    @Builder
    public static class UserInfo {
        private String id;          // 字符串，避免 JS 大数精度问题
        private String username;
        private String role;
        private String subRole;
        private String tenantId;
    }
}
