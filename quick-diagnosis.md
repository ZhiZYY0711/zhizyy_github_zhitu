# 快速诊断：Token 已失效问题

## 当前状态
✅ 登录成功，token 已保存到前端  
❌ 访问 API 时返回 "Token 已失效，请重新登录"

## 立即执行的诊断步骤

### 步骤 1：检查 Redis 中是否有 token（最关键）

打开终端，执行：

```bash
redis-cli -h localhost -p 6379 -a 123456
```

然后在 Redis CLI 中执行：

```redis
# 查看所有 token key
KEYS token:access:*

# 如果有结果，查看具体内容（假设看到 token:access:1）
GET token:access:1

# 查看过期时间
TTL token:access:1
```

**预期结果**：
- 应该能看到 `token:access:1` 或类似的 key
- GET 应该返回完整的 JWT（以 `eyJ` 开头的长字符串）
- TTL 应该显示剩余秒数（如 7200）

**如果没有看到任何 key**：
→ 说明 Auth 服务没有成功将 token 存入 Redis
→ 需要检查 Auth 服务的 Redis 连接

**如果看到了 key 但 GET 返回 nil**：
→ Token 已过期或被删除
→ 需要重新登录

### 步骤 2：重启网关服务

网关可能没有正确连接 Redis，重启后会重新初始化连接：

```bash
# 停止网关进程（Ctrl+C 或 kill）
# 然后重新启动网关
cd backend/zhitu-gateway
mvn spring-boot:run
```

启动后观察日志，确认：
1. Redis 连接是否成功
2. 是否有 "ReactiveRedisTemplate" 相关的错误

### 步骤 3：查看网关日志

我已经添加了详细的调试日志，重启网关后，再次登录并访问 API，观察网关日志输出：

**正常情况应该看到**：
```
请求路径: /api/student-portal/xxx
Token 签名验证成功，userId: 1
开始验证 Redis 中的 token，userId: 1, key: token:access:1
Redis 中找到 token，长度: 200+
Token 验证通过，注入用户信息头
```

**如果看到**：
```
Redis 中没有找到 token，userId: 1, key: token:access:1
```
→ 说明 Redis 中确实没有这个 key，需要检查 Auth 服务

**如果看到**：
```
Token 不匹配！userId: 1
```
→ 说明 Redis 中的 token 和请求中的 token 不一致，可能是：
  - 登录后又重新生成了新 token
  - 前端保存的 token 不是最新的

### 步骤 4：查看 Auth 服务日志

我也添加了详细的日志，重新登录时观察 Auth 服务日志：

**应该看到**：
```
用户登录成功: userId=1, username=student01, role=student
生成的 accessToken 前20字符: eyJhbGciOiJIUzI1NiJ9...
存储 token 到 Redis: key=token:access:1, expiresIn=7200秒
Token 已存储到 Redis: key=token:access:1
```

**如果没有看到这些日志**：
→ 登录流程可能在某个地方失败了

**如果看到 Redis 连接错误**：
→ Auth 服务的 Redis 配置有问题

### 步骤 5：对比 token

1. **从浏览器获取 token**：
   ```javascript
   // 在浏览器控制台执行
   JSON.parse(localStorage.getItem('auth_session')).accessToken
   ```

2. **从 Redis 获取 token**：
   ```bash
   redis-cli -h localhost -p 6379 -a 123456 GET "token:access:1"
   ```

3. **对比两个 token 的前 50 个字符**，看是否一致

## 最可能的原因

根据经验，最可能的原因是：

### 原因 1：网关的 Redis 配置没有生效（80% 可能性）

**解决方案**：
1. 确认 Nacos 中的 `zhitu-redis.yaml` 配置正确
2. 重启网关服务
3. 检查网关启动日志，确认 Redis 连接成功

### 原因 2：Auth 服务没有成功存储 token（15% 可能性）

**解决方案**：
1. 检查 Auth 服务的日志
2. 确认 Redis 连接正常
3. 手动在 Redis 中查看是否有 token

### 原因 3：网关和 Auth 连接了不同的 Redis 实例（5% 可能性）

**解决方案**：
1. 确认两个服务都使用了 Nacos 的 `zhitu-redis.yaml` 配置
2. 确认配置中的 host、port、database 都一致

## 临时解决方案（用于测试）

如果确认 Redis 中没有 token，可以手动添加：

```bash
# 1. 从浏览器复制 accessToken
# 2. 在 Redis CLI 中执行
SET token:access:1 "粘贴你的token"
EXPIRE token:access:1 7200
```

然后刷新页面，看是否能正常访问。

## 下一步

请按照上述步骤执行，并告诉我：
1. Redis 中是否有 token？
2. 网关日志显示什么？
3. Auth 服务日志显示什么？

这样我可以更准确地定位问题。
