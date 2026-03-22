-- =====================================================
-- 智图平台测试数据
-- 密码统一: 123456
-- BCrypt Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- =====================================================

-- =====================================================
-- 1. 租户数据 (1个平台 + 2所高校 + 3家企业)
-- =====================================================
INSERT INTO auth_center.sys_tenant (id, name, type, status, config) VALUES
(1, '智图平台运营中心', 0, 1, '{"theme":"#1890ff"}'),
(2, '清华大学',         1, 1, '{"theme":"#722ed1","logo":"https://example.com/tsinghua.png"}'),
(3, '北京大学',         1, 1, '{"theme":"#eb2f96","logo":"https://example.com/pku.png"}'),
(4, '字节跳动',         2, 1, '{"theme":"#000000","logo":"https://example.com/bytedance.png"}'),
(5, '阿里巴巴',         2, 1, '{"theme":"#ff6a00","logo":"https://example.com/alibaba.png"}'),
(6, '腾讯科技',         2, 1, '{"theme":"#07c160","logo":"https://example.com/tencent.png"}');

-- 重置序列
SELECT setval('auth_center.sys_tenant_id_seq', 6);

-- =====================================================
-- 2. 用户账号
-- 平台管理员: admin / 123456
-- 高校管理员: tsinghua_admin, pku_admin
-- 企业HR: bytedance_hr, alibaba_hr, tencent_hr
-- 企业导师: bytedance_mentor, alibaba_mentor
-- 学生: student01 ~ student05
-- =====================================================
INSERT INTO auth_center.sys_user (id, tenant_id, username, password_hash, phone, role, sub_role, status) VALUES
-- 平台管理员
(1,  1, 'admin',           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000001', 'platform', 'admin',    1),
-- 清华大学
(2,  2, 'tsinghua_admin',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000002', 'college',  'admin',    1),
(3,  2, 'tsinghua_dean',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000003', 'college',  'dean',     1),
(4,  2, 'tsinghua_counselor','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','13800000004','college', 'counselor',1),
-- 北京大学
(5,  3, 'pku_admin',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000005', 'college',  'admin',    1),
(6,  3, 'pku_counselor',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000006', 'college',  'counselor',1),
-- 字节跳动
(7,  4, 'bytedance_admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000007', 'enterprise','admin',   1),
(8,  4, 'bytedance_hr',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000008', 'enterprise','hr',      1),
(9,  4, 'bytedance_mentor','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000009', 'enterprise','mentor',  1),
-- 阿里巴巴
(10, 5, 'alibaba_admin',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000010', 'enterprise','admin',   1),
(11, 5, 'alibaba_hr',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000011', 'enterprise','hr',      1),
(12, 5, 'alibaba_mentor',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000012', 'enterprise','mentor',  1),
-- 腾讯
(13, 6, 'tencent_admin',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000013', 'enterprise','admin',   1),
(14, 6, 'tencent_hr',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13800000014', 'enterprise','hr',      1),
-- 学生 (清华3个 + 北大2个)
(15, 2, 'student01',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13900000001', 'student',  NULL,       1),
(16, 2, 'student02',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13900000002', 'student',  NULL,       1),
(17, 2, 'student03',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13900000003', 'student',  NULL,       1),
(18, 3, 'student04',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13900000004', 'student',  NULL,       1),
(19, 3, 'student05',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '13900000005', 'student',  NULL,       1);

SELECT setval('auth_center.sys_user_id_seq', 19);

-- =====================================================
-- 3. 高校信息
-- =====================================================
INSERT INTO college_svc.college_info (id, tenant_id, college_name, college_code, province, city, address, contact_name, contact_phone, contact_email, cooperation_level, status) VALUES
(1, 2, '清华大学', 'THU', '北京市', '北京', '北京市海淀区清华园1号', '张主任', '010-62781234', 'contact@tsinghua.edu.cn', 3, 1),
(2, 3, '北京大学', 'PKU', '北京市', '北京', '北京市海淀区颐和园路5号', '李主任', '010-62751234', 'contact@pku.edu.cn', 3, 1);

SELECT setval('college_svc.college_info_id_seq', 2);

-- =====================================================
-- 4. 组织架构 (学院 → 专业 → 班级)
-- 清华: 计算机学院(1) → 软件工程(2) → 软工2101班(3)
-- 北大: 信息学院(4) → 计算机科学(5) → 计科2101班(6)
-- =====================================================
INSERT INTO college_svc.organization (id, tenant_id, parent_id, org_type, org_name, org_code, sort_order) VALUES
-- 清华大学
(1, 2, NULL, 1, '计算机科学与技术学院', 'THU-CS',    1),
(2, 2, 1,    2, '软件工程',             'THU-CS-SE', 1),
(3, 2, 2,    3, '软工2101班',           'THU-SE-2101',1),
-- 北京大学
(4, 3, NULL, 1, '信息科学技术学院',     'PKU-IS',    1),
(5, 3, 4,    2, '计算机科学',           'PKU-IS-CS', 1),
(6, 3, 5,    3, '计科2101班',           'PKU-CS-2101',1);

SELECT setval('college_svc.organization_id_seq', 6);

-- =====================================================
-- 5. 学生档案 (user_id 15-19)
-- student01~03: 清华 软工2101班
-- student04~05: 北大 计科2101班
-- =====================================================
INSERT INTO student_svc.student_info (id, user_id, tenant_id, student_no, real_name, gender, phone, email, college_id, major_id, class_id, grade, enrollment_date, graduation_date, skills) VALUES
(1, 15, 2, '2021010001', '王小明', 1, '13900000001', 'student01@tsinghua.edu.cn', 1, 2, 3, '2021级', '2021-09-01', '2025-06-30', '["Java","Spring Boot","MySQL"]'),
(2, 16, 2, '2021010002', '李小红', 2, '13900000002', 'student02@tsinghua.edu.cn', 1, 2, 3, '2021级', '2021-09-01', '2025-06-30', '["Python","Django","Redis"]'),
(3, 17, 2, '2021010003', '张小强', 1, '13900000003', 'student03@tsinghua.edu.cn', 1, 2, 3, '2021级', '2021-09-01', '2025-06-30', '["Vue.js","React","TypeScript"]'),
(4, 18, 3, '2021020001', '陈小芳', 2, '13900000004', 'student04@pku.edu.cn',      4, 5, 6, '2021级', '2021-09-01', '2025-06-30', '["Go","Kubernetes","Docker"]'),
(5, 19, 3, '2021020002', '刘小伟', 1, '13900000005', 'student05@pku.edu.cn',      4, 5, 6, '2021级', '2021-09-01', '2025-06-30', '["Android","Kotlin","Flutter"]');

SELECT setval('student_svc.student_info_id_seq', 5);

-- =====================================================
-- 6. 企业信息 (tenant_id: 4=字节 5=阿里 6=腾讯)
-- =====================================================
INSERT INTO enterprise_svc.enterprise_info (id, tenant_id, enterprise_name, enterprise_code, industry, scale, province, city, address, website, description, contact_name, contact_phone, contact_email, audit_status, status) VALUES
(1, 4, '字节跳动有限公司', '91110000MA01BYTE01', '互联网', '10000人以上', '北京市', '北京', '北京市海淀区中关村北二街8号', 'https://www.bytedance.com', '字节跳动是一家全球化的科技公司，旗下产品包括抖音、今日头条等。', '赵HR', '010-58341234', 'hr@bytedance.com', 1, 1),
(2, 5, '阿里巴巴（中国）有限公司', '91330000MA27ALIB01', '电子商务', '10000人以上', '浙江省', '杭州', '浙江省杭州市余杭区文一西路969号', 'https://www.alibaba.com', '阿里巴巴集团是全球最大的零售商业体，旗下有淘宝、天猫、支付宝等。', '钱HR', '0571-88581234', 'hr@alibaba.com', 1, 1),
(3, 6, '腾讯科技（深圳）有限公司', '91440300MA5TENC01', '互联网', '10000人以上', '广东省', '深圳', '广东省深圳市南山区粤海街道腾讯滨海大厦', 'https://www.tencent.com', '腾讯是中国最大的互联网综合服务提供商，旗下有微信、QQ、王者荣耀等。', '孙HR', '0755-86013388', 'hr@tencent.com', 1, 1);

SELECT setval('enterprise_svc.enterprise_info_id_seq', 3);

-- =====================================================
-- 7. 企业员工档案 (user_id 7-14)
-- =====================================================
INSERT INTO enterprise_svc.enterprise_staff (id, tenant_id, user_id, department, position, is_mentor) VALUES
-- 字节跳动
(1,  4, 7,  '人力资源部', '企业管理员',   FALSE),
(2,  4, 8,  '人力资源部', 'HR招聘专员',   FALSE),
(3,  4, 9,  '技术部',     '高级工程师',   TRUE),
-- 阿里巴巴
(4,  5, 10, '人力资源部', '企业管理员',   FALSE),
(5,  5, 11, '人力资源部', 'HR招聘专员',   FALSE),
(6,  5, 12, '技术部',     '技术专家',     TRUE),
-- 腾讯
(7,  6, 13, '人力资源部', '企业管理员',   FALSE),
(8,  6, 14, '人力资源部', 'HR招聘专员',   FALSE);

SELECT setval('enterprise_svc.enterprise_staff_id_seq', 8);

-- =====================================================
-- 8. 实习岗位 (各企业发布)
-- =====================================================
INSERT INTO internship_svc.internship_job (id, enterprise_id, job_title, job_type, description, requirements, tech_stack, industry, city, salary_min, salary_max, headcount, start_date, end_date, status) VALUES
-- 字节跳动
(1, 4, 'Java后端开发实习生', '技术类', '参与抖音后端服务开发，负责高并发接口设计与优化', '熟悉Java/Spring Boot，了解分布式系统', '["Java","Spring Boot","MySQL","Redis"]', '互联网', '北京', 200, 300, 3, '2025-03-01', '2025-08-31', 1),
(2, 4, '前端开发实习生',     '技术类', '参与今日头条H5/小程序开发', '熟悉Vue.js或React，了解TypeScript', '["Vue.js","React","TypeScript","Webpack"]', '互联网', '北京', 180, 250, 2, '2025-03-01', '2025-08-31', 1),
-- 阿里巴巴
(3, 5, '算法工程师实习生',   '技术类', '参与推荐算法研发，优化用户体验', '熟悉Python，了解机器学习基础', '["Python","TensorFlow","Spark","Hive"]', '电子商务', '杭州', 250, 350, 2, '2025-03-01', '2025-09-30', 1),
(4, 5, '产品经理实习生',     '产品类', '协助产品规划，参与需求分析和竞品调研', '逻辑思维强，有产品sense', '["Axure","Figma","SQL"]', '电子商务', '杭州', 150, 200, 2, '2025-04-01', '2025-09-30', 1),
-- 腾讯
(5, 6, '后台开发实习生',     '技术类', '参与微信后台服务开发，负责接口开发与维护', '熟悉C++或Go，了解网络编程', '["Go","C++","MySQL","Kafka"]', '互联网', '深圳', 200, 300, 3, '2025-03-01', '2025-08-31', 1);

SELECT setval('internship_svc.internship_job_id_seq', 5);

-- =====================================================
-- 9. 求职申请
-- student01(王小明) → 字节Java(job1), 阿里算法(job3)
-- student02(李小红) → 字节前端(job2), 阿里算法(job3)
-- student03(张小强) → 字节前端(job2)
-- student04(陈小芳) → 腾讯后台(job5)
-- student05(刘小伟) → 腾讯后台(job5)
-- =====================================================
INSERT INTO internship_svc.job_application (id, job_id, student_id, resume_url, cover_letter, status) VALUES
(1, 1, 1, 'https://oss.example.com/resume/student01.pdf', '我对Java后端开发充满热情，期待加入字节跳动。', 4), -- 录用
(2, 3, 1, 'https://oss.example.com/resume/student01.pdf', '对推荐算法有浓厚兴趣，希望在阿里学习成长。',   3), -- 拒绝
(3, 2, 2, 'https://oss.example.com/resume/student02.pdf', '熟悉Vue和React，期待参与今日头条前端开发。',  3), -- 拒绝
(4, 3, 2, 'https://oss.example.com/resume/student02.pdf', '有Python和机器学习基础，希望加入阿里算法团队。',4), -- 录用
(5, 2, 3, 'https://oss.example.com/resume/student03.pdf', '擅长前端开发，对字节的产品很感兴趣。',        4), -- 录用
(6, 5, 4, 'https://oss.example.com/resume/student04.pdf', '熟悉Go语言，期待参与微信后台开发。',          4), -- 录用
(7, 5, 5, 'https://oss.example.com/resume/student05.pdf', '有Android和后台开发经验，希望加入腾讯。',     2); -- Offer阶段

SELECT setval('internship_svc.job_application_id_seq', 7);

-- =====================================================
-- 10. Offer记录 (对应录用的申请)
-- =====================================================
INSERT INTO internship_svc.internship_offer (id, application_id, student_id, enterprise_id, job_id, salary, start_date, end_date, status, college_audit) VALUES
(1, 1, 1, 4, 1, 250, '2025-03-10', '2025-08-31', 1, 1), -- 王小明@字节Java: 已接受+高校审核通过
(2, 4, 2, 5, 3, 300, '2025-03-15', '2025-09-30', 1, 1), -- 李小红@阿里算法: 已接受+高校审核通过
(3, 5, 3, 4, 2, 220, '2025-03-10', '2025-08-31', 1, 1), -- 张小强@字节前端: 已接受+高校审核通过
(4, 6, 4, 6, 5, 250, '2025-03-20', '2025-08-31', 1, 1), -- 陈小芳@腾讯后台: 已接受+高校审核通过
(5, 7, 5, 6, 5, 250, '2025-04-01', '2025-08-31', 0, 0); -- 刘小伟@腾讯后台: 待确认

SELECT setval('internship_svc.internship_offer_id_seq', 5);

-- =====================================================
-- 11. 实习记录 (4人正在实习中)
-- mentor_id: 9=字节导师, 12=阿里导师
-- teacher_id: 4=清华辅导员, 6=北大辅导员
-- =====================================================
INSERT INTO internship_svc.internship_record (id, student_id, enterprise_id, job_id, mentor_id, teacher_id, start_date, end_date, status) VALUES
(1, 1, 4, 1, 9,    4, '2025-03-10', NULL,         1), -- 王小明@字节 实习中
(2, 2, 5, 3, 12,   4, '2025-03-15', NULL,         1), -- 李小红@阿里 实习中
(3, 3, 4, 2, 9,    4, '2025-03-10', NULL,         1), -- 张小强@字节 实习中
(4, 4, 6, 5, NULL, 6, '2025-03-20', NULL,         1); -- 陈小芳@腾讯 实习中(暂无导师)

SELECT setval('internship_svc.internship_record_id_seq', 4);

-- =====================================================
-- 12. 周报 (每人2周)
-- =====================================================
INSERT INTO internship_svc.weekly_report (id, internship_id, student_id, week_start, week_end, content, work_hours, status, review_comment, reviewed_by, reviewed_at) VALUES
-- 王小明 (internship_id=1)
(1, 1, 1, '2025-03-10', '2025-03-16', '本周完成了项目环境搭建，熟悉了团队代码规范，参与了两个接口的开发。', 40.0, 2, '适应很快，代码质量不错，继续保持。', 9, '2025-03-17 10:00:00+08'),
(2, 1, 1, '2025-03-17', '2025-03-23', '本周独立完成了用户信息查询接口，并修复了3个线上bug，参与了代码review。', 42.5, 2, '独立解决问题的能力很强，值得表扬。', 9, '2025-03-24 09:30:00+08'),
-- 李小红 (internship_id=2)
(3, 2, 2, '2025-03-17', '2025-03-23', '本周学习了推荐系统基础架构，完成了数据预处理模块的部分代码。', 40.0, 2, '学习能力强，对算法理解到位。', 12, '2025-03-24 14:00:00+08'),
(4, 2, 2, '2025-03-24', '2025-03-30', '本周完成了特征工程模块，跑通了第一个推荐模型的训练流程。', 45.0, 1, NULL, NULL, NULL),
-- 张小强 (internship_id=3)
(5, 3, 3, '2025-03-10', '2025-03-16', '本周熟悉了前端项目架构，完成了两个页面组件的开发。', 40.0, 2, '组件封装思路清晰，代码可复用性好。', 9, '2025-03-17 11:00:00+08'),
(6, 3, 3, '2025-03-17', '2025-03-23', '本周完成了首页改版需求，优化了页面加载性能，减少了30%的首屏时间。', 42.0, 1, NULL, NULL, NULL),
-- 陈小芳 (internship_id=4)
(7, 4, 4, '2025-03-24', '2025-03-30', '本周完成了微信后台接口文档阅读，搭建了本地开发环境，完成了第一个简单接口。', 40.0, 1, NULL, NULL, NULL);

SELECT setval('internship_svc.weekly_report_id_seq', 7);

-- =====================================================
-- 13. 考勤记录 (近期打卡数据)
-- =====================================================
INSERT INTO internship_svc.attendance (id, internship_id, student_id, clock_in_time, clock_out_time, clock_in_lat, clock_in_lng, status, audited_by) VALUES
-- 王小明 (字节, 北京)
(1,  1, 1, '2025-03-24 09:02:00+08', '2025-03-24 18:35:00+08', 40.0000000, 116.3200000, 1, 9),
(2,  1, 1, '2025-03-25 08:58:00+08', '2025-03-25 19:10:00+08', 40.0000000, 116.3200000, 1, 9),
(3,  1, 1, '2025-03-26 09:15:00+08', '2025-03-26 18:20:00+08', 40.0000000, 116.3200000, 1, 9),
-- 李小红 (阿里, 杭州)
(4,  2, 2, '2025-03-24 09:05:00+08', '2025-03-24 18:30:00+08', 30.2741000, 120.1551000, 1, 12),
(5,  2, 2, '2025-03-25 09:00:00+08', '2025-03-25 18:45:00+08', 30.2741000, 120.1551000, 1, 12),
-- 张小强 (字节, 北京)
(6,  3, 3, '2025-03-24 09:30:00+08', '2025-03-24 18:00:00+08', 40.0000000, 116.3200000, 1, 9),
(7,  3, 3, '2025-03-25 09:10:00+08', '2025-03-25 18:30:00+08', 40.0000000, 116.3200000, 1, 9),
-- 陈小芳 (腾讯, 深圳) - 有一条异常
(8,  4, 4, '2025-03-24 09:00:00+08', '2025-03-24 18:00:00+08', 22.5431000, 113.9400000, 1, NULL),
(9,  4, 4, '2025-03-25 10:30:00+08', '2025-03-25 18:00:00+08', 22.5431000, 113.9400000, 2, NULL); -- 迟到异常

SELECT setval('internship_svc.attendance_id_seq', 9);

-- =====================================================
-- 14. 实训项目
-- =====================================================
INSERT INTO training_svc.training_project (id, enterprise_id, project_name, description, tech_stack, industry, max_teams, max_members, start_date, end_date, audit_status, status) VALUES
(1, 4, '字节跳动推荐系统实训', '基于真实业务场景的推荐算法实训项目，学生将参与内容推荐系统的设计与实现', '["Python","TensorFlow","Spark","Hive"]', '互联网', 5, 4, '2025-04-01', '2025-06-30', 1, 1),
(2, 5, '阿里云微服务架构实训', '基于阿里云平台的微服务架构设计与实践，涵盖Spring Cloud Alibaba全套组件', '["Java","Spring Cloud","Nacos","Sentinel"]', '云计算', 8, 5, '2025-04-15', '2025-07-15', 1, 1),
(3, 6, '腾讯小程序开发实训', '微信小程序从零到一的完整开发实训，包含前后端联调', '["JavaScript","微信小程序","Node.js","MySQL"]', '互联网', 6, 4, '2025-05-01', '2025-07-31', 0, 1); -- 待审核

SELECT setval('training_svc.training_project_id_seq', 3);

-- =====================================================
-- 15. 实训排期计划 (高校安排学生参与实训)
-- =====================================================
INSERT INTO training_svc.training_plan (id, tenant_id, project_id, plan_name, start_date, end_date, teacher_id, status) VALUES
(1, 2, 1, '清华2025春季推荐系统实训', '2025-04-01', '2025-06-30', 3, 2), -- 清华 进行中 (teacher=tsinghua_dean)
(2, 2, 2, '清华2025春季微服务实训',   '2025-04-15', '2025-07-15', 3, 1), -- 清华 计划中
(3, 3, 1, '北大2025春季推荐系统实训', '2025-04-01', '2025-06-30', 6, 2); -- 北大 进行中 (teacher=pku_counselor)

SELECT setval('training_svc.training_plan_id_seq', 3);

-- =====================================================
-- 16. 评价记录
-- =====================================================
INSERT INTO growth_svc.evaluation_record (id, student_id, evaluator_id, source_type, ref_type, ref_id, scores, comment, hire_recommendation) VALUES
-- 企业导师评价 (字节导师9 评价 王小明1 和 张小强3)
(1, 1, 9,  'enterprise', 'internship', 1, '{"technical":88,"attitude":92,"communication":85,"innovation":80}', '王小明同学技术基础扎实，学习能力强，能快速融入团队，代码质量较高，建议录用。', 'strongly_recommend'),
(2, 3, 9,  'enterprise', 'internship', 3, '{"technical":85,"attitude":90,"communication":88,"innovation":82}', '张小强同学前端技术熟练，对性能优化有独到见解，团队协作能力强。', 'recommend'),
-- 企业导师评价 (阿里导师12 评价 李小红2)
(3, 2, 12, 'enterprise', 'internship', 2, '{"technical":90,"attitude":88,"communication":82,"innovation":91}', '李小红同学算法理解深刻，代码实现能力强，在推荐系统方向有很大潜力。', 'strongly_recommend'),
-- 高校辅导员评价 (清华辅导员4 评价 王小明1)
(4, 1, 4,  'school',     'internship', 1, '{"professional":85,"attitude":90,"growth":88}', '该同学实习期间表现优秀，按时提交周报，与企业导师沟通顺畅，综合表现良好。', 'recommend');

SELECT setval('growth_svc.evaluation_record_id_seq', 4);

-- =====================================================
-- 17. 徽章/证书
-- =====================================================
INSERT INTO growth_svc.growth_badge (id, student_id, type, name, issue_date, image_url, blockchain_hash) VALUES
(1, 1, 'certificate', '字节跳动实习证明',         '2025-03-31', 'https://oss.example.com/cert/bytedance_intern_001.png', 'sha256:a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456'),
(2, 2, 'badge',       '算法工程师潜力奖',          '2025-03-28', 'https://oss.example.com/badge/algo_potential.png',       NULL),
(3, 1, 'badge',       '优秀实习生',                '2025-03-25', 'https://oss.example.com/badge/excellent_intern.png',     NULL),
(4, 3, 'badge',       '前端性能优化达人',          '2025-03-24', 'https://oss.example.com/badge/frontend_perf.png',        NULL);

SELECT setval('growth_svc.growth_badge_id_seq', 4);

-- =====================================================
-- 18. 预警记录 (陈小芳考勤异常)
-- =====================================================
INSERT INTO growth_svc.warning_record (id, tenant_id, student_id, warning_type, warning_level, description, status) VALUES
(1, 3, 4, 'attendance', 1, '学生陈小芳于2025-03-25出现迟到打卡异常，请关注。', 0); -- 待处理

SELECT setval('growth_svc.warning_record_id_seq', 1);

-- =====================================================
-- 验证查询
-- =====================================================
SELECT '=== 数据验证 ===' AS info;
SELECT 'sys_tenant'          AS tbl, COUNT(*) AS cnt FROM auth_center.sys_tenant          WHERE is_deleted=FALSE
UNION ALL
SELECT 'sys_user',                   COUNT(*)         FROM auth_center.sys_user            WHERE is_deleted=FALSE
UNION ALL
SELECT 'college_info',               COUNT(*)         FROM college_svc.college_info        WHERE is_deleted=FALSE
UNION ALL
SELECT 'organization',               COUNT(*)         FROM college_svc.organization        WHERE is_deleted=FALSE
UNION ALL
SELECT 'student_info',               COUNT(*)         FROM student_svc.student_info        WHERE is_deleted=FALSE
UNION ALL
SELECT 'enterprise_info',            COUNT(*)         FROM enterprise_svc.enterprise_info  WHERE is_deleted=FALSE
UNION ALL
SELECT 'enterprise_staff',           COUNT(*)         FROM enterprise_svc.enterprise_staff WHERE is_deleted=FALSE
UNION ALL
SELECT 'internship_job',             COUNT(*)         FROM internship_svc.internship_job   WHERE is_deleted=FALSE
UNION ALL
SELECT 'job_application',            COUNT(*)         FROM internship_svc.job_application
UNION ALL
SELECT 'internship_offer',           COUNT(*)         FROM internship_svc.internship_offer
UNION ALL
SELECT 'internship_record',          COUNT(*)         FROM internship_svc.internship_record
UNION ALL
SELECT 'weekly_report',              COUNT(*)         FROM internship_svc.weekly_report
UNION ALL
SELECT 'attendance',                 COUNT(*)         FROM internship_svc.attendance
UNION ALL
SELECT 'training_project',           COUNT(*)         FROM training_svc.training_project   WHERE is_deleted=FALSE
UNION ALL
SELECT 'training_plan',              COUNT(*)         FROM training_svc.training_plan      WHERE is_deleted=FALSE
UNION ALL
SELECT 'evaluation_record',          COUNT(*)         FROM growth_svc.evaluation_record    WHERE is_deleted=FALSE
UNION ALL
SELECT 'growth_badge',               COUNT(*)         FROM growth_svc.growth_badge         WHERE is_deleted=FALSE
UNION ALL
SELECT 'warning_record',             COUNT(*)         FROM growth_svc.warning_record       WHERE is_deleted=FALSE;
