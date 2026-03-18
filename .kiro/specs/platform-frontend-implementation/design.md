# Design Document: Platform Frontend Implementation

## Overview

本设计文档定义了校企合作实习实训管理平台的平台端（Platform）前端应用的技术架构和实现方案。平台端面向系统管理员和运营人员，提供基础数据维护、机构审核、全局资源调度和系统监控等核心功能。

### 设计目标

1. **架构一致性**: 遵循学生端已建立的架构模式，确保代码风格和项目结构的统一性
2. **可靠性**: 实现 API 调用失败时的 mock 数据降级机制，保证开发和演示的连续性
3. **可维护性**: 采用模块化设计，清晰的职责分离，便于后续功能扩展
4. **用户体验**: 提供流畅的交互体验，实时数据更新，友好的错误提示
5. **权限控制**: 实现基于角色的访问控制，确保不同角色只能访问授权的功能模块

### 技术栈

- **框架**: React 18 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: shadcn/ui (基于 Radix UI)
- **路由**: React Router 6
- **图表库**: ECharts (用于监控仪表盘和数据可视化)
- **样式**: Tailwind CSS 4
- **图标**: Lucide React
- **状态管理**: React Hooks (useState, useEffect)

## Architecture

### 系统架构

平台端前端采用分层架构，从上到下分为：

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (Pages, Components, Layout)                            │
├─────────────────────────────────────────────────────────┤
│                    Service Layer                         │
│  (API Service, Mock Generator)                          │
├─────────────────────────────────────────────────────────┤
│                    Infrastructure Layer                  │
│  (Router, Auth Guard, Error Boundary)                   │
└─────────────────────────────────────────────────────────┘
```

### 目录结构

```
frontend/src/platform/
├── layout.tsx                    # 平台端布局组件
├── services/
│   └── api.ts                    # API 调用封装
├── mock/
│   └── generator.ts              # Mock 数据生成器
├── dashboard/
│   ├── page.tsx                  # 数据概览仪表盘
│   └── components/
│       ├── StatsCards.tsx        # 统计卡片
│       ├── TrendChart.tsx        # 趋势图表
│       └── QuickActions.tsx      # 快捷操作
├── master-data/
│   ├── page.tsx                  # 主数据管理页面
│   └── components/
│       ├── TagManager.tsx        # 标签管理
│       ├── SkillTreeEditor.tsx   # 技能树编辑器
│       └── TemplateManager.tsx   # 模板管理
├── audit/
│   ├── page.tsx                  # 审核管理页面
│   └── components/
│       ├── TenantList.tsx        # 租户列表
│       ├── EnterpriseAudit.tsx   # 企业审核
│       └── ProjectAudit.tsx      # 项目审核
├── resources/
│   ├── page.tsx                  # 资源调度页面
│   └── components/
│       ├── RecommendationManager.tsx  # 推荐位管理
│       └── ResourceAllocator.tsx      # 资源分配
├── monitor/
│   ├── page.tsx                  # 系统监控页面
│   └── components/
│       ├── HealthDashboard.tsx   # 健康状态仪表盘
│       ├── MetricsChart.tsx      # 指标图表
│       └── AlertPanel.tsx        # 告警面板
├── logs/
│   ├── page.tsx                  # 日志审计页面
│   └── components/
│       ├── OperationLogTable.tsx # 操作日志表格
│       └── SecurityLogTable.tsx  # 安全日志表格
└── utils/
    ├── auth.ts                   # 权限工具函数
    └── polling.ts                # 轮询工具函数
```

### 架构设计原则

1. **关注点分离**: 
   - Presentation Layer 负责 UI 渲染和用户交互
   - Service Layer 负责数据获取和业务逻辑
   - Infrastructure Layer 负责路由、认证和错误处理

2. **依赖注入**: 
   - 组件通过 props 接收数据和回调函数
   - 避免组件直接依赖具体的 API 实现

3. **错误边界**: 
   - 使用 React Error Boundary 捕获组件错误
   - 提供友好的错误提示和恢复机制

4. **代码复用**: 
   - 提取通用组件到 components/ui
   - 共享工具函数到 utils 目录

## Components and Interfaces

### 核心组件设计

#### 1. Layout Component

```typescript
// platform/layout.tsx
interface PlatformLayoutProps {
  children: React.ReactNode;
}

