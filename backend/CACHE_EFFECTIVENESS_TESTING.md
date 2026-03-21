# Cache Effectiveness Testing Guide

## Overview

This document provides guidelines for testing cache effectiveness in the Zhitu Cloud Platform. The platform uses Redis for caching with the following targets:

- **Cache hit rate**: ≥ 70%
- **Cache invalidation**: Within 1 second of data mutation
- **TTL compliance**: All cached data respects configured TTL

## Cache Strategy Overview

### Cached Endpoints and TTLs

| Endpoint | Cache Key Pattern | TTL | Invalidation Trigger |
|----------|------------------|-----|---------------------|
| Student Dashboard | `student:dashboard:{userId}` | 5 min | Task/project updates |
| Capability Radar | `student:capability:{userId}` | 10 min | Evaluation updates |
| Recommendations | `student:recommendations:{userId}:{type}` | 15 min | New recommendations |
| Training Projects | `student:projects:list:{page}:{size}` | 5 min | Project creation/update |
| Internship Jobs | `student:jobs:list:{page}:{size}` | 5 min | Job creation/update |
| Enterprise Dashboard | `enterprise:dashboard:{tenantId}` | 5 min | Application/intern updates |
| Enterprise Activities | `enterprise:activities:{tenantId}:{page}` | 3 min | New activities |
| Enterprise Analytics | `enterprise:analytics:{tenantId}:{range}` | 30 min | Data updates |
| College Dashboard | `college:dashboard:{tenantId}:{year}` | 1 hour | Student/employment updates |
| College Trends | `college:trends:{tenantId}:{dimension}` | 1 hour | Employment data updates |
| Warning Stats | `college:warnings:stats:{tenantId}` | 10 min | Warning updates |
| Platform Dashboard | `platform:dashboard:stats` | 10 min | Tenant/user updates |
| System Health | `platform:health` | 1 min | Health check updates |
| Online User Trend | `platform:users:online-trend` | 5 min | User activity updates |
| Recommendation Banners | `platform:banners:{portal}` | 30 min | Banner save |
| Top Lists | `platform:toplist:{listType}` | 1 hour | Top list save |

## Test Scenarios

### Test 1: Cache Hit Rate Measurement

**Objective**: Verify cache hit rate meets 70% threshold

**Test Steps**:

1. Clear all Redis cache
2. Execute 1000 requests with the following distribution:
   - 30% unique requests (cache misses expected)
   - 70% repeated requests (cache hits expected)
3. Measure cache hit rate

**Implementation**:

```java
@Test
@DisplayName("Test Cache Hit Rate - Target ≥ 70%")
void testCacheHitRate() {
    // Clear cache
    redisTemplate.getConnectionFactory().getConnection().flushDb();
    
    int totalRequests = 1000;
    int uniqueRequests = 300;
    int repeatedRequests = 700;
    
    List<Long> uniqueUserIds = generateUniqueUserIds(uniqueRequests);
    Long repeatedUserId = 1000L;
    
    int cacheHits = 0;
    int cacheMisses = 0;
    
    // Execute unique requests (expect cache misses)
    for (Long userId : uniqueUserIds) {
        boolean cached = executeRequestAndCheckCache(userId);
        if (cached) cacheHits++;
        else cacheMisses++;
    }
    
    // Execute repeated requests (expect cache hits)
    for (int i = 0; i < repeatedRequests; i++) {
        boolean cached = executeRequestAndCheckCache(repeatedUserId);
        if (cached) cacheHits++;
        else cacheMisses++;
    }
    
    double hitRate = (double) cacheHits / totalRequests * 100;
    
    System.out.println("Cache Hit Rate: " + hitRate + "%");
    System.out.println("Cache Hits: " + cacheHits);
    System.out.println("Cache Misses: " + cacheMisses);
    
    assertThat(hitRate).isGreaterThanOrEqualTo(70.0);
}

private boolean executeRequestAndCheckCache(Long userId) {
    String cacheKey = "student:dashboard:" + userId;
    
    // Check if data is in cache before request
    boolean wasCached = redisTemplate.hasKey(cacheKey);
    
    // Execute request (will populate cache if not present)
    studentPortalService.getDashboardStats(userId);
    
    return wasCached;
}
```

**Success Criteria**:
- Cache hit rate ≥ 70%
- No cache errors or exceptions
- Cache keys follow naming conventions

### Test 2: TTL Expiration Verification

**Objective**: Verify cached data expires according to configured TTL

**Test Steps**:

1. Execute request to populate cache
2. Verify data is cached
3. Wait for TTL to expire
4. Verify data is no longer cached

