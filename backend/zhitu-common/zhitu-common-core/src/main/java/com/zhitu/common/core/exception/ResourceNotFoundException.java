package com.zhitu.common.core.exception;

import lombok.Getter;

/**
 * 资源不存在异常
 * Requirement 43.4: 当资源不存在时抛出此异常
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId = "";
    }
}
