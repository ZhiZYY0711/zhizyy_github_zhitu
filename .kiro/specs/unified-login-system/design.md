# Design Document: 统一登录系统

## Overview

统一登录系统为现有的四平台 React 应用添加集中式身份验证入口。当前应用直接暴露所有平台路由，任何人无需认证即可访问。本设计在不引入新 npm 包的前提下，通过 React Context + localStorage 实现会话管理，通过路由守卫组件保护现有四个平台路由，并提供 `/login` 统一登录页面。

登录逻辑采用开发阶段的简化策略：任何非空用户名和密码组合均视为有效凭据，用户通过角色选择器决定登录后跳转的目标平台。

## Architecture

```mermaid
graph TD
    A[用户访问任意路由] --> B{Route_Guard 检查}
    B -->|未认证| C[重定向到 /login]
    B -->|已认证| D[渲染目标平台]
    C --> E[LoginPage 组件]
    E --> F[用户填写表单并选择角色]
    F --> G{AuthService 验证}
    G -->|空字段| H[显示错误信息]
    G -->|非空凭据| I[Session_Manager 写入 localStorage]
    I --> J[Platform_Router 跳转]
    J --> K[/student/dashboard]
    J --> L[/enterprise/dashboard]
    J --> M[/college/dashboard]
    J --> N[/platform/dashboard]
```

### 核心模块

- `src/auth/types.ts` — 类型定义（UserRole、AuthSession、LoginCredentials）
- `src/auth/session.ts` — Session_Manager，封装 localStorage 读写
- `src/auth/context.tsx` — AuthContext + AuthProvider，全局认证状态
- `src/auth/ProtectedRoute.tsx` — Route_Guard 路由守卫组件
- `src/login/page.tsx` — 登录页面组件
- `src/App.tsx`（修改）— 集成 AuthProvider、/login 路由、路由守卫

## Components and Interfaces

### AuthProvider

包裹整个应用，向子组件提供认证状态和操作方法。

```tsx
interface AuthContextValue {
  session: AuthSession | null;
  login: (credentials: LoginCredentials) => LoginResult;
  logout: () => void;
  isAuthenticated: boolean;
}
```

### ProtectedRoute

包裹需要保护的路由，未认证时重定向到 `/login`，并通过 `state.from` 保存原始目标路径。

```tsx
// 用法
<Route path="/student" element={<ProtectedRoute><StudentLayout /></ProtectedRoute>}>
```

### LoginPage

使用 shadcn/ui 的 `Card`、`Input`、`Label`、`Button`、`Select` 组件构建登录表单。

- 用户名输入框（页面加载时自动聚焦）
- 密码输入框
- 角色选择下拉框（四个选项）
- 登录按钮（提交时显示加载状态）
- 错误信息展示区（中文）

### Session_Manager（session.ts）

```ts
const SESSION_KEY = 'auth_session';

function saveSession(session: AuthSession): void
function loadSession(): AuthSession | null
function clearSession(): void
```

## Data Models

```ts
// src/auth/types.ts

export type UserRole = 'student' | 'enterprise' | 'college' | 'platform';

export interface LoginCredentials {
  username: string;
  password: string;
  role: UserRole;
}

export interface AuthSession {
  username: string;
  role: UserRole;
  loginAt: number; // Unix timestamp (ms)
}

export interface LoginResult {
  success: boolean;
  error?: string;
}

// 角色到路由的映射
export const ROLE_ROUTES: Record<UserRole, string> = {
  student: '/student/dashboard',
  enterprise: '/enterprise/dashboard',
  college: '/college/dashboard',
  platform: '/platform/dashboard',
};

// 角色显示名称
export const ROLE_LABELS: Record<UserRole, string> = {
  student: '学生端',
  enterprise: '企业端',
  college: '高校端',
  platform: '平台端',
};
```

### localStorage 存储结构

```json
{
  "auth_session": {
    "username": "testuser",
    "role": "student",
    "loginAt": 1700000000000
  }
}
```

