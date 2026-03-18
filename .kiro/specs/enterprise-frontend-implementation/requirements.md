# Requirements Document

## Introduction

本文档定义企业端前端实现的功能需求。企业端面向HR人员、企业导师和企业管理员，提供人才招聘选拔、实训项目管理、实习生管理和导师工作站等核心功能。系统基于 React 18 + TypeScript + Vite + shadcn/ui + Tailwind CSS 技术栈，使用 recharts 图表库，支持 API 失败时自动降级到 mock 数据。

## Glossary

- **Enterprise_Frontend**: 企业端前端应用模块
- **API_Service**: 后端 API 服务接口
- **Mock_Generator**: Mock 数据生成器
- **Layout_Component**: 布局组件（包含侧边栏导航和用户信息）
- **Role_Filter**: 角色权限过滤器
- **Dashboard**: 企业人才工作台仪表盘
- **Job_Management**: 职位管理模块
- **Resume_Screening**: 简历筛选模块
- **Talent_Pool**: 人才蓄水池模块
- **Training_Project**: 实训项目管理模块
- **Internship_Management**: 实习生管理模块
- **Mentor_Workstation**: 导师工作站模块
- **Analytics_Dashboard**: 企业效益分析仪表盘
- **Type_Definitions**: TypeScript 类型定义文件
- **Router**: React Router 路由配置

## Requirements

### Requirement 1: 类型定义系统

**User Story:** 作为开发者，我需要完整的 TypeScript 类型定义，以便确保类型安全和代码提示。

#### Acceptance Criteria

1. THE Type_Definitions SHALL 定义企业员工角色类型（hr, mentor, admin）
2. THE Type_Definitions SHALL 定义用户信息接口（id, name, role, enterprise_id, avatar）
3. THE Type_Definitions SHALL 定义职位信息接口（id, title, type, description, requirements, salary_range, location, status）
4. THE Type_Definitions SHALL 定义简历申请接口（application_id, student_name, school, resume_url, match_score, apply_time, status）
5. THE Type_Definitions SHALL 定义实训项目接口（id, name, description, difficulty, tech_stack, max_teams, resources_url, status）
6. THE Type_Definitions SHALL 定义实习生信息接口（id, name, school, position, start_date, mentor_id, status）
7. THE Type_Definitions SHALL 定义周报接口（id, intern_id, week, content, score, mentor_comment, submit_time）
8. THE Type_Definitions SHALL 定义代码评审接口（id, project_id, student_name, file, line, comment, status）
9. THE Type_Definitions SHALL 定义人才蓄水池接口（id, student_id, student_name, tags, collect_time）
10. THE Type_Definitions SHALL 定义统计数据接口（active_jobs, pending_applications, active_interns, pending_reviews）

### Requirement 2: API 服务层

**User Story:** 作为开发者，我需要统一的 API 调用服务，以便与后端交互并在失败时自动降级到 mock 数据。

#### Acceptance Criteria

1. THE API_Service SHALL 实现通用的 fetchWithFallback 函数支持自动降级
2. WHEN API 请求失败，THE API_Service SHALL 自动调用对应的 Mock_Generator 函数
3. WHEN API 返回非 JSON 响应，THE API_Service SHALL 降级到 mock 数据
4. THE API_Service SHALL 提供 fetchDashboardStats 函数获取工作台统计数据
5. THE API_Service SHALL 提供 fetchJobs 函数获取职位列表
6. THE API_Service SHALL 提供 fetchApplications 函数获取简历申请列表
7. THE API_Service SHALL 提供 fetchTalentPool 函数获取人才蓄水池数据
8. THE API_Service SHALL 提供 fetchTrainingProjects 函数获取实训项目列表
9. THE API_Service SHALL 提供 fetchInterns 函数获取实习生列表
10. THE API_Service SHALL 提供 fetchWeeklyReports 函数获取周报列表
11. THE API_Service SHALL 提供 fetchCodeReviews 函数获取代码评审列表
12. THE API_Service SHALL 提供 fetchMentorDashboard 函数获取导师仪表盘数据
13. THE API_Service SHALL 提供 fetchAnalytics 函数获取企业效益分析数据
14. WHEN 调用 mock 数据时，THE API_Service SHALL 模拟 500ms 网络延迟

