package com.zhitu.common.security.filter;

import com.zhitu.common.core.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器（用于业务服务）
 * 网关已验证 token，此处直接从请求头读取网关注入的用户信息
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 网关注入的请求头 key
    public static final String HEADER_USER_ID   = "X-User-Id";
    public static final String HEADER_ROLE      = "X-User-Role";
    public static final String HEADER_SUB_ROLE  = "X-User-Sub-Role";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userId   = request.getHeader(HEADER_USER_ID);
            String role     = request.getHeader(HEADER_ROLE);
            String subRole  = request.getHeader(HEADER_SUB_ROLE);
            String tenantId = request.getHeader(HEADER_TENANT_ID);

            if (StringUtils.hasText(userId) && StringUtils.hasText(role)) {
                // 存入 UserContext
                UserContext.set(UserContext.LoginUser.builder()
                        .userId(Long.parseLong(userId))
                        .role(role)
                        .subRole(subRole)
                        .tenantId(StringUtils.hasText(tenantId) ? Long.parseLong(tenantId) : 0L)
                        .build());

                // 存入 SecurityContext
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.warn("解析用户请求头失败: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
