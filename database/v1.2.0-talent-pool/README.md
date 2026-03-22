# 数据库迁移 v1.2.0 - 人才库软删除

## 概述

为企业服务的人才库表（talent_pool）添加软删除功能，支持数据恢复和审计追踪。

## 变更内容

### 修改表

**enterprise_svc.talent_pool**
- 新增字段：`is_deleted BOOLEAN NOT NULL DEFAULT FALSE`
- 新增索引：`idx_talent_pool_deleted` (is_deleted)
- 更新现有记录：设置 `is_deleted = FALSE`

## 变更原因

1. **数据安全**：防止误删除重要的人才数据
2. **审计追踪**：保留删除记录用于审计
3. **数据恢复**：支持恢复误删除的人才信息
4. **统一标准**：与其他表的软删除机制保持一致

## 部署步骤

### 前置条件
- 已完成v1.1.0迁移
- 数据库连接正常

### 升级

```bash
# 执行升级脚本
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql
```

### 回滚（如需要）

```bash
# 执行回滚脚本（将删除is_deleted字段）
psql -U zhitu_user -d zhitu_cloud -f rollback.sql
```

**警告：** 回滚将丢失软删除状态信息！

## 验证

```sql
-- 检查字段是否添加成功
SELECT 
    column_name, 
    data_type, 
    column_default,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'enterprise_svc' 
    AND table_name = 'talent_pool'
    AND column_name = 'is_deleted';

-- 检查索引是否创建成功
SELECT 
    indexname, 
    indexdef
FROM pg_indexes
WHERE schemaname = 'enterprise_svc' 
    AND tablename = 'talent_pool'
    AND indexname = 'idx_talent_pool_deleted';

-- 检查现有数据
SELECT 
    COUNT(*) as total_records,
    COUNT(*) FILTER (WHERE is_deleted = FALSE) as active_records,
    COUNT(*) FILTER (WHERE is_deleted = TRUE) as deleted_records
FROM enterprise_svc.talent_pool;
```

## 影响范围

### 应用层修改

需要更新以下查询：

**修改前：**
```sql
SELECT * FROM enterprise_svc.talent_pool WHERE enterprise_id = ?;
```

**修改后：**
```sql
SELECT * FROM enterprise_svc.talent_pool 
WHERE enterprise_id = ? AND is_deleted = FALSE;
```

### API变更

1. **查询接口**：默认过滤已删除记录
2. **删除接口**：改为软删除（UPDATE is_deleted = TRUE）
3. **恢复接口**：新增恢复功能（UPDATE is_deleted = FALSE）

### 性能影响

- 索引优化：查询时自动使用 `idx_talent_pool_deleted` 索引
- 存储增加：每条记录增加1字节（BOOLEAN字段）
- 查询性能：基本无影响（已添加索引）

## 使用示例

### 软删除

```sql
-- 软删除人才记录
UPDATE enterprise_svc.talent_pool 
SET is_deleted = TRUE, updated_at = CURRENT_TIMESTAMP
WHERE id = 123;
```

### 恢复记录

```sql
-- 恢复已删除的人才记录
UPDATE enterprise_svc.talent_pool 
SET is_deleted = FALSE, updated_at = CURRENT_TIMESTAMP
WHERE id = 123;
```

### 查询活跃记录

```sql
-- 查询未删除的人才
SELECT * FROM enterprise_svc.talent_pool 
WHERE enterprise_id = 1 AND is_deleted = FALSE;
```

### 查询已删除记录

```sql
-- 查询已删除的人才（用于审计）
SELECT * FROM enterprise_svc.talent_pool 
WHERE enterprise_id = 1 AND is_deleted = TRUE;
```

## 注意事项

1. 此迁移会锁定talent_pool表，建议在非高峰期执行
2. 现有数据的is_deleted字段将被设置为FALSE
3. 应用代码需要同步更新以支持软删除
4. 回滚操作将永久删除软删除状态信息

## 后续工作

1. 更新后端Service层代码
2. 更新Mapper查询语句
3. 添加恢复功能的API接口
4. 更新API文档

---

**版本：** v1.2.0  
**发布日期：** 2025-03-22  
**依赖版本：** v1.1.0  
**向后兼容：** 需要代码配合修改