### Requirement 3: Mock 数据生成器

**User Story:** 作为开发者，我需要完整的 mock 数据生成函数，以便在 API 不可用时进行开发和测试。

#### Acceptance Criteria

1. THE Mock_Generator SHALL 提供 getMockDashboardStats 函数生成工作台统计数据
2. THE Mock_Generator SHALL 提供 getMockJobs 函数生成职位列表（至少 5 条）
3. THE Mock_Generator SHALL 提供 getMockApplications 函数生成简历申请列表（至少 10 条）
4. THE Mock_Generator SHALL 提供 getMockTalentPool 函数生成人才蓄水池数据（至少 8 条）
5. THE Mock_Generator SHALL 提供 getMockTrainingProjects 函数生成实训项目列表（至少 4 条）
6. THE Mock_Generator SHALL 提供 getMockInterns 函数生成实习生列表（至少 6 条）
7. THE Mock_Generator SHALL 提供 getMockWeeklyReports 函数生成周报列表（至少 8 条）
8. THE Mock_Generator SHALL 提供 getMockCodeReviews 函数生成代码评审列表（至少 5 条）
9. THE Mock_Generator SHALL 提供 getMockMentorDashboard 函数生成导师仪表盘数据
10. THE Mock_Generator SHALL 提供 getMockAnalytics 函数生成企业效益分析数据（包含图表数据）
11. THE Mock_Generator SHALL 生成符合真实业务场景的数据（合理的日期、状态、分数等）

### Requirement 4: 布局组件

**User Story:** 作为企业用户，我需要统一的页面布局和导航，以便快速访问各功能模块。

#### Acceptance Criteria

1. THE Layout_Component SHALL 显示企业端品牌标识（"智途·企业"）
2. THE Layout_Component SHALL 在侧边栏显示导航菜单
3. THE Layout_Component SHALL 根据用户角色过滤导航菜单项
4. THE Layout_Component SHALL 在底部显示当前用户信息（头像、姓名、角色标签）
5. THE Layout_Component SHALL 支持响应式布局（移动端收起侧边栏为图标模式）
6. WHEN 屏幕宽度小于 768px，THE Layout_Component SHALL 仅显示导航图标
7. WHEN 屏幕宽度大于等于 1024px，THE Layout_Component SHALL 显示完整导航文字
8. THE Layout_Component SHALL 高亮当前激活的导航项
9. THE Layout_Component SHALL 使用 Outlet 组件渲染子路由内容
10. THE Layout_Component SHALL 为不同角色显示不同颜色的角色标签（hr: 蓝色, mentor: 绿色, admin: 红色）

### Requirement 5: 角色权限系统

**User Story:** 作为系统管理员，我需要基于角色的权限控制，以便不同角色只能访问授权的功能。

#### Acceptance Criteria

1. THE Role_Filter SHALL 定义 hr 角色可访问的导航项（工作台、招聘管理、实习管理、效益分析）
2. THE Role_Filter SHALL 定义 mentor 角色可访问的导航项（工作台、导师工作站、实训项目）
3. THE Role_Filter SHALL 定义 admin 角色可访问所有导航项
4. THE Role_Filter SHALL 提供 filterNavItemsByRole 函数根据角色过滤导航
5. THE Role_Filter SHALL 提供 getMockCurrentUser 函数获取当前用户信息（开发阶段）
6. WHEN 用户角色为 hr，THE Role_Filter SHALL 隐藏导师工作站相关菜单
7. WHEN 用户角色为 mentor，THE Role_Filter SHALL 隐藏招聘管理相关菜单

### Requirement 6: 企业人才工作台

**User Story:** 作为企业用户，我需要一个统一的工作台仪表盘，以便快速了解招聘、实习和项目的整体情况。

#### Acceptance Criteria

