/*
 智图云平台 - 测试数据验证脚本
 版本: v1.4.0
 日期: 2026-03-22
 说明: 验证测试数据是否正确导入
*/

-- ============================================
-- 数据统计验证
-- ============================================

SELECT '=== 数据导入验证报告 ===' AS report_title;

-- 1. college_svc schema
SELECT 'college_svc.enterprise_relationship' AS table_name, COUNT(*) AS record_count FROM "college_svc"."enterprise_relationship"
UNION ALL
SELECT 'college_svc.enterprise_visit', COUNT(*) FROM "college_svc"."enterprise_visit"
UNION ALL
SELECT 'college_svc.internship_inspection', COUNT(*) FROM "college_svc"."internship_inspection"
UNION ALL
SELECT 'college_svc.enterprise_audit', COUNT(*) FROM "college_svc"."enterprise_audit"

UNION ALL

-- 2. enterprise_svc schema
SELECT 'enterprise_svc.enterprise_activity', COUNT(*) FROM "enterprise_svc"."enterprise_activity"
UNION ALL
SELECT 'enterprise_svc.enterprise_todo', COUNT(*) FROM "enterprise_svc"."enterprise_todo"
UNION ALL
SELECT 'enterprise_svc.interview_schedule', COUNT(*) FROM "enterprise_svc"."interview_schedule"
UNION ALL
SELECT 'enterprise_svc.talent_pool', COUNT(*) FROM "enterprise_svc"."talent_pool"

UNION ALL

-- 3. internship_svc schema
SELECT 'internship_svc.internship_certificate', COUNT(*) FROM "internship_svc"."internship_certificate"

UNION ALL

-- 4. platform_service schema
SELECT 'platform_service.sys_dict', COUNT(*) FROM "platform_service"."sys_dict"
UNION ALL
SELECT 'platform_service.sys_tag', COUNT(*) FROM "platform_service"."sys_tag"
UNION ALL
SELECT 'platform_service.skill_tree', COUNT(*) FROM "platform_service"."skill_tree"
UNION ALL
SELECT 'platform_service.contract_template', COUNT(*) FROM "platform_service"."contract_template"
UNION ALL
SELECT 'platform_service.certificate_template', COUNT(*) FROM "platform_service"."certificate_template"
UNION ALL
SELECT 'platform_service.recommendation_banner', COUNT(*) FROM "platform_service"."recommendation_banner"
UNION ALL
SELECT 'platform_service.recommendation_top_list', COUNT(*) FROM "platform_service"."recommendation_top_list"
UNION ALL
SELECT 'platform_service.operation_log', COUNT(*) FROM "platform_service"."operation_log"
UNION ALL
SELECT 'platform_service.security_log', COUNT(*) FROM "platform_service"."security_log"

UNION ALL

-- 5. student_svc schema
SELECT 'student_svc.student_capability', COUNT(*) FROM "student_svc"."student_capability"
UNION ALL
SELECT 'student_svc.student_recommendation', COUNT(*) FROM "student_svc"."student_recommendation"
UNION ALL
SELECT 'student_svc.student_task', COUNT(*) FROM "student_svc"."student_task"

UNION ALL

-- 6. training_svc schema
SELECT 'training_svc.project_enrollment', COUNT(*) FROM "training_svc"."project_enrollment"
UNION ALL
SELECT 'training_svc.project_task', COUNT(*) FROM "training_svc"."project_task";

-- ============================================
-- 数据完整性验证
-- ============================================

SELECT '=== 数据完整性验证 ===' AS integrity_check;

-- 检查外键关系
SELECT 'enterprise_relationship外键检查' AS check_name,
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END AS result
FROM "college_svc"."enterprise_relationship" er
LEFT JOIN "auth_center"."sys_tenant" t1 ON er.college_tenant_id = t1.id
LEFT JOIN "auth_center"."sys_tenant" t2 ON er.enterprise_tenant_id = t2.id
WHERE t1.id IS NULL OR t2.id IS NULL

UNION ALL

SELECT 'interview_schedule外键检查',
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END
FROM "enterprise_svc"."interview_schedule" i
LEFT JOIN "internship_svc"."job_application" a ON i.application_id = a.id
LEFT JOIN "student_svc"."student_info" s ON i.student_id = s.id
WHERE a.id IS NULL OR s.id IS NULL

UNION ALL

SELECT 'student_capability外键检查',
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END
FROM "student_svc"."student_capability" c
LEFT JOIN "student_svc"."student_info" s ON c.student_id = s.id
WHERE s.id IS NULL

UNION ALL

SELECT 'project_enrollment外键检查',
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END
FROM "training_svc"."project_enrollment" e
LEFT JOIN "training_svc"."training_project" p ON e.project_id = p.id
LEFT JOIN "student_svc"."student_info" s ON e.student_id = s.id
WHERE p.id IS NULL OR s.id IS NULL;

-- ============================================
-- 数据质量验证
-- ============================================

SELECT '=== 数据质量验证 ===' AS quality_check;

-- 检查必填字段
SELECT 'sys_dict必填字段检查' AS check_name,
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END AS result
FROM "platform_service"."sys_dict"
WHERE category IS NULL OR code IS NULL OR label IS NULL

UNION ALL

SELECT 'student_capability分数范围检查',
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END
FROM "student_svc"."student_capability"
WHERE score < 0 OR score > 100

UNION ALL

SELECT 'enterprise_todo优先级检查',
       CASE WHEN COUNT(*) = 0 THEN '通过' ELSE '失败' END
FROM "enterprise_svc"."enterprise_todo"
WHERE priority NOT IN (1, 2, 3);

-- 验证完成
SELECT '=== 验证完成 ===' AS completion_message;
