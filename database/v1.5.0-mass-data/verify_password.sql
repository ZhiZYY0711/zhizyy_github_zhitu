-- =====================================================
-- 密码验证脚本
-- 用于检查数据库中的密码哈希是否正确
-- =====================================================

-- 1. 检查用户 stu_3164 是否存在及其密码哈希
SELECT 
    id,
    tenant_id,
    username,
    password_hash,
    role,
    status,
    LENGTH(password_hash) as hash_length,
    SUBSTRING(password_hash, 1, 7) as hash_prefix
FROM auth_center.sys_user
WHERE username = 'stu_3164';

-- 2. 检查所有学生用户的密码哈希是否一致
SELECT 
    password_hash,
    COUNT(*) as user_count
FROM auth_center.sys_user
WHERE role = 'student'
GROUP BY password_hash
ORDER BY user_count DESC;

-- 3. 检查是否有密码哈希为空或异常的用户
SELECT 
    id,
    username,
    role,
    password_hash,
    LENGTH(password_hash) as hash_length
FROM auth_center.sys_user
WHERE password_hash IS NULL 
   OR password_hash = '' 
   OR LENGTH(password_hash) < 50
LIMIT 10;

-- 4. 对比原始测试数据中的密码哈希
-- 预期的BCrypt哈希: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
SELECT 
    CASE 
        WHEN password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
        THEN '密码哈希正确'
        ELSE '密码哈希不匹配: ' || password_hash
    END as check_result,
    COUNT(*) as user_count
FROM auth_center.sys_user
GROUP BY password_hash
ORDER BY user_count DESC;

-- 5. 检查sys_user表的总记录数
SELECT 
    '总用户数' as metric,
    COUNT(*) as value
FROM auth_center.sys_user
WHERE is_deleted = FALSE
UNION ALL
SELECT 
    '学生用户数',
    COUNT(*)
FROM auth_center.sys_user
WHERE role = 'student' AND is_deleted = FALSE
UNION ALL
SELECT 
    '企业用户数',
    COUNT(*)
FROM auth_center.sys_user
WHERE role = 'enterprise' AND is_deleted = FALSE
UNION ALL
SELECT 
    '高校用户数',
    COUNT(*)
FROM auth_center.sys_user
WHERE role = 'college' AND is_deleted = FALSE;