1. THE Dashboard SHALL 显示关键统计数据卡片（在招职位数、待处理简历数、在岗实习生数、待评审任务数）
2. THE Dashboard SHALL 显示待办事项列表（待面试、待批阅周报、待代码评审）
3. THE Dashboard SHALL 显示最近活动时间线（简历投递、面试安排、周报提交）
4. THE Dashboard SHALL 显示快捷操作入口（发布职位、查看简历、批阅周报）
5. WHEN 用户角色为 mentor，THE Dashboard SHALL 突出显示导师相关待办（代码评审、周报批阅）
6. WHEN 用户角色为 hr，THE Dashboard SHALL 突出显示招聘相关待办（简历筛选、面试安排）
7. THE Dashboard SHALL 使用 recharts 显示趋势图表（简历投递趋势、实习生入职趋势）

### Requirement 7: 招聘与选拔模块

**User Story:** 作为 HR 人员，我需要管理职位和筛选简历，以便高效完成人才招聘。

#### Acceptance Criteria

1. THE Job_Management SHALL 显示职位列表（标题、类型、状态、发布时间）
2. THE Job_Management SHALL 支持筛选职位（按状态：active/closed）
3. THE Job_Management SHALL 提供发布新职位功能（表单包含标题、描述、要求、薪资、地点）
4. THE Job_Management SHALL 提供编辑和关闭职位功能
5. THE Resume_Screening SHALL 显示简历申请列表（学生姓名、学校、匹配度、申请时间、状态）
6. THE Resume_Screening SHALL 支持按职位和状态筛选简历
7. THE Resume_Screening SHALL 显示智能匹配度评分（0-1 范围，使用进度条可视化）
8. THE Resume_Screening SHALL 提供查看简历详情功能（打开简历 URL）
9. THE Resume_Screening SHALL 提供发起面试邀约功能（选择时间、类型、会议链接）
10. THE Resume_Screening SHALL 提供加入人才蓄水池功能（添加标签）
11. THE Resume_Screening SHALL 支持批量操作（批量拒绝、批量邀约）

### Requirement 8: 人才蓄水池模块

**User Story:** 作为 HR 人员，我需要管理人才蓄水池，以便储备和跟踪优质候选人。

#### Acceptance Criteria

1. THE Talent_Pool SHALL 显示收藏的学生列表（姓名、学校、标签、收藏时间）
2. THE Talent_Pool SHALL 支持按标签筛选学生
3. THE Talent_Pool SHALL 提供添加和编辑标签功能
4. THE Talent_Pool SHALL 提供查看学生详细画像功能
5. THE Talent_Pool SHALL 提供移除学生功能
6. THE Talent_Pool SHALL 显示学生的技能雷达图
7. THE Talent_Pool SHALL 提供向学生发送职位邀约功能

### Requirement 9: 实训项目管理模块

**User Story:** 作为企业管理员，我需要发布和管理实训项目，以便通过实训选拔人才。

#### Acceptance Criteria

1. THE Training_Project SHALL 显示企业发布的项目列表（名称、难度、技术栈、参与团队数、状态）
2. THE Training_Project SHALL 提供发布新项目功能（表单包含名称、描述、难度、技术栈、最大团队数、资源链接）
3. THE Training_Project SHALL 显示项目的参与团队列表
4. THE Training_Project SHALL 显示每个团队的进度和成员信息
5. THE Training_Project SHALL 提供分配企业导师功能（选择导师和负责团队）
6. THE Training_Project SHALL 显示项目的技术栈标签（使用 Badge 组件）
7. THE Training_Project SHALL 显示项目难度（1-5 星级）

### Requirement 10: 实习生管理模块

**User Story:** 作为 HR 人员，我需要管理实习生的全流程，以便完成入职、考勤和离职等操作。

#### Acceptance Criteria

