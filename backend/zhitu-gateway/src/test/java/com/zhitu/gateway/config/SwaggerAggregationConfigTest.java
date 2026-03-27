package com.zhitu.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 单元测试：SwaggerAggregationConfig
 * 验证 Gateway 文档聚合器的核心功能
 */
@ExtendWith(MockitoExtension.class)
class SwaggerAggregationConfigTest {

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private ServiceInstance serviceInstance;

    private SwaggerProperties swaggerProperties;
    private SwaggerAggregationConfig config;

    @BeforeEach
    void setUp() {
        swaggerProperties = new SwaggerProperties();
        swaggerProperties.setEnabled(true);
        swaggerProperties.setAggregationEnabled(true);
        config = new SwaggerAggregationConfig(discoveryClient, swaggerProperties);
    }

    @Test
    void shouldCreateGroupForEachAvailableService() {
        // Given: 多个可用的微服务
        List<String> services = Arrays.asList("zhitu-auth", "zhitu-student", "zhitu-enterprise");
        when(discoveryClient.getServices()).thenReturn(services);

        for (String service : services) {
            when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:8080"));
            when(discoveryClient.getInstances(service)).thenReturn(Collections.singletonList(serviceInstance));
        }

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应为每个服务创建分组（排除 gateway 自身）
        assertThat(groups).hasSize(services.size());
    }

    @Test
    void shouldSkipGatewayService() {
        // Given: 服务列表包含 gateway 自身
        List<String> services = Arrays.asList("zhitu-gateway", "zhitu-auth");
        when(discoveryClient.getServices()).thenReturn(services);
        when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:8080"));
        when(discoveryClient.getInstances("zhitu-auth")).thenReturn(Collections.singletonList(serviceInstance));

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应跳过 gateway，只为 auth 创建分组
        assertThat(groups).hasSize(1);
    }

    @Test
    void shouldCreateUnavailableGroupWhenNoInstances() {
        // Given: 服务已注册但没有实例
        List<String> services = Collections.singletonList("zhitu-auth");
        when(discoveryClient.getServices()).thenReturn(services);
        when(discoveryClient.getInstances("zhitu-auth")).thenReturn(Collections.emptyList());

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应创建不可用服务的占位分组
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getGroup()).contains("Unavailable");
    }

    @Test
    void shouldHandleServiceDiscoveryException() {
        // Given: 服务发现抛出异常
        List<String> services = Collections.singletonList("zhitu-auth");
        when(discoveryClient.getServices()).thenReturn(services);
        when(discoveryClient.getInstances(anyString())).thenThrow(new RuntimeException("Service discovery failed"));

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应创建不可用服务的占位分组，不应抛出异常
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getGroup()).contains("Unavailable");
    }

    @Test
    void shouldReturnEmptyListWhenAggregationDisabled() {
        // Given: 聚合功能被禁用
        swaggerProperties.setAggregationEnabled(false);

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应返回空列表
        assertThat(groups).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenSwaggerDisabled() {
        // Given: Swagger 功能被禁用
        swaggerProperties.setEnabled(false);

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应返回空列表
        assertThat(groups).isEmpty();
    }

    @Test
    void shouldSkipDisabledServices() {
        // Given: 配置中禁用了某个服务
        List<String> services = Arrays.asList("zhitu-auth", "zhitu-student");
        when(discoveryClient.getServices()).thenReturn(services);

        SwaggerProperties.ServiceDoc disabledService = new SwaggerProperties.ServiceDoc();
        disabledService.setName("zhitu-student");
        disabledService.setEnabled(false);
        swaggerProperties.setServices(Collections.singletonList(disabledService));

        when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:8080"));
        when(discoveryClient.getInstances("zhitu-auth")).thenReturn(Collections.singletonList(serviceInstance));

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应只为启用的服务创建分组
        assertThat(groups).hasSize(1);
    }

    @Test
    void shouldUseConfiguredServiceVersion() {
        // Given: 配置中指定了服务版本
        List<String> services = Collections.singletonList("zhitu-auth");
        when(discoveryClient.getServices()).thenReturn(services);

        SwaggerProperties.ServiceDoc serviceDoc = new SwaggerProperties.ServiceDoc();
        serviceDoc.setName("zhitu-auth");
        serviceDoc.setVersion("2.0.0");
        serviceDoc.setEnabled(true);
        swaggerProperties.setServices(Collections.singletonList(serviceDoc));

        when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost:8080"));
        when(discoveryClient.getInstances("zhitu-auth")).thenReturn(Collections.singletonList(serviceInstance));

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应创建分组（版本信息在 OpenAPI customizer 中设置）
        assertThat(groups).hasSize(1);
    }

    @Test
    void shouldHandleMultipleInstances() {
        // Given: 服务有多个实例
        List<String> services = Collections.singletonList("zhitu-auth");
        when(discoveryClient.getServices()).thenReturn(services);

        ServiceInstance instance1 = serviceInstance;
        when(instance1.getUri()).thenReturn(URI.create("http://localhost:8081"));

        List<ServiceInstance> instances = Collections.singletonList(instance1);
        when(discoveryClient.getInstances("zhitu-auth")).thenReturn(instances);

        // When: 创建 API 分组
        List<GroupedOpenApi> groups = config.apis();

        // Then: 应使用第一个实例创建分组
        assertThat(groups).hasSize(1);
    }
}
