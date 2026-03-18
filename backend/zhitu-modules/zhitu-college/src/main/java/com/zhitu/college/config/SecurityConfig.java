package com.zhitu.college.config;

import com.zhitu.common.security.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return buildSecurityFilterChain(http);
    }
}