1. THE Internship_Management SHALL 显示实习生列表（姓名、学校、岗位、导师、入职时间、状态）
2. THE Internship_Management SHALL 支持按状态筛选实习生（在岗、已离职）
3. THE Internship_Management SHALL 提供发送 Offer 功能（选择候选人、填写薪资、开始日期、合同模板）
4. THE Internship_Management SHALL 显示签约状态（待签约、已签约、已拒绝）
5. THE Internship_Management SHALL 提供考勤审批功能（查看异常记录、批准或拒绝）
6. THE Internship_Management SHALL 显示实习生的周报列表
7. THE Internship_Management SHALL 提供发放实习证明功能（填写评价意见）
8. THE Internship_Management SHALL 显示实习生的基本信息和联系方式

### Requirement 11: 导师工作站模块

**User Story:** 作为企业导师，我需要专属的工作站，以便指导学生和完成评审任务。

#### Acceptance Criteria

1. THE Mentor_Workstation SHALL 显示导师仪表盘（待代码评审数、待周报批阅数、即将到来的面试数）
2. THE Mentor_Workstation SHALL 显示指导的学生列表（实训学生和实习生）
3. THE Mentor_Workstation SHALL 显示待评审的代码列表（项目名、学生姓名、文件、状态）
4. THE Mentor_Workstation SHALL 提供代码评审功能（查看代码、添加行级评论、提交评审）
5. THE Mentor_Workstation SHALL 显示待批阅的周报列表（实习生姓名、周次、提交时间）
6. THE Mentor_Workstation SHALL 提供周报批阅功能（查看内容、打分、填写评语）
7. THE Mentor_Workstation SHALL 提供实习生最终评价功能（技能评分、团队协作评分、潜力评分、转正推荐）
8. THE Mentor_Workstation SHALL 显示历史评审记录
9. THE Mentor_Workstation SHALL 支持筛选待办任务（按状态、按学生）

### Requirement 12: 企业效益分析模块

**User Story:** 作为企业管理员，我需要查看企业效益分析，以便评估人才合作的价值。

#### Acceptance Criteria

1. THE Analytics_Dashboard SHALL 显示人才转化率统计（实习转正率、招聘成本节省）
2. THE Analytics_Dashboard SHALL 使用 recharts 显示转化率趋势图（折线图）
3. THE Analytics_Dashboard SHALL 显示合作贡献度评估（实习生创造的价值）
4. THE Analytics_Dashboard SHALL 使用 recharts 显示贡献度分布图（饼图或柱状图）
5. THE Analytics_Dashboard SHALL 提供证书验证功能（扫码或输入证书编号）
6. THE Analytics_Dashboard SHALL 显示验证结果（证书真伪、持有人信息、颁发时间）
7. THE Analytics_Dashboard SHALL 显示招聘漏斗图（简历投递 → 面试 → Offer → 入职）
8. THE Analytics_Dashboard SHALL 支持选择时间范围（本月、本季度、本年度）

### Requirement 13: 路由配置

**User Story:** 作为开发者，我需要配置企业端路由，以便用户可以访问各功能页面。

#### Acceptance Criteria

1. THE Router SHALL 在 App.tsx 中添加 /enterprise 路由组
2. THE Router SHALL 配置 /enterprise/dashboard 路由指向工作台页面
3. THE Router SHALL 配置 /enterprise/recruitment 路由指向招聘管理页面
4. THE Router SHALL 配置 /enterprise/talent-pool 路由指向人才蓄水池页面
5. THE Router SHALL 配置 /enterprise/training 路由指向实训项目页面
6. THE Router SHALL 配置 /enterprise/internship 路由指向实习管理页面
7. THE Router SHALL 配置 /enterprise/mentor 路由指向导师工作站页面
8. THE Router SHALL 配置 /enterprise/analytics 路由指向效益分析页面
9. WHEN 访问 /enterprise，THE Router SHALL 重定向到 /enterprise/dashboard
10. THE Router SHALL 使用 Layout_Component 作为企业端路由的父组件

### Requirement 14: UI 组件复用

**User Story:** 作为开发者，我需要复用 shadcn/ui 组件库，以便保持 UI 一致性和开发效率。

#### Acceptance Criteria

