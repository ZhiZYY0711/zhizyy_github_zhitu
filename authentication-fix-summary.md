# 认证问题修复总结

## 已解决的问题

### 1. ✅ 字段名不匹配问题
**问题**：前端使用下划线命名读取后端的驼峰命名字段
**修复**：修改前端 `context.tsx` 和 `http.ts` 使用驼峰命名

### 2. ✅ Redis 序列化器不匹配问题
**问题**：
- Auth 服务使用 `Jackson2JsonRedisSerializer`，存储时添加引号：`"eyJ..."`
- Gateway 使用 `StringRedisSerializer`，读取时不带引号：`eyJ...`
- 导致字符串比较失败

**修复**：
- 在 `RedisConfig` 中添加 `stringRedisTemplate` Bean
- 修改 `AuthService` 使用 `stringRedisTemplate` 存储 token
- 修复文件：
  - `backend/zhitu-common/zhitu-common-redis/src/main/java/com/zhitu/common/redis/config/RedisConfig.java`
  - `backend/zhitu-auth/src/main/java/com/zhitu/auth/service/AuthService.java`

### 3. ✅ Gateway 响应已提交错误
**问题**：在 reactive 流中，响应已提交后尝试修改响应头
**修复**：调整 `AuthFilter` 的错误处理逻辑，使用 `onErrorResume` 统一处理

## 当前问题

### ❌ 请求绕过网关直接访问业务服务

**现象**：
```
No static resource api/student-portal/v1/tasks
No static resource api/student-portal/v1/recommendations
```

**原因**：前端请求直接访问了 student 服务（8085），而不是通过网关（8888）

**诊断**：
1. 检查浏览器 Network 面板，看请求的 URL 是什么
2. 应该是：`http://localhost:3000/api/student-portal/v1/tasks`
3. Vite 代理应该转发到：`http://localhost:8888/api/student-portal/v1/tasks`
4. 网关应该路由到：`lb://zhitu-student` (student 服务)

**可能的原因**：
1. 前端某些请求没有使用 `/api` 前缀
2. Vite 代理没有正常工作
3. 前端直接配置了业务服务的地址（8085）

## 下一步诊断

### 1. 检查浏览器 Network 面板

打开浏览器开发者工具（F12），查看 Network 面板：

**查看失败的请求**：
- Request URL 应该是什么？
- 实际是什么？

**正确的流程**：
```
浏览器 → http://localhost:3000/api/student-portal/v1/tasks
         ↓ (Vite 代理)
         http://localhost:8888/api/student-portal/v1/tasks
         ↓ (Gateway 路由)
         http://student-service:8085/api/student-portal/v1/tasks
```

**错误的流程**（当前情况）：
```
浏览器 → http://localhost:8085/api/student-portal/v1/tasks
         ↓ (直接访问 student 服务，绕过网关)
         404 Not Found (Spring 把它当作静态资源)
```

### 2. 检查前端 API 调用

查找前端代码中是否有硬编码的服务地址：

```bash
# 在前端目录搜索
grep -r "8085" frontend/src/
grep -r "localhost:8085" frontend/src/
```

### 3. 检查 Vite 代理是否工作

在浏览器控制台执行：
```javascript
fetch('/api/student-portal/v1/tasks')
  .then(r => console.log('Status:', r.status, 'URL:', r.url))
```

应该看到请求被代理到 `http://localhost:8888`

### 4. 检查前端环境变量

查看是否有环境变量配置了 API 基础 URL：
- `.env`
- `.env.local`
- `.env.development`

## 临时解决方案

如果 Vite 代理有问题，可以临时修改 `vite.config.ts`：

```typescript
server: {
  host: '0.0.0.0',
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8888',
      changeOrigin: true,
      configure: (proxy, options) => {
        proxy.on('proxyReq', (proxyReq, req, res) => {
          console.log('Proxying:', req.method, req.url, '→', options.target + req.url);
        });
      }
    }
  }
}
```

这会在控制台打印代理日志，帮助诊断问题。

## 验证步骤

修复后，按以下步骤验证：

1. **重启前端**：
   ```bash
   cd frontend
   npm run dev
   ```

2. **清除浏览器缓存**

3. **重新登录**

4. **检查 Network 面板**：
   - 所有 `/api/*` 请求应该显示为 `http://localhost:3000/api/...`
   - 不应该看到任何 `http://localhost:8085` 的请求

5. **检查网关日志**：
   - 应该看到所有 API 请求都经过 `AuthFilter`
   - 应该看到 "Token 验证通过" 的日志

## 完整的认证流程（正确）

```
1. 用户登录
   前端 → Vite(3000) → Gateway(8888) → Auth(8081)
   ← Token

2. 访问 API
   前端 → Vite(3000) → Gateway(8888) → Student(8085)
          [携带 Token]    [验证 Token]    [处理请求]
                         [注入用户头]
```

## 需要的信息

请提供以下信息帮助诊断：

1. **浏览器 Network 面板截图**：
   - 失败请求的 Request URL
   - Request Headers

2. **前端控制台是否有错误**

3. **Vite 开发服务器的控制台输出**：
   - 是否有代理相关的日志

4. **前端代码中是否有硬编码的 URL**：
   ```bash
   grep -r "8085" frontend/src/
   ```
