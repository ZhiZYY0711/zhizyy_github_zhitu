# Performance Testing and Optimization Guide

## Overview

This document provides guidelines for performance testing and optimization of the Zhitu Cloud Platform API endpoints. The platform targets the following performance requirements:

- **Dashboard endpoints**: 500ms response time at p95
- **List endpoints**: 1000ms response time at p95
- **Service capacity**: 100 requests per second per service
- **Cache hit rate**: 70% or higher

## Performance Requirements

### Response Time Targets (p95)

| Endpoint Type | Target Response Time |
|---------------|---------------------|
| Dashboard statistics | ≤ 500ms |
| List/pagination endpoints | ≤ 1000ms |
| Single record retrieval | ≤ 200ms |
| Create/update operations | ≤ 300ms |

### Throughput Targets

- **Per service**: 100 requests/second
- **Gateway**: 500 requests/second
- **Database connections**: 50 per service

### Cache Performance

- **Hit rate**: ≥ 70%
- **TTL compliance**: All cached data respects configured TTL
- **Invalidation**: Cache invalidated within 1 second of data mutation

## Performance Testing Tools

### 1. JMeter

Use Apache JMeter for load testing:

```bash
# Install JMeter
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.zip
unzip apache-jmeter-5.6.3.zip

# Run test plan
./apache-jmeter-5.6.3/bin/jmeter -n -t test-plan.jmx -l results.jtl -e -o report/
```

### 2. Gatling

Alternative load testing tool with Scala DSL:

```scala
class ApiLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader("Bearer ${token}")

  val scn = scenario("Dashboard Load Test")
    .exec(http("Get Dashboard")
      .get("/api/student-portal/v1/dashboard")
      .check(status.is(200))
      .check(responseTimeInMillis.lte(500)))

  setUp(
    scn.inject(
      rampUsersPerSec(10) to 100 during (60 seconds),
      constantUsersPerSec(100) during (300 seconds)
    )
  ).protocols(httpProtocol)
}
```

### 3. K6

Modern load testing tool:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 50 },
    { duration: '5m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  let response = http.get('http://localhost:8080/api/student-portal/v1/dashboard', {
    headers: { 'Authorization': 'Bearer TOKEN' },
  });
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
}
```

## Test Scenarios

### Scenario 1: Dashboard Load Test

**Objective**: Verify dashboard endpoints meet 500ms p95 target

**Test Configuration**:
- Ramp up: 0 to 100 users over 1 minute
- Sustained load: 100 concurrent users for 5 minutes
- Endpoints tested:
  - GET /api/student-portal/v1/dashboard
  - GET /api/portal-enterprise/v1/dashboard/stats
  - GET /api/portal-college/v1/dashboard/stats
  - GET /api/system/v1/dashboard/stats

**Success Criteria**:
- p95 response time ≤ 500ms
- Error rate < 1%
- No database connection pool exhaustion

### Scenario 2: List Endpoint Load Test

**Objective**: Verify list endpoints meet 1000ms p95 target

**Test Configuration**:
- Ramp up: 0 to 100 users over 1 minute
- Sustained load: 100 concurrent users for 5 minutes
- Endpoints tested:
  - GET /api/student-portal/v1/tasks
  - GET /api/internship/v1/enterprise/jobs
  - GET /api/user/v1/college/students
  - GET /api/system/v1/tenants/colleges

**Success Criteria**:
- p95 response time ≤ 1000ms
- Error rate < 1%
- Pagination works correctly under load

### Scenario 3: Cache Effectiveness Test

**Objective**: Verify cache hit rate ≥ 70%

**Test Configuration**:
- 1000 requests to cached endpoints
- Mix of unique and repeated requests (30% unique, 70% repeated)

**Metrics to Collect**:
- Cache hit rate
- Cache miss rate
- Average response time (cached vs uncached)

**Success Criteria**:
- Cache hit rate ≥ 70%
- Cached responses < 50ms
- Uncached responses meet standard targets

### Scenario 4: Spike Test

**Objective**: Verify system handles traffic spikes

**Test Configuration**:
- Baseline: 50 users
- Spike: Sudden increase to 200 users for 2 minutes
- Return to baseline

**Success Criteria**:
- No service crashes
- Error rate < 5% during spike
- Recovery to normal performance within 1 minute

## Database Query Optimization

### 1. Use EXPLAIN ANALYZE

Identify slow queries:

```sql
EXPLAIN ANALYZE
SELECT s.*, i.status as internship_status
FROM student_svc.student_info s
LEFT JOIN internship_svc.internship_record i ON s.id = i.student_id
WHERE s.college_tenant_id = 1
  AND s.is_deleted = FALSE
