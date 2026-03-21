package com.zhitu.platform.listener;

import com.zhitu.common.core.event.SecurityEvent;
import com.zhitu.platform.service.SecurityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 安全事件监听器
 * 监听安全事件并记录到安全日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private final SecurityLogService securityLogService;

    /**
     * 处理安全事件
     * 
     * @param event 安全事件
     */
    @EventListener
    public void handleSecurityEvent(SecurityEvent event) {
        log.debug("Received security event: eventType={}, userId={}, ipAddress={}", 
                 event.getEventType(), event.getUserId(), event.getIpAddress());
        
        securityLogService.logSecurityEvent(
            event.getLevel(),
            event.getEventType(),
            event.getUserId(),
            event.getIpAddress(),
            event.getDescription(),
            event.getDetails()
        );
    }
}
