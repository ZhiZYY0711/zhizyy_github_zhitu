package com.zhitu.common.core.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResourceNotFoundException 单元测试
 */
@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("使用资源类型和 ID 创建异常")
    void testCreateWithTypeAndId() {
        // Given
        String resourceType = "User";
        String resourceId = "12345";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resourceType, resourceId);

        // Then
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceId));
        assertEquals("User not found: 12345", exception.getMessage());
    }

    @Test
    @DisplayName("使用自定义消息创建异常")
    void testCreateWithMessage() {
        // Given
        String message = "The requested resource does not exist";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertEquals("Resource", exception.getResourceType());
        assertEquals("", exception.getResourceId());
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("验证异常消息格式")
    void testMessageFormat() {
        // Given & When
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Project", "proj-001");
        ResourceNotFoundException exception2 = new ResourceNotFoundException("Student", "stu-999");

        // Then
        assertEquals("Project not found: proj-001", exception1.getMessage());
        assertEquals("Student not found: stu-999", exception2.getMessage());
    }
}
