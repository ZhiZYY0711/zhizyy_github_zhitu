# 智途平台 - 数据库文档

## 概述

智途平台采用PostgreSQL 15+数据库，使用多Schema架构设计，支持多租户隔离和微服务架构。

## 目录结构

```
database/
├── README.md                    # 本文件 - 总体说明
├── v1.0.0-init/                 # 初始化版本
│   ├── README.md                # 初始化说明
│   ├── 01_auth_center.sql       # 认证中心schema
│   ├── 02_platform_service.sql  # 平台服务schema
│   ├── 03_student_svc.sql       # 学生服务schema
│   ├── 04_college_svc.sql       # 高校服务schema
│   ├── 05_enterprise_svc.sql    # 企业服务schema
│   ├── 06_internship_svc.sql    # 实习服务schema
│   ├── 07_training_svc.sql      # 实训服务schema
│   ├── 08_growth_svc.sql        # 成长服务schema
│   └── test_data.sql            # 测试数据
├── v1.1.0-missing-api/          # 缺失API表补充
│   ├── README.md                # 迁移说明
│   ├── upgrade.sql              # 升级脚本
│   └── rollback.sql             # 回滚脚本
├── v1.2.0-talent-pool/          # 人才库软删除
│   ├── README.md                # 迁移说明
│   └── upgrade.sql              # 升级脚本
└── v1.3.0-growth-fix/           # 职业成长模块修复
    ├── README.md                # 迁移说明
    ├── upgrade.sql              # 升级脚本
    ├── rollback.sql             # 回滚脚本
    └── verify.sql               # 验证脚本
```

## 版本历史

