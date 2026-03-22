-- =====================================================
-- Rollback: 003_fix_growth_api_data
-- Description: 回滚职业成长模块测试数据修复
-- Date: 2025-03-22
-- =====================================================

-- 注意：此回滚脚本仅删除本次迁移添加的测试数据
-- 如果数据已被修改或依赖，请谨慎执行

-- 1. 删除添加的徽章/证书（仅删除ID 1-4）
DELETE FROM growth_svc.growth_badge WHERE id IN (1, 2, 3, 4);

-- 2. 删除添加的评价记录（仅删除ID 1-2）
DELETE FROM growth_svc.evaluation_record WHERE id IN (1, 2);

-- 3. 删除添加的学生信息（仅删除ID 1）
-- 注意：如果该学生有其他关联数据，此操作可能失败
DELETE FROM student_svc.student_info WHERE id = 1;

-- 验证回滚结果
SELECT 'Rollback completed' as status;
