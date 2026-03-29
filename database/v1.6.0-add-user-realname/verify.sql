-- =====================================================
-- v1.6.0 - 验证脚本
-- Date: 2026-03-29
-- =====================================================

-- 1. 检查 real_name 字段是否存在
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM information_schema.columns 
WHERE table_schema = 'auth_center' 
  AND table_name = 'sys_user' 
  AND column_name = 'real_name';
-- 应该返回 1 行，is_nullable = 'NO'

-- 2. 检查索引是否创建
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'auth_center' 
  AND tablename = 'sys_user' 
  AND indexname = 'idx_user_realname';
-- 应该返回 1 行

-- 3. 检查数据完整性
SELECT 
    role,
    COUNT(*) as total,
    COUNT(real_name) as with_realname,
    COUNT(*) - COUNT(real_name) as without_realname
FROM auth_center.sys_user
WHERE is_deleted = FALSE
GROUP BY role
ORDER BY role;
-- 所有角色的 without_realname 应该为 0

-- 4. 测试查询（模拟实际业务场景）
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.role,
    u.sub_role
FROM auth_center.sys_user u
WHERE u.role IN ('enterprise', 'college')
  AND u.is_deleted = FALSE
LIMIT 5;
-- 应该能正常返回数据，且 real_name 不为空
