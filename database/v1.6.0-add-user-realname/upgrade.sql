-- =====================================================
-- v1.6.0 - 添加用户真实姓名字段
-- Date: 2026-03-29
-- =====================================================

BEGIN;

-- 1. 为 sys_user 表添加 real_name 字段
ALTER TABLE auth_center.sys_user 
ADD COLUMN real_name VARCHAR(50);

COMMENT ON COLUMN auth_center.sys_user.real_name IS '真实姓名';

-- 2. 将现有用户的 username 复制到 real_name 作为默认值
UPDATE auth_center.sys_user 
SET real_name = username 
WHERE real_name IS NULL;

-- 3. 设置 real_name 为 NOT NULL（在数据迁移后）
ALTER TABLE auth_center.sys_user 
ALTER COLUMN real_name SET NOT NULL;

-- 4. 创建索引以提高查询性能
CREATE INDEX idx_user_realname ON auth_center.sys_user(real_name) 
WHERE is_deleted = FALSE;

COMMIT;

-- 验证
SELECT 
    COUNT(*) as total_users,
    COUNT(real_name) as users_with_realname,
    COUNT(*) - COUNT(real_name) as users_without_realname
FROM auth_center.sys_user
WHERE is_deleted = FALSE;
