# 数据库初始化 v1.0.0

## 概述

这是智途平台数据库的初始版本，包含完整的数据库schema定义和测试数据。

## 文件说明

### Schema文件（按执行顺序）

1. **01_auth_center.sql** - 认证中心
   - 租户管理（sys_tenant）
   - 用户管理（sys_user）
   - Token管理（sys_refresh_token）

2. **02_platform_service.sql** - 平台服务
   - 系统标签（sys_tag）
   - 技能树（skill_tree）
   - 证书模板（certificate_template）
   - 合同模板（contract_template）
   - 推荐横幅（recommendation_banner）
   - 推荐榜单（recommendation_top_list）
   - 操作日志（operation_log）
   - 安全日志（security_log）
   - 服务健康监控（service_health）
   - 在线用户趋势（online_user_trend）

3. **03_student_svc.sql** - 学生服务
   - 学生档案（student_info）
   - 学生任务（student_task）
   - 学生能力（student_capability）
   - 学生推荐（student_recommendation）

4. **04_college_svc.sql** - 高校服务
   - 高校信息（college_info）
   - 组织架构（organization）
   - 企业关系（enterprise_relationship）
   - 企业拜访（enterprise_visit）
   - 企业审核（enterprise_audit）
   - 实习巡查（internship_inspection）

5. **05_enterprise_svc.sql** - 企业服务
   - 企业信息（enterprise_info）
   - 人才库（talent_pool）
   - 企业活动（enterprise_activity）
   - 企业待办（enterprise_todo）
   - 面试安排（interview_schedule）

6. **06_internship_svc.sql** - 实习服务
   - 实习岗位（internship_job）
   - 实习申请（internship_application）
   - 实习记录（internship_record）
   - 周报（weekly_report）
   - 三方协议（tripartite_agreement）

7. **07_training_svc.sql** - 实训服务
   - 实训项目（training_project）
   - 项目任务（project_task）
   - 项目注册（project_enrollment）

8. **08_growth_svc.sql** - 成长服务
   - 评价记录（evaluation_record）
   - 徽章证书（growth_badge）
   - 预警记录（warning_record）

### 测试数据

- **test_data.sql** - 完整的测试数据
  - 6个租户（1个平台 + 2所高校 + 3家企业）
  - 19个用户（管理员、HR、导师、学生等）
  - 完整的业务测试数据

## 部署步骤

### 1. 创建数据库

```bash
# 使用PostgreSQL 15+
createdb -U postgres zhitu_cloud
```

### 2. 执行Schema文件

```bash
# 按顺序执行所有schema文件
psql -U zhitu_user -d zhitu_cloud -f 01_auth_center.sql
psql -U zhitu_user -d zhitu_cloud -f 02_platform_service.sql
psql -U zhitu_user -d zhitu_cloud -f 03_student_svc.sql
psql -U zhitu_user -d zhitu_cloud -f 04_college_svc.sql
psql -U zhitu_user -d zhitu_cloud -f 05_enterprise_svc.sql
psql -U zhitu_user -d zhitu_cloud -f 06_internship_svc.sql
psql -U zhitu_user -d zhitu_cloud -f 07_training_svc.sql
psql -U zhitu_user -d zhitu_cloud -f 08_growth_svc.sql
```

或使用一键脚本：

```bash
for file in 0*.sql; do
  psql -U zhitu_user -d zhitu_cloud -f "$file"
done
```

### 3. 导入测试数据（可选）

```bash
psql -U zhitu_user -d zhitu_cloud -f test_data.sql
```

## 测试账号

所有测试账号的密码统一为：`123456`

### 平台管理员
- 用户名：`admin`

### 高校账号
- 清华管理员：`tsinghua_admin`
- 清华辅导员：`tsinghua_counselor`
- 北大管理员：`pku_admin`

### 企业账号
- 字节跳动HR：`bytedance_hr`
- 字节跳动导师：`bytedance_mentor`
- 阿里巴巴HR：`alibaba_hr`
- 阿里巴巴导师：`alibaba_mentor`
- 腾讯HR：`tencent_hr`

### 学生账号
- 学生1：`student01` (清华大学)
- 学生2：`student02` (清华大学)
- 学生3：`student03` (清华大学)
- 学生4：`student04` (北京大学)
- 学生5：`student05` (北京大学)

## 数据库架构

### Schema划分

- **auth_center** - 认证中心（用户、租户、权限）
- **platform_service** - 平台服务（通用功能）
- **student_svc** - 学生服务
- **college_svc** - 高校服务
- **enterprise_svc** - 企业服务
- **internship_svc** - 实习服务
- **training_svc** - 实训服务
- **growth_svc** - 成长服务

### 设计原则

1. **多租户隔离**：通过tenant_id实现数据隔离
2. **软删除**：所有表支持is_deleted软删除
3. **时间戳**：created_at和updated_at自动管理
4. **外键约束**：确保数据完整性
5. **索引优化**：为常用查询字段添加索引

## 验证安装

```sql
-- 检查所有schema
SELECT schema_name FROM information_schema.schemata 
WHERE schema_name LIKE '%svc' OR schema_name IN ('auth_center', 'platform_service');

-- 检查表数量
SELECT 
    table_schema, 
    COUNT(*) as table_count 
FROM information_schema.tables 
WHERE table_schema IN ('auth_center', 'platform_service', 'student_svc', 
                       'college_svc', 'enterprise_svc', 'internship_svc', 
                       'training_svc', 'growth_svc')
GROUP BY table_schema;

-- 检查测试数据
SELECT 'Tenants' as type, COUNT(*) as count FROM auth_center.sys_tenant
UNION ALL
SELECT 'Users', COUNT(*) FROM auth_center.sys_user
UNION ALL
SELECT 'Students', COUNT(*) FROM student_svc.student_info;
```

## 注意事项

1. 确保PostgreSQL版本为15或更高
2. 数据库字符集应为UTF-8
3. 时区设置为Asia/Shanghai
4. 建议生产环境修改默认密码
5. 根据实际需求调整连接池配置

## 下一步

安装完成后，可以：
1. 访问前端应用进行登录测试
2. 查看后续的迁移版本（v1.1.0+）
3. 根据需要自定义测试数据

---

**版本：** v1.0.0  
**发布日期：** 2025-03-22  
**数据库：** PostgreSQL 15+
