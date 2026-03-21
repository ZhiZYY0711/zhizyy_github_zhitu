# Redis Caching Infrastructure Usage Guide

## Overview

This module provides a unified caching infrastructure for the Zhitu Cloud Platform, including:
- **CacheService**: Service class for cache operations (getOrSet, invalidate, invalidatePattern)
- **CacheConstants**: Centralized cache key patterns and TTL configurations

## CacheService Usage

### Basic Usage

```java
@Service
@RequiredArgsConstructor
public class StudentPortalService {
    private final CacheService cacheService;
    private final StudentMapper studentMapper;
    
    public DashboardStatsDTO getDashboardStats(Long userId) {
        String cacheKey = String.format(CacheConstants.KEY_STUDENT_DASHBOARD, userId);
        
        return cacheService.getOrSet(
            cacheKey,
            CacheConstants.TTL_DASHBOARD_STATS,
            CacheConstants.TTL_DASHBOARD_STATS_UNIT,
            () -> {
                // This lambda is only executed on cache miss
                return studentMapper.selectDashboardStats(userId);
            }
        );
    }
}
```

### Cache Invalidation

```java
@Service
@RequiredArgsConstructor
public class StudentService {
    private final CacheService cacheService;
    
    public void updateStudent(Long userId, StudentDTO dto) {
        // Update database
        studentMapper.updateById(userId, dto);
        
        // Invalidate specific cache
        String cacheKey = String.format(CacheConstants.KEY_STUDENT_DASHBOARD, userId);
        cacheService.invalidate(cacheKey);
    }
    
    public void enrollInProject(Long userId, Long projectId) {
        // Enroll student
        enrollmentMapper.insert(userId, projectId);
        
        // Invalidate all student project-related caches
        cacheService.invalidatePattern(CacheConstants.PATTERN_STUDENT_PROJECTS);
    }
}
```

## CacheConstants Reference

### TTL Constants

| Constant | Value | Use Case |
|----------|-------|----------|
| `TTL_DASHBOARD_STATS` | 5 minutes | Dashboard statistics |
| `TTL_LIST_DATA` | 5 minutes | Paginated list data |
| `TTL_ANALYTICS` | 30 minutes | Analytics/reports |
| `TTL_CONFIG` | 1 hour | Configuration data |
| `TTL_HEALTH` | 1 minute | Health metrics |
| `TTL_RECOMMENDATIONS` | 15 minutes | Recommendation data |
| `TTL_CAPABILITY` | 10 minutes | Student capability data |
| `TTL_ACTIVITIES` | 3 minutes | Activity feed data |
| `TTL_TRENDS` | 1 hour | Trend analysis data |
| `TTL_WARNING_STATS` | 10 minutes | Warning statistics |
| `TTL_BANNERS` | 30 minutes | Banner configurations |
| `TTL_TOP_LIST` | 1 hour | Top list data |

### Cache Key Patterns

#### Student Portal
- `KEY_STUDENT_DASHBOARD` - `student:dashboard:{userId}`
- `KEY_STUDENT_CAPABILITY` - `student:capability:{userId}`
- `KEY_STUDENT_TASKS` - `student:tasks:{userId}:{status}`
- `KEY_STUDENT_RECOMMENDATIONS` - `student:recommendations:{userId}:{type}`
- `KEY_STUDENT_PROJECTS_LIST` - `student:projects:list:{page}:{size}`
- `KEY_STUDENT_PROJECT_BOARD` - `student:project:board:{projectId}`
- `KEY_STUDENT_JOBS_LIST` - `student:jobs:list:{page}:{size}`

#### Enterprise Portal
- `KEY_ENTERPRISE_DASHBOARD` - `enterprise:dashboard:{tenantId}`
- `KEY_ENTERPRISE_TODOS` - `enterprise:todos:{userId}:{page}`
- `KEY_ENTERPRISE_ACTIVITIES` - `enterprise:activities:{tenantId}:{page}`
- `KEY_ENTERPRISE_JOBS_LIST` - `enterprise:jobs:list:{tenantId}:{status}:{page}`
- `KEY_ENTERPRISE_ANALYTICS` - `enterprise:analytics:{tenantId}:{range}`

