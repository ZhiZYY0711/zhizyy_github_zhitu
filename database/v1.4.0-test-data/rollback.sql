/*
 智图云平台 - 测试数据回滚脚本
 版本: v1.4.0
 日期: 2026-03-22
 说明: 回滚v1.4.0版本添加的测试数据
*/

-- ============================================
-- 回滚顺序: 先删除有外键依赖的数据,再删除被依赖的数据
-- ============================================

-- 1. 删除 training_svc 数据
DELETE FROM "training_svc"."project_task" WHERE id > 0;
DELETE FROM "training_svc"."project_enrollment" WHERE id > 0;

-- 2. 删除 student_svc 数据
DELETE FROM "student_svc"."student_task" WHERE id > 0;
DELETE FROM "student_svc"."student_recommendation" WHERE id > 0;
DELETE FROM "student_svc"."student_capability" WHERE id > 0;

-- 3. 删除 platform_service 数据
DELETE FROM "platform_service"."security_log" WHERE id > 0;
DELETE FROM "platform_service"."operation_log" WHERE id > 0;
DELETE FROM "platform_service"."recommendation_top_list" WHERE id > 0;
DELETE FROM "platform_service"."recommendation_banner" WHERE id > 0;
DELETE FROM "platform_service"."certificate_template" WHERE id > 0;
DELETE FROM "platform_service"."contract_template" WHERE id > 0;
DELETE FROM "platform_service"."skill_tree" WHERE parent_id IS NOT NULL;
DELETE FROM "platform_service"."skill_tree" WHERE parent_id IS NULL;
DELETE FROM "platform_service"."sys_tag" WHERE parent_id IS NOT NULL;
DELETE FROM "platform_service"."sys_tag" WHERE parent_id IS NULL;
DELETE FROM "platform_service"."sys_dict" WHERE id > 0;

-- 4. 删除 internship_svc 数据
DELETE FROM "internship_svc"."internship_certificate" WHERE id > 0;

-- 5. 删除 enterprise_svc 数据
DELETE FROM "enterprise_svc"."talent_pool" WHERE id > 0;
DELETE FROM "enterprise_svc"."interview_schedule" WHERE id > 0;
DELETE FROM "enterprise_svc"."enterprise_todo" WHERE id > 0;
DELETE FROM "enterprise_svc"."enterprise_activity" WHERE id > 0;

-- 6. 删除 college_svc 数据
DELETE FROM "college_svc"."enterprise_audit" WHERE id > 0;
DELETE FROM "college_svc"."internship_inspection" WHERE id > 0;
DELETE FROM "college_svc"."enterprise_visit" WHERE id > 0;
DELETE FROM "college_svc"."enterprise_relationship" WHERE id > 0;

-- 重置序列值
SELECT setval('"college_svc"."enterprise_relationship_id_seq"', 1, false);
SELECT setval('"college_svc"."enterprise_visit_id_seq"', 1, false);
SELECT setval('"college_svc"."internship_inspection_id_seq"', 1, false);
SELECT setval('"college_svc"."enterprise_audit_id_seq"', 1, false);

SELECT setval('"enterprise_svc"."enterprise_activity_id_seq"', 1, false);
SELECT setval('"enterprise_svc"."enterprise_todo_id_seq"', 1, false);
SELECT setval('"enterprise_svc"."interview_schedule_id_seq"', 1, false);
SELECT setval('"enterprise_svc"."talent_pool_id_seq"', 1, false);

SELECT setval('"internship_svc"."internship_certificate_id_seq"', 1, false);

SELECT setval('"platform_service"."sys_dict_id_seq"', 1, false);
SELECT setval('"platform_service"."sys_tag_id_seq"', 1, false);
SELECT setval('"platform_service"."skill_tree_id_seq"', 1, false);
SELECT setval('"platform_service"."contract_template_id_seq"', 1, false);
SELECT setval('"platform_service"."certificate_template_id_seq"', 1, false);
SELECT setval('"platform_service"."recommendation_banner_id_seq"', 1, false);
SELECT setval('"platform_service"."recommendation_top_list_id_seq"', 1, false);
SELECT setval('"platform_service"."operation_log_id_seq"', 1, false);
SELECT setval('"platform_service"."security_log_id_seq"', 1, false);

SELECT setval('"student_svc"."student_capability_id_seq"', 1, false);
SELECT setval('"student_svc"."student_recommendation_id_seq"', 1, false);
SELECT setval('"student_svc"."student_task_id_seq"', 1, false);

SELECT setval('"training_svc"."project_enrollment_id_seq"', 1, false);
SELECT setval('"training_svc"."project_task_id_seq"', 1, false);

-- 回滚完成
