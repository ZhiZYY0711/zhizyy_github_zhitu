# Requirements Document

## Introduction

本文档定义了校企合作实习实训管理平台的平台端（Platform）前端应用的功能需求。平台端面向系统管理员和运营人员，提供基础数据维护、机构审核、全局资源调度和系统监控等核心功能。

该应用将采用 React 18 + TypeScript + Vite 技术栈，使用 shadcn/ui 组件库，遵循现有学生端的架构模式，支持 API 调用失败时降级到 mock 数据。

## Glossary

- **Platform_Frontend**: 平台端前端应用，面向系统管理员和运营人员
- **System_Admin**: 系统管理员，拥有最高权限，可管理所有功能模块
- **Audit_Manager**: 审核管理员，负责机构入驻和资源审核
- **Operations_Manager**: 运营管理员，负责推荐管理和数据配置
- **DevOps_Manager**: 运维管理员，负责系统监控和日志审计
- **API_Service**: 后端 API 服务
- **Mock_Generator**: Mock 数据生成器，用于 API 失败时的降级
- **Layout_Component**: 布局组件，包含侧边栏导航和顶部栏
- **Dashboard**: 仪表盘页面，展示系统概览和关键指标
- **Master_Data**: 基础主数据，包括行业标签、技能树、证书模板等
- **Tenant**: 租户，指高校或企业机构
- **Audit_Queue**: 审核队列，包含待审核的机构和资源
- **Resource_Scheduler**: 资源调度器，管理实训项目和推荐位
- **Monitor_Dashboard**: 监控仪表盘，展示系统健康状态和实时指标
- **Chart_Component**: 图表组件，使用 ECharts 实现数据可视化
- **Polling_Service**: 轮询服务，定期获取实时监控数据
- **Role_Based_Access**: 基于角色的访问控制

## Requirements

### Requirement 1: 应用架构与布局

**User Story:** 作为开发者，我需要建立平台端前端的基础架构，以便后续功能模块能够统一集成。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 采用与学生端相同的目录结构模式（layout、services、mock、pages）
2. THE Layout_Component SHALL 提供侧边栏导航，包含所有功能模块的入口
3. THE Layout_Component SHALL 在顶部栏显示当前登录用户信息和角色标识
4. THE Platform_Frontend SHALL 使用 React Router 6 实现路由管理
5. THE Platform_Frontend SHALL 使用 shadcn/ui 组件库保持与学生端的 UI 一致性
6. THE Platform_Frontend SHALL 支持响应式布局，适配桌面端使用（最小宽度 1280px）

### Requirement 2: API 服务层与 Mock 降级

**User Story:** 作为开发者，我需要实现 API 调用层和 mock 数据降级机制，以便在后端服务不可用时仍能进行前端开发和演示。

#### Acceptance Criteria

1. THE API_Service SHALL 封装所有平台端 API 调用，使用统一的 base URL 配置
2. WHEN API 调用失败，THE API_Service SHALL 自动降级到 Mock_Generator 提供的数据
3. THE API_Service SHALL 在控制台输出警告信息，说明正在使用 mock 数据
4. THE Mock_Generator SHALL 生成符合 API 文档规范的模拟数据
5. THE Mock_Generator SHALL 使用随机数据生成器，确保每次刷新数据具有合理的变化
6. THE API_Service SHALL 支持环境变量配置 API base URL（VITE_API_BASE_URL）

### Requirement 3: 基础主数据管理

**User Story:** 作为系统管理员，我需要管理平台的基础主数据，以便为高校和企业提供统一的标签体系和模板。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供行业标签管理界面，支持创建、编辑、删除标签
2. THE Platform_Frontend SHALL 提供技能树管理界面，支持树形结构的展示和编辑
3. THE Platform_Frontend SHALL 提供证书模板管理界面，支持上传模板背景图和配置元素布局
4. THE Platform_Frontend SHALL 提供合同模板管理界面，支持模板的版本管理
5. WHEN 用户创建或编辑主数据，THE Platform_Frontend SHALL 进行表单验证
6. WHEN 主数据保存成功，THE Platform_Frontend SHALL 显示成功提示并刷新列表

### Requirement 4: 多租户与机构审核

**User Story:** 作为审核管理员，我需要审核高校和企业的入驻申请，以便确保平台生态的质量。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供高校租户管理界面，支持开通新租户和配置租户参数
2. THE Platform_Frontend SHALL 提供企业入驻审核队列，展示待审核企业列表
3. WHEN 审核管理员查看企业申请，THE Platform_Frontend SHALL 显示企业名称、营业执照和申请时间
4. THE Platform_Frontend SHALL 提供审核操作按钮（通过/拒绝），支持填写审核意见
5. WHEN 审核操作完成，THE Platform_Frontend SHALL 更新审核队列并显示操作结果
6. THE Platform_Frontend SHALL 支持按审核状态（待审核/已通过/已拒绝）筛选机构列表

### Requirement 5: 实训项目审核与推荐管理

