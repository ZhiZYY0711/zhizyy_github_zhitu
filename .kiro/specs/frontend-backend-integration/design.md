# 前后端联调设计文档

## 1. 高层设计（High-Level Design）

### 1.1 整体架构

```
浏览器
  └── Vite Dev Server (localhost:3000)
        └── /api/* → proxy → Spring Cloud Gateway (localhost:8080)
                              ├── /api/auth/**       → zhitu-auth (8081)
                              ├── /api/internship/** → zhitu-enterprise (8082)
                              ├── /api/training/**   → zhitu-enterprise (8082)
                              ├── /api/portal-enterprise/** → zhitu-enterprise (8082)
                              ├── /api/portal-college/**    → zhitu-college (8083)
                              ├── /api/internship/college/** → zhitu-college (8083)
                              ├── /api/portal-platform/**   → zhitu-platform (8084)
                              ├── /api/system/**     → zhitu-system (8085)
                              └── /api/student-portal/** → zhitu-student (8086)
```

### 1.2 认证流程

```
用户点击登录
  → 前端调用 POST /api/auth/v1/login
  → 后端返回 { access_token, refresh_token, expires_in, user: { id, username, role } }
  → 前端存储 token 到 localStorage (AuthSession)
  → 后续所有请求自动附加 Authorization: Bearer <access_token>
  → access_token 过期 → 自动调用 POST /api/auth/v1/refresh 换新 token
  → refresh_token 过期 → 清除 session，跳转 /login
```

### 1.3 统一 HTTP 客户端

引入统一的 `http.ts` 替代各模块分散的 `fetchWithFallback`：

- 自动注入 `Authorization: Bearer <token>` 请求头
- 统一处理 401 → 尝试刷新 token，失败则跳转登录
- 保留 mock fallback 机制（开发阶段）
- 统一错误格式

### 1.4 组件关系图

```
AuthContext (context.tsx)
  ├── login()  → 调用 POST /api/auth/v1/login → 存储 token
  ├── logout() → 清除 session + 调用 POST /api/auth/v1/logout
  └── session  → { username, role, accessToken, refreshToken, expiresAt }

http.ts (统一客户端)
  ├── get / post / put / delete
  ├── 请求拦截：注入 Authorization header
  └── 响应拦截：401 → refreshToken → 重试 or 跳转登录

各模块 services/api.ts
  └── 使用 http.ts 替代原有 fetchWithFallback
```

---

## 2. 低层设计（Low-Level Design）

### 2.1 `frontend/src/lib/http.ts` — 统一 HTTP 客户端

```typescript
// 核心接口
interface RequestOptions extends RequestInit {
  skipAuth?: boolean;   // 跳过 token 注入（登录接口用）
  skipMock?: boolean;   // 强制不 fallback mock
}

async function request<T>(url: string, options?: RequestOptions): Promise<T>
async function get<T>(url: string, options?: RequestOptions): Promise<T>
async function post<T>(url: string, body?: object, options?: RequestOptions): Promise<T>
async function put<T>(url: string, body?: object, options?: RequestOptions): Promise<T>
async function del<T>(url: string, options?: RequestOptions): Promise<T>

// 内部逻辑
// 1. 从 session 读取 accessToken，注入 Authorization header
// 2. 发起 fetch
// 3. 若响应 401：调用 refreshAccessToken()，成功则重试原请求
// 4. refreshAccessToken() 失败：clearSession() + window.location = '/login'
// 5. 非 401 错误：抛出 ApiError { status, message }
```

### 2.2 `frontend/src/auth/context.tsx` — 改造 login 函数

```typescript
// 改造前（mock）
const login = (credentials) => {
  // 直接创建 session，不调用 API
  saveSession({ username, role, loginAt });
  return { success: true };
};

// 改造后（真实 API）
const login = async (credentials): Promise<LoginResult> => {
  const res = await fetch('/api/auth/v1/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password, role }),
    // skipAuth: true — 登录接口不需要 token
  });
  if (!res.ok) return { success: false, error: '用户名或密码错误' };
  const data = await res.json();
  // data: { access_token, refresh_token, expires_in, user }
  saveSession({
    username: data.user.username,
    role: data.user.role,
    accessToken: data.access_token,
    refreshToken: data.refresh_token,
    expiresAt: Date.now() + data.expires_in * 1000,
    loginAt: Date.now(),
  });
  return { success: true };
};
```