#### College Portal
- `KEY_COLLEGE_DASHBOARD` - `college:dashboard:{tenantId}:{year}`
- `KEY_COLLEGE_TRENDS` - `college:trends:{tenantId}:{dimension}`
- `KEY_COLLEGE_WARNINGS_STATS` - `college:warnings:stats:{tenantId}`

#### Platform Administration
- `KEY_PLATFORM_DASHBOARD_STATS` - `platform:dashboard:stats`
- `KEY_PLATFORM_HEALTH` - `platform:health`
- `KEY_PLATFORM_ONLINE_TREND` - `platform:online:trend`
- `KEY_PLATFORM_BANNERS` - `platform:banners:{portal}`
- `KEY_PLATFORM_TOP_LIST` - `platform:toplist:{listType}`

### Invalidation Patterns

Use these patterns with `invalidatePattern()` to clear multiple related caches:

- `PATTERN_STUDENT_ALL` - `student:*` - All student caches
- `PATTERN_ENTERPRISE_ALL` - `enterprise:*` - All enterprise caches
- `PATTERN_COLLEGE_ALL` - `college:*` - All college caches
- `PATTERN_PLATFORM_ALL` - `platform:*` - All platform caches
- `PATTERN_ENTERPRISE_JOBS` - `enterprise:jobs:*` - Enterprise job caches
- `PATTERN_STUDENT_PROJECTS` - `student:projects:*` - Student project caches
- `PATTERN_COLLEGE_WARNINGS` - `college:warnings:*` - College warning caches
- `PATTERN_PLATFORM_BANNERS` - `platform:banners:*` - Platform banner caches

## Best Practices

### 1. Always Use Constants
```java
// ✅ Good
String key = String.format(CacheConstants.KEY_STUDENT_DASHBOARD, userId);
cacheService.getOrSet(key, CacheConstants.TTL_DASHBOARD_STATS, 
    CacheConstants.TTL_DASHBOARD_STATS_UNIT, supplier);

// ❌ Bad
String key = "student:dashboard:" + userId;
cacheService.getOrSet(key, 300, TimeUnit.SECONDS, supplier);
```

### 2. Invalidate on Data Changes
```java
@Transactional
public void updateJob(Long jobId, JobDTO dto) {
    // Update database
    jobMapper.updateById(jobId, dto);
    
    // Invalidate affected caches
    cacheService.invalidatePattern(CacheConstants.PATTERN_ENTERPRISE_JOBS);
}
```

### 3. Use Pattern Invalidation Carefully
Pattern invalidation uses Redis `KEYS` command which can be slow on large datasets. For production:
- Use specific key invalidation when possible
- Consider using Redis `SCAN` for large-scale pattern invalidation
- Monitor performance impact

### 4. Handle Null Values
```java
// The supplier should return null if data doesn't exist
return cacheService.getOrSet(key, ttl, unit, () -> {
    StudentDTO student = studentMapper.selectById(id);
    return student; // Can be null
});
```

### 5. Exception Handling
CacheService handles Redis exceptions gracefully - if caching fails, it falls back to computing the value directly. No need for try-catch in your code.

## Testing

See `CacheServiceTest.java` for comprehensive test examples covering:
- Cache hits and misses
- TTL configuration
- Null value handling
- Exception handling
- Pattern invalidation

## Performance Considerations

1. **TTL Selection**: Choose appropriate TTLs based on data freshness requirements
2. **Cache Warming**: Consider pre-loading frequently accessed data
3. **Monitoring**: Track cache hit rates and adjust TTLs accordingly
4. **Memory Usage**: Monitor Redis memory usage and adjust eviction policies if needed
