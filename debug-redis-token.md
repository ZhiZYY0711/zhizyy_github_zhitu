# Redis Token 调试指南

## 问题现象
登录成功，token 已保存到前端，但访问 API 时网关返回 "Token 已失效，请重新登录"。

## 可能原因

### 1. Redis 中没有存储 token
Auth 服务登录时应该将 token 存入 Redis，key 格式为 `token:access:{userId}`

### 2. 网关连接的 Redis 实例与 Auth 服务不同
两个服务可能连接了不同的 Redis 数据库或实例。

### 3. Token 格式不匹配
存储的 token 和请求中的 token 不一致。

## 调试步骤

### 步骤 1：检查 Redis 中是否有 token

连接到 Redis：
```bash
redis-cli -h localhost -p 6379 -a 123456
```

查看所有 token 相关的 key：
```redis
KEYS token:access:*
```

如果有结果，查看具体内容（假设 userId 是 1）：
```redis
GET token:access:1
TTL token:access:1
```

**预期结果**：
- 应该能看到 `token:access:1` 这样的 key
- GET 命令应该返回完整的 JWT token（以 `eyJ` 开头）
- TTL 应该显示剩余秒数（如 7200）

### 步骤 2：检查网关日志

查看网关启动日志，确认：
1. Redis 连接是否成功
2. 是否有 Redis 连接错误

在网关日志中搜索：
```
Redis
ReactiveRedisTemplate
```

### 步骤 3：检查 Auth 服务日志

查看 Auth 服务登录时的日志，确认：
1. Token 是否成功存入 Redis
2. 存储的 key 是什么

### 步骤 4：对比 token

1. **从浏览器获取 token**：
   - F12 → Application → Local Storage → `auth_session`
   - 复制 `accessToken` 的值

2. **从 Redis 获取 token**：
   ```redis
   GET token:access:1
   ```

3. **对比两个 token 是否完全一致**

### 步骤 5：检查网关 Redis 配置

确认网关是否正确加载了 Nacos 的 Redis 配置：

1. 访问网关的 actuator 端点：
   ```
   http://localhost:8888/actuator/env
   ```

2. 搜索 `spring.data.redis`，确认：
   - host: localhost
   - port: 6379
   - password: 123456
   - database: 0

## 临时解决方案

如果发现 Redis 中没有 token，可以手动添加（用于测试）：

```redis
# 从浏览器复制 accessToken
SET token:access:1 "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZSI6InN0dWRlbnQiLCJzdWJSb2xlIjpudWxsLCJ0ZW5hbnRJZCI6MCwiaWF0IjoxNzExMDQwMDAwLCJleHAiOjE3MTEwNDcyMDB9.xxx"

# 设置过期时间（7200 秒 = 2 小时）
EXPIRE token:access:1 7200
```

## 根本解决方案

### 方案 1：重启网关服务

网关可能没有正确加载 Nacos 配置，重启后会重新拉取：

```bash
# 停止网关
# 重新启动网关
```

### 方案 2：检查 Auth 服务的 Redis 配置

确认 Auth 服务的 `bootstrap.yml` 中引用了 `zhitu-redis.yaml`：

```yaml
spring:
  cloud:
    nacos:
      config:
        shared-configs:
          - data-id: zhitu-redis.yaml
            group: DEFAULT_GROUP
            refresh: true
```

### 方案 3：添加详细日志

在 Auth 服务的 `AuthService.login()` 方法中添加日志：

```java
// 6. 存 access_token 到 Redis（用于主动吊销）
log.info("存储 token 到 Redis: key={}, token={}", TOKEN_KEY_PREFIX + user.getId(), accessToken);
redisUtils.set(TOKEN_KEY_PREFIX + user.getId(), accessToken,
        jwtUtils.getAccessTokenExpiration(), TimeUnit.SECONDS);
log.info("Token 存储成功");
```

在网关的 `AuthFilter.filter()` 方法中添加日志：

```java
// 验证 Redis 中 token 是否有效
log.info("从 Redis 查询 token: key={}", TOKEN_KEY_PREFIX + userId);
return reactiveRedisTemplate.opsForValue()
        .get(TOKEN_KEY_PREFIX + userId)
        .flatMap(storedToken -> {
            log.info("Redis 中的 token: {}", storedToken);
            log.info("请求中的 token: {}", token);
            if (!token.equals(storedToken)) {
                log.warn("Token 不匹配！");
                return unauthorized(exchange, "Token 已失效，请重新登录");
            }
            // ...
        })
        .switchIfEmpty(Mono.defer(() -> {
            log.warn("Redis 中没有找到 token: key={}", TOKEN_KEY_PREFIX + userId);
            return unauthorized(exchange, "Token 已失效，请重新登录");
        }));
```

## 快速诊断命令

```bash
# 1. 检查 Redis 是否运行
redis-cli -h localhost -p 6379 -a 123456 PING

# 2. 查看所有 token
redis-cli -h localhost -p 6379 -a 123456 KEYS "token:*"

# 3. 查看特定用户的 token（假设 userId=1）
redis-cli -h localhost -p 6379 -a 123456 GET "token:access:1"

# 4. 查看 token 过期时间
redis-cli -h localhost -p 6379 -a 123456 TTL "token:access:1"

# 5. 清空所有 token（重新测试用）
redis-cli -h localhost -p 6379 -a 123456 DEL "token:access:1"
```
