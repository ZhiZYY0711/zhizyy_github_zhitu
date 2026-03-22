-- =====================================================
-- Migration: 003_fix_growth_api_data
-- Description: 确保职业成长模块的测试数据完整性
-- Date: 2025-03-22
-- =====================================================

-- 注意：根据test_data.sql，student01的实际映射是：
-- user_id: 15, tenant_id: 2 (清华大学), student_id: 1
-- 本迁移脚本仅添加缺失的评价记录和徽章数据

-- 1. 确保评价记录数据存在
-- evaluator_id: 9=字节跳动导师(bytedance_mentor), 4=清华辅导员(tsinghua_counselor)
INSERT INTO growth_svc.evaluation_record (id, student_id, evaluator_id, source_type, ref_type, ref_id, scores, comment, hire_recommendation, is_deleted)
VALUES 
(1, 1, 9, 'enterprise', 'internship', 1, '{"technical":88,"attitude":92,"communication":85,"innovation":80}', '王小明同学技术基础扎实，学习能力强，能快速融入团队，代码质量较高，建议录用。', 'strongly_recommend', false),
(2, 1, 4, 'school', 'internship', 1, '{"professional":85,"attitude":90,"growth":88}', '该同学实习期间表现优秀，按时提交周报，与企业导师沟通顺畅，综合表现良好。', 'recommend', false)
ON CONFLICT (id) DO UPDATE SET
    student_id = EXCLUDED.student_id,
    evaluator_id = EXCLUDED.evaluator_id,
    source_type = EXCLUDED.source_type,
    scores = EXCLUDED.scores,
    comment = EXCLUDED.comment,
    hire_recommendation = EXCLUDED.hire_recommendation,
    is_deleted = false;

-- 2. 确保徽章/证书数据存在
INSERT INTO growth_svc.growth_badge (id, student_id, type, name, issue_date, image_url, blockchain_hash, is_deleted)
VALUES 
(1, 1, 'certificate', '字节跳动实习证明', '2025-03-31', 'https://oss.example.com/cert/bytedance_intern_001.png', 'sha256:a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456', false),
(2, 1, 'badge', '优秀实习生', '2025-03-25', 'https://oss.example.com/badge/excellent_intern.png', NULL, false),
(3, 1, 'badge', 'Java全栈工程师', '2023-12-30', 'https://oss.example.com/badge/java_fullstack.png', NULL, false),
(4, 1, 'badge', 'Scrum Master', '2024-01-15', 'https://oss.example.com/badge/scrum_master.png', NULL, false)
ON CONFLICT (id) DO UPDATE SET
    student_id = EXCLUDED.student_id,
    type = EXCLUDED.type,
    name = EXCLUDED.name,
    issue_date = EXCLUDED.issue_date,
    image_url = EXCLUDED.image_url,
    blockchain_hash = EXCLUDED.blockchain_hash,
    is_deleted = false;

-- 3. 更新序列值（确保后续插入不会冲突）
SELECT setval('growth_svc.evaluation_record_id_seq', (SELECT COALESCE(MAX(id), 0) FROM growth_svc.evaluation_record), true);
SELECT setval('growth_svc.growth_badge_id_seq', (SELECT COALESCE(MAX(id), 0) FROM growth_svc.growth_badge), true);

-- 4. 验证数据
SELECT 'Student Info Count:' as check_type, COUNT(*) as count FROM student_svc.student_info WHERE is_deleted = false
UNION ALL
SELECT 'Evaluation Records Count:', COUNT(*) FROM growth_svc.evaluation_record WHERE is_deleted = false
UNION ALL
SELECT 'Badges Count:', COUNT(*) FROM growth_svc.growth_badge WHERE is_deleted = false;
