# Implementation Tasks

## Tasks

- [x] 1. 基础架构与类型定义
  - [x] 1.1 创建 `frontend/src/enterprise/types.ts`，定义 EnterpriseRole、EnterpriseUser、Job、Application、TalentPoolItem、TrainingProject、ProjectTeam、Intern、WeeklyReport、CodeReview、DashboardStats、MentorDashboard、AnalyticsData 等核心接口
  - [x] 1.2 创建 `frontend/src/enterprise/utils/auth.ts`，实现 filterNavItemsByRole、hasRole、getMockCurrentUser 函数
  - [x] 1.3 创建 `frontend/src/enterprise/mock/generator.ts`，实现所有 mock 数据生成函数
  - [x] 1.4 创建 `frontend/src/enterprise/services/api.ts`，实现 fetchWithFallback 和所有业务 API 函数

- [x] 2. 企业端布局组件
  - [x] 2.1 创建 `frontend/src/enterprise/layout.tsx`，实现侧边栏导航（角色权限过滤）、响应式布局、用户信息展示

- [x] 3. 企业人才工作台
  - [x] 3.1 创建 `frontend/src/enterprise/dashboard/page.tsx`，展示统计卡片、待办事项、活动时间线、快捷操作和趋势图表（recharts）

- [x] 4. 招聘与选拔模块
  - [x] 4.1 创建 `frontend/src/enterprise/recruitment/components/JobList.tsx`，实现职位列表（筛选、发布新职位 Dialog、关闭职位）
  - [x] 4.2 创建 `frontend/src/enterprise/recruitment/components/ApplicationList.tsx`，实现简历列表（匹配度进度条、面试邀约 Dialog、加入人才蓄水池）
  - [x] 4.3 创建 `frontend/src/enterprise/recruitment/page.tsx`，用 Tabs 整合职位管理和简历筛选

- [x] 5. 人才蓄水池模块
  - [x] 5.1 创建 `frontend/src/enterprise/talent-pool/page.tsx`，实现学生卡片列表（标签筛选、查看详情、移除）

- [x] 6. 实训项目管理模块
  - [x] 6.1 创建 `frontend/src/enterprise/training/page.tsx`，实现项目列表（难度星级、技术栈 Badge、发布新项目 Dialog、团队列表展开）

- [x] 7. 实习生管理模块
  - [x] 7.1 创建 `frontend/src/enterprise/internship/components/InternList.tsx`，实现实习生列表（状态筛选、发送 Offer、考勤审批、发放证明）
  - [x] 7.2 创建 `frontend/src/enterprise/internship/components/WeeklyReportList.tsx`，实现周报列表（查看内容、打分、评语）
  - [x] 7.3 创建 `frontend/src/enterprise/internship/page.tsx`，用 Tabs 整合实习生管理和周报管理

- [x] 8. 导师工作站模块
  - [x] 8.1 创建 `frontend/src/enterprise/mentor/components/CodeReviewList.tsx`，实现代码评审列表（查看代码片段、添加行级评论、提交评审）
  - [x] 8.2 创建 `frontend/src/enterprise/mentor/components/MentorStats.tsx`，实现导师仪表盘统计卡片
  - [x] 8.3 创建 `frontend/src/enterprise/mentor/page.tsx`，整合导师工作站（统计卡片、指导学生列表、代码评审、周报批阅 Tabs）

- [x] 9. 企业效益分析模块
  - [x] 9.1 创建 `frontend/src/enterprise/analytics/page.tsx`，实现转化率统计、趋势折线图、招聘漏斗图（recharts）、证书验证功能

- [x] 10. 更新应用路由
  - [x] 10.1 更新 `frontend/src/App.tsx`，添加 /enterprise/* 路由配置，集成 EnterpriseLayout 和所有页面组件
