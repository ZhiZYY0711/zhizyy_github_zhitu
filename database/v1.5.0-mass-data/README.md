# v1.5.0 - 批量测试数据

## 版本信息

- **版本号**: v1.5.0
- **日期**: 2026-03-28
- **类型**: 测试数据批量生成
- **数据规模**: 94,302条记录，覆盖8个Schema的50+张表

## 🚨 重要：密码问题修复

### 问题确认

**根本原因**：使用的BCrypt哈希 `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi` 不是密码 "123456" 的正确哈希！

单元测试结果：
```
新生成哈希验证: ✓ 成功
存储哈希验证: ✗ 失败  ← 这就是登录失败的原因
```

### 快速修复（3步）

#### 步骤1: 生成正确的哈希

**方法A - Java测试（推荐）**
```bash
cd backend/zhitu-auth
mvn test -Dtest=BCryptPasswordTest#testGenerateNewHash
```
复制输出的任意一个哈希。

**方法B - 在线工具**
- 访问: https://bcrypt-generator.com/
- 密码: `123456`，Rounds: `10`
- 复制生成的哈希（60字符，以 `$2a$10$` 开头）

**方法C - Python**
```bash
pip install bcrypt
python -c "import bcrypt; print(bcrypt.hashpw(b'123456', bcrypt.gensalt(rounds=10)).decode())"
```

#### 步骤2: 更新数据库

编辑 `update_password_hash.sql` 第19行，粘贴步骤1生成的哈希：
```sql
correct_hash TEXT := '$2a$10$[粘贴这里]';
```

执行更新：
```bash
psql -h localhost -U postgres -d zhitu_platform -f v1.5.0-mass-data/update_password_hash.sql
```

#### 步骤3: 验证登录

使用任意账号登录，密码 `123456`：
- `admin` (平台管理员)
- `stu_3164` (学生)
- `college_admin_3586` (高校管理员)

### 永久修复（更新脚本）

为避免重新生成数据时出现同样问题：

1. **更新Python脚本** - `database/generate_test_data.py` 第75行：
   ```python
   PASSWORD_HASH = '$2a$10$[粘贴正确的哈希]'
   ```

2. **更新测试文件** - `backend/zhitu-auth/src/test/java/com/zhitu/auth/BCryptPasswordTest.java` 第18行：
   ```java
   private static final String STORED_HASH = "$2a$10$[粘贴正确的哈希]";
   ```

3. **重新生成数据**：
   ```bash
   cd database
   python generate_test_data.py
   psql -h localhost -U postgres -d zhitu_platform -f v1.5.0-mass-data/mass_test_data.sql
   ```

---

## 使用方法

### 1. 生成SQL文件

```bash
cd database
python generate_test_data.py
```

### 2. 执行SQL文件

```bash
psql -h localhost -U postgres -d zhitu_platform -f v1.5.0-mass-data/mass_test_data.sql
```

### 3. 验证数据

```sql
SELECT schemaname, tablename, n_live_tup as row_count
FROM pg_stat_user_tables
WHERE schemaname IN ('auth_center', 'student_svc', 'college_svc', 'enterprise_svc', 
                     'internship_svc', 'training_svc', 'growth_svc', 'platform_service')
ORDER BY schemaname, tablename;
```

---

## 数据规模

### 租户与用户 (2,208条)
- 租户: 21个 (1平台 + 5高校 + 15企业)
- 用户: 2,087个 (2平台管理员 + 25高校用户 + 60企业用户 + 2000学生)

### 业务数据 (92,094条)
- 学生相关: 22,000条 (档案、能力、推荐、任务)
- 高校相关: 620条 (高校信息、组织架构、校企关系、走访、审核)
- 企业相关: 10,075条 (企业信息、员工、人才库、活动、待办)
- 实习相关: 45,295条 (岗位、申请、Offer、实习记录、周报、考勤、证书)
- 实训相关: 1,550条 (项目、排期、报名、任务)
- 成长相关: 5,500条 (评价、徽章、预警)
- 平台相关: 7,054条 (字典、标签、技能树、模板、日志)

---

## 数据特点

- **真实性**: 中文姓名、手机号、地址等真实格式，时间范围 2024-06 ~ 2025-12
- **一致性**: 所有外键关系正确，用户名/学号/手机号唯一
- **多样性**: 5高校 × 15企业 = 75种校企合作，2000学生 × 4次申请 = 8000条求职记录
- **完整性**: 覆盖完整业务流程（申请→面试→Offer→实习→周报→证书）

---

## 注意事项

> [!WARNING]
> 执行SQL文件会**清空所有现有数据** (TRUNCATE CASCADE)，请在测试环境使用！

> [!IMPORTANT]
> - 所有用户密码: `123456`
> - SQL文件大小: 14.7 MB
> - 执行时间: 约2-5分钟

---

## 脚本配置

可在 `generate_test_data.py` 的 `Config` 类修改数据规模：

```python
class Config:
    COLLEGE_TENANTS = 5          # 高校数量
    ENTERPRISE_TENANTS = 15      # 企业数量
    STUDENTS_PER_COLLEGE = 400   # 每校学生数
    JOBS_PER_ENTERPRISE = 13     # 每企业岗位数
    # ... 更多配置
```

---

## 性能优化

- 批量INSERT: 每500行一个INSERT语句
- 禁用外键检查: `SET session_replication_role = 'replica'`
- 序列自动重置
- 执行后自动ANALYZE

---

## 相关文件

```
database/v1.5.0-mass-data/
├── README.md                    # 本文档
├── SUMMARY.md                   # 完成总结
├── mass_test_data.sql           # 测试数据 (14.7 MB)
├── update_password_hash.sql     # 密码哈希更新脚本
└── verify_password.sql          # 验证脚本

backend/zhitu-auth/src/test/java/com/zhitu/auth/
├── BCryptPasswordTest.java      # 单元测试
└── GenerateCorrectHash.java     # 哈希生成工具
```

---

## 更新日志

### v1.5.0 (2026-03-28)

- 生成约10万条测试数据
- 覆盖8个Schema的50+张表
- 支持完整业务流程数据
- 修复: project_enrollment唯一约束冲突
- 修复: attendance外键引用错误
- 修复: 周报模板格式化错误
- 发现: 密码哈希不正确（需要手动修复）
