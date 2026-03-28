# 密码问题修复指南

## 问题确认

测试证实：使用的BCrypt哈希 `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi` **不是**密码 "123456" 的正确哈希！

```
测试结果:
- 新生成哈希验证: ✓ 成功
- 存储哈希验证: ✗ 失败  ← 这就是登录失败的原因
```

## 快速修复（3步）

### 步骤1: 生成正确的哈希

**方法A: 使用Java测试（推荐）**
```bash
cd backend/zhitu-auth
mvn test -Dtest=BCryptPasswordTest#testGenerateNewHash
```
测试会输出3个可用的哈希，复制任意一个。

**方法B: 使用在线工具**
1. 访问: https://bcrypt-generator.com/
2. 输入密码: `123456`，Rounds: `10`
3. 复制生成的哈希（以 `$2a$10$` 开头，长度60字符）

**方法C: 使用Python**
```bash
pip install bcrypt
python -c "import bcrypt; print(bcrypt.hashpw(b'123456', bcrypt.gensalt(rounds=10)).decode())"
```

### 步骤2: 更新数据库

编辑 `update_password_hash.sql` 第19行，替换为步骤1生成的哈希：
```sql
correct_hash TEXT := '$2a$10$[粘贴你生成的哈希]';
```

然后执行：
```bash
psql -h localhost -U postgres -d zhitu_platform -f v1.5.0-mass-data/update_password_hash.sql
```

### 步骤3: 验证登录

使用任意账号登录，密码 `123456`，应该成功！

## 永久修复（更新脚本）

为避免将来重新生成数据时出现同样问题：

1. **更新Python脚本** - `database/generate_test_data.py` 第75行：
   ```python
   PASSWORD_HASH = '$2a$10$[你生成的正确哈希]'
   ```

2. **更新测试文件** - `backend/zhitu-auth/src/test/java/com/zhitu/auth/BCryptPasswordTest.java` 第18行：
   ```java
   private static final String STORED_HASH = "$2a$10$[你生成的正确哈希]";
   ```

3. **重新生成数据**（可选）：
   ```bash
   cd database
   python generate_test_data.py
   psql -h localhost -U postgres -d zhitu_platform -f v1.5.0-mass-data/mass_test_data.sql
   ```

## 验证修复

```bash
cd backend/zhitu-auth
mvn test -Dtest=BCryptPasswordTest
```

所有测试应该通过：
```
✓ testPasswordMatches - 密码验证成功
✓ testGenerateNewHash - 哈希生成和验证成功
✓ testWrongPassword - 错误密码正确拒绝
✓ testHashFormat - 哈希格式正确
```

## 为什么会出现这个问题？

这个错误的哈希可能来自网上的示例代码（对应其他密码）或复制粘贴错误。

**教训**：生成密码哈希后，一定要立即验证！
```java
String hash = encoder.encode("123456");
assert encoder.matches("123456", hash); // 必须验证
```
