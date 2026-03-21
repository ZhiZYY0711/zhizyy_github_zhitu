# Redis Token 序列化问题修复

## 问题根源

Auth 服务和 Gateway 使用了不同的 Redis 序列化器：

1. **Auth 服务**：使用 `Jackson2JsonRedisSerializer`
   - 存储字符串时会序列化成 JSON 格式：`"eyJhbGciOiJIUzM4NCJ9..."`（带引号）
   - 长度：211

2. **Gateway**：使用 `StringRedisSerializer`
   - 直接存储字符串：`eyJhbGciOiJIUzM4NCJ9...`（不带引号）
   - 长度：209

导致字符串比较失败，返回 "Token 已失效"。

## 已修复的内容

### 1. 添加 stringRedisTemplate Bean
在 `backend/zhitu-common/zhitu-common-redis/src/main/java/com/zhitu/common/redis/config/RedisConfig.java` 中添加了专门用于存储 token 的 RedisTemplate，使用 StringRedisSerializer。

### 2. 修改 AuthService
在 `backend/zhitu-auth/src/main/java/com/zhitu/auth/service/AuthService.java` 中：
- 将依赖从 `RedisUtils` 改为 `RedisTemplate<String, String>`
- 所有 token 存储操作都使用 `stringRedisTemplate`

## 需要执行的操作

### 1. 清理 Redis 中的旧 token

```bash
redis-cli -h localhost -p 6379 -a 123456
```

在 Redis CLI 中执行：
```redis
# 删除所有旧的 token（带引号的）
DEL token:access:15

# 或者清空整个数据库（如果没有其他重要数据）
FLUSHDB
```

### 2. 重启 Auth 服务

```bash
# 停止 Auth 服务（Ctrl+C）
# 重新启动
cd backend/zhitu-auth
mvn spring-boot:run
```

### 3. 重新登录测试

1. 清除浏览器缓存：
   - F12 → Application → Local Storage → 删除 `auth_session`

2. 重新登录：
   - 用户名：`student01`
   - 密码：`password`
   - 角色：学生

3. 验证 Redis 中的 token：
   ```redis
   GET token:access:15
   ```
   
   **应该看到**：不带引号的 JWT token
   ```
   eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxNSIsInJvbGUiOiJzdHVkZW50Ii...
   ```

4. 访问 API，应该不再有 401 错误

## 验证步骤

### 1. 检查 Auth 服务日志

登录时应该看到：
```
用户登录成功: userId=15, username=student01, role=student
Token 已存储到 Redis: key=token:access:15
```

### 2. 检查 Gateway 日志

访问 API 时应该看到：
```
Token 签名验证成功，userId: 15
开始验证 Redis 中的 token，userId: 15, key: token:access:15
Redis 中找到 token，长度: 209
请求 token 长度: 209
Token 前20字符 - 请求: eyJhbGciOiJIUzM4NCJ9, Redis: eyJhbGciOiJIUzM4NCJ9
Token 验证通过，注入用户信息头
```

注意：现在 Redis 中的 token 不再有引号，长度应该一致。

### 3. 检查 Redis

```bash
redis-cli -h localhost -p 6379 -a 123456 GET "token:access:15"
```

应该返回不带引号的 JWT token。

## 为什么会出现这个问题？

1. **Jackson2JsonRedisSerializer** 是为了存储复杂对象（如 POJO）设计的，会将所有值序列化成 JSON 格式
2. 对于字符串类型，JSON 序列化会添加引号：`"string"` → `"\"string\""`
3. **StringRedisSerializer** 直接存储字符串的字节，不做任何转换

## 最佳实践

- **简单字符串**（如 token、session ID）：使用 `StringRedisSerializer`
- **复杂对象**（如用户信息、缓存对象）：使用 `Jackson2JsonRedisSerializer`

这就是为什么我们创建了两个 RedisTemplate：
- `redisTemplate<String, Object>`：用于存储复杂对象
- `stringRedisTemplate<String, String>`：用于存储简单字符串
