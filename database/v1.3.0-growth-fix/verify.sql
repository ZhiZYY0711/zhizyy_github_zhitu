-- =====================================================
-- 验证脚本：检查职业成长模块所需的数据是否完整
-- =====================================================

-- 1. 检查student01用户的映射关系
SELECT 
    'student01用户映射' as check_item,
    u.id as user_id,
    u.username,
    s.id as student_id,
    s.student_no,
    s.real_name,
    s.tenant_id
FROM auth_center.sys_user u
LEFT JOIN student_svc.student_info s ON u.id = s.user_id
WHERE u.username = 'student01';

-- 2. 检查评价记录
SELECT 
    'student01评价记录' as check_item,
    er.id,
    er.student_id,
    er.evaluator_id,
    u.username as evaluator_username,
    er.source_type,
    er.scores,
    er.comment,
    er.is_deleted
FROM growth_svc.evaluation_record er
LEFT JOIN auth_center.sys_user u ON er.evaluator_id = u.id
WHERE er.student_id = 1 AND er.is_deleted = false
ORDER BY er.id;

-- 3. 检查徽章/证书
SELECT 
    'student01徽章证书' as check_item,
    gb.id,
    gb.student_id,
    gb.type,
    gb.name,
    gb.issue_date,
    gb.blockchain_hash,
    gb.is_deleted
FROM growth_svc.growth_badge gb
WHERE gb.student_id = 1 AND gb.is_deleted = false
ORDER BY gb.id;

-- 4. 统计汇总
SELECT 
    '数据统计' as summary,
    (SELECT COUNT(*) FROM student_svc.student_info WHERE user_id = 15 AND is_deleted = false) as student_count,
    (SELECT COUNT(*) FROM growth_svc.evaluation_record WHERE student_id = 1 AND is_deleted = false) as evaluation_count,
    (SELECT COUNT(*) FROM growth_svc.growth_badge WHERE student_id = 1 AND is_deleted = false) as badge_count;

-- 5. 检查评价人是否存在
SELECT 
    '评价人检查' as check_item,
    id,
    username,
    role,
    sub_role,
    status
FROM auth_center.sys_user
WHERE id IN (4, 9)
ORDER BY id;