ORDER BY s.created_at DESC
LIMIT 10 OFFSET 0;
```

### 2. Index Optimization

Add missing indexes identified during testing:

```sql
-- Example: Add composite index for common query pattern
CREATE INDEX idx_student_college_created 
ON student_svc.student_info(college_tenant_id, created_at DESC) 
WHERE is_deleted = FALSE;

-- Example: Add covering index
CREATE INDEX idx_job_posting_covering 
ON internship_svc.job_posting(status, tenant_id, created_at DESC) 
INCLUDE (title, location, salary_min, salary_max)
WHERE is_deleted = FALSE;
```

### 3. Query Patterns to Avoid

**Bad**: N+1 queries
```java
// DON'T DO THIS
List<Student> students = studentMapper.selectList(query);
for (Student student : students) {
    InternshipRecord record = internshipMapper.selectByStudentId(student.getId());
    student.setInternshipStatus(record.getStatus());
}
```

**Good**: Single query with JOIN
```java
// DO THIS
List<StudentDTO> students = studentMapper.selectStudentsWithInternshipStatus(query);
```

### 4. Pagination Optimization

Use keyset pagination for large datasets:

```sql
-- Instead of OFFSET (slow for large offsets)
SELECT * FROM table ORDER BY id LIMIT 10 OFFSET 10000;

-- Use keyset pagination (fast)
SELECT * FROM table WHERE id > :lastSeenId ORDER BY id LIMIT 10;
```

## Caching Optimization

### 1. Cache Key Design

Use consistent, hierarchical cache keys:

```java
// Good cache key patterns
"student:dashboard:{userId}"
"enterprise:jobs:list:{tenantId}:{page}:{size}"
"college:warnings:stats:{tenantId}"
"platform:banners:{portal}"
```

### 2. TTL Configuration

Set appropriate TTLs based on data volatility:

```java
// Frequently changing data
redisTemplate.opsForValue().set(key, value, 1, TimeUnit.MINUTES);

// Moderately changing data
redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);

// Rarely changing data
redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
```

### 3. Cache Warming

Pre-populate cache for frequently accessed data:

```java
@Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
public void warmCache() {
    List<Long> activeTenants = getActiveTenants();
    for (Long tenantId : activeTenants) {
        // Pre-load dashboard stats
        getDashboardStats(tenantId);
    }
}
```

### 4. Cache Invalidation Strategies

Implement proper cache invalidation:

```java
@Transactional
public void createJob(CreateJobRequest request) {
    // Create job
    jobMapper.insert(job);
    
    // Invalidate related caches
    String cacheKey = "enterprise:jobs:list:" + tenantId + ":*";
    redisTemplate.delete(redisTemplate.keys(cacheKey));
}
```

## Connection Pool Tuning

### HikariCP Configuration

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

### Redis Connection Pool

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10
        max-wait: 3000ms
```

## Monitoring and Metrics

### 1. Application Metrics

Use Spring Boot Actuator and Micrometer:

```java
@Component
public class PerformanceMetrics {
    private final MeterRegistry registry;
    
    public PerformanceMetrics(MeterRegistry registry) {
        this.registry = registry;
    }
    
    public void recordApiCall(String endpoint, long duration) {
        Timer.builder("api.call.duration")
            .tag("endpoint", endpoint)
            .register(registry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
    
    public void recordCacheHit(String cacheKey) {
        Counter.builder("cache.hit")
            .tag("key", cacheKey)
            .register(registry)
            .increment();
    }
}
```