**Implementation**:

```java
@Test
@DisplayName("Test TTL Expiration - Dashboard Cache (5 minutes)")
void testDashboardCacheTTL() throws InterruptedException {
    Long userId = 1000L;
    String cacheKey = "student:dashboard:" + userId;
    
    // Clear cache
    redisTemplate.delete(cacheKey);
    
    // Execute request to populate cache
    studentPortalService.getDashboardStats(userId);
    
    // Verify data is cached
    assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    
    // Check TTL is set correctly (5 minutes = 300 seconds)
    Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
    assertThat(ttl).isBetween(290L, 300L); // Allow 10 second margin
    
    // Wait for TTL to expire (use shorter TTL in test environment)
    Thread.sleep(6000); // 6 seconds (assuming test TTL is 5 seconds)
    
    // Verify data is no longer cached
    assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
}

@Test
@DisplayName("Test TTL Expiration - All Cached Endpoints")
void testAllCacheTTLs() {
    Map<String, Long> expectedTTLs = Map.of(
        "student:dashboard:1000", 300L,      // 5 minutes
        "student:capability:1000", 600L,     // 10 minutes
        "student:recommendations:1000:all", 900L, // 15 minutes
        "enterprise:dashboard:1", 300L,      // 5 minutes
        "enterprise:analytics:1:month", 1800L, // 30 minutes
        "college:dashboard:1:2024", 3600L,   // 1 hour
        "platform:dashboard:stats", 600L     // 10 minutes
    );
    
    for (Map.Entry<String, Long> entry : expectedTTLs.entrySet()) {
        String cacheKey = entry.getKey();
        Long expectedTTL = entry.getValue();
        
        // Populate cache
        populateCacheForKey(cacheKey);
        
        // Verify TTL
        Long actualTTL = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        assertThat(actualTTL)
            .as("TTL for " + cacheKey)
            .isBetween(expectedTTL - 10, expectedTTL);
    }
}
```

**Success Criteria**:
- All cached data expires according to configured TTL
- TTL is set correctly on cache population
- No data persists beyond TTL

### Test 3: Cache Invalidation on Mutation

**Objective**: Verify cache is invalidated within 1 second of data mutation

**Test Steps**:

1. Execute GET request to populate cache
2. Verify data is cached
3. Execute mutation operation (POST/PUT/DELETE)
4. Verify cache is invalidated
5. Measure invalidation time

**Implementation**:

```java
@Test
@DisplayName("Test Cache Invalidation - Job Creation")
void testCacheInvalidationOnJobCreation() {
    Long tenantId = 1L;
    String cacheKey = "enterprise:jobs:list:" + tenantId + ":1:10";
    
    // Populate cache
    enterpriseJobService.getJobs(null, 1, 10);
    assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
    
    // Record time before mutation
    long startTime = System.currentTimeMillis();
    
    // Create new job (should invalidate cache)
    CreateJobRequest request = new CreateJobRequest();
    request.setTitle("Test Job");
    request.setDescription("Test Description");
    request.setRequirements("Test Requirements");
    request.setSalaryMin(5000);
    request.setSalaryMax(8000);
    enterpriseJobService.createJob(request);
    
    // Record time after mutation
    long endTime = System.currentTimeMillis();
    long invalidationTime = endTime - startTime;
    
    // Verify cache is invalidated
    assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    
    // Verify invalidation happened within 1 second
    assertThat(invalidationTime).isLessThan(1000L);
    
    System.out.println("Cache invalidation time: " + invalidationTime + "ms");
}

@Test
@DisplayName("Test Cache Invalidation - Multiple Related Caches")
void testMultipleCacheInvalidation() {
    Long tenantId = 1L;
    
    // Populate multiple related caches
    enterprisePortalService.getDashboardStats();
    enterpriseJobService.getJobs(null, 1, 10);
    enterprisePortalService.getActivities(1, 10);
    
    String dashboardKey = "enterprise:dashboard:" + tenantId;
    String jobsKey = "enterprise:jobs:list:" + tenantId + ":1:10";
    String activitiesKey = "enterprise:activities:" + tenantId + ":1";
    
    // Verify all caches are populated
    assertThat(redisTemplate.hasKey(dashboardKey)).isTrue();
    assertThat(redisTemplate.hasKey(jobsKey)).isTrue();
    assertThat(redisTemplate.hasKey(activitiesKey)).isTrue();
    
    // Create new job (should invalidate related caches)
    CreateJobRequest request = new CreateJobRequest();
    request.setTitle("Test Job");
    request.setDescription("Test Description");
    request.setRequirements("Test Requirements");
    request.setSalaryMin(5000);
    request.setSalaryMax(8000);
    enterpriseJobService.createJob(request);
    
    // Verify related caches are invalidated
    assertThat(redisTemplate.hasKey(dashboardKey)).isFalse();
    assertThat(redisTemplate.hasKey(jobsKey)).isFalse();
    // Activities cache should remain (not directly related)
    assertThat(redisTemplate.hasKey(activitiesKey)).isTrue();
}
```

