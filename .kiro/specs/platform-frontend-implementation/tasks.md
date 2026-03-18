# Implementation Tasks

## Tasks

- [x] 1. 基础架构与类型定义
  - [x] 1.1 创建平台端类型定义文件 `frontend/src/platform/types.ts`，包含 UserRole、User、Tenant、AuditItem、SystemHealth、OperationLog、SecurityLog 等核心接口
  - [x] 1.2 创建权限工具函数 `frontend/src/platform/utils/auth.ts`，实现 filterNavItemsByRole、hasRole、hasPermission 等函数
  - [x] 1.3 创建轮询工具函数 `frontend/src/platform/utils/polling.ts`，实现 usePolling hook

- [x] 2. Mock 数据生成器
  - [x] 2.1 创建 `frontend/src/platform/mock/generator.ts`，实现所有模块的 mock 数据生成函数（getMockDashboardStats、getMockSystemHealth、getMockTenantList、getMockEnterpriseAuditList、getMockProjectAuditList、getMockOperationLogs、getMockSecurityLogs、getMockRecommendations、getMockTags、getMockSkillTree、getMockCertificateTemplates）

- [x] 3. API 服务层
  - [x] 3.1 创建 `frontend/src/platform/services/api.ts`，实现 fetchWithFallback 通用方法和所有业务 API 调用函数，支持环境变量配置

- [x] 4. 平台端布局组件
  - [x] 4.1 创建 `frontend/src/platform/layout.tsx`，实现侧边栏导航（含角色权限过滤）、顶部用户信息栏、响应式布局

- [x] 5. 数据概览仪表盘
  - [x] 5.1 创建 `frontend/src/platform/dashboard/components/StatsCards.tsx`，展示总用户数、活跃项目数、实习岗位数、待审核数
  - [x] 5.2 创建 `frontend/src/platform/dashboard/components/TrendChart.tsx`，使用 ECharts 渲染用户增长趋势折线图
  - [x] 5.3 创建 `frontend/src/platform/dashboard/components/QuickActions.tsx`，提供快捷操作入口
  - [x] 5.4 创建 `frontend/src/platform/dashboard/page.tsx`，整合仪表盘组件

- [x] 6. 基础主数据管理
  - [x] 6.1 创建 `frontend/src/platform/master-data/components/TagManager.tsx`，实现行业/技能标签的增删改查
  - [x] 6.2 创建 `frontend/src/platform/master-data/components/SkillTreeEditor.tsx`，实现技能树树形展示与编辑
  - [x] 6.3 创建 `frontend/src/platform/master-data/components/TemplateManager.tsx`，实现证书模板和合同模板管理
  - [x] 6.4 创建 `frontend/src/platform/master-data/page.tsx`，整合主数据管理页面（使用 Tabs 切换）

- [x] 7. 多租户与机构审核
  - [x] 7.1 创建 `frontend/src/platform/audit/components/TenantList.tsx`，实现高校租户列表与开通新租户功能
  - [x] 7.2 创建 `frontend/src/platform/audit/components/EnterpriseAudit.tsx`，实现企业入驻审核队列（展示营业执照、通过/拒绝操作）
  - [x] 7.3 创建 `frontend/src/platform/audit/components/ProjectAudit.tsx`，实现实训项目审核（含质量评级 S/A/B/C）
  - [x] 7.4 创建 `frontend/src/platform/audit/page.tsx`，整合审核管理页面（使用 Tabs 切换）

- [x] 8. 全局资源调度
  - [x] 8.1 创建 `frontend/src/platform/resources/components/RecommendationManager.tsx`，实现首页推荐位配置和导师/课程置顶管理
  - [x] 8.2 创建 `frontend/src/platform/resources/page.tsx`，整合资源调度页面

- [x] 9. 系统监控仪表盘
  - [ ] 9.1 创建 `frontend/src/platform/monitor/components/HealthDashboard.tsx`，展示 CPU、内存、磁盘、在线用户数等指标（超阈值红色高亮）
  - [ ] 9.2 创建 `frontend/src/platform/monitor/components/MetricsChart.tsx`，使用 ECharts 渲染实时指标折线图（含 30 秒轮询）
  - [ ] 9.3 创建 `frontend/src/platform/monitor/components/AlertPanel.tsx`，展示服务健康状态列表
  - [ ] 9.4 创建 `frontend/src/platform/monitor/page.tsx`，整合监控页面

- [x] 10. 操作日志与安全审计
  - [x] 10.1 创建 `frontend/src/platform/logs/components/OperationLogTable.tsx`，实现操作日志表格（筛选、分页、CSV 导出、失败操作高亮）
  - [x] 10.2 创建 `frontend/src/platform/logs/components/SecurityLogTable.tsx`，实现安全预警日志表格（按风险等级筛选、高风险高亮）
  - [x] 10.3 创建 `frontend/src/platform/logs/page.tsx`，整合日志审计页面

- [x] 11. 更新应用路由
  - [x] 11.1 更新 `frontend/src/App.tsx`，添加平台端路由配置（/platform/*），集成 PlatformLayout 和所有页面组件