interface NavItem {
  title: string;
  href: string;
  icon: LucideIcon;
  roles: UserRole[];  // 允许访问的角色
}

const PlatformLayout: React.FC<PlatformLayoutProps> = ({ children }) => {
  // 侧边栏导航
  // 顶部用户信息栏
  // 权限控制：根据用户角色过滤导航项
  // 响应式布局
}
```

#### 2. API Service

```typescript
// platform/services/api.ts
interface ApiConfig {
  baseURL: string;
  timeout: number;
}

interface ApiResponse<T> {
  data: T;
  message?: string;
  code: number;
}

class PlatformApiService {
  private config: ApiConfig;
  
  // 通用请求方法
  async request<T>(
    endpoint: string,
    options?: RequestInit
  ): Promise<T>;
  
  // 带降级的请求方法
  async fetchWithFallback<T>(
    endpoint: string,
    mockFn: () => T,
    options?: RequestInit
  ): Promise<T>;
  
  // 具体业务方法
  async fetchSystemHealth(): Promise<SystemHealth>;
  async fetchTenantList(query: TenantQuery): Promise<TenantList>;
  async auditEnterprise(id: string, action: AuditAction): Promise<void>;
  // ... 其他 API 方法
}
```

#### 3. Mock Generator

```typescript
// platform/mock/generator.ts
export interface SystemHealth {
  cpu_usage: number;
  memory_usage: number;
  active_services: number;
  error_rate: number;
  online_users: number;
}

export interface Tenant {
  id: string;
  name: string;
  type: 'college' | 'enterprise';
  status: 'active' | 'inactive' | 'pending';
  created_at: string;
  max_students?: number;
  expire_date?: string;
}

export interface AuditItem {
  id: string;
  type: 'enterprise' | 'project';
  name: string;
  status: 'pending' | 'approved' | 'rejected';
  apply_time: string;
  details: Record<string, any>;
}

// Mock 数据生成函数
export const getMockSystemHealth = (): SystemHealth => { /* ... */ };
export const getMockTenantList = (): Tenant[] => { /* ... */ };
export const getMockAuditQueue = (): AuditItem[] => { /* ... */ };
// ... 其他 mock 生成函数
```

#### 4. Dashboard Components

```typescript
// platform/dashboard/components/StatsCards.tsx
interface StatsCardsProps {
  stats: {
    total_users: number;
    active_projects: number;
    internship_positions: number;
    pending_audits: number;
  } | null;
  loading: boolean;
}

// platform/dashboard/components/TrendChart.tsx
interface TrendChartProps {
  data: {
    date: string;
    value: number;
  }[];
  title: string;
  loading: boolean;
}
```

#### 5. Audit Components

```typescript
// platform/audit/components/EnterpriseAudit.tsx
interface EnterpriseAuditProps {
  item: AuditItem;
  onAudit: (id: string, action: AuditAction, reason?: string) => Promise<void>;
}

interface AuditAction {
  action: 'pass' | 'reject';
  reject_reason?: string;
}

// platform/audit/components/ProjectAudit.tsx
interface ProjectAuditProps {
  item: AuditItem;
  onAudit: (id: string, action: ProjectAuditAction) => Promise<void>;
}

interface ProjectAuditAction {
  action: 'pass' | 'reject';
  quality_rating?: 'S' | 'A' | 'B' | 'C';
  comment?: string;
}
```

#### 6. Monitor Components

```typescript
// platform/monitor/components/HealthDashboard.tsx
interface HealthDashboardProps {
  health: SystemHealth | null;
  loading: boolean;
}

// platform/monitor/components/MetricsChart.tsx
interface MetricsChartProps {
  metrics: {
    timestamp: string;
    cpu: number;
    memory: number;
    requests: number;
  }[];
  loading: boolean;
}