1. THE Enterprise_Frontend SHALL 使用 Card 组件展示统计数据和列表
2. THE Enterprise_Frontend SHALL 使用 Table 组件展示职位、简历、实习生等列表数据
3. THE Enterprise_Frontend SHALL 使用 Badge 组件展示状态、标签和角色
4. THE Enterprise_Frontend SHALL 使用 Button 组件提供操作入口
5. THE Enterprise_Frontend SHALL 使用 Dialog 组件实现弹窗表单（发布职位、发送 Offer）
6. THE Enterprise_Frontend SHALL 使用 Select 组件实现筛选和下拉选择
7. THE Enterprise_Frontend SHALL 使用 Tabs 组件实现多标签页切换
8. THE Enterprise_Frontend SHALL 使用 Progress 组件展示匹配度和进度
9. THE Enterprise_Frontend SHALL 使用 Avatar 组件展示用户头像
10. THE Enterprise_Frontend SHALL 使用 Textarea 组件实现多行文本输入（评语、评论）

### Requirement 15: 响应式设计

**User Story:** 作为企业用户，我需要在不同设备上使用系统，以便随时随地处理工作。

#### Acceptance Criteria

1. WHEN 屏幕宽度小于 768px，THE Enterprise_Frontend SHALL 使用移动端布局
2. WHEN 屏幕宽度小于 768px，THE Enterprise_Frontend SHALL 将表格转换为卡片列表
3. WHEN 屏幕宽度小于 768px，THE Enterprise_Frontend SHALL 收起侧边栏为汉堡菜单
4. WHEN 屏幕宽度大于等于 768px，THE Enterprise_Frontend SHALL 显示完整表格
5. WHEN 屏幕宽度大于等于 1024px，THE Enterprise_Frontend SHALL 显示完整侧边栏导航
6. THE Enterprise_Frontend SHALL 使用 Tailwind CSS 响应式类（sm:, md:, lg:）
7. THE Enterprise_Frontend SHALL 确保所有交互元素在移动端可点击（最小 44x44px）

### Requirement 16: 数据加载状态

**User Story:** 作为企业用户，我需要清晰的加载状态提示，以便了解数据是否正在加载。

#### Acceptance Criteria

1. WHEN 数据正在加载，THE Enterprise_Frontend SHALL 显示加载指示器（Skeleton 或 Spinner）
2. WHEN 数据加载失败，THE Enterprise_Frontend SHALL 显示错误提示和重试按钮
3. WHEN 数据为空，THE Enterprise_Frontend SHALL 显示空状态提示和引导操作
4. THE Enterprise_Frontend SHALL 在 API 调用期间禁用提交按钮防止重复提交
5. WHEN 使用 mock 数据，THE Enterprise_Frontend SHALL 在控制台输出提示信息

### Requirement 17: 文件结构组织

**User Story:** 作为开发者，我需要清晰的文件结构，以便快速定位和维护代码。

#### Acceptance Criteria

1. THE Enterprise_Frontend SHALL 在 frontend/src/enterprise/ 目录下组织所有文件
2. THE Enterprise_Frontend SHALL 创建 types.ts 文件定义所有类型
3. THE Enterprise_Frontend SHALL 创建 services/api.ts 文件实现 API 调用
4. THE Enterprise_Frontend SHALL 创建 mock/generator.ts 文件实现 mock 数据生成
5. THE Enterprise_Frontend SHALL 创建 layout.tsx 文件实现布局组件
6. THE Enterprise_Frontend SHALL 创建 utils/auth.ts 文件实现权限工具函数
7. THE Enterprise_Frontend SHALL 为每个功能模块创建独立目录（dashboard/, recruitment/, training/, internship/, mentor/, analytics/）
8. THE Enterprise_Frontend SHALL 在每个模块目录下创建 page.tsx 作为页面入口
9. THE Enterprise_Frontend SHALL 在需要时创建 components/ 子目录存放模块专用组件
10. THE Enterprise_Frontend SHALL 参考 platform 和 student 模块的文件结构保持一致性