| 版本 | 发布日期 | 说明 | 状态 |
|------|----------|------|------|
| [v1.0.0](#v100---初始化版本) | 2025-03-22 | 数据库初始化 | ✅ 稳定 |
| [v1.1.0](#v110---缺失api表补充) | 2025-03-22 | 补充22个缺失的API表 | ✅ 稳定 |
| [v1.2.0](#v120---人才库软删除) | 2025-03-22 | 人才库添加软删除 | ✅ 稳定 |
| [v1.3.0](#v130---职业成长模块修复) | 2025-03-22 | 修复职业成长API | ✅ 稳定 |

## 快速开始

### 全新安装

```bash
# 1. 创建数据库
createdb -U postgres zhitu_cloud

# 2. 执行初始化脚本
cd database/v1.0.0-init
for file in 0*.sql; do
  psql -U zhitu_user -d zhitu_cloud -f "$file"
done

# 3. 导入测试数据（可选）
psql -U zhitu_user -d zhitu_cloud -f test_data.sql

# 4. 执行所有迁移
cd ../v1.1.0-missing-api
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql

cd ../v1.2.0-talent-pool
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql

cd ../v1.3.0-growth-fix
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql
```

### 从特定版本升级

```bash
# 例如：从v1.1.0升级到v1.3.0
cd database/v1.2.0-talent-pool
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql

cd ../v1.3.0-growth-fix
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql
```

## 版本详情

### v1.0.0 - 初始化版本

**发布日期：** 2025-03-22

**内容：**
- 8个Schema的完整定义
- 50+个数据库表
- 完整的测试数据

**Schema列表：**
- auth_center - 认证中心
- platform_service - 平台服务
- student_svc - 学生服务
- college_svc - 高校服务
- enterprise_svc - 企业服务
- internship_svc - 实习服务
- training_svc - 实训服务
- growth_svc - 成长服务

**详细文档：** [v1.0.0-init/README.md](v1.0.0-init/README.md)

---

### v1.1.0 - 缺失API表补充

**发布日期：** 2025-03-22  
**依赖版本：** v1.0.0

**内容：**
- 新增22个数据库表
- 完善API端点数据模型
- 支持更多业务功能

**新增表分类：**
- student_svc: 3个表（任务、能力、推荐）
- training_svc: 2个表（项目任务、注册）
- enterprise_svc: 3个表（活动、待办、面试）
- college_svc: 4个表（关系、拜访、审核、巡查）
- platform_service: 10个表（标签、技能树、模板、日志、监控）

**详细文档：** [v1.1.0-missing-api/README.md](v1.1.0-missing-api/README.md)

---

### v1.2.0 - 人才库软删除

**发布日期：** 2025-03-22  
**依赖版本：** v1.1.0

**内容：**
- 为talent_pool表添加软删除功能
- 支持数据恢复和审计追踪

**变更：**
- 新增字段：is_deleted
- 新增索引：idx_talent_pool_deleted

**详细文档：** [v1.2.0-talent-pool/README.md](v1.2.0-talent-pool/README.md)

---

### v1.3.0 - 职业成长模块修复

**发布日期：** 2025-03-22  
**依赖版本：** v1.2.0

**内容：**
- 修复职业成长模块API 500错误
- 补充评价记录和徽章证书测试数据

**新增数据：**
- 2条评价记录（企业评价 + 校方评价）
- 4条徽章证书（1个证书 + 3个徽章）

**配套修复：**
- 后端代码类型转换修复
- 前端组件渲染优化

**详细文档：** [v1.3.0-growth-fix/README.md](v1.3.0-growth-fix/README.md)

---

## 数据库架构

### Schema设计原则

1. **按业务领域划分**：每个微服务对应一个Schema
2. **多租户隔离**：通过tenant_id实现数据隔离
3. **软删除支持**：所有表支持is_deleted软删除
4. **时间戳管理**：created_at和updated_at自动管理
5. **外键约束**：确保数据完整性
6. **索引优化**：为常用查询字段添加索引

### 表命名规范

- 使用小写字母和下划线
- 表名使用单数形式
- 关联表使用两个表名组合

### 字段命名规范

- 主键：id (BIGSERIAL)
- 外键：{table}_id
- 时间戳：created_at, updated_at
- 软删除：is_deleted
- 状态字段：status
- 类型字段：type

## 测试账号

所有测试账号的密码统一为：`123456`

### 平台管理员
- **用户名：** admin
- **角色：** platform/admin

### 高校账号
- **清华管理员：** tsinghua_admin
- **清华辅导员：** tsinghua_counselor
- **北大管理员：** pku_admin

### 企业账号
- **字节跳动HR：** bytedance_hr
- **字节跳动导师：** bytedance_mentor
- **阿里巴巴HR：** alibaba_hr
- **阿里巴巴导师：** alibaba_mentor
- **腾讯HR：** tencent_hr

### 学生账号
- **学生1：** student01 (清华大学)
- **学生2：** student02 (清华大学)
- **学生3：** student03 (清华大学)
- **学生4：** student04 (北京大学)
- **学生5：** student05 (北京大学)

## 迁移指南

### 创建新迁移

1. 创建版本文件夹：`database/vX.Y.Z-description/`
2. 创建README.md说明文档
3. 创建upgrade.sql升级脚本
4. 创建rollback.sql回滚脚本（如需要）
5. 创建verify.sql验证脚本（如需要）
6. 更新本README的版本历史

### 迁移脚本规范

**upgrade.sql：**
```sql
-- =====================================================
-- Migration: vX.Y.Z - Description
-- Date: YYYY-MM-DD
-- =====================================================

-- 变更内容
ALTER TABLE ...;

-- 验证
SELECT ...;
```

**rollback.sql：**
```sql
-- =====================================================
-- Rollback: vX.Y.Z - Description
-- Date: YYYY-MM-DD
-- =====================================================

-- 回滚操作
ALTER TABLE ...;
```

### 迁移最佳实践

1. **向后兼容**：尽量保持向后兼容
2. **事务管理**：使用事务确保原子性
3. **备份数据**：迁移前备份数据库
4. **测试环境**：先在测试环境验证
5. **文档完整**：提供详细的README文档
6. **回滚方案**：准备回滚脚本

## 维护指南

### 日常维护

```sql
-- 1. 检查表大小
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname IN ('auth_center', 'platform_service', 'student_svc', 
                     'college_svc', 'enterprise_svc', 'internship_svc', 
                     'training_svc', 'growth_svc')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 2. 检查索引使用情况
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname IN ('auth_center', 'platform_service', 'student_svc', 
                     'college_svc', 'enterprise_svc', 'internship_svc', 
                     'training_svc', 'growth_svc')
ORDER BY idx_scan DESC;

-- 3. 清理软删除数据（谨慎操作）
-- DELETE FROM table_name WHERE is_deleted = TRUE AND updated_at < NOW() - INTERVAL '90 days';
```

### 性能优化

1. **定期VACUUM**：清理死元组
2. **ANALYZE统计**：更新表统计信息
3. **索引维护**：检查和重建索引
4. **查询优化**：使用EXPLAIN分析慢查询

### 备份策略

```bash
# 全量备份
pg_dump -U zhitu_user -d zhitu_cloud -F c -f zhitu_cloud_backup_$(date +%Y%m%d).dump

# 仅备份schema
pg_dump -U zhitu_user -d zhitu_cloud -s -f zhitu_cloud_schema_$(date +%Y%m%d).sql

# 仅备份数据
pg_dump -U zhitu_user -d zhitu_cloud -a -f zhitu_cloud_data_$(date +%Y%m%d).sql
```

## 故障排查

### 常见问题

**1. 连接失败**
```bash
# 检查PostgreSQL服务状态
systemctl status postgresql

# 检查连接配置
psql -U zhitu_user -d zhitu_cloud -h localhost
```

**2. 权限问题**
```sql
-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE zhitu_cloud TO zhitu_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth_center TO zhitu_user;
```

**3. 迁移失败**
```bash
# 查看错误日志
tail -f /var/log/postgresql/postgresql-15-main.log

# 回滚迁移
psql -U zhitu_user -d zhitu_cloud -f rollback.sql
```

## 相关文档

- [ER图](ER_DIAGRAM.md) - 实体关系图
- [迁移指南](MIGRATION_GUIDE.md) - 详细迁移说明
- [测试报告](../test/测试报告_学生端功能测试_2025-03-22.md) - 功能测试报告
- [修复报告](../test/修复报告_学生端功能测试_2025-03-22.md) - 问题修复报告

## 技术栈

- **数据库：** PostgreSQL 15+
- **字符集：** UTF-8
- **时区：** Asia/Shanghai
- **连接池：** HikariCP
- **ORM：** MyBatis-Plus

## 联系方式

如有问题，请联系：
- **数据库管理员：** [DBA邮箱]
- **技术负责人：** [技术负责人邮箱]
- **项目经理：** [项目经理邮箱]

---

**最后更新：** 2025-03-22  
**当前版本：** v1.3.0  
**数据库：** PostgreSQL 15+