### 2.3 `frontend/src/auth/types.ts` — 扩展 AuthSession

```typescript
// 新增字段
interface AuthSession {
  username: string;
  role: UserRole;
  loginAt: number;
  // 新增：
  accessToken?: string;
  refreshToken?: string;
  expiresAt?: number;
}
```

### 2.4 `frontend/vite.config.ts` — 开发代理配置

```typescript
server: {
  host: '0.0.0.0',
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // Spring Cloud Gateway
      changeOrigin: true,
      // 不重写路径，保持 /api/xxx 原样转发
    }
  }
}
```

### 2.5 各模块 `services/api.ts` 改造模式

```typescript
// 改造前
async function fetchWithFallback<T>(url, mockFn, options?) {
  try {
    const res = await fetch(url, options);
    ...
  } catch {
    return mockFn();
  }
}

// 改造后：使用统一 http 客户端
import { http } from '@/lib/http';

export const fetchJobs = (status?: string) => {
  const qs = status ? `?status=${status}` : '';
  return http.get(`${INTERNSHIP_API}/enterprise/jobs${qs}`, {
    mockFn: Mock.getMockJobs,  // 后端不可用时降级
  });
};

export const createJob = (data: object) =>
  http.post(`${INTERNSHIP_API}/enterprise/jobs`, data);
```

### 2.6 后端 API 响应格式统一

后端所有接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

前端 `http.ts` 自动解包 `data` 字段，业务代码直接拿到数据。

### 2.7 登录接口后端适配

后端 `zhitu-auth` 的 `/api/auth/v1/login` 需要支持 `role` 字段路由：

```
role = "student"   → 查 student 表
role = "enterprise" → 查 enterprise_staff 表
role = "college"   → 查 college_staff 表（或 teacher 表）
role = "admin"     → 查 system_user 表
```

### 2.8 Token 刷新流程（伪代码）

```typescript
let isRefreshing = false;
let pendingQueue: Array<(token: string) => void> = [];

async function refreshAccessToken(): Promise<string> {
  if (isRefreshing) {
    // 等待刷新完成
    return new Promise(resolve => pendingQueue.push(resolve));
  }
  isRefreshing = true;
  const session = loadSession();
  const res = await fetch('/api/auth/v1/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refresh_token: session?.refreshToken }),
  });
  if (!res.ok) throw new Error('Refresh failed');
  const { access_token, expires_in } = await res.json();
  updateAccessToken(access_token, expires_in);
  pendingQueue.forEach(cb => cb(access_token));
  pendingQueue = [];
  isRefreshing = false;
  return access_token;
}
```

---

## 3. 实现任务清单

### Phase 1：基础设施（必须先完成）

- [ ] 1.1 创建 `frontend/src/lib/http.ts` — 统一 HTTP 客户端（含 token 注入、401 处理、mock fallback）
- [ ] 1.2 扩展 `frontend/src/auth/types.ts` — AuthSession 增加 token 字段
- [ ] 1.3 改造 `frontend/src/auth/context.tsx` — login 调用真实 API
- [ ] 1.4 配置 `frontend/vite.config.ts` — 添加 /api proxy 到 Gateway

### Phase 2：各模块 API 接入

- [ ] 2.1 改造 `frontend/src/student/services/api.ts`
- [ ] 2.2 改造 `frontend/src/enterprise/services/api.ts`
- [ ] 2.3 改造 `frontend/src/college/services/api.ts`
- [ ] 2.4 改造 `frontend/src/platform/services/api.ts`

### Phase 3：后端适配

- [ ] 3.1 `zhitu-auth` 登录接口支持 role 路由 + 返回标准 token 结构
- [ ] 3.2 `zhitu-gateway` 路由规则配置（各模块路径映射）
- [ ] 3.3 各模块 Controller 统一返回 `Result<T>` 包装格式

### Phase 4：联调验证

- [ ] 4.1 学生端登录 → 获取 token → 访问 dashboard 接口
- [ ] 4.2 企业端登录 → 发布职位 → 查看申请
- [ ] 4.3 高校端登录 → 查看学生列表 → 审核合同
- [ ] 4.4 平台端登录 → 审核企业 → 查看监控数据
