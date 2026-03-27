package com.zhitu.common.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OpenApiConfig
 * 
 * Tests OpenAPI configuration:
 * - API information (title, version, description)
 * - JWT Bearer authentication scheme
 * - Public API grouping
 * 
 * Validates: Requirements 3.2, 3.3, 1.5
 */
@DisplayName("OpenAPI Configuration Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
        ReflectionTestUtils.setField(openApiConfig, "serviceName", "zhitu-test-service");
        ReflectionTestUtils.setField(openApiConfig, "version", "1.0.0");
        ReflectionTestUtils.setField(openApiConfig, "apiDocsEnabled", true);
    }

    @Test
    @DisplayName("customOpenAPI should configure API information correctly")
    void testCustomOpenAPI_ConfiguresApiInfo() {
        // When: Create custom OpenAPI configuration
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then: API info should be configured correctly
        assertThat(openAPI).isNotNull();
        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("zhitu-test-service API");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription()).contains("智途云平台");
        assertThat(info.getDescription()).contains("zhitu-test-service");
    }

    @Test
    @DisplayName("customOpenAPI should configure contact information")
    void testCustomOpenAPI_ConfiguresContact() {
        // When: Create custom OpenAPI configuration
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then: Contact info should be configured
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("智途云平台团队");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("support@zhitu.com");
        assertThat(openAPI.getInfo().getContact().getUrl()).isEqualTo("https://www.zhitu.com");
    }

    @Test
    @DisplayName("customOpenAPI should configure license information")
    void testCustomOpenAPI_ConfiguresLicense() {
        // When: Create custom OpenAPI configuration
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then: License info should be configured
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache 2.0");
        assertThat(openAPI.getInfo().getLicense().getUrl())
                .isEqualTo("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Test
    @DisplayName("customOpenAPI should configure JWT Bearer security scheme")
    void testCustomOpenAPI_ConfiguresJwtSecurity() {
        // When: Create custom OpenAPI configuration
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then: JWT security scheme should be configured
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("BearerAuth");
        
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("BearerAuth");
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(securityScheme.getName()).isEqualTo("BearerAuth");
    }

    @Test
    @DisplayName("customOpenAPI should add security requirement")
    void testCustomOpenAPI_AddsSecurityRequirement() {
        // When: Create custom OpenAPI configuration
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then: Security requirement should be added
        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().get(0).containsKey("BearerAuth")).isTrue();
    }

    @Test
    @DisplayName("publicApi should configure public API group")
    void testPublicApi_ConfiguresGroup() {
        // When: Create public API group
        GroupedOpenApi publicApi = openApiConfig.publicApi();

        // Then: Group should be configured correctly
        assertThat(publicApi).isNotNull();
        assertThat(publicApi.getGroup()).isEqualTo("public");
    }
}
