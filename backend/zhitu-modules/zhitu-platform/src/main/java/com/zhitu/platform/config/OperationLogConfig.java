package com.zhitu.platform.config;

import com.zhitu.common.core.interceptor.OperationLogInterceptor;
import com.zhitu.platform.entity.OperationLog;
import com.zhitu.platform.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 操作日志配置
 * 注册拦截器并配置日志保存逻辑
 */
@Configuration
@RequiredArgsConstructor
public class OperationLogConfig implements WebMvcConfigurer {

    private final OperationLogService operationLogService;

    @Bean
    public OperationLogInterceptor operationLogInterceptor() {
        return new OperationLogInterceptor(this::saveOperationLog);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLogInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",           // 排除认证接口
                    "/api/system/v1/health",  // 排除健康检查
                    "/api/actuator/**"        // 排除监控端点
                );
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(Map<String, Object> logData) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(getLong(logData, "userId"));
            operationLog.setUserName(getString(logData, "userName"));
            operationLog.setTenantId(getLong(logData, "tenantId"));
            operationLog.setModule(getString(logData, "module"));
            operationLog.setOperation(getString(logData, "operation"));
            operationLog.setRequestParams(getString(logData, "requestParams"));
            operationLog.setResponseStatus(getInteger(logData, "responseStatus"));
            operationLog.setResult(getString(logData, "result"));
            operationLog.setIpAddress(getString(logData, "ipAddress"));
            operationLog.setUserAgent(getString(logData, "userAgent"));
            operationLog.setExecutionTime(getInteger(logData, "executionTime"));
            operationLog.setCreatedAt(OffsetDateTime.now());
            
            // 异步保存
            operationLogService.saveLogAsync(operationLog);
        } catch (Exception e) {
            // 静默失败，不影响主流程
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
