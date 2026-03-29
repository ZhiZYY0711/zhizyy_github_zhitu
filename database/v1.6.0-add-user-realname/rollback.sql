-- =====================================================
-- v1.6.0 - 回滚：删除用户真实姓名字段
-- Date: 2026-03-29
-- =====================================================

BEGIN;

-- 1. 删除索引
DROP INDEX IF EXISTS auth_center.idx_user_realname;

-- 2. 删除 real_name 字段
ALTER TABLE auth_center.sys_user 
DROP COLUMN IF EXISTS real_name;

COMMIT;

-- 验证回滚
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'auth_center' 
  AND table_name = 'sys_user' 
  AND column_name = 'real_name';
-- 应该返回 0 行