// 使用 ECharts 渲染实时监控图表
```

#### 7. Log Components

```typescript
// platform/logs/components/OperationLogTable.tsx
interface OperationLog {
  id: string;
  user_name: string;
  action: string;
  module: string;
  ip: string;
  time: string;
  result: 'success' | 'fail';
}

interface OperationLogTableProps {
  logs: OperationLog[];
  loading: boolean;
  onExport: () => void;
}

// platform/logs/components/SecurityLogTable.tsx
interface SecurityLog {
  id: string;
  event_type: string;
  level: 'low' | 'medium' | 'high';
  description: string;
  ip: string;
  time: string;
}

interface SecurityLogTableProps {
  logs: SecurityLog[];
  loading: boolean;
  onExport: () => void;
}
```

### 组件通信模式

1. **父子组件通信**: 通过 props 传递数据和回调函数
2. **兄弟组件通信**: 通过共同的父组件状态管理
3. **跨层级通信**: 对于深层嵌套，使用 Context API（如用户权限上下文）

### 组件复用策略

1. **UI 组件**: 使用 shadcn/ui 提供的基础组件（Button, Card, Dialog, Table 等）
2. **业务组件**: 提取通用的业务逻辑组件（如 AuditDialog, ConfirmDialog）
3. **图表组件**: 封装 ECharts 为可复用的 React 组件

## Data Models

### 核心数据模型

#### 1. 用户与权限

```typescript
enum UserRole {
  SYSTEM_ADMIN = 'system_admin',
  AUDIT_MANAGER = 'audit_manager',
  OPERATIONS_MANAGER = 'operations_manager',
  DEVOPS_MANAGER = 'devops_manager'
}

interface User {
  id: string;
  username: string;
  name: string;
  role: UserRole;
  permissions: string[];
  avatar?: string;
}

interface AuthContext {
  user: User | null;
  isAuthenticated: boolean;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: UserRole) => boolean;
}
```

#### 2. 租户管理

```typescript
interface Tenant {
  id: string;
  name: string;
  type: 'college' | 'enterprise';
  status: 'active' | 'inactive' | 'pending';
  domain?: string;  // 专属子域名（高校）
  admin_username: string;
  admin_email: string;
  max_students?: number;  // 高校租户
  expire_date?: string;
  created_at: string;
  updated_at: string;
}

interface TenantQuery {
  type?: 'college' | 'enterprise';
  status?: 'active' | 'inactive' | 'pending';
  keyword?: string;
  page: number;
  page_size: number;
}

interface TenantListResponse {
  total: number;
  records: Tenant[];
}
```

#### 3. 审核管理

```typescript
interface EnterpriseAuditItem {
  id: string;
  name: string;
  license_url: string;  // 营业执照
  contact_person: string;
  contact_phone: string;
  apply_time: string;
  status: 'pending' | 'approved' | 'rejected';
  audit_time?: string;
  auditor?: string;
  reject_reason?: string;
}

interface ProjectAuditItem {
  id: string;
  name: string;
  provider: string;
  provider_type: 'enterprise' | 'college';
  tech_stack: string[];
  description: string;
  difficulty: number;
  apply_time: string;
  status: 'pending' | 'approved' | 'rejected';
  quality_rating?: 'S' | 'A' | 'B' | 'C';
  audit_comment?: string;
}

interface AuditQuery {
  type: 'enterprise' | 'project';
  status?: 'pending' | 'approved' | 'rejected';
  provider_type?: 'enterprise' | 'college';
  start_time?: string;
  end_time?: string;
  page: number;
  page_size: number;
}
```

#### 4. 主数据管理

```typescript
interface Tag {
  id: string;
  category: 'industry' | 'tech_stack' | 'skill';
  name: string;
  parent_id?: string;
  order: number;
  created_at: string;
}

interface SkillTreeNode {
  id: string;
  name: string;
  level: number;  // 1: 一级分类, 2: 二级分类, 3: 具体技能
  parent_id?: string;
  children?: SkillTreeNode[];
  description?: string;
}

interface CertificateTemplate {
  id: string;
  name: string;
  background_url: string;
  elements_layout: {
    name: { x: number; y: number; fontSize: number };
    date: { x: number; y: number; fontSize: number };
    // ... 其他元素
  };
  created_at: string;
  updated_at: string;
}