**Success Criteria**:
- Cache invalidated within 1 second of mutation
- All related caches invalidated
- Unrelated caches remain intact

### Test 4: Cache Performance Comparison

**Objective**: Verify cached responses are significantly faster than uncached

**Test Steps**:

1. Execute request without cache (measure time)
2. Execute same request with cache (measure time)
3. Compare response times

**Implementation**:

```java
@Test
@DisplayName("Test Cache Performance - Response Time Comparison")
void testCachePerformance() {
    Long userId = 1000L;
    String cacheKey = "student:dashboard:" + userId;
    
    // Clear cache
    redisTemplate.delete(cacheKey);
    
    // Measure uncached request time
    long uncachedStart = System.nanoTime();
    studentPortalService.getDashboardStats(userId);
    long uncachedEnd = System.nanoTime();
    long uncachedTime = (uncachedEnd - uncachedStart) / 1_000_000; // Convert to ms
    
    // Measure cached request time
    long cachedStart = System.nanoTime();
    studentPortalService.getDashboardStats(userId);
    long cachedEnd = System.nanoTime();
    long cachedTime = (cachedEnd - cachedStart) / 1_000_000; // Convert to ms
    
    System.out.println("Uncached response time: " + uncachedTime + "ms");
    System.out.println("Cached response time: " + cachedTime + "ms");
    System.out.println("Performance improvement: " + (uncachedTime / cachedTime) + "x");
    
    // Cached response should be at least 5x faster
    assertThat(cachedTime).isLessThan(uncachedTime / 5);
    
    // Cached response should be under 50ms
    assertThat(cachedTime).isLessThan(50L);
}
```

**Success Criteria**:
- Cached responses < 50ms
- Cached responses at least 5x faster than uncached
- Consistent cache performance

### Test 5: Cache Under Load

**Objective**: Verify cache performs well under concurrent load

**Test Steps**:

1. Execute 100 concurrent requests to same endpoint
2. Measure cache hit rate
3. Verify no cache errors

**Implementation**:

```java
@Test
@DisplayName("Test Cache Under Concurrent Load")
void testCacheUnderLoad() throws InterruptedException {
    Long userId = 1000L;
    int concurrentRequests = 100;
    
    // Clear cache
    String cacheKey = "student:dashboard:" + userId;
    redisTemplate.delete(cacheKey);
    
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(concurrentRequests);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);
    
    // Execute concurrent requests
    for (int i = 0; i < concurrentRequests; i++) {
        executor.submit(() -> {
            try {
                studentPortalService.getDashboardStats(userId);
                successCount.incrementAndGet();
            } catch (Exception e) {
                errorCount.incrementAndGet();
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
    }
    
    // Wait for all requests to complete
    latch.await(30, TimeUnit.SECONDS);
    executor.shutdown();
    
    System.out.println("Successful requests: " + successCount.get());
    System.out.println("Failed requests: " + errorCount.get());
    
    // Verify no errors
    assertThat(errorCount.get()).isEqualTo(0);
    assertThat(successCount.get()).isEqualTo(concurrentRequests);
    
    // Verify cache is populated
    assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
}
```

**Success Criteria**:
- No cache errors under load
- All requests succeed
- Cache remains consistent

## Cache Monitoring

### Redis Metrics to Monitor

```java
@Component
public class CacheMetrics {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry registry;
    
    public CacheMetrics(RedisTemplate<String, Object> redisTemplate, 
                       MeterRegistry registry) {
        this.redisTemplate = redisTemplate;
        this.registry = registry;
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectCacheMetrics() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Properties info = connection.info();
        
        // Memory usage
        String usedMemory = info.getProperty("used_memory");
        registry.gauge("redis.memory.used", Double.parseDouble(usedMemory));
        
        // Hit rate
        String hits = info.getProperty("keyspace_hits");
        String misses = info.getProperty("keyspace_misses");
        double hitRate = Double.parseDouble(hits) / 
                        (Double.parseDouble(hits) + Double.parseDouble(misses)) * 100;
        registry.gauge("redis.hit.rate", hitRate);
        
        // Connected clients
        String clients = info.getProperty("connected_clients");
        registry.gauge("redis.clients.connected", Double.parseDouble(clients));
        
        connection.close();
    }
}
```

