package com.zhitu.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger 聚合配置属性类
 * 用于配置 Gateway 的 API 文档聚合功能
 */
@Data
@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    /**
     * 是否启用 Swagger UI
     */
    private boolean enabled = true;

    /**
     * 是否启用文档聚合功能
     */
    private boolean aggregationEnabled = true;

    /**
     * 微服务文档配置列表
     */
    private List<ServiceDoc> services = new ArrayList<>();

    /**
     * 访问控制配置
     */
    private AccessControl accessControl = new AccessControl();

    /**
     * 微服务文档配置
     */
    @Data
    public static class ServiceDoc {
        /**
         * 服务名称
         */
        private String name;

        /**
         * 服务 URL
         */
        private String url;

        /**
         * 服务版本
         */
        private String version;

        /**
         * 是否启用该服务的文档
         */
        private boolean enabled = true;
    }

    /**
     * 访问控制配置
     */
    @Data
    public static class AccessControl {
        /**
         * 是否启用访问控制
         */
        private boolean enabled = false;

        /**
         * 允许访问的角色列表
         */
        private List<String> allowedRoles = new ArrayList<>();

        /**
         * 是否启用审计日志
         */
        private boolean auditEnabled = true;
    }
}