**User Story:** 作为运营管理员，我需要审核企业发布的实训项目并管理推荐位，以便为学生提供优质的学习资源。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供实训项目审核队列，展示待审核项目列表
2. WHEN 运营管理员查看项目详情，THE Platform_Frontend SHALL 显示项目名称、提供方、技术栈和描述
3. THE Platform_Frontend SHALL 提供项目审核操作，支持设置质量评级（S/A/B/C）和审核意见
4. THE Platform_Frontend SHALL 提供首页推荐位管理界面，支持配置推荐内容（项目/企业/课程）
5. THE Platform_Frontend SHALL 提供优质导师和课程置顶功能，支持拖拽排序
6. WHEN 推荐配置保存成功，THE Platform_Frontend SHALL 显示预览效果

### Requirement 6: 系统监控仪表盘

**User Story:** 作为运维管理员，我需要实时监控系统健康状态，以便及时发现和处理系统问题。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供监控仪表盘，展示系统关键指标（CPU、内存、在线用户数、错误率）
2. THE Chart_Component SHALL 使用 ECharts 渲染实时数据图表（折线图、柱状图、仪表盘）
3. THE Polling_Service SHALL 每 30 秒轮询一次监控数据接口
4. WHEN 监控数据更新，THE Chart_Component SHALL 平滑更新图表显示
5. THE Platform_Frontend SHALL 提供在线用户趋势图，展示过去 24 小时的用户活跃度
6. WHEN 系统指标超过阈值，THE Platform_Frontend SHALL 使用醒目颜色标识异常状态

### Requirement 7: 操作日志与安全审计

**User Story:** 作为运维管理员，我需要查询操作日志和安全事件，以便进行审计和问题追溯。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供操作日志查询界面，支持按用户、模块、时间范围筛选
2. THE Platform_Frontend SHALL 展示日志详情，包括操作人、操作类型、IP 地址、时间和结果
3. THE Platform_Frontend SHALL 提供安全预警日志查询，支持按风险等级筛选
4. THE Platform_Frontend SHALL 支持导出日志数据为 CSV 格式
5. WHEN 用户查询日志，THE Platform_Frontend SHALL 使用分页加载，每页显示 50 条记录
6. THE Platform_Frontend SHALL 高亮显示失败操作和高风险安全事件

### Requirement 8: 权限管理与角色控制

**User Story:** 作为系统管理员，我需要管理不同角色的权限，以便实现精细化的访问控制。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 根据用户角色显示或隐藏功能模块入口
2. THE Role_Based_Access SHALL 支持四种角色：System_Admin、Audit_Manager、Operations_Manager、DevOps_Manager
3. WHEN 用户角色为 Audit_Manager，THE Platform_Frontend SHALL 仅显示机构审核和项目审核模块
4. WHEN 用户角色为 Operations_Manager，THE Platform_Frontend SHALL 仅显示推荐管理和主数据管理模块
5. WHEN 用户角色为 DevOps_Manager，THE Platform_Frontend SHALL 仅显示系统监控和日志审计模块
6. WHEN 用户角色为 System_Admin，THE Platform_Frontend SHALL 显示所有功能模块
7. WHEN 用户尝试访问无权限的页面，THE Platform_Frontend SHALL 重定向到 403 错误页面

### Requirement 9: 数据可视化与报表

**User Story:** 作为运营管理员，我需要查看平台运营数据的可视化报表，以便分析平台使用情况和优化运营策略。

#### Acceptance Criteria

1. THE Platform_Frontend SHALL 提供数据概览仪表盘，展示关键运营指标（注册用户数、活跃项目数、实习岗位数）
2. THE Chart_Component SHALL 使用 ECharts 渲染多种图表类型（折线图、柱状图、饼图、雷达图）
3. THE Platform_Frontend SHALL 提供时间范围选择器，支持查看不同时间段的数据
4. THE Platform_Frontend SHALL 提供数据对比功能，支持同比和环比分析
5. WHEN 用户切换时间范围，THE Chart_Component SHALL 重新加载并渲染数据
6. THE Platform_Frontend SHALL 支持图表导出为图片格式（PNG）

### Requirement 10: 用户体验与交互优化

**User Story:** 作为平台端用户，我需要流畅的交互体验和友好的界面提示，以便高效完成工作任务。

#### Acceptance Criteria

1. WHEN 数据加载中，THE Platform_Frontend SHALL 显示骨架屏或加载动画
2. WHEN 用户操作成功，THE Platform_Frontend SHALL 显示 Toast 提示消息（3 秒后自动消失）
3. WHEN 用户操作失败，THE Platform_Frontend SHALL 显示错误提示并说明失败原因
4. THE Platform_Frontend SHALL 在表单提交时禁用提交按钮，防止重复提交
5. THE Platform_Frontend SHALL 在数据表格中支持排序、筛选和搜索功能
6. THE Platform_Frontend SHALL 使用 Dialog 组件展示详情和确认操作，避免页面跳转
7. WHEN 用户执行危险操作（如删除），THE Platform_Frontend SHALL 显示二次确认对话框