interface ContractTemplate {
  id: string;
  name: string;
  version: string;
  content: string;  // 合同内容（支持变量占位符）
  variables: string[];  // 可用变量列表
  status: 'draft' | 'active' | 'archived';
  created_at: string;
}
```

#### 5. 推荐管理

```typescript
interface RecommendationBanner {
  id: string;
  target_type: 'project' | 'enterprise' | 'course';
  target_id: string;
  title: string;
  image_url: string;
  order: number;
  status: 'active' | 'inactive';
  start_date?: string;
  end_date?: string;
}

interface TopListItem {
  id: string;
  list_type: 'mentor' | 'course' | 'project';
  item_id: string;
  item_name: string;
  order: number;
  reason?: string;  // 推荐理由
}
```

#### 6. 系统监控

```typescript
interface SystemHealth {
  cpu_usage: number;  // 0-1
  memory_usage: number;  // 0-1
  disk_usage: number;  // 0-1
  active_services: number;
  error_rate: number;  // 0-1
  online_users: number;
  timestamp: string;
}

interface OnlineUserTrend {
  data: {
    time: string;
    count: number;
  }[];
  period: '1h' | '24h' | '7d' | '30d';
}

interface ServiceStatus {
  name: string;
  status: 'healthy' | 'degraded' | 'down';
  response_time: number;  // ms
  last_check: string;
}
```

#### 7. 日志审计

```typescript
interface OperationLog {
  id: string;
  user_id: string;
  user_name: string;
  action: string;
  module: string;
  details?: Record<string, any>;
  ip: string;
  user_agent?: string;
  time: string;
  result: 'success' | 'fail';
  error_message?: string;
}

interface SecurityLog {
  id: string;
  event_type: 'login_fail' | 'abnormal_access' | 'permission_denied' | 'data_breach';
  level: 'low' | 'medium' | 'high' | 'critical';
  description: string;
  user_id?: string;
  ip: string;
  time: string;
  handled: boolean;
}

interface LogQuery {
  type: 'operation' | 'security';
  user_id?: string;
  module?: string;
  level?: 'low' | 'medium' | 'high' | 'critical';
  result?: 'success' | 'fail';
  start_time?: string;
  end_time?: string;
  page: number;
  page_size: number;
}
```

#### 8. 数据可视化

```typescript
interface DashboardStats {
  total_users: number;
  total_colleges: number;
  total_enterprises: number;
  active_projects: number;
  internship_positions: number;
  pending_audits: number;
  system_health_score: number;  // 0-100
}

interface TrendData {
  date: string;
  value: number;
  label?: string;
}

interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'gauge' | 'radar';
  title: string;
  xAxis?: string;
  yAxis?: string;
  series: {
    name: string;
    data: number[];
  }[];
}
```

### 数据流设计

```
User Action → Component Event Handler → API Service → Backend API
                                              ↓ (on error)
                                        Mock Generator
                                              ↓
                                        Component State Update
                                              ↓
                                        UI Re-render