### Cache Hit Rate Calculation

```java
public class CacheHitRateCalculator {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    
    public void recordHit() {
        hits.incrementAndGet();
    }
    
    public void recordMiss() {
        misses.incrementAndGet();
    }
    
    public double getHitRate() {
        long totalHits = hits.get();
        long totalMisses = misses.get();
        long total = totalHits + totalMisses;
        
        if (total == 0) return 0.0;
        
        return (double) totalHits / total * 100;
    }
    
    public void reset() {
        hits.set(0);
        misses.set(0);
    }
}
```

## Cache Testing Checklist

- [ ] Measure cache hit rate (target ≥ 70%)
- [ ] Verify TTL expiration for all cached endpoints
- [ ] Test cache invalidation on data mutations
- [ ] Verify invalidation happens within 1 second
- [ ] Compare cached vs uncached response times
- [ ] Verify cached responses < 50ms
- [ ] Test cache under concurrent load (100 requests)
- [ ] Verify no cache errors or exceptions
- [ ] Monitor Redis memory usage
- [ ] Verify cache key naming conventions
- [ ] Test cache warming strategies
- [ ] Verify cache stampede prevention
- [ ] Test cache with realistic data volumes
- [ ] Document cache hit rates per endpoint
- [ ] Create optimization plan for low hit rates

## Common Cache Issues and Solutions

### Issue 1: Low Cache Hit Rate

**Symptoms**:
- Cache hit rate < 70%
- High database load
- Slow response times

**Solutions**:
1. Increase TTL for stable data
2. Implement cache warming
3. Review cache key patterns
4. Add caching to more endpoints

### Issue 2: Cache Stampede

**Symptoms**:
- Multiple simultaneous cache misses
- Database overload when cache expires
- Periodic performance spikes

**Solutions**:
1. Implement cache locking
2. Use probabilistic early expiration
3. Implement stale-while-revalidate
4. Add jitter to TTL

### Issue 3: Stale Data

**Symptoms**:
- Users see outdated information
- Cache not invalidated on updates
- Inconsistent data across requests

**Solutions**:
1. Verify cache invalidation logic
2. Reduce TTL for frequently changing data
3. Implement event-based invalidation
4. Add cache versioning

### Issue 4: Memory Pressure

**Symptoms**:
- Redis memory usage high
- Cache evictions
- Performance degradation

**Solutions**:
1. Reduce TTL for large objects
2. Implement cache size limits
3. Use cache compression
4. Review cache key patterns

## Cache Effectiveness Report Template

```markdown
# Cache Effectiveness Test Report

## Test Date
[Date]

## Test Environment
- Redis Version: [version]
- Test Duration: [duration]
- Total Requests: [count]

## Cache Hit Rate Results

| Endpoint | Requests | Hits | Misses | Hit Rate | Target Met |
|----------|----------|------|--------|----------|------------|
| Student Dashboard | 1000 | 750 | 250 | 75% | ✅ |
| Enterprise Jobs | 1000 | 680 | 320 | 68% | ❌ |
| College Dashboard | 1000 | 820 | 180 | 82% | ✅ |

**Overall Hit Rate**: 74% ✅

## TTL Compliance

| Cache Key Pattern | Expected TTL | Actual TTL | Compliant |
|-------------------|--------------|------------|-----------|
| student:dashboard:* | 300s | 298s | ✅ |
| enterprise:analytics:* | 1800s | 1795s | ✅ |

## Cache Invalidation Performance

| Operation | Invalidation Time | Target Met |
|-----------|------------------|------------|
| Job Creation | 45ms | ✅ |
| Banner Save | 120ms | ✅ |
| Student Update | 80ms | ✅ |

**Average Invalidation Time**: 82ms ✅

## Performance Comparison

| Endpoint | Uncached | Cached | Improvement |
|----------|----------|--------|-------------|
| Dashboard | 420ms | 35ms | 12x |
| Job List | 580ms | 42ms | 14x |

## Issues Found
1. [Issue description]
2. [Issue description]

## Recommendations
1. [Recommendation]
2. [Recommendation]
```

## Conclusion

Regular cache effectiveness testing ensures optimal system performance. Follow this guide to:
1. Measure and maintain cache hit rates
2. Verify TTL compliance
3. Test cache invalidation
4. Monitor cache performance
5. Identify and resolve cache issues
