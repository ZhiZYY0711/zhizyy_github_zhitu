# v1.4.0 测试数据迁移

## 概述
本次迁移为系统中数据较少或为空的表补充测试数据,以便进行完整的功能测试。

## 变更内容

### 1. college_svc schema
- `enterprise_relationship`: 添加校企合作关系数据
- `enterprise_visit`: 添加企业走访记录
- `internship_inspection`: 添加实习巡查记录
- `enterprise_audit`: 添加企业审核记录

### 2. enterprise_svc schema
- `enterprise_activity`: 添加企业动态数据
- `enterprise_todo`: 添加企业待办事项
- `interview_schedule`: 添加面试安排数据
- `talent_pool`: 添加人才库数据

### 3. growth_svc schema
- 已有部分数据,补充更多评价和徽章记录

### 4. internship_svc schema
- `internship_certificate`: 添加实习证明数据

### 5. platform_service schema
- `sys_dict`: 添加数据字典
- `sys_tag`: 添加系统标签
- `skill_tree`: 添加技能树数据
- `contract_template`: 添加合同模板
- `certificate_template`: 添加证书模板
- `recommendation_banner`: 添加推荐横幅
- `recommendation_top_list`: 添加推荐榜单
- `operation_log`: 添加操作日志样例
- `security_log`: 添加安全日志样例

### 6. student_svc schema
- `student_capability`: 添加学生能力雷达图数据
- `student_recommendation`: 添加个性化推荐数据
- `student_task`: 添加学生任务数据

### 7. training_svc schema
- `project_enrollment`: 添加项目报名数据
- `project_task`: 添加项目任务数据

## 执行方式

```bash
# PostgreSQL
psql -h localhost -p 15432 -U postgres -d zhitu_cloud -f upgrade.sql
```

## 回滚方式

```bash
# PostgreSQL
psql -h localhost -p 15432 -U postgres -d zhitu_cloud -f rollback.sql
```

## 注意事项

1. 本迁移脚本包含大量测试数据,仅用于开发和测试环境
2. 生产环境请谨慎使用,建议根据实际业务需求调整数据
3. 所有密码哈希使用 bcrypt 加密,默认密码为 "password"
4. 时间戳基于 2025-2026 年度,符合当前系统时间线

## 数据统计

- 校企合作关系: 6条
- 企业走访记录: 8条
- 实习巡查记录: 6条
- 企业审核记录: 3条
- 企业动态: 15条
- 企业待办: 12条
- 面试安排: 8条
- 人才库: 10条
- 实习证明: 4条
- 数据字典: 30+条
- 系统标签: 40+条
- 技能树: 25条
- 学生能力数据: 15条
- 学生推荐: 20条
- 学生任务: 15条
- 项目报名: 12条
- 项目任务: 20条

## 版本信息

- 版本号: v1.4.0
- 创建日期: 2026-03-22
- 作者: System
