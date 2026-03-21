# 401 认证问题完整分析

## 问题现象
用户成功登录后，进入学生端立即出现大量 401 错误，被强制返回登录页面。

## 根本原因：字段名不匹配

### 后端返回的数据结构
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200,
    "userInfo": {
      "id": "1",
      "username": "student01",
      "role": "student",
      "subRole": null,
      "tenantId": "0"
    }
  }
}
```

### 前端期望的字段名（使用下划线）
```typescript
// frontend/src/auth/context.tsx 第 26-27 行
accessToken: payload.access_token,  // ❌ 期望 access_token
refreshToken: payload.refresh_token, // ❌ 期望 refresh_token
expiresAt: payload.expires_in ? Date.now() + payload.expires_in * 1000 : undefined, // ❌ 期望 expires_in
```

### 实际后端返回的字段名（使用驼峰）
```java
// backend/zhitu-auth/src/main/java/com/zhitu/auth/dto/LoginResponse.java
@Data
@Builder
public class LoginResponse {
    private String accessToken;      // ✓ 实际是 accessToken
    private String refreshToken;     // ✓ 实际是 refreshToken
    private Long expiresIn;          // ✓ 实际是 expiresIn
    private UserInfo userInfo;       // ✓ 实际是 userInfo
}
```

## 问题链条

1. **登录成功**：后端返回 `accessToken`（驼峰）
2. **前端解析错误**：前端尝试读取 `payload.access_token`（下划线），得到 `undefined`
3. **Token 未保存**：`session.accessToken = undefined`
4. **后续请求失败**：
   - 前端发送请求时，`Authorization` 头为 `Bearer undefined`
   - 网关 `AuthFilter` 提取 token 失败（token 为 null）
   - 返回 401 错误："缺少 Authorization 请求头"
5. **刷新失败**：尝试刷新 token 时，`refreshToken` 也是 `undefined`，刷新失败
6. **强制登出**：前端清除 session，跳转回登录页

## 认证流程（正常情况）

```
┌─────────┐      ┌─────────┐      ┌─────────┐      ┌──────────┐
│ 前端    │      │ Gateway │      │  Auth   │      │  Redis   │
│ (3000)  │      │ (8888)  │      │ (8081)  │      │          │
└────┬────┘      └────┬────┘      └────┬────┘      └────┬─────┘
     │                │                │                │
     │ 1. POST /api/auth/v1/login     │                │
     ├───────────────>│                │                │
     │                │ 2. 白名单放行   │                │
     │                ├───────────────>│                │
     │                │                │ 3. 验证密码     │
     │                │                │ 4. 生成 JWT    │
     │                │                │ 5. 存 Redis    │
     │                │                ├───────────────>│
     │                │ 6. 返回 token  │                │
     │                │<───────────────┤                │
     │ 7. 保存 token  │                │                │
     │<───────────────┤                │                │
     │                │                │                │
     │ 8. GET /api/student-portal/xxx │                │
     │    Authorization: Bearer <token>                │
     ├───────────────>│                │                │
     │                │ 9. 验证 JWT    │                │
     │                │ 10. 查 Redis   │                │
     │                ├───────────────────────────────>│
     │                │ 11. token 有效 │                │
     │                │<───────────────────────────────┤
     │                │ 12. 注入用户头  │                │
     │                │    X-User-Id: 1                │
     │                │    X-User-Role: student        │
     │                │ 13. 转发到业务服务              │
     │                ├───────────────>│                │
     │ 14. 返回数据   │                │                │
     │<───────────────┤                │                │
```

## 解决方案

### ✅ 已修复：修改前端使用驼峰命名

**修改文件 1：`frontend/src/auth/context.tsx`**
```typescript
// 第 26-30 行（已修复）
const newSession: AuthSession = {
  username: payload.userInfo?.username ?? username.trim(),
  role: payload.userInfo?.role ?? role,
  loginAt: Date.now(),
  accessToken: payload.accessToken,      // ✅ 改为驼峰
  refreshToken: payload.refreshToken,    // ✅ 改为驼峰
  expiresAt: payload.expiresIn ? Date.now() + payload.expiresIn * 1000 : undefined, // ✅ 改为驼峰
  userInfo: payload.userInfo,            // ✅ 改为驼峰
};
```

**修改文件 2：`frontend/src/lib/http.ts`**
```typescript
// refreshAccessToken 函数（已修复）
const data = await res.json();
const payload = data.data ?? data;
updateAccessToken(payload.accessToken, payload.expiresIn);  // ✅ 改为驼峰
pendingQueue.forEach(cb => cb(payload.accessToken));
return payload.accessToken;
```

**修改文件 3：`frontend/src/lib/http.ts`**
```typescript
// 刷新请求体（已修复）
body: JSON.stringify({ refreshToken: session.refreshToken }),  // ✅ 改为驼峰
```

## 验证步骤

修复后，按以下步骤验证：

1. **清除浏览器缓存**：
   ```
   F12 → Application → Local Storage → 删除 auth_session
   ```

2. **重新登录**：
   - 用户名：`student01`
   - 密码：`password`
   - 角色：学生

3. **检查 Network 面板**：
   - 登录请求返回的 `accessToken` 字段
   - 后续请求的 `Authorization` 头应为 `Bearer eyJ...`（不是 `Bearer undefined`）

4. **检查 Console**：
   - 不应有 401 错误
   - 不应有 "Token 已失效" 提示

5. **检查 Local Storage**：
   ```javascript
   JSON.parse(localStorage.getItem('auth_session'))
   // 应该看到 accessToken 有值，不是 undefined
   ```

## 其他潜在问题（已排除）

### ✅ Vite 代理配置正确
```typescript
// frontend/vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8888',  // 正确指向 Gateway
    changeOrigin: true,
  }
}
```

### ✅ Gateway 白名单配置正确
```yaml
# backend/zhitu-gateway/src/main/resources/application.yml
zhitu:
  security:
    white-list:
      - /api/auth/v1/login          # 登录接口放行
      - /api/auth/v1/token/refresh  # 刷新接口放行
```

### ✅ Gateway AuthFilter 逻辑正确
- 白名单路径直接放行
- 提取 `Authorization: Bearer <token>`
- 验证 JWT 签名
- 验证 Redis 中 token 是否有效
- 注入用户信息头（X-User-Id, X-User-Role 等）

### ✅ Redis 配置已修复
- 使用 Spring Boot 3.x 配置路径：`spring.data.redis.*`
- Redis 密码正确配置

### ✅ 密码加密已修复
- 测试用户密码：`password`
- 数据库存储：BCrypt hash `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`

## 总结

**唯一问题**：前端使用下划线命名（`access_token`）读取后端返回的驼峰命名（`accessToken`）字段，导致 token 未正确保存，后续所有请求都因缺少有效 token 而返回 401。

**修复方法**：修改前端 `context.tsx` 使用驼峰命名读取字段。
