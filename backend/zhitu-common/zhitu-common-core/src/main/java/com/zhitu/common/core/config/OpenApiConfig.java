package com.zhitu.common.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 公共配置类
 * 提供统一的 API 文档配置，包括 JWT 认证和 API 分组
 * 
 * @author zhitu
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:zhitu-service}")
    private String serviceName;

    @Value("${springdoc.version:1.0.0}")
    private String version;

    @Value("${springdoc.api-docs.enabled:true}")
    private boolean apiDocsEnabled;

    @Value("${springdoc.server.url:http://localhost:8888}")
    private String serverUrl;

    @Value("${springdoc.server.description:API Gateway}")
    private String serverDescription;

    /**
     * 配置自定义 OpenAPI 信息
     * 包括 API 标题、版本、描述、联系人和许可证信息
     * 
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 强制使用网关地址，忽略自动生成的服务器 URL
        Server gatewayServer = new Server()
                .url(serverUrl)
                .description(serverDescription);
        
        return new OpenAPI()
                .info(new Info()
                        .title(serviceName + " API")
                        .version(version)
                        .description("智途云平台 - " + serviceName + " 微服务接口文档")
                        .contact(new Contact()
                                .name("智途云平台团队")
                                .email("support@zhitu.com")
                                .url("https://www.zhitu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(gatewayServer))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", jwtSecurityScheme()));
    }

    /**
     * OpenAPI 自定义器
     * 确保服务器配置不被 SpringDoc 自动生成的配置覆盖
     * 
     * @return OpenAPI 自定义器
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // 强制设置服务器列表，覆盖自动生成的配置
            Server gatewayServer = new Server()
                    .url(serverUrl)
                    .description(serverDescription);
            openApi.setServers(List.of(gatewayServer));
        };
    }

    /**
     * 配置 JWT Bearer 认证方案
     * 
     * @return JWT 安全方案配置
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name("BearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("请在此处输入 JWT Token，格式为：Bearer {token}");
    }

    /**
     * 配置公共 API 分组
     * 扫描所有 Controller 并生成 API 文档
     * 
     * @return API 分组配置
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}
