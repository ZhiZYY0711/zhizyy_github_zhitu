package com.zhitu.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "登录响应信息")
@Data
@Builder
public class LoginResponse {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;
    
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
    
    @Schema(description = "访问令牌有效期（秒）", example = "3600", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long expiresIn;     // access_token 有效期（秒）
    
    @Schema(description = "用户信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserInfo userInfo;

    @Schema(description = "用户信息")
    @Data
    @Builder
    public static class UserInfo {
        @Schema(description = "用户ID（字符串格式，避免JS大数精度问题）", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
        private String id;          // 字符串，避免 JS 大数精度问题
        
        @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
        private String username;
        
        @Schema(description = "用户角色", example = "student", requiredMode = Schema.RequiredMode.REQUIRED)
        private String role;
        
        @Schema(description = "子角色", example = "undergraduate", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String subRole;
        
        @Schema(description = "租户ID", example = "tenant_001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String tenantId;
    }
}
