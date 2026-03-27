package com.zhitu.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger 文档聚合配置类
 * 从 Nacos 服务注册中心动态发现微服务并聚合其 API 文档
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SwaggerAggregationConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerProperties swaggerProperties;

    /**
     * 创建所有微服务的文档分组
     * 从 Nacos DiscoveryClient 获取服务列表，为每个服务创建 GroupedOpenApi 分组
     *
     * @return 文档分组列表
     */
    @Bean
    public List<GroupedOpenApi> apis() {
        List<GroupedOpenApi> groups = new ArrayList<>();

        // 检查是否启用聚合功能
        if (!swaggerProperties.isEnabled() || !swaggerProperties.isAggregationEnabled()) {
            log.info("Swagger aggregation is disabled");
            return groups;
        }

        // 获取所有已注册的服务
        List<String> services = discoveryClient.getServices();
        log.info("Discovered {} services from Nacos", services.size());

        for (String serviceName : services) {
            try {
                // 跳过网关自身
                if ("zhitu-gateway".equals(serviceName)) {
                    continue;
                }

                // 检查服务是否在配置中被禁用
                if (isServiceDisabled(serviceName)) {
                    log.debug("Service {} is disabled in configuration", serviceName);
                    continue;
                }

                // 获取服务实例
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                if (instances.isEmpty()) {
                    log.warn("No instances found for service: {}", serviceName);
                    groups.add(createUnavailableServiceGroup(serviceName));
                    continue;
                }

                // 使用第一个实例创建文档分组
                ServiceInstance instance = instances.get(0);
                String serviceUrl = instance.getUri().toString();
                log.info("Creating API group for service: {} at {}", serviceName, serviceUrl);

                GroupedOpenApi group = createServiceGroup(serviceName, serviceUrl);
                groups.add(group);

            } catch (Exception e) {
                log.error("Failed to create API group for service: {}", serviceName, e);
                // 服务不可用时创建占位分组
                groups.add(createUnavailableServiceGroup(serviceName));
            }
        }

        log.info("Successfully created {} API documentation groups", groups.size());
        return groups;
    }

    /**
     * 为指定服务创建文档分组
     * 配置服务路径前缀，使文档能够正确显示该服务的所有 API
     *
     * @param serviceName 服务名称
     * @param serviceUrl  服务 URL
     * @return GroupedOpenApi 分组
     */
    private GroupedOpenApi createServiceGroup(String serviceName, String serviceUrl) {
        // 根据服务名称确定路径前缀
        String pathPrefix = determinePathPrefix(serviceName);

        return GroupedOpenApi.builder()
                .group(serviceName)
                .pathsToMatch(pathPrefix + "/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(new Info()
                            .title(serviceName + " API")
                            .version(getServiceVersion(serviceName))
                            .description("API documentation for " + serviceName));
                })
                .build();
    }

    /**
     * 创建服务不可用时的占位分组
     * 在聚合文档界面显示服务不可用的提示信息
     *
     * @param serviceName 服务名称
     * @return GroupedOpenApi 分组
     */
    private GroupedOpenApi createUnavailableServiceGroup(String serviceName) {
        return GroupedOpenApi.builder()
                .group(serviceName + " (Unavailable)")
                .pathsToMatch("/unavailable/" + serviceName + "/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.info(new Info()
                            .title(serviceName + " API (Service Unavailable)")
                            .version("N/A")
                            .description("Service " + serviceName + " is currently unavailable. " +
                                    "Please check if the service is running and registered in Nacos."));
                })
                .build();
    }

    /**
     * 根据服务名称确定路径前缀
     * 保留各服务的原始路径前缀，确保路由正确
     *
     * @param serviceName 服务名称
     * @return 路径前缀
     */
    private String determinePathPrefix(String serviceName) {
        // 根据 Gateway 路由配置确定路径前缀
        return switch (serviceName) {
            case "zhitu-auth" -> "/api/auth";
            case "zhitu-system" -> "/api/system";
            case "zhitu-student" -> "/api/student-portal";
            case "zhitu-enterprise" -> "/api/portal-enterprise";
            case "zhitu-college" -> "/api/portal-college";
            case "zhitu-platform" -> "/api/portal-platform";
            default -> "/api/" + serviceName.replace("zhitu-", "");
        };
    }

    /**
     * 获取服务版本
     * 从配置中读取服务版本，如果未配置则返回默认版本
     *
     * @param serviceName 服务名称
     * @return 服务版本
     */
    private String getServiceVersion(String serviceName) {
        return swaggerProperties.getServices().stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .map(SwaggerProperties.ServiceDoc::getVersion)
                .orElse("1.0.0");
    }

    /**
     * 检查服务是否在配置中被禁用
     *
     * @param serviceName 服务名称
     * @return true 如果服务被禁用
     */
    private boolean isServiceDisabled(String serviceName) {
        return swaggerProperties.getServices().stream()
                .filter(s -> s.getName().equals(serviceName))
                .findFirst()
                .map(s -> !s.isEnabled())
                .orElse(false);
    }
}