```

### 数据缓存策略

1. **无缓存**: 实时监控数据（每 30 秒轮询）
2. **短期缓存**: 审核队列、日志列表（5 分钟）
3. **长期缓存**: 主数据（标签、模板）（30 分钟）
4. **手动刷新**: 所有页面提供刷新按钮，允许用户手动更新数据


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

在分析需求文档的验收标准后，我识别出以下可测试的属性。为了避免冗余，我进行了以下合并：

- **CRUD 操作属性合并**: 需求 3.1、3.3、3.4、4.1 都涉及创建后列表更新，可以合并为一个通用的"创建操作更新列表"属性
- **详情展示属性合并**: 需求 4.3、5.2、7.2 都涉及详情页显示必需字段，可以合并为一个通用的"详情完整性"属性
- **筛选功能属性合并**: 需求 4.6、7.1、7.3 都涉及列表筛选，可以合并为一个通用的"筛选正确性"属性
- **审核操作属性合并**: 需求 4.4、4.5、5.3 都涉及审核操作和状态更新，可以合并为一个通用的"审核流程"属性
- **条件渲染属性合并**: 需求 6.6、7.6 都涉及根据条件应用样式，可以合并为一个通用的"条件样式"属性
- **角色权限示例**: 需求 8.3-8.6 是具体角色的权限配置，作为示例测试，不需要为每个角色单独创建属性

### Property 1: API 降级机制

*For any* API 端点调用失败时，系统应该自动降级到 Mock_Generator 提供的数据，并在控制台输出警告信息。

**Validates: Requirements 2.2, 2.3**

### Property 2: Mock 数据格式一致性

*For any* Mock_Generator 生成的数据，其结构应该符合对应的 TypeScript 接口定义，确保类型安全。

**Validates: Requirements 2.4**

### Property 3: Mock 数据随机性

*For any* Mock_Generator 函数，多次调用应该返回不同的数据值（至少有一个字段不同），确保数据具有合理的变化。

**Validates: Requirements 2.5**

### Property 4: 用户信息显示完整性

*For any* 登录用户对象，Layout 组件的顶部栏应该显示用户名称和角色标识。

**Validates: Requirements 1.3**

### Property 5: 创建操作更新列表

*For any* 实体类型（标签、模板、租户等），当创建操作成功后，该实体的列表应该包含新创建的项目，并显示成功提示。

**Validates: Requirements 3.1, 3.3, 3.4, 4.1**

### Property 6: 表单验证阻止无效提交

*For any* 包含无效输入的表单，提交操作应该被阻止，并显示相应的错误提示信息。

**Validates: Requirements 3.5**

### Property 7: 详情展示完整性

*For any* 实体详情页面（企业申请、项目详情、日志记录等），应该显示该实体类型的所有必需字段。

**Validates: Requirements 4.3, 5.2, 7.2**

### Property 8: 列表筛选正确性

*For any* 列表筛选条件（状态、时间范围、风险等级等），筛选后的结果应该只包含符合该条件的记录。

**Validates: Requirements 4.6, 7.1, 7.3**

### Property 9: 审核操作状态更新

*For any* 审核操作（通过或拒绝），操作完成后，被审核项目的状态应该正确更新，并从待审核队列中移除或更新显示。

**Validates: Requirements 4.4, 4.5, 5.3**

### Property 10: 推荐列表排序一致性

*For any* 推荐列表的排序操作，保存后的顺序应该与用户设置的顺序一致。

**Validates: Requirements 5.5**

### Property 11: 监控数据完整渲染

*For any* 系统监控数据对象，监控仪表盘应该渲染所有关键指标（CPU、内存、在线用户数、错误率）。

**Validates: Requirements 6.1, 9.1**

### Property 12: 图表组件数据绑定

*For any* 图表配置和数据，Chart_Component 应该正确初始化 ECharts 实例并渲染对应类型的图表。

**Validates: Requirements 6.2, 9.2**

### Property 13: 轮询服务定时执行

*For any* 配置的轮询间隔（如 30 秒），Polling_Service 应该按照该间隔定期调用 API 端点。

**Validates: Requirements 6.3**

### Property 14: 图表数据更新响应

*For any* 图表组件，当数据源更新时，图表应该调用 ECharts 的更新方法重新渲染。

**Validates: Requirements 6.4, 9.5**

### Property 15: 条件样式应用

*For any* 超过阈值的指标或失败/高风险的记录，应该应用醒目的警告样式（如红色、高亮）。

**Validates: Requirements 6.6, 7.6**

### Property 16: 日志导出格式正确性

*For any* 日志数据集合，导出的 CSV 文件应该包含所有记录和正确的列标题。

**Validates: Requirements 7.4**

### Property 17: 分页记录数限制

*For any* 分页列表，每页显示的记录数应该不超过配置的限制（如 50 条）。

**Validates: Requirements 7.5**

### Property 18: 角色权限菜单过滤

*For any* 用户角色，导航菜单应该只显示该角色有权访问的功能模块入口。

**Validates: Requirements 8.1**

### Property 19: 无权限访问拦截

*For any* 用户尝试访问无权限的路由，应该被重定向到 403 错误页面。

**Validates: Requirements 8.7**

### Property 20: 时间范围数据筛选

*For any* 时间范围选择，图表和报表应该只显示该时间段内的数据。

**Validates: Requirements 9.3**

### Property 21: 数据对比计算正确性

*For any* 两个时间段的数据，同比和环比计算应该返回正确的百分比变化值。

**Validates: Requirements 9.4**

### Property 22: 图表导出功能

*For any* 图表实例，导出操作应该生成包含图表内容的 PNG 图片文件。

**Validates: Requirements 9.6**

### Property 23: 加载状态显示

*For any* 异步数据加载过程，在数据加载完成前应该显示加载指示器（骨架屏或加载动画）。

**Validates: Requirements 10.1**

### Property 24: 操作反馈提示

*For any* 用户操作（成功或失败），应该显示相应的 Toast 提示消息，成功提示应在 3 秒后自动消失。

**Validates: Requirements 10.2, 10.3**

### Property 25: 表单提交防重复

*For any* 表单提交操作，在提交过程中提交按钮应该被禁用，防止重复提交。

**Validates: Requirements 10.4**

### Property 26: 表格功能完整性

*For any* 数据表格，应该支持排序、筛选和搜索功能，并正确返回符合条件的结果。

**Validates: Requirements 10.5**

### Property 27: 危险操作二次确认

*For any* 危险操作（如删除、撤销），执行前应该显示确认对话框，只有用户确认后才执行操作。

**Validates: Requirements 10.7**

## Error Handling

### 错误分类

1. **网络错误**: API 调用失败、超时
2. **业务错误**: 表单验证失败、权限不足、操作冲突
3. **系统错误**: 组件渲染错误、未捕获的异常

### 错误处理策略

#### 1. API 调用错误

```typescript
// 自动降级到 mock 数据
async fetchWithFallback<T>(
  endpoint: string,
  mockFn: () => T,
  options?: RequestInit
): Promise<T> {
  try {
    const response = await fetch(`${this.baseURL}${endpoint}`, options);
    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.warn(`API call failed for ${endpoint}, using mock data`, error);
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    return mockFn();
  }
}
```

#### 2. 表单验证错误

```typescript
// 使用 React Hook Form 或自定义验证
const validateForm = (data: FormData): ValidationResult => {
  const errors: Record<string, string> = {};
  
  if (!data.name || data.name.trim() === '') {
    errors.name = '名称不能为空';
  }
  
  if (data.email && !isValidEmail(data.email)) {
    errors.email = '邮箱格式不正确';
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

#### 3. 权限错误

```typescript
// 路由守卫
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ 
  children, 
  requiredRole 
}) => {
  const { user, hasRole } = useAuth();
  
  if (!user) {
    return <Navigate to="/login" />;
  }
  
  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/403" />;
  }
  
  return <>{children}</>;
};
```

#### 4. 组件错误边界

```typescript
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean; error?: Error }
> {
  constructor(props: any) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Component error:', error, errorInfo);
    // 可以发送错误日志到监控服务
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-container">
          <h2>出错了</h2>
          <p>页面加载失败，请刷新重试</p>
          <Button onClick={() => window.location.reload()}>
            刷新页面
          </Button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

### 用户友好的错误提示

1. **Toast 提示**: 用于临时性的操作反馈
   - 成功: 绿色，3 秒后自动消失
   - 失败: 红色，显示错误原因，需要用户手动关闭
   - 警告: 黄色，提示用户注意事项

2. **表单内联错误**: 在表单字段下方显示验证错误信息

3. **错误页面**: 
   - 403: 权限不足
   - 404: 页面不存在
   - 500: 服务器错误

4. **加载失败重试**: 提供"重试"按钮，允许用户手动重新加载数据

### 错误日志记录

```typescript
// 错误日志工具
const logError = (error: Error, context: string) => {
  const errorLog = {
    message: error.message,
    stack: error.stack,
    context,
    timestamp: new Date().toISOString(),
    userAgent: navigator.userAgent,
    url: window.location.href
  };
  
  console.error('Error logged:', errorLog);
  
  // 在生产环境中，发送到监控服务
  if (import.meta.env.PROD) {
    // sendToMonitoringService(errorLog);
  }
};
```

## Testing Strategy

### 测试方法论

平台端前端采用双重测试策略，结合单元测试和属性测试，确保代码质量和功能正确性。

#### 1. 单元测试 (Unit Tests)

使用 **Vitest** 和 **React Testing Library** 进行单元测试。

**测试范围**:
- 组件渲染: 验证组件在不同 props 下的渲染结果
- 用户交互: 模拟用户操作（点击、输入）并验证响应
- 边界条件: 测试空数据、错误状态等边界情况
- 工具函数: 测试纯函数的输入输出

**示例**:

```typescript
// __tests__/components/StatsCards.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import StatsCards from '../dashboard/components/StatsCards';

describe('StatsCards', () => {
  it('should render loading skeleton when loading', () => {
    render(<StatsCards stats={null} loading={true} />);
    expect(screen.getAllByTestId('skeleton')).toHaveLength(4);
  });

  it('should render stats when data is provided', () => {
    const stats = {
      total_users: 10000,
      active_projects: 50,
      internship_positions: 200,
      pending_audits: 5
    };
    
    render(<StatsCards stats={stats} loading={false} />);
    expect(screen.getByText('10000')).toBeInTheDocument();
    expect(screen.getByText('50')).toBeInTheDocument();
  });

  it('should handle empty stats gracefully', () => {
    render(<StatsCards stats={null} loading={false} />);
    expect(screen.getByText('暂无数据')).toBeInTheDocument();
  });
});
```

#### 2. 属性测试 (Property-Based Tests)

使用 **fast-check** 库进行属性测试，验证通用属性在大量随机输入下的正确性。

**测试配置**:
- 每个属性测试运行 **100 次迭代**
- 使用随机数据生成器生成测试输入
- 每个测试必须引用设计文档中的属性编号

**示例**:

```typescript
// __tests__/properties/api-fallback.property.test.tsx
import { describe, it, expect, vi } from 'vitest';
import * as fc from 'fast-check';
import { PlatformApiService } from '../services/api';

describe('Property Tests: API Service', () => {
  /**
   * Feature: platform-frontend-implementation
   * Property 1: API 降级机制
   * 
   * For any API 端点调用失败时，系统应该自动降级到 Mock_Generator 提供的数据
   */
  it('should fallback to mock data when API fails', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string(), // 随机 endpoint
        fc.anything(), // 随机 mock 数据
        async (endpoint, mockData) => {
          const mockFn = vi.fn(() => mockData);
          const apiService = new PlatformApiService();
          
          // 模拟 API 失败
          global.fetch = vi.fn(() => Promise.reject(new Error('Network error')));
          
          const result = await apiService.fetchWithFallback(endpoint, mockFn);
          
          expect(result).toEqual(mockData);
          expect(mockFn).toHaveBeenCalled();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: platform-frontend-implementation
   * Property 3: Mock 数据随机性
   * 
   * For any Mock_Generator 函数，多次调用应该返回不同的数据值
   */
  it('should generate different mock data on multiple calls', () => {
    fc.assert(
      fc.property(
        fc.constant(getMockSystemHealth), // 测试 mock 生成函数
        (mockFn) => {
          const result1 = mockFn();
          const result2 = mockFn();
          
          // 至少有一个字段不同
          const isDifferent = 
            result1.cpu_usage !== result2.cpu_usage ||
            result1.memory_usage !== result2.memory_usage ||
            result1.online_users !== result2.online_users;
          
          expect(isDifferent).toBe(true);
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

```typescript
// __tests__/properties/role-based-access.property.test.tsx
import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { filterNavItemsByRole } from '../utils/auth';
import { UserRole } from '../types';

describe('Property Tests: Role-Based Access', () => {
  /**
   * Feature: platform-frontend-implementation
   * Property 18: 角色权限菜单过滤
   * 
   * For any 用户角色，导航菜单应该只显示该角色有权访问的功能模块入口
   */
  it('should only show authorized nav items for any role', () => {
    const allNavItems = [
      { title: '数据概览', href: '/dashboard', roles: [UserRole.SYSTEM_ADMIN, UserRole.OPERATIONS_MANAGER] },
      { title: '机构审核', href: '/audit', roles: [UserRole.SYSTEM_ADMIN, UserRole.AUDIT_MANAGER] },
      { title: '系统监控', href: '/monitor', roles: [UserRole.SYSTEM_ADMIN, UserRole.DEVOPS_MANAGER] }
    ];
    
    fc.assert(
      fc.property(
        fc.constantFrom(...Object.values(UserRole)), // 随机角色
        (role) => {
          const filteredItems = filterNavItemsByRole(allNavItems, role);
          
          // 所有返回的项目都应该包含该角色
          filteredItems.forEach(item => {
            expect(item.roles).toContain(role);
          });
          
          // 不应该返回不包含该角色的项目
          const unauthorizedItems = allNavItems.filter(
            item => !item.roles.includes(role)
          );
          unauthorizedItems.forEach(item => {
            expect(filteredItems).not.toContain(item);
          });
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

```typescript
// __tests__/properties/list-filtering.property.test.tsx
import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { filterAuditItems } from '../utils/filters';

describe('Property Tests: List Filtering', () => {
  /**
   * Feature: platform-frontend-implementation
   * Property 8: 列表筛选正确性
   * 
   * For any 列表筛选条件，筛选后的结果应该只包含符合该条件的记录
   */
  it('should only return items matching filter criteria', () => {
    fc.assert(
      fc.property(
        fc.array(fc.record({
          id: fc.string(),
          status: fc.constantFrom('pending', 'approved', 'rejected'),
          type: fc.constantFrom('enterprise', 'project'),
          apply_time: fc.date().map(d => d.toISOString())
        })), // 随机审核项目列表
        fc.constantFrom('pending', 'approved', 'rejected'), // 随机状态筛选
        (items, statusFilter) => {
          const filtered = filterAuditItems(items, { status: statusFilter });
          
          // 所有返回的项目都应该匹配筛选条件
          filtered.forEach(item => {
            expect(item.status).toBe(statusFilter);
          });
          
          // 返回的数量应该等于原列表中匹配的数量
          const expectedCount = items.filter(
            item => item.status === statusFilter
          ).length;
          expect(filtered.length).toBe(expectedCount);
        }
      ),
      { numRuns: 100 }
    );
  });
});
```

#### 3. 集成测试

测试多个组件和服务的协同工作。

**测试场景**:
- 完整的用户流程（如审核流程：查看列表 → 查看详情 → 执行审核 → 更新列表）
- API 调用与 UI 更新的集成
- 路由导航和权限控制

#### 4. E2E 测试 (可选)

使用 **Playwright** 进行端到端测试，模拟真实用户操作。

**测试场景**:
- 登录流程
- 关键业务流程（审核、配置推荐）
- 跨页面导航

### 测试覆盖率目标

- 单元测试覆盖率: ≥ 80%
- 属性测试: 覆盖所有设计文档中定义的属性
- 集成测试: 覆盖主要用户流程

### 测试运行

```json
// package.json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "test:property": "vitest --run --grep 'Property Tests'"
  }
}
```

### 持续集成

在 CI/CD 流程中自动运行测试：

1. 每次 PR 提交时运行单元测试和属性测试
2. 测试失败时阻止合并
3. 生成测试覆盖率报告
4. 定期运行 E2E 测试（每日构建）

### 测试最佳实践

1. **测试隔离**: 每个测试应该独立运行，不依赖其他测试的状态
2. **Mock 外部依赖**: 使用 vi.fn() 模拟 API 调用和外部服务
3. **清晰的测试命名**: 使用描述性的测试名称，说明测试的场景和预期结果
4. **避免过度测试**: 不要测试第三方库的功能，专注于自己的业务逻辑
5. **保持测试简单**: 每个测试只验证一个行为或属性
6. **使用测试数据生成器**: 使用 fast-check 或自定义生成器创建测试数据，提高测试覆盖面

