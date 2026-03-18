package com.zhitu.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "zhitu.security")
public class GatewaySecurityProperties {

    private List<String> whiteList = List.of(
            "/api/auth/v1/login",
            "/api/auth/v1/token/refresh",
            "/actuator/**"
    );
}
