package com.zhitu.common.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        // Setup is done per test as needed
    }

    @Test
    void testGetOrSet_CacheHit() {
        // Given
        String key = "test:key";
        String cachedValue = "cached-data";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(cachedValue);

        // When
        String result = cacheService.getOrSet(key, 300, () -> "new-data");

        // Then
        assertEquals(cachedValue, result);
        verify(valueOperations).get(key);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testGetOrSet_CacheMiss() {
        // Given
        String key = "test:key";
        String newValue = "new-data";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        // When
        String result = cacheService.getOrSet(key, 300, () -> newValue);

        // Then
        assertEquals(newValue, result);
        verify(valueOperations).get(key);
        verify(valueOperations).set(key, newValue, 300, TimeUnit.SECONDS);
    }

    @Test
    void testGetOrSet_WithTimeUnit() {
        // Given
        String key = "test:key";
        String newValue = "new-data";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        // When
        String result = cacheService.getOrSet(key, 5, TimeUnit.MINUTES, () -> newValue);

        // Then
        assertEquals(newValue, result);
        verify(valueOperations).set(key, newValue, 5, TimeUnit.MINUTES);
    }

    @Test
    void testGetOrSet_SupplierReturnsNull() {
        // Given
        String key = "test:key";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        // When
        String result = cacheService.getOrSet(key, 300, () -> null);

        // Then
        assertNull(result);
        verify(valueOperations).get(key);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testGetOrSet_ExceptionHandling() {
        // Given
        String key = "test:key";
        String newValue = "new-data";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis error"));

        // When
        String result = cacheService.getOrSet(key, 300, () -> newValue);

        // Then
        assertEquals(newValue, result);
    }

    @Test
    void testInvalidate() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(true);

        // When
        cacheService.invalidate(key);

        // Then
        verify(redisTemplate).delete(key);
    }

    @Test
    void testInvalidatePattern() {
        // Given
        String pattern = "test:*";
        Set<String> keys = new HashSet<>();
        keys.add("test:key1");
        keys.add("test:key2");
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // When
        cacheService.invalidatePattern(pattern);

        // Then
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate).delete(keys);
    }

    @Test
    void testInvalidatePattern_NoKeysFound() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        // When
        cacheService.invalidatePattern(pattern);

        // Then
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(anySet());
    }

    @Test
    void testInvalidatePattern_NullKeys() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(null);

        // When
        cacheService.invalidatePattern(pattern);

        // Then
        verify(redisTemplate).keys(pattern);
        verify(redisTemplate, never()).delete(anySet());
    }
}
