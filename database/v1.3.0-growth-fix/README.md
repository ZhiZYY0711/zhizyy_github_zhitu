# 数据库迁移 v1.3.0 - 职业成长模块修复

## 概述

修复职业成长模块API 500错误，补充缺失的评价记录和徽章证书测试数据。

## 问题背景

测试发现职业成长模块的三个API返回500错误：
- `GET /api/student-portal/v1/growth/evaluation` - 获取评价汇总
- `GET /api/student-portal/v1/growth/certificates` - 获取证书列表
- `GET /api/student-portal/v1/growth/badges` - 获取徽章列表

**根本原因：**
1. 缺少测试数据（评价记录和徽章证书）
2. 后端代码存在类型转换错误（已在代码层面修复）

## 变更内容

### 新增数据

#### 1. 评价记录（growth_svc.evaluation_record）

**记录1：企业导师评价**
- student_id: 1 (王小明)
- evaluator_id: 9 (字节跳动导师)
- source_type: enterprise
- scores: {"technical":88,"attitude":92,"communication":85,"innovation":80}
- comment: 技术基础扎实，学习能力强...
- hire_recommendation: strongly_recommend

**记录2：校方辅导员评价**
- student_id: 1 (王小明)
- evaluator_id: 4 (清华辅导员)
- source_type: school
- scores: {"professional":85,"attitude":90,"growth":88}
- comment: 实习期间表现优秀...
- hire_recommendation: recommend

#### 2. 徽章证书（growth_svc.growth_badge）

**证书1：字节跳动实习证明**
- type: certificate
- issue_date: 2025-03-31
- blockchain_hash: sha256:a1b2c3d4...

**徽章1：优秀实习生**
- type: badge
- issue_date: 2025-03-25

**徽章2：Java全栈工程师**
- type: badge
- issue_date: 2023-12-30

**徽章3：Scrum Master**
- type: badge
- issue_date: 2024-01-15

## 部署步骤

### 前置条件
- 已完成v1.2.0迁移
- 数据库连接正常
- 确保test_data.sql已执行（包含student01用户数据）

### 升级

```bash
# 1. 先验证当前数据状态（可选）
psql -U zhitu_user -d zhitu_cloud -f verify.sql

# 2. 执行升级脚本
psql -U zhitu_user -d zhitu_cloud -f upgrade.sql

# 3. 再次验证数据（确认迁移成功）
psql -U zhitu_user -d zhitu_cloud -f verify.sql
```

### 回滚（如需要）

```bash
# 执行回滚脚本（将删除添加的测试数据）
psql -U zhitu_user -d zhitu_cloud -f rollback.sql
```

**警告：** 回滚将删除ID为1-4的评价记录和徽章数据！

## 验证

### 使用验证脚本

```bash
psql -U zhitu_user -d zhitu_cloud -f verify.sql
```

### 手动验证

```sql
-- 1. 检查student01用户映射
SELECT 
    u.id as user_id,
    u.username,
    s.id as student_id,
    s.student_no,
    s.real_name
FROM auth_center.sys_user u
LEFT JOIN student_svc.student_info s ON u.id = s.user_id
WHERE u.username = 'student01';
-- 预期：user_id=15, student_id=1

-- 2. 检查评价记录
SELECT COUNT(*) FROM growth_svc.evaluation_record 
WHERE student_id = 1 AND is_deleted = FALSE;
-- 预期：2条

-- 3. 检查徽章证书
SELECT COUNT(*) FROM growth_svc.growth_badge 
WHERE student_id = 1 AND is_deleted = FALSE;
-- 预期：4条（1个证书 + 3个徽章）
```

## 影响范围

### API修复

此迁移配合后端代码修复，解决以下API的500错误：

1. **评价汇总API**
   - 端点：`GET /api/student-portal/v1/growth/evaluation`
   - 返回：综合评价分数和评价列表

2. **证书列表API**
   - 端点：`GET /api/student-portal/v1/growth/certificates`
   - 返回：学生的证书列表（分页）

3. **徽章列表API**
   - 端点：`GET /api/student-portal/v1/growth/badges`
   - 返回：学生的徽章列表（分页）

### 前端展示

修复后，职业成长页面将正常显示：
- 综合评价结果（87分）
- 导师评语（企业导师 + 校方导师）
- 技能徽章墙（4个徽章）
- 数字凭证（1个证书）

## 测试账号

使用以下账号测试职业成长模块：

- **用户名：** student01
- **密码：** 123456
- **角色：** 学生端

## 数据说明

### 用户映射关系

根据test_data.sql的实际数据：
- student01的user_id是15（不是1）
- tenant_id是2（清华大学）
- student_id是1

### 评价人信息

- evaluator_id=9：bytedance_mentor（字节跳动导师）
- evaluator_id=4：tsinghua_counselor（清华辅导员）

## 相关修复

此迁移是完整修复方案的一部分，还包括：

### 后端代码修复
- 文件：`StudentPortalService.java`
- 修复类型转换错误（TIMESTAMPTZ → LocalDateTime）
- 修复弃用API（BigDecimal.ROUND_HALF_UP）
- 添加异常处理和日志

### 前端组件优化
- 能力雷达图渲染优化
- 表单字段可访问性增强

详见：`test/修复报告_学生端功能测试_2025-03-22.md`

## 注意事项

1. 此迁移使用ON CONFLICT DO UPDATE，可安全重复执行
2. 不会影响现有数据
3. 序列值会自动更新，避免ID冲突
4. 建议在测试环境先验证

## 后续工作

1. 为其他学生添加评价和徽章数据
2. 完善评价体系的权重配置
3. 添加更多类型的徽章和证书
4. 实现区块链存证功能

---

**版本：** v1.3.0  
**发布日期：** 2025-03-22  
**依赖版本：** v1.2.0  
**向后兼容：** 是  
**测试报告：** `test/测试报告_学生端功能测试_2025-03-22.md`
