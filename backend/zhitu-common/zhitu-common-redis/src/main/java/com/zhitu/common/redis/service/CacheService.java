package com.zhitu.common.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务
 * 提供统一的缓存操作接口，支持缓存获取、设置、失效等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 从缓存获取数据，如果不存在则通过 supplier 计算并存储
     *
     * @param key      缓存键
     * @param ttl      过期时间
     * @param supplier 数据提供者
     * @param <T>      数据类型
     * @return 缓存或计算的数据
     */
    public <T> T getOrSet(String key, long ttl, Supplier<T> supplier) {
        return getOrSet(key, ttl, TimeUnit.SECONDS, supplier);
    }

    /**
     * 从缓存获取数据，如果不存在则通过 supplier 计算并存储
     *
     * @param key      缓存键
     * @param ttl      过期时间
     * @param unit     时间单位
     * @param supplier 数据提供者
     * @param <T>      数据类型
     * @return 缓存或计算的数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrSet(String key, long ttl, TimeUnit unit, Supplier<T> supplier) {
        try {
            // 尝试从缓存获取
            T cached = (T) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Cache hit for key: {}", key);
                return cached;
            }

            // 缓存未命中，通过 supplier 获取数据
            log.debug("Cache miss for key: {}, computing value", key);
            T value = supplier.get();

            // 如果值不为 null，存入缓存
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, ttl, unit);
                log.debug("Cached value for key: {} with TTL: {} {}", key, ttl, unit);
            }

            return value;
        } catch (Exception e) {
            log.error("Error in getOrSet for key: {}", key, e);
            // 缓存失败时，直接返回计算结果
            return supplier.get();
        }
    }

    /**
     * 使指定键的缓存失效
     *
     * @param key 缓存键
     */
    public void invalidate(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Invalidated cache for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error invalidating cache for key: {}", key, e);
        }
    }

    /**
     * 使匹配模式的所有缓存失效
     * 注意：此方法使用 KEYS 命令，在生产环境中应谨慎使用
     * 对于大量键的场景，建议使用 SCAN 命令
     *
     * @param pattern 键模式，支持通配符 * 和 ?
     */
    public void invalidatePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.debug("Invalidated {} cache entries matching pattern: {}", deleted, pattern);
            } else {
                log.debug("No cache entries found matching pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Error invalidating cache for pattern: {}", pattern, e);
        }
    }
}
