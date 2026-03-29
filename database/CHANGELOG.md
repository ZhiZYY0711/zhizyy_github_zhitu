# 数据库变更日志

本文档记录智途平台数据库的所有版本变更历史。

---

## [v1.6.0] - 2026-03-29

### 新增
- 为 `auth_center.sys_user` 表添加 `real_name` 字段
- 支持存储所有用户类型（企业员工、教师、平台管理员等）的真实姓名
- 添加 `idx_user_realname` 索引优化查询性能

### 修复
- 修复跨服务查询时无法获取企业导师和教师姓名的问题
- 修复 `InternshipManageService.getInternList()` SQL 错误
- 简化 JOIN 逻辑，直接从 `sys_user` 获取导师和教师姓名

### 数据迁移
- 将现有用户的 `username` 复制到 `real_name` 作为默认值
- 所有现有数据保持向后兼容

### 影响范围
- `auth_center.sys_user` 表结构变更
- `InternshipManageService` 查询逻辑优化
- 不影响现有功能，向后兼容

### 文件
- `v1.6.0-add-user-realname/upgrade.sql` - 升级脚本
- `v1.6.0-add-user-realname/rollback.sql` - 回滚脚本
- `v1.6.0-add-user-realname/verify.sql` - 验证脚本
- `v1.6.0-add-user-realname/deploy.sh` - Linux部署脚本
- `v1.6.0-add-user-realname/deploy.bat` - Windows部署脚本
- `v1.6.0-add-user-realname/README.md` - 详细说明

---

## [v1.5.0] - 2026-03-28

### 新增
- 新增代码评审功能表 `training_svc.code_review`
- 支持导师对学生项目代码进行在线评审
- 支持代码行级评审和评审意见分级
- 生成500条测试数据

### 功能特性
- 评审严重程度分级：suggestion（建议）、warning（警告）、error（错误）
- 评审状态跟踪：pending（待处理）、resolved（已解决）、closed（已关闭）
- 支持记录文件路径、行号、代码片段
- 支持评审解决人和解决时间记录

### 索引优化
- 项目ID、学生ID、导师ID索引
- 状态索引、创建时间索引

### 文件
- `v1.5.0-code-review/upgrade.sql` - 升级脚本
- `v1.5.0-code-review/rollback.sql` - 回滚脚本
- `v1.5.0-code-review/verify.sql` - 验证脚本
- `v1.5.0-code-review/README.md` - 详细说明

---

## [v1.3.0] - 2025-03-22

### 新增
- 为student_id=1的学生添加2条评价记录
- 为student_id=1的学生添加4条徽章证书记录

### 修复
- 修复职业成长模块API 500错误
- 补充缺失的测试数据

### 配套修复
- 后端：修复StudentPortalService的类型转换问题
- 后端：修复BigDecimal弃用API使用
- 前端：优化能力雷达图渲染
- 前端：增强表单字段可访问性

### 文件
- `v1.3.0-growth-fix/upgrade.sql` - 升级脚本
- `v1.3.0-growth-fix/rollback.sql` - 回滚脚本
- `v1.3.0-growth-fix/verify.sql` - 验证脚本
- `v1.3.0-growth-fix/README.md` - 详细说明

---

## [v1.2.0] - 2025-03-22

### 新增
- 为enterprise_svc.talent_pool表添加软删除功能
- 新增字段：is_deleted (BOOLEAN)
- 新增索引：idx_talent_pool_deleted

### 变更
- 更新现有talent_pool记录，设置is_deleted=FALSE

### 文件
- `v1.2.0-talent-pool/upgrade.sql` - 升级脚本
- `v1.2.0-talent-pool/README.md` - 详细说明

---

## [v1.1.0] - 2025-03-22

### 新增
共新增22个数据库表，完善API端点数据模型：

#### student_svc (3个表)
- student_task - 学生任务管理
- student_capability - 学生能力雷达图
- student_recommendation - 个性化推荐

#### training_svc (2个表)
- project_task - Scrum看板任务
- project_enrollment - 项目注册记录

#### enterprise_svc (3个表)
- enterprise_activity - 企业活动流
- enterprise_todo - 企业待办事项
- interview_schedule - 面试安排

#### college_svc (4个表)
- enterprise_relationship - 校企合作关系
- enterprise_visit - 企业拜访记录
- enterprise_audit - 企业资质审核
- internship_inspection - 实习巡查记录

#### platform_service (10个表)
- sys_tag - 系统标签
- skill_tree - 技能树
- certificate_template - 证书模板
- contract_template - 合同模板
- recommendation_banner - 推荐横幅
- recommendation_top_list - 推荐榜单
- operation_log - 操作审计日志
- security_log - 安全事件日志
- service_health - 微服务健康监控
- online_user_trend - 在线用户趋势

### 文件
- `v1.1.0-missing-api/upgrade.sql` - 升级脚本
- `v1.1.0-missing-api/rollback.sql` - 回滚脚本
- `v1.1.0-missing-api/README.md` - 详细说明

---

## [v1.0.0] - 2025-03-22

### 新增
数据库初始版本，包含完整的schema定义和测试数据。

#### Schema (8个)
- auth_center - 认证中心
- platform_service - 平台服务
- student_svc - 学生服务
- college_svc - 高校服务
- enterprise_svc - 企业服务
- internship_svc - 实习服务
- training_svc - 实训服务
- growth_svc - 成长服务

#### 核心表 (50+个)
- 租户管理：sys_tenant
- 用户管理：sys_user, sys_refresh_token
- 学生档案：student_info
- 高校信息：college_info, organization
- 企业信息：enterprise_info, talent_pool
- 实习管理：internship_job, internship_application, internship_record
- 实训管理：training_project
- 成长评价：evaluation_record, growth_badge
- 更多...

#### 测试数据
- 6个租户（1个平台 + 2所高校 + 3家企业）
- 19个用户（管理员、HR、导师、学生等）
- 完整的业务测试数据

### 文件
- `v1.0.0-init/01_auth_center.sql` - 认证中心schema
- `v1.0.0-init/02_platform_service.sql` - 平台服务schema
- `v1.0.0-init/03_student_svc.sql` - 学生服务schema
- `v1.0.0-init/04_college_svc.sql` - 高校服务schema
- `v1.0.0-init/05_enterprise_svc.sql` - 企业服务schema
- `v1.0.0-init/06_internship_svc.sql` - 实习服务schema
- `v1.0.0-init/07_training_svc.sql` - 实训服务schema
- `v1.0.0-init/08_growth_svc.sql` - 成长服务schema
- `v1.0.0-init/test_data.sql` - 测试数据
- `v1.0.0-init/README.md` - 详细说明

---

## 版本规范

### 版本号格式
采用语义化版本号：`vMAJOR.MINOR.PATCH`

- **MAJOR**：不兼容的API修改
- **MINOR**：向后兼容的功能性新增
- **PATCH**：向后兼容的问题修正

### 变更类型
- **新增 (Added)**：新功能、新表、新字段
- **变更 (Changed)**：现有功能的变更
- **弃用 (Deprecated)**：即将移除的功能
- **移除 (Removed)**：已移除的功能
- **修复 (Fixed)**：Bug修复
- **安全 (Security)**：安全相关的修复

---

**最后更新：** 2026-03-29  
**当前版本：** v1.6.0
