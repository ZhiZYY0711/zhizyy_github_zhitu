package com.zhitu.common.core.interceptor;

import com.zhitu.common.core.context.UserContext;
import com.zhitu.common.core.util.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 操作日志拦截器
 * 记录所有 API 请求的详细信息，用于审计和故障排查
 * 
 * Requirements: 39.1-39.7, 48.1-48.2
 */
@Slf4j
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "startTime";
    private static final int MAX_PARAM_LENGTH = 2000;
    
    private final Consumer<Map<String, Object>> logConsumer;

    public OperationLogInterceptor(Consumer<Map<String, Object>> logConsumer) {
        this.logConsumer = logConsumer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求开始时间
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        try {
            // 收集日志数据
            Map<String, Object> logData = new HashMap<>();
            
            // 基本请求信息
            logData.put("method", request.getMethod());
            logData.put("uri", request.getRequestURI());
            logData.put("ipAddress", getClientIp(request));
            logData.put("userAgent", request.getHeader("User-Agent"));
            
            // 用户上下文
            UserContext.LoginUser user = UserContext.get();
            if (user != null) {
                logData.put("userId", user.getUserId());
                logData.put("userName", user.getUsername());
                logData.put("tenantId", user.getTenantId());
            }
            
            // 模块和操作
            String[] moduleAndOp = extractModuleAndOperation(request.getRequestURI());
            logData.put("module", moduleAndOp[0]);
            logData.put("operation", moduleAndOp[1]);
            
            // 请求参数
            logData.put("requestParams", collectRequestParams(request));
            
            // 响应信息
            Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
            int executionTime = startTime != null ? 
                (int) (System.currentTimeMillis() - startTime) : 0;
            
            logData.put("executionTime", executionTime);
            logData.put("responseStatus", response.getStatus());
            logData.put("result", response.getStatus() < 400 ? "success" : "failure");
            
            // 调用消费者处理日志
            if (logConsumer != null) {
                logConsumer.accept(logData);
            }
            
        } catch (Exception e) {
            log.error("Failed to record operation log", e);
        }
    }

    /**
     * 获取客户端真实 IP 地址
     * 考虑代理和负载均衡器的情况
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从 URI 中提取模块和操作
     * 例如: /api/student-portal/v1/dashboard -> module=student, operation=get_dashboard
     */
    private String[] extractModuleAndOperation(String uri) {
        String module = "unknown";
        String operation = "unknown";
        
        try {
            // 移除 /api/ 前缀
            String path = uri.replaceFirst("^/api/", "");
            
            // 提取模块
            if (path.startsWith("student-portal/") || path.startsWith("student/")) {
                module = "student";
            } else if (path.startsWith("portal-enterprise/") || path.startsWith("enterprise/")) {
                module = "enterprise";
            } else if (path.startsWith("college/")) {
                module = "college";
            } else if (path.startsWith("portal-platform/") || path.startsWith("system/")) {
                module = "platform";
            }
            
            // 提取操作（使用 URI 的最后几段）
            String[] segments = path.split("/");
            if (segments.length >= 2) {
                // 取最后两段作为操作名
                int start = Math.max(0, segments.length - 2);
                operation = String.join("_", segments[start], segments[segments.length - 1]);
                // 移除数字 ID
                operation = operation.replaceAll("\\d+", "{id}");
            }
        } catch (Exception e) {
            log.warn("Failed to extract module and operation from URI: {}", uri, e);
        }
        
        return new String[]{module, operation};
    }

    /**
     * 收集请求参数（查询参数和请求体）
     * 限制长度以避免日志过大
     */
    private String collectRequestParams(HttpServletRequest request) {
        try {
            Map<String, Object> params = new HashMap<>();
            
            // 收集查询参数
            if (request.getQueryString() != null) {
                params.put("query", request.getQueryString());
            }
            
            // 收集请求体（仅对 POST/PUT/PATCH）
            String method = request.getMethod();
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                if (request instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                    byte[] content = wrapper.getContentAsByteArray();
                    if (content.length > 0) {
                        String body = new String(content, StandardCharsets.UTF_8);
                        // 限制长度
                        if (body.length() > MAX_PARAM_LENGTH) {
                            body = body.substring(0, MAX_PARAM_LENGTH) + "...[truncated]";
                        }
                        params.put("body", body);
                    }
                }
            }
            
            String result = JsonUtils.toJson(params);
            if (result.length() > MAX_PARAM_LENGTH) {
                result = result.substring(0, MAX_PARAM_LENGTH) + "...[truncated]";
            }
            return result;
            
        } catch (Exception e) {
            log.warn("Failed to collect request params", e);
            return "{}";
        }
    }
}
