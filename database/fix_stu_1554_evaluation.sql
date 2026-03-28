-- 为 stu_1554 用户 (user_id=110, student_id=2) 添加评价记录
-- 问题：该学生在 student_info 表中存在，但在 evaluation_record 表中没有任何评价记录

-- 插入一些测试评价数据
INSERT INTO "growth_svc"."evaluation_record" 
("student_id", "evaluator_id", "source_type", "ref_type", "ref_id", "scores", "comment", "hire_recommendation", "created_at", "updated_at", "is_deleted") 
VALUES
-- 企业导师评价
(2, 100, 'enterprise', 'internship', 1, '{"technical": 85, "attitude": 88, "communication": 82, "innovation": 86}', '该同学在实习期间表现优秀，技术能力扎实，学习能力强，能够快速适应新环境。', 'recommend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),

-- 学校老师评价
(2, 4, 'school', 'project', 10, '{"technical": 90, "attitude": 92, "communication": 85, "innovation": 88}', '该同学在项目中表现突出，具有良好的团队协作精神和创新思维。', 'strongly_recommend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),

-- 同学互评
(2, 109, 'peer', 'project', 15, '{"technical": 82, "attitude": 85, "communication": 88, "innovation": 80}', '沟通能力强，乐于助人，是团队中的重要成员。', 'recommend', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

-- 验证插入结果
SELECT 
    er.id,
    er.student_id,
    si.real_name as student_name,
    u.username as student_username,
    er.source_type,
    er.scores,
    er.comment,
    er.created_at
FROM growth_svc.evaluation_record er
JOIN student_svc.student_info si ON er.student_id = si.id
JOIN auth_center.sys_user u ON si.user_id = u.id
WHERE er.student_id = 2 AND er.is_deleted = FALSE
ORDER BY er.created_at DESC;
