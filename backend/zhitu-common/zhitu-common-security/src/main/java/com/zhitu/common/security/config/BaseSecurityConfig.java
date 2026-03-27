package com.zhitu.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.core.result.Result;
import com.zhitu.common.core.result.ResultCode;
import com.zhitu.common.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 业务服务通用 Security 配置
 * 各业务服务继承此类，或直接使用此 Bean
 */
public abstract class BaseSecurityConfig {

    protected SecurityFilterChain buildSecurityFilterChain(HttpSecurity http,
                                                            String... additionalWhiteList) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/actuator/**").permitAll();
                // Swagger/OpenAPI paths - 支持完整路径和相对路径
                auth.requestMatchers("/v3/api-docs/**").permitAll();
                auth.requestMatchers("/api/*/v3/api-docs/**").permitAll();
                auth.requestMatchers("/swagger-ui/**").permitAll();
                auth.requestMatchers("/swagger-ui.html").permitAll();
                for (String path : additionalWhiteList) {
                    auth.requestMatchers(path).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(new JwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    try { writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, Result.fail(ResultCode.UNAUTHORIZED)); }
                    catch (Exception ignored) {}
                })
                .accessDeniedHandler((req, res, e) -> {
                    try { writeJson(res, HttpServletResponse.SC_FORBIDDEN, Result.fail(ResultCode.FORBIDDEN)); }
                    catch (Exception ignored) {}
                })
            );

        return http.build();
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }

    @Bean
    public abstract SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception;
}
