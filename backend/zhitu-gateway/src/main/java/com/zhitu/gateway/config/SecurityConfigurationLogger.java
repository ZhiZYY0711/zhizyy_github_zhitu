package com.zhitu.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 安全配置日志记录器
 * 在应用启动时打印安全配置信息，用于调试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityConfigurationLogger {

    private final GatewaySecurityProperties securityProperties;
    private final SwaggerProperties swaggerProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logSecurityConfiguration() {
        log.info("=".repeat(80));
        log.info("网关安全配置信息:");
        log.info("=".repeat(80));
        
        log.info("白名单路径数量: {}", securityProperties.getWhiteList().size());
        securityProperties.getWhiteList().forEach(path -> 
            log.info("  - {}", path)
        );
        
        log.info("-".repeat(80));
        log.info("Swagger 配置:");
        log.info("  启用状态: {}", swaggerProperties.isEnabled());
        log.info("  访问控制启用: {}", swaggerProperties.getAccessControl().isEnabled());
        log.info("  允许的角色: {}", swaggerProperties.getAccessControl().getAllowedRoles());
        log.info("=".repeat(80));
    }
}
