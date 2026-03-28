package com.zhitu.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "zhitu.security")
public class GatewaySecurityProperties {

    private List<String> whiteList = new ArrayList<>();

    /**
     * 初始化默认白名单
     * 如果配置文件中没有配置白名单，则使用默认值
     */
    @PostConstruct
    public void init() {
        if (whiteList == null || whiteList.isEmpty()) {
            whiteList = getDefaultWhiteList();
        }
    }

    /**
     * 获取默认白名单
     */
    private List<String> getDefaultWhiteList() {
        return new ArrayList<>(List.of(
                "/api/auth/v1/login",
                "/api/auth/v1/logout",
                "/api/auth/v1/token/refresh",
                "/actuator/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/api/auth/v3/api-docs",
                "/api/auth/v3/api-docs/**",
                "/api/system/v3/api-docs",
                "/api/system/v3/api-docs/**",
                "/api/student-portal/v3/api-docs",
                "/api/student-portal/v3/api-docs/**",
                "/api/portal-enterprise/v3/api-docs",
                "/api/portal-enterprise/v3/api-docs/**",
                "/api/portal-college/v3/api-docs",
                "/api/portal-college/v3/api-docs/**",
                "/api/portal-platform/v3/api-docs",
                "/api/portal-platform/v3/api-docs/**",
                "/webjars/**"
        ));
    }
}