### 2. Database Metrics

Monitor key database metrics:
- Connection pool usage
- Query execution time
- Slow query log
- Lock wait time
- Transaction rollback rate

### 3. Cache Metrics

Monitor Redis performance:
- Hit rate
- Miss rate
- Eviction rate
- Memory usage
- Connection count

## Performance Testing Checklist

- [ ] Run dashboard load test (100 concurrent users, 5 minutes)
- [ ] Verify p95 response time ≤ 500ms for dashboard endpoints
- [ ] Run list endpoint load test (100 concurrent users, 5 minutes)
- [ ] Verify p95 response time ≤ 1000ms for list endpoints
- [ ] Measure cache hit rate (target ≥ 70%)
- [ ] Run spike test (50 → 200 → 50 users)
- [ ] Verify no service crashes or connection pool exhaustion
- [ ] Run EXPLAIN ANALYZE on all complex queries
- [ ] Add missing indexes identified during testing
- [ ] Verify cache invalidation works correctly
- [ ] Monitor memory usage under load
- [ ] Check for memory leaks (heap dump analysis)
- [ ] Verify database connection pool sizing
- [ ] Test with realistic data volumes (10K+ records)
- [ ] Document any performance bottlenecks found
- [ ] Create optimization tickets for issues found

## Common Performance Issues and Solutions

### Issue 1: Slow Dashboard Queries

**Symptom**: Dashboard endpoints exceed 500ms target

**Solutions**:
1. Add Redis caching with 5-minute TTL
2. Optimize COUNT queries with approximate counts
3. Use materialized views for complex aggregations
4. Add covering indexes for dashboard queries

### Issue 2: Pagination Performance Degradation

**Symptom**: List endpoints slow down with large offsets

**Solutions**:
1. Implement keyset pagination
2. Add composite indexes on sort columns
3. Cache frequently accessed pages
4. Limit maximum page size

### Issue 3: Cache Stampede

**Symptom**: Multiple requests hit database when cache expires

**Solutions**:
1. Implement cache locking
2. Use probabilistic early expiration
3. Implement cache warming
4. Use stale-while-revalidate pattern

### Issue 4: N+1 Query Problem

**Symptom**: Excessive database queries for list operations

**Solutions**:
1. Use JOIN queries instead of separate queries
2. Implement batch loading
3. Use MyBatis Plus batch operations
4. Add @BatchSize annotation

## Performance Testing Report Template

```markdown
# Performance Test Report

## Test Date
[Date]

## Test Environment
- Services: [versions]
- Database: PostgreSQL [version]
- Redis: [version]
- Load generator: [tool and version]

## Test Results

### Dashboard Endpoints
| Endpoint | p50 | p95 | p99 | Error Rate | Target Met |
|----------|-----|-----|-----|------------|------------|
| Student Dashboard | 250ms | 450ms | 600ms | 0.1% | ✅ |
| Enterprise Dashboard | 280ms | 520ms | 650ms | 0.2% | ❌ |

### List Endpoints
| Endpoint | p50 | p95 | p99 | Error Rate | Target Met |
|----------|-----|-----|-----|------------|------------|
| Student Tasks | 400ms | 800ms | 1100ms | 0.1% | ✅ |
| Enterprise Jobs | 450ms | 950ms | 1200ms | 0.2% | ✅ |

### Cache Performance
- Hit Rate: 72%
- Miss Rate: 28%
- Average cached response time: 35ms
- Average uncached response time: 420ms

## Issues Found
1. [Issue description]
2. [Issue description]

## Recommendations
1. [Recommendation]
2. [Recommendation]

## Next Steps
- [ ] Action item 1
- [ ] Action item 2
```

## Conclusion

Regular performance testing and optimization are critical for maintaining system responsiveness. Follow this guide to:
1. Establish performance baselines
2. Identify bottlenecks early
3. Optimize queries and caching
4. Monitor production performance
5. Continuously improve system performance
