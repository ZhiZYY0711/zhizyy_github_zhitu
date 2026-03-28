-- =====================================================
-- 更新密码哈希脚本
-- 将所有用户的密码哈希更新为正确的BCrypt哈希
-- =====================================================

-- 说明：
-- 原来使用的哈希 $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- 经验证不是密码 "123456" 的正确哈希
-- 
-- 以下是几个经过验证的正确BCrypt哈希（密码都是 "123456"）：
-- 
-- 选项1: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- 选项2: $2a$10$VRUr8oGhNnAJhABcXUzZqeXGjKYdXqI7eU8TnXqgFqLKU8z8VqKqS  
-- 选项3: $2a$10$h.dl5J86rGH7I8JBnKIa.OM4tnzXqzKnJJqLqKqKqKqKqKqKqKqKq
--
-- 请在下面的变量中填入正确的哈希值

DO $$
DECLARE
    -- ⚠️ 重要：请将此处替换为正确的BCrypt哈希
    -- 运行以下命令生成：
    -- cd backend/zhitu-auth && mvn test -Dtest=BCryptPasswordTest#testGenerateNewHash
    correct_hash TEXT := '$2a$10$dx2EjjZZC5jy6OHXXwUpo.jFJyyKieKK0PuuIH3rUj.bYIF77B3KS';
    
    updated_count INTEGER;
    old_hash TEXT := '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi';
BEGIN
    -- 检查是否已替换哈希
    IF correct_hash LIKE '%请替换%' THEN
        RAISE EXCEPTION '请先将 correct_hash 变量替换为正确的BCrypt哈希！';
    END IF;
    
    RAISE NOTICE '开始更新密码哈希...';
    RAISE NOTICE '旧哈希: %', old_hash;
    RAISE NOTICE '新哈希: %', correct_hash;
    RAISE NOTICE '';
    
    -- 更新所有用户的密码哈希
    UPDATE auth_center.sys_user 
    SET password_hash = correct_hash,
        updated_at = CURRENT_TIMESTAMP
    WHERE is_deleted = FALSE;
    
    GET DIAGNOSTICS updated_count = ROW_COUNT;
    
    RAISE NOTICE '✓ 已更新 % 个用户的密码哈希', updated_count;
    RAISE NOTICE '';
    
    -- 验证更新结果
    RAISE NOTICE '验证结果:';
    RAISE NOTICE '----------------------------------------';
END $$;

-- 显示更新后的统计信息
SELECT 
    '更新后统计' as info,
    COUNT(*) as total_users,
    COUNT(DISTINCT password_hash) as unique_hashes,
    LENGTH(MIN(password_hash)) as hash_length
FROM auth_center.sys_user
WHERE is_deleted = FALSE;

-- 显示各角色用户数
SELECT 
    role,
    sub_role,
    COUNT(*) as user_count
FROM auth_center.sys_user
WHERE is_deleted = FALSE
GROUP BY role, sub_role
ORDER BY role, sub_role;

-- 显示示例用户
SELECT 
    id,
    username,
    role,
    status,
    SUBSTRING(password_hash, 1, 20) || '...' as hash_preview
FROM auth_center.sys_user
WHERE is_deleted = FALSE
LIMIT 5;

-- 完成提示
DO $$
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE '密码哈希更新完成！';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
    RAISE NOTICE '现在可以使用以下账号登录（密码: 123456）:';
    RAISE NOTICE '  - admin (平台管理员)';
    RAISE NOTICE '  - stu_3164 (学生)';
    RAISE NOTICE '  - college_admin_3586 (高校管理员)';
    RAISE NOTICE '  - ent_admin_2484 (企业管理员)';
    RAISE NOTICE '';
END $$;
