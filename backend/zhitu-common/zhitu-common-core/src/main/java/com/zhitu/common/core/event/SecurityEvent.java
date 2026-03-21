package com.zhitu.common.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 安全事件基类
 */
@Getter
public class SecurityEvent extends ApplicationEvent {
    
    private final String level;
    private final String eventType;
    private final Long userId;
    private final String ipAddress;
    private final String description;
    private final String details;

    public SecurityEvent(Object source, String level, String eventType, Long userId, 
                        String ipAddress, String description, String details) {
        super(source);
        this.level = level;
        this.eventType = eventType;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.description = description;
        this.details = details;
    }
}
