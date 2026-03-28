/*
 智图云平台 - 测试数据迁移脚本
 版本: v1.4.0
 日期: 2026-03-22
 说明: 为系统中数据较少或为空的表补充测试数据
*/

-- ============================================
-- 1. college_svc - 校企合作关系数据
-- ============================================

-- 添加校企合作关系
INSERT INTO "college_svc"."enterprise_relationship" 
("college_tenant_id", "enterprise_tenant_id", "cooperation_level", "status", "created_at", "updated_at", "is_deleted")
VALUES
(2, 4, 3, 1, '2024-09-01 10:00:00+08', '2024-09-01 10:00:00+08', false), -- 清华-字节跳动(战略)
(2, 5, 2, 1, '2024-09-15 10:00:00+08', '2024-09-15 10:00:00+08', false), -- 清华-阿里巴巴(重点)
(2, 6, 2, 1, '2024-10-01 10:00:00+08', '2024-10-01 10:00:00+08', false), -- 清华-腾讯(重点)
(3, 4, 2, 1, '2024-09-10 10:00:00+08', '2024-09-10 10:00:00+08', false), -- 北大-字节跳动(重点)
(3, 5, 3, 1, '2024-08-20 10:00:00+08', '2024-08-20 10:00:00+08', false), -- 北大-阿里巴巴(战略)
(3, 6, 2, 1, '2024-09-25 10:00:00+08', '2024-09-25 10:00:00+08', false); -- 北大-腾讯(重点)

-- 添加企业走访记录
INSERT INTO "college_svc"."enterprise_visit"
("college_tenant_id", "enterprise_tenant_id", "visit_date", "visitor_id", "visitor_name", "purpose", "outcome", "next_action", "created_at", "is_deleted")
VALUES
(2, 4, '2024-10-15', 3, '李院长', '洽谈2025年实习合作计划', '企业表示愿意提供30个实习岗位,并可安排技术专家进行校园宣讲', '11月组织企业宣讲会', '2024-10-15 16:00:00+08', false),
(2, 5, '2024-11-05', 3, '李院长', '商讨联合实训项目', '达成共识,将在春季学期开展微服务架构实训项目', '12月确定项目细节和导师安排', '2024-11-05 15:30:00+08', false),
(2, 6, '2024-12-10', 2, '张主任', '了解学生实习情况', '企业对实习生表现满意,希望继续深化合作', '下学期增加实习名额', '2024-12-10 14:00:00+08', false),
(3, 4, '2024-10-20', 6, '王辅导员', '回访实习学生', '学生适应良好,企业导师反馈积极', '定期跟进学生实习进展', '2024-10-20 10:30:00+08', false),
(3, 5, '2024-11-12', 5, '陈主任', '探讨校企联合培养模式', '企业愿意参与课程设计,提供真实项目案例', '1月召开校企联合会议', '2024-11-12 14:30:00+08', false),
(2, 4, '2025-01-08', 3, '李院长', '春季实习岗位对接', '确认春季实习岗位20个,涵盖前后端和算法方向', '2月组织学生面试', '2025-01-08 10:00:00+08', false),
(3, 6, '2025-01-15', 5, '陈主任', '实习基地建设讨论', '企业同意作为学校实习基地,提供长期实习机会', '签订实习基地协议', '2025-01-15 15:00:00+08', false),
(2, 5, '2025-02-20', 2, '张主任', '实训项目中期检查', '项目进展顺利,学生学习积极性高', '3月组织项目答辩', '2025-02-20 14:00:00+08', false);


-- 添加实习巡查记录
INSERT INTO "college_svc"."internship_inspection"
("college_tenant_id", "internship_id", "inspector_id", "inspection_date", "location", "findings", "issues", "recommendations", "created_at")
VALUES
(2, 1, 4, '2025-03-20', '字节跳动北京总部', '学生王小明工作认真负责,已独立完成多个开发任务,团队协作能力强', '无明显问题', '建议继续保持,可适当增加技术难度', '2025-03-20 16:00:00+08'),
(2, 3, 4, '2025-03-20', '字节跳动北京总部', '学生张小强前端开发能力突出,页面优化效果显著', '偶尔加班较晚,需注意劳逸结合', '提醒学生注意身体健康', '2025-03-20 17:00:00+08'),
(2, 2, 4, '2025-03-22', '阿里巴巴杭州园区', '学生李小红算法基础扎实,学习能力强,导师评价很高', '无', '鼓励学生多参与技术分享', '2025-03-22 15:30:00+08'),
(3, 4, 6, '2025-03-25', '腾讯深圳总部', '学生陈小芳适应能力强,但技术深度还需提升', '部分技术细节理解不够深入', '建议导师加强技术指导', '2025-03-25 14:00:00+08'),
(2, 1, 3, '2025-04-10', '字节跳动北京总部(线上)', '学生实习进展良好,已参与核心业务开发', '无', '继续跟进实习进度', '2025-04-10 10:00:00+08'),
(2, 2, 3, '2025-04-12', '阿里巴巴杭州园区(线上)', '学生在推荐算法方向有突出表现,企业有留用意向', '无', '提前做好就业指导', '2025-04-12 11:00:00+08');

-- 添加企业审核记录
INSERT INTO "college_svc"."enterprise_audit"
("enterprise_tenant_id", "audit_type", "status", "auditor_id", "audit_comment", "audited_at", "created_at")
VALUES
(4, 'qualification', 1, 2, '企业资质齐全,营业执照、组织机构代码等证件真实有效,同意合作', '2024-08-15 10:00:00+08', '2024-08-10 09:00:00+08'),
(5, 'qualification', 1, 5, '企业规模大,信誉良好,具备接收实习生的完善体系,审核通过', '2024-08-12 14:00:00+08', '2024-08-08 10:00:00+08'),
(6, 'qualification', 1, 2, '企业实力雄厚,实习岗位规范,导师配备完善,同意建立合作关系', '2024-08-20 11:00:00+08', '2024-08-16 09:30:00+08');

-- ============================================
-- 2. enterprise_svc - 企业动态和待办
-- ============================================

-- 添加企业动态
INSERT INTO "enterprise_svc"."enterprise_activity"
("tenant_id", "activity_type", "description", "ref_type", "ref_id", "created_at")
VALUES
(4, 'job_published', '发布了新的实习岗位: Java后端开发实习生', 'job', 1, '2025-02-20 10:00:00+08'),
(4, 'job_published', '发布了新的实习岗位: 前端开发实习生', 'job', 2, '2025-02-20 10:30:00+08'),
(5, 'job_published', '发布了新的实习岗位: 算法工程师实习生', 'job', 3, '2025-02-22 09:00:00+08'),
(5, 'job_published', '发布了新的实习岗位: 产品经理实习生', 'job', 4, '2025-03-01 10:00:00+08'),
(6, 'job_published', '发布了新的实习岗位: 后台开发实习生', 'job', 5, '2025-02-25 14:00:00+08'),
(4, 'application_received', '收到学生王小明的求职申请', 'application', 1, '2025-02-25 15:30:00+08'),
(4, 'interview_scheduled', '安排了与学生王小明的面试', 'interview', 1, '2025-03-01 10:00:00+08'),
(4, 'offer_sent', '向学生王小明发送了Offer', 'offer', 1, '2025-03-05 16:00:00+08'),
(5, 'application_received', '收到学生李小红的求职申请', 'application', 4, '2025-02-28 14:00:00+08'),
(5, 'offer_sent', '向学生李小红发送了Offer', 'offer', 2, '2025-03-08 15:00:00+08'),
(4, 'internship_started', '学生王小明开始实习', 'internship', 1, '2025-03-10 09:00:00+08'),
(5, 'internship_started', '学生李小红开始实习', 'internship', 2, '2025-03-15 09:00:00+08'),
(4, 'talent_collected', 'HR张三将学生王小明加入人才库', 'talent', 1, '2025-03-20 10:00:00+08'),
(6, 'interview_scheduled', '安排了与学生刘小伟的面试', 'interview', 5, '2025-03-25 14:00:00+08'),
(5, 'evaluation_submitted', '导师对学生李小红提交了实习评价', 'evaluation', 3, '2025-03-26 16:00:00+08');


-- 添加企业待办事项
INSERT INTO "enterprise_svc"."enterprise_todo"
("tenant_id", "user_id", "todo_type", "ref_type", "ref_id", "title", "priority", "due_date", "status", "created_at")
VALUES
(4, 8, 'application_review', 'application', 1, '审核学生王小明的求职申请', 2, '2025-02-26 18:00:00+08', 1, '2025-02-25 15:30:00+08'),
(4, 8, 'interview_arrange', 'application', 1, '安排学生王小明的面试', 1, '2025-03-02 18:00:00+08', 1, '2025-02-26 10:00:00+08'),
(4, 9, 'interview_conduct', 'interview', 1, '面试学生王小明', 1, '2025-03-03 10:00:00+08', 1, '2025-03-01 10:00:00+08'),
(4, 8, 'offer_prepare', 'application', 1, '准备学生王小明的Offer', 2, '2025-03-06 18:00:00+08', 1, '2025-03-03 15:00:00+08'),
(5, 11, 'application_review', 'application', 4, '审核学生李小红的求职申请', 2, '2025-03-01 18:00:00+08', 1, '2025-02-28 14:00:00+08'),
(5, 12, 'interview_conduct', 'interview', 2, '面试学生李小红', 1, '2025-03-05 14:00:00+08', 1, '2025-03-02 10:00:00+08'),
(6, 14, 'application_review', 'application', 7, '审核学生刘小伟的求职申请', 2, '2025-03-23 18:00:00+08', 0, '2025-03-22 16:00:00+08'),
(6, 14, 'interview_arrange', 'application', 7, '安排学生刘小伟的面试', 1, '2025-03-26 18:00:00+08', 0, '2025-03-23 10:00:00+08'),
(4, 9, 'weekly_report_review', 'report', 1, '批阅学生王小明的实习周报', 2, '2025-03-18 18:00:00+08', 1, '2025-03-17 09:00:00+08'),
(4, 9, 'weekly_report_review', 'report', 2, '批阅学生王小明的实习周报', 2, '2025-03-25 18:00:00+08', 1, '2025-03-24 09:00:00+08'),
(5, 12, 'evaluation_submit', 'internship', 2, '提交学生李小红的实习评价', 1, '2025-03-30 18:00:00+08', 0, '2025-03-25 10:00:00+08'),
(4, 8, 'talent_pool_review', NULL, NULL, '整理和更新人才库信息', 3, '2025-04-05 18:00:00+08', 0, '2025-03-20 14:00:00+08');

-- 添加面试安排
INSERT INTO "enterprise_svc"."interview_schedule"
("application_id", "student_id", "enterprise_id", "interview_time", "location", "interviewer_id", "interview_type", "status", "notes", "created_at", "updated_at")
VALUES
(1, 1, 4, '2025-03-03 10:00:00+08', '字节跳动北京总部 3号楼5层会议室', 9, '技术面试', 2, '面试表现优秀,技术基础扎实,沟通能力强', '2025-03-01 10:00:00+08', '2025-03-03 11:30:00+08'),
(4, 2, 5, '2025-03-05 14:00:00+08', '阿里巴巴杭州园区 A区3楼', 12, '技术面试', 2, '算法理解深刻,有很好的数学基础,推荐录用', '2025-03-02 10:00:00+08', '2025-03-05 16:00:00+08'),
(5, 3, 4, '2025-03-04 15:00:00+08', '字节跳动北京总部 线上面试', 9, '技术面试', 2, '前端技术扎实,有良好的代码规范,同意录用', '2025-03-02 14:00:00+08', '2025-03-04 16:30:00+08'),
(6, 4, 6, '2025-03-12 10:00:00+08', '腾讯深圳总部 T1大厦', NULL, '技术面试', 2, 'Go语言基础良好,学习能力强', '2025-03-10 09:00:00+08', '2025-03-12 11:30:00+08'),
(7, 5, 6, '2025-03-28 14:00:00+08', '腾讯深圳总部 线上面试', NULL, '技术面试', 1, '已安排面试,等待进行', '2025-03-25 10:00:00+08', '2025-03-25 10:00:00+08'),
(2, 1, 5, '2025-03-06 10:00:00+08', '阿里巴巴杭州园区 线上面试', 12, '技术面试', 2, '技术能力可以,但算法方向经验不足', '2025-03-04 14:00:00+08', '2025-03-06 11:30:00+08'),
(3, 2, 4, '2025-03-05 16:00:00+08', '字节跳动北京总部 线上面试', 9, '技术面试', 2, '前端基础一般,不太符合岗位要求', '2025-03-03 10:00:00+08', '2025-03-05 17:00:00+08'),
(1, 1, 4, '2025-03-04 14:00:00+08', '字节跳动北京总部 3号楼5层会议室', 7, 'HR面试', 2, '综合素质良好,沟通表达清晰,团队协作意识强', '2025-03-03 15:00:00+08', '2025-03-04 15:30:00+08');


-- 添加人才库数据
INSERT INTO "enterprise_svc"."talent_pool"
("tenant_id", "student_id", "collected_by", "remark", "created_at", "is_deleted")
VALUES
(4, 1, 8, '技术能力强,Java后端开发经验丰富,有留用意向', '2025-03-20 10:00:00+08', false),
(4, 3, 8, '前端开发能力突出,页面优化经验丰富,可考虑转正', '2025-03-20 11:00:00+08', false),
(5, 2, 11, '算法基础扎实,推荐系统方向有潜力,重点培养对象', '2025-03-22 14:00:00+08', false),
(6, 4, 14, 'Go语言开发能力不错,后台开发经验需加强', '2025-03-25 15:00:00+08', false),
(6, 5, 14, 'Android开发经验丰富,可关注后续发展', '2025-03-26 10:00:00+08', false),
(4, 2, 8, '算法方向有一定基础,但不太适合当前岗位', '2025-03-10 16:00:00+08', false),
(5, 1, 11, 'Java开发能力可以,但算法理解不够深入', '2025-03-12 11:00:00+08', false),
(5, 3, 11, '前端技术不错,可考虑前端相关岗位', '2025-03-15 14:00:00+08', false),
(4, 4, 8, '后台开发基础良好,可持续关注', '2025-03-18 10:00:00+08', false),
(4, 5, 8, '移动端开发经验丰富,有合作潜力', '2025-03-19 15:00:00+08', false);

-- ============================================
-- 3. internship_svc - 实习证明
-- ============================================

-- 添加实习证明
INSERT INTO "internship_svc"."internship_certificate"
("internship_id", "student_id", "enterprise_id", "cert_no", "cert_url", "issued_by", "issued_at")
VALUES
(1, 1, 4, 'BYTEDANCE-2025-001', 'https://oss.example.com/cert/bytedance_2025_001.pdf', 7, '2025-08-31 16:00:00+08'),
(2, 2, 5, 'ALIBABA-2025-001', 'https://oss.example.com/cert/alibaba_2025_001.pdf', 10, '2025-09-30 16:00:00+08'),
(3, 3, 4, 'BYTEDANCE-2025-002', 'https://oss.example.com/cert/bytedance_2025_002.pdf', 7, '2025-08-31 16:30:00+08'),
(4, 4, 6, 'TENCENT-2025-001', 'https://oss.example.com/cert/tencent_2025_001.pdf', 13, '2025-08-31 17:00:00+08');

-- ============================================
-- 4. platform_service - 平台基础数据
-- ============================================

-- 添加数据字典
INSERT INTO "platform_service"."sys_dict"
("category", "code", "label", "sort_order", "created_at", "updated_at", "is_deleted")
VALUES
-- 行业分类
('industry', 'internet', '互联网', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'finance', '金融', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'ecommerce', '电子商务', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'education', '教育', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'healthcare', '医疗健康', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'manufacturing', '制造业', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'logistics', '物流', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', 'gaming', '游戏', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 技术栈
('tech_stack', 'java', 'Java', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'python', 'Python', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'javascript', 'JavaScript', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'go', 'Go', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'cpp', 'C++', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'php', 'PHP', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech_stack', 'rust', 'Rust', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 岗位类型
('job_type', 'backend', '后端开发', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'frontend', '前端开发', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'fullstack', '全栈开发', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'algorithm', '算法工程师', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'mobile', '移动端开发', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'testing', '测试工程师', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'devops', '运维工程师', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'product', '产品经理', 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('job_type', 'design', 'UI/UX设计师', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 企业规模
('company_scale', '0-50', '50人以下', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('company_scale', '50-200', '50-200人', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('company_scale', '200-500', '200-500人', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('company_scale', '500-1000', '500-1000人', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('company_scale', '1000-5000', '1000-5000人', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('company_scale', '5000+', '5000人以上', 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);


-- 添加系统标签
INSERT INTO "platform_service"."sys_tag"
("category", "name", "parent_id", "sort_order", "usage_count", "created_at", "updated_at", "is_deleted")
VALUES
-- 技术标签
('tech', '编程语言', NULL, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Java', 1, 1, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Python', 1, 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'JavaScript', 1, 3, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Go', 1, 4, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'C++', 1, 5, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', '框架', NULL, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Spring Boot', 7, 1, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Django', 7, 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Vue.js', 7, 3, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'React', 7, 4, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Flask', 7, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', '数据库', NULL, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'MySQL', 13, 1, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'PostgreSQL', 13, 2, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Redis', 13, 3, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'MongoDB', 13, 4, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', '中间件', NULL, 4, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Kafka', 18, 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'RabbitMQ', 18, 2, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('tech', 'Elasticsearch', 18, 3, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 技能标签
('skill', '软技能', NULL, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '沟通能力', 22, 1, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '团队协作', 22, 2, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '问题解决', 22, 3, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '学习能力', 22, 4, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '时间管理', 22, 5, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('skill', '领导力', 22, 6, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 项目标签
('project', '项目类型', NULL, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', 'Web应用', 29, 1, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', '移动应用', 29, 2, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', '数据分析', 29, 3, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', '机器学习', 29, 4, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', '微服务', 29, 5, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('project', '大数据', 29, 6, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
-- 行业标签
('industry', '互联网', NULL, 1, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', '金融科技', NULL, 2, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', '电子商务', NULL, 3, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', '人工智能', NULL, 4, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', '云计算', NULL, 5, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('industry', '物联网', NULL, 6, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);


-- 添加技能树数据
INSERT INTO "platform_service"."skill_tree"
("skill_name", "skill_category", "parent_id", "level", "description", "sort_order", "created_at", "is_deleted")
VALUES
-- 技术技能
('编程基础', 'technical', NULL, 1, '编程语言和基础算法', 1, CURRENT_TIMESTAMP, false),
('Java编程', 'technical', 1, 2, 'Java语言基础和面向对象编程', 1, CURRENT_TIMESTAMP, false),
('Python编程', 'technical', 1, 2, 'Python语言基础和常用库', 2, CURRENT_TIMESTAMP, false),
('数据结构与算法', 'technical', 1, 2, '常用数据结构和算法设计', 3, CURRENT_TIMESTAMP, false),
('Web开发', 'technical', NULL, 1, 'Web应用开发技术', 2, CURRENT_TIMESTAMP, false),
('前端开发', 'technical', 5, 2, 'HTML/CSS/JavaScript前端技术', 1, CURRENT_TIMESTAMP, false),
('后端开发', 'technical', 5, 2, '服务端开发和API设计', 2, CURRENT_TIMESTAMP, false),
('数据库技术', 'technical', 5, 2, 'SQL和NoSQL数据库', 3, CURRENT_TIMESTAMP, false),
('框架应用', 'technical', NULL, 1, '主流开发框架', 3, CURRENT_TIMESTAMP, false),
('Spring生态', 'technical', 9, 2, 'Spring Boot/Cloud等框架', 1, CURRENT_TIMESTAMP, false),
('前端框架', 'technical', 9, 2, 'Vue/React/Angular框架', 2, CURRENT_TIMESTAMP, false),
('微服务架构', 'technical', 9, 2, '微服务设计和实践', 3, CURRENT_TIMESTAMP, false),
-- 软技能
('沟通协作', 'soft_skill', NULL, 1, '团队沟通和协作能力', 1, CURRENT_TIMESTAMP, false),
('有效沟通', 'soft_skill', 13, 2, '清晰表达和倾听理解', 1, CURRENT_TIMESTAMP, false),
('团队合作', 'soft_skill', 13, 2, '团队协作和冲突解决', 2, CURRENT_TIMESTAMP, false),
('跨部门协作', 'soft_skill', 13, 2, '跨团队沟通和协调', 3, CURRENT_TIMESTAMP, false),
('问题解决', 'soft_skill', NULL, 1, '分析和解决问题的能力', 2, CURRENT_TIMESTAMP, false),
('逻辑思维', 'soft_skill', 17, 2, '逻辑分析和推理能力', 1, CURRENT_TIMESTAMP, false),
('创新思维', 'soft_skill', 17, 2, '创造性思考和解决方案', 2, CURRENT_TIMESTAMP, false),
('决策能力', 'soft_skill', 17, 2, '快速决策和风险评估', 3, CURRENT_TIMESTAMP, false),
-- 领域知识
('业务理解', 'domain_knowledge', NULL, 1, '行业和业务知识', 1, CURRENT_TIMESTAMP, false),
('互联网产品', 'domain_knowledge', 21, 2, '互联网产品设计和运营', 1, CURRENT_TIMESTAMP, false),
('金融业务', 'domain_knowledge', 21, 2, '金融行业知识和规则', 2, CURRENT_TIMESTAMP, false),
('电商运营', 'domain_knowledge', 21, 2, '电商平台运营和管理', 3, CURRENT_TIMESTAMP, false),
('项目管理', 'domain_knowledge', NULL, 1, '项目管理方法和工具', 2, CURRENT_TIMESTAMP, false);

-- 添加合同模板
INSERT INTO "platform_service"."contract_template"
("template_name", "description", "contract_type", "content", "variables", "legal_terms", "usage_count", "created_at", "updated_at", "is_deleted")
VALUES
('实习协议标准模板', '适用于一般实习岗位的标准实习协议', 'internship', 
'实习协议

甲方(实习单位): {{enterprise_name}}
乙方(实习学生): {{student_name}}
丙方(学校): {{college_name}}

根据国家有关法律法规,经三方协商一致,就乙方到甲方实习事宜达成如下协议:

一、实习期限
实习期限自{{start_date}}至{{end_date}},共计{{duration}}个月。

二、实习岗位
乙方实习岗位为: {{job_title}}
工作地点: {{work_location}}

三、实习待遇
实习补贴: {{salary}}元/月
其他福利: {{benefits}}

四、工作时间
每周工作{{work_hours}}小时,具体工作时间由甲方安排。

五、权利义务
(详细条款内容...)',
'{"enterprise_name":"企业名称","student_name":"学生姓名","college_name":"学校名称","start_date":"开始日期","end_date":"结束日期","duration":"实习时长","job_title":"岗位名称","work_location":"工作地点","salary":"实习工资","benefits":"福利待遇","work_hours":"工作时长"}',
'1. 本协议一式三份,甲乙丙三方各执一份;
2. 本协议自三方签字盖章之日起生效;
3. 未尽事宜,三方协商解决。',
0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),

('保密协议模板', '实习生保密协议模板', 'confidentiality',
'保密协议

甲方: {{enterprise_name}}
乙方: {{student_name}}

鉴于乙方在甲方实习期间可能接触到甲方的商业秘密和机密信息,为保护甲方的合法权益,双方达成如下保密协议:

一、保密范围
1. 技术信息: {{tech_info}}
2. 商业信息: {{business_info}}
3. 其他机密信息

二、保密期限
保密期限自签订之日起至{{confidentiality_period}}年。

三、违约责任
(详细条款内容...)',
'{"enterprise_name":"企业名称","student_name":"学生姓名","tech_info":"技术信息范围","business_info":"商业信息范围","confidentiality_period":"保密期限"}',
'1. 本协议具有法律效力;
2. 违反保密义务将承担法律责任。',
0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);


-- 添加证书模板
INSERT INTO "platform_service"."certificate_template"
("template_name", "description", "layout_config", "background_url", "signature_urls", "variables", "usage_count", "created_at", "updated_at", "is_deleted")
VALUES
('实习证明标准模板', '企业实习证明标准模板', 
'{"title":{"x":200,"y":100,"fontSize":32,"fontWeight":"bold"},"content":{"x":100,"y":200,"fontSize":16,"lineHeight":30},"signature":{"x":400,"y":500,"width":150,"height":80}}',
'https://oss.example.com/template/internship_cert_bg.png',
'{"enterprise":"https://oss.example.com/signature/enterprise_seal.png","issuer":"https://oss.example.com/signature/issuer_sign.png"}',
'{"student_name":"学生姓名","student_no":"学号","enterprise_name":"企业名称","job_title":"实习岗位","start_date":"开始日期","end_date":"结束日期","performance":"实习表现","issue_date":"颁发日期","cert_no":"证书编号"}',
0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),

('优秀实习生证书模板', '优秀实习生荣誉证书模板',
'{"title":{"x":200,"y":80,"fontSize":36,"fontWeight":"bold","color":"#d4af37"},"content":{"x":100,"y":180,"fontSize":18,"lineHeight":35},"badge":{"x":50,"y":50,"width":100,"height":100}}',
'https://oss.example.com/template/excellent_intern_bg.png',
'{"enterprise":"https://oss.example.com/signature/enterprise_seal_gold.png","issuer":"https://oss.example.com/signature/ceo_sign.png"}',
'{"student_name":"学生姓名","enterprise_name":"企业名称","honor_title":"荣誉称号","achievement":"主要成就","issue_date":"颁发日期"}',
0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- 添加推荐横幅
INSERT INTO "platform_service"."recommendation_banner"
("title", "image_url", "link_url", "target_portal", "start_date", "end_date", "sort_order", "status", "created_at", "updated_at", "is_deleted")
VALUES
('字节跳动春季实习招聘', 'https://oss.example.com/banner/bytedance_spring_2025.jpg', '/jobs?enterprise=4', 'student', '2025-02-01', '2025-04-30', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('阿里巴巴算法实训营', 'https://oss.example.com/banner/alibaba_training_2025.jpg', '/training?project=1', 'student', '2025-03-01', '2025-05-31', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('腾讯微信小程序开发实训', 'https://oss.example.com/banner/tencent_miniprogram.jpg', '/training?project=3', 'student', '2025-04-01', '2025-06-30', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('清华大学优质生源推荐', 'https://oss.example.com/banner/tsinghua_talent.jpg', '/students?college=2', 'enterprise', '2025-01-01', '2025-12-31', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('北京大学校企合作', 'https://oss.example.com/banner/pku_cooperation.jpg', '/cooperation', 'enterprise', '2025-01-01', '2025-12-31', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
('实习管理系统升级通知', 'https://oss.example.com/banner/system_upgrade.jpg', '/announcement/upgrade', 'all', '2025-03-15', '2025-03-31', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- 添加推荐榜单
INSERT INTO "platform_service"."recommendation_top_list"
("list_type", "item_ids", "updated_at")
VALUES
('mentor', '[9,12,14,7,10,13,8,11,6,4]', CURRENT_TIMESTAMP),
('course', '[1,2,3]', CURRENT_TIMESTAMP),
('project', '[1,2,3]', CURRENT_TIMESTAMP);

-- 添加操作日志样例
INSERT INTO "platform_service"."operation_log"
("user_id", "user_name", "tenant_id", "module", "operation", "request_params", "response_status", "result", "ip_address", "user_agent", "execution_time", "created_at")
VALUES
(15, 'student01', 2, 'job', '查看岗位详情', '{"job_id":1}', 200, 'success', '192.168.1.100', 'Mozilla/5.0', 45, '2025-02-25 10:30:00+08'),
(15, 'student01', 2, 'job', '提交求职申请', '{"job_id":1,"resume_url":"..."}', 200, 'success', '192.168.1.100', 'Mozilla/5.0', 120, '2025-02-25 15:30:00+08'),
(8, 'bytedance_hr', 4, 'application', '审核求职申请', '{"application_id":1,"status":"approved"}', 200, 'success', '10.0.1.50', 'Mozilla/5.0', 80, '2025-02-26 10:00:00+08'),
(9, 'bytedance_mentor', 4, 'interview', '安排面试', '{"application_id":1,"interview_time":"2025-03-03 10:00:00"}', 200, 'success', '10.0.1.51', 'Mozilla/5.0', 95, '2025-03-01 10:00:00+08'),
(15, 'student01', 2, 'offer', '接受Offer', '{"offer_id":1}', 200, 'success', '192.168.1.100', 'Mozilla/5.0', 60, '2025-03-06 09:00:00+08'),
(15, 'student01', 2, 'report', '提交周报', '{"internship_id":1,"week_start":"2025-03-10"}', 200, 'success', '192.168.1.100', 'Mozilla/5.0', 150, '2025-03-16 18:00:00+08'),
(9, 'bytedance_mentor', 4, 'report', '批阅周报', '{"report_id":1,"comment":"..."}', 200, 'success', '10.0.1.51', 'Mozilla/5.0', 200, '2025-03-17 10:00:00+08'),
(1, 'admin', 1, 'system', '导出数据', '{"type":"internship","format":"excel"}', 200, 'success', '10.0.0.1', 'Mozilla/5.0', 3500, '2025-03-20 14:00:00+08');


-- 添加安全日志样例
INSERT INTO "platform_service"."security_log"
("level", "event_type", "user_id", "ip_address", "description", "details", "created_at")
VALUES
('info', 'login_success', 15, '192.168.1.100', '用户student01登录成功', '{"browser":"Chrome","os":"Windows 10"}', '2025-03-22 08:30:00+08'),
('info', 'login_success', 8, '10.0.1.50', '用户bytedance_hr登录成功', '{"browser":"Chrome","os":"macOS"}', '2025-03-22 09:00:00+08'),
('warning', 'login_failed', NULL, '192.168.1.200', '登录失败: 用户名或密码错误', '{"username":"test_user","attempts":3}', '2025-03-22 10:15:00+08'),
('warning', 'password_change', 15, '192.168.1.100', '用户student01修改密码', '{"old_password_hash":"...","new_password_hash":"..."}', '2025-03-22 11:00:00+08'),
('critical', 'unauthorized_access', NULL, '203.0.113.50', '未授权访问尝试', '{"url":"/api/admin/users","method":"GET"}', '2025-03-22 12:30:00+08'),
('info', 'logout', 15, '192.168.1.100', '用户student01退出登录', '{"session_duration":"4h30m"}', '2025-03-22 13:00:00+08'),
('warning', 'token_expired', 16, '192.168.1.101', '用户token过期', '{"token_type":"refresh_token"}', '2025-03-22 14:00:00+08'),
('info', 'permission_granted', 8, '10.0.1.50', '授予用户权限', '{"permission":"interview_schedule","resource_id":1}', '2025-03-22 15:00:00+08');

-- ============================================
-- 5. student_svc - 学生能力和任务数据
-- ============================================

-- 添加学生能力雷达图数据
INSERT INTO "student_svc"."student_capability"
("student_id", "dimension", "score", "updated_at")
VALUES
-- 学生1: 王小明
(1, 'technical_skill', 85, CURRENT_TIMESTAMP),
(1, 'communication', 78, CURRENT_TIMESTAMP),
(1, 'teamwork', 82, CURRENT_TIMESTAMP),
(1, 'innovation', 88, CURRENT_TIMESTAMP),
(1, 'problem_solving', 80, CURRENT_TIMESTAMP),
-- 学生2: 李小红
(2, 'technical_skill', 90, CURRENT_TIMESTAMP),
(2, 'communication', 75, CURRENT_TIMESTAMP),
(2, 'teamwork', 80, CURRENT_TIMESTAMP),
(2, 'innovation', 92, CURRENT_TIMESTAMP),
(2, 'problem_solving', 88, CURRENT_TIMESTAMP),
-- 学生3: 张小强
(3, 'technical_skill', 82, CURRENT_TIMESTAMP),
(3, 'communication', 85, CURRENT_TIMESTAMP),
(3, 'teamwork', 88, CURRENT_TIMESTAMP),
(3, 'innovation', 80, CURRENT_TIMESTAMP),
(3, 'problem_solving', 78, CURRENT_TIMESTAMP);

-- 添加学生个性化推荐
INSERT INTO "student_svc"."student_recommendation"
("student_id", "rec_type", "ref_id", "score", "reason", "created_at")
VALUES
(1, 'job', 1, 92.50, '技能匹配度高,Java后端开发经验丰富,与岗位要求高度契合', '2025-02-20 10:00:00+08'),
(1, 'job', 5, 85.30, 'Go语言有一定基础,后台开发方向可以尝试', '2025-02-20 10:00:00+08'),
(1, 'project', 2, 88.00, '微服务架构项目适合你的技术栈', '2025-02-20 10:00:00+08'),
(2, 'job', 3, 95.80, '算法基础扎实,推荐系统方向非常适合', '2025-02-22 09:00:00+08'),
(2, 'job', 4, 78.50, '产品经理岗位可以拓展职业方向', '2025-02-22 09:00:00+08'),
(2, 'project', 1, 93.00, '推荐系统实训项目与你的兴趣方向一致', '2025-02-22 09:00:00+08'),
(3, 'job', 2, 91.20, '前端开发技能突出,Vue和React经验丰富', '2025-02-23 10:00:00+08'),
(3, 'job', 1, 82.00, 'Java后端也可以尝试,全栈发展', '2025-02-23 10:00:00+08'),
(3, 'project', 3, 85.50, '小程序开发项目可以提升前端技能', '2025-02-23 10:00:00+08'),
(4, 'job', 5, 89.70, 'Go语言开发能力强,后台开发方向合适', '2025-02-25 09:00:00+08'),
(4, 'job', 1, 75.00, 'Java后端可以作为备选方向', '2025-02-25 09:00:00+08'),
(4, 'project', 2, 80.00, '微服务架构项目可以学习分布式系统', '2025-02-25 09:00:00+08'),
(5, 'job', 5, 87.50, 'Android开发经验丰富,后台开发可以拓展', '2025-02-26 10:00:00+08'),
(5, 'job', 2, 80.00, '前端开发可以与移动端结合', '2025-02-26 10:00:00+08'),
(5, 'project', 3, 88.00, '小程序开发项目适合移动端开发者', '2025-02-26 10:00:00+08');


-- 添加学生任务数据
INSERT INTO "student_svc"."student_task"
("student_id", "task_type", "ref_id", "title", "description", "priority", "status", "due_date", "created_at", "updated_at", "is_deleted")
VALUES
(1, 'internship', 1, '提交第3周实习周报', '总结本周工作内容,包括完成的任务、遇到的问题和下周计划', 1, 0, '2025-03-30 18:00:00+08', '2025-03-24 09:00:00+08', '2025-03-24 09:00:00+08', false),
(1, 'internship', 1, '完成导师布置的代码review任务', '审查团队成员提交的代码,提出改进建议', 2, 0, '2025-03-28 18:00:00+08', '2025-03-25 10:00:00+08', '2025-03-25 10:00:00+08', false),
(1, 'evaluation', 1, '完成企业导师评价', '对导师的指导进行评价反馈', 3, 0, '2025-04-05 18:00:00+08', '2025-03-20 14:00:00+08', '2025-03-20 14:00:00+08', false),
(2, 'internship', 2, '提交第2周实习周报', '记录本周学习内容和项目进展', 1, 1, '2025-03-30 18:00:00+08', '2025-03-24 09:00:00+08', '2025-03-30 17:00:00+08', false),
(2, 'internship', 2, '完成推荐模型训练任务', '使用新的特征集训练推荐模型并评估效果', 1, 0, '2025-04-02 18:00:00+08', '2025-03-26 10:00:00+08', '2025-03-26 10:00:00+08', false),
(2, 'training', 1, '参加推荐系统实训项目', '按时参加实训课程,完成项目任务', 2, 0, '2025-06-30 18:00:00+08', '2025-04-01 09:00:00+08', '2025-04-01 09:00:00+08', false),
(3, 'internship', 3, '提交第3周实习周报', '总结前端开发工作和页面优化成果', 1, 0, '2025-03-30 18:00:00+08', '2025-03-24 09:00:00+08', '2025-03-24 09:00:00+08', false),
(3, 'internship', 3, '完成首页性能优化', '优化首页加载速度,目标减少40%加载时间', 1, 0, '2025-04-05 18:00:00+08', '2025-03-25 14:00:00+08', '2025-03-25 14:00:00+08', false),
(4, 'internship', 4, '提交第1周实习周报', '记录第一周的学习和适应情况', 1, 1, '2025-03-30 18:00:00+08', '2025-03-24 09:00:00+08', '2025-03-30 16:00:00+08', false),
(4, 'internship', 4, '学习微信后台架构文档', '熟悉微信后台系统架构和开发规范', 2, 0, '2025-04-01 18:00:00+08', '2025-03-25 10:00:00+08', '2025-03-25 10:00:00+08', false),
(5, 'internship', NULL, '准备面试', '准备腾讯后台开发岗位的技术面试', 1, 0, '2025-03-28 10:00:00+08', '2025-03-25 14:00:00+08', '2025-03-25 14:00:00+08', false),
(5, 'training', 3, '了解小程序开发实训项目', '查看项目详情,评估是否参加', 3, 0, '2025-04-10 18:00:00+08', '2025-03-26 10:00:00+08', '2025-03-26 10:00:00+08', false),
(1, 'internship', 1, '参加团队技术分享会', '准备分享主题: Spring Boot最佳实践', 2, 0, '2025-04-08 15:00:00+08', '2025-03-28 10:00:00+08', '2025-03-28 10:00:00+08', false),
(2, 'evaluation', 2, '完成实习中期自评', '总结实习前半程的收获和不足', 2, 0, '2025-04-15 18:00:00+08', '2025-03-28 14:00:00+08', '2025-03-28 14:00:00+08', false),
(3, 'internship', 3, '学习React性能优化技巧', '深入学习React性能优化方法', 3, 0, '2025-04-10 18:00:00+08', '2025-03-28 15:00:00+08', '2025-03-28 15:00:00+08', false);

-- ============================================
-- 6. training_svc - 实训项目报名和任务
-- ============================================

-- 添加项目报名数据
INSERT INTO "training_svc"."project_enrollment"
("project_id", "student_id", "team_id", "role", "status", "enrolled_at")
VALUES
(1, 1, 1, 'member', 1, '2025-03-25 10:00:00+08'),
(1, 2, 1, 'leader', 1, '2025-03-25 10:30:00+08'),
(1, 3, 2, 'member', 1, '2025-03-25 11:00:00+08'),
(1, 4, 2, 'member', 1, '2025-03-25 11:30:00+08'),
(2, 1, 3, 'leader', 1, '2025-04-10 09:00:00+08'),
(2, 3, 3, 'member', 1, '2025-04-10 09:30:00+08'),
(2, 5, 4, 'member', 1, '2025-04-10 10:00:00+08'),
(3, 3, 5, 'leader', 1, '2025-04-20 14:00:00+08'),
(3, 5, 5, 'member', 1, '2025-04-20 14:30:00+08'),
(3, 4, 6, 'member', 1, '2025-04-20 15:00:00+08'),
(1, 5, 2, 'member', 2, '2025-03-25 12:00:00+08'),
(2, 2, 4, 'member', 2, '2025-04-10 10:30:00+08');


-- 添加项目任务数据
INSERT INTO "training_svc"."project_task"
("project_id", "team_id", "title", "description", "assignee_id", "status", "priority", "story_points", "created_at", "updated_at", "is_deleted")
VALUES
-- 推荐系统项目任务
(1, 1, '需求分析和系统设计', '分析推荐系统需求,设计整体架构', 16, 'done', 1, 5, '2025-04-01 10:00:00+08', '2025-04-05 16:00:00+08', false),
(1, 1, '数据采集模块开发', '开发用户行为数据采集接口', 15, 'done', 1, 8, '2025-04-05 10:00:00+08', '2025-04-12 17:00:00+08', false),
(1, 1, '特征工程实现', '实现用户和物品特征提取', 16, 'in_progress', 1, 13, '2025-04-12 10:00:00+08', '2025-04-20 14:00:00+08', false),
(1, 1, '推荐算法模型训练', '训练协同过滤和深度学习模型', 16, 'todo', 1, 21, '2025-04-20 10:00:00+08', '2025-04-20 10:00:00+08', false),
(1, 2, '需求分析和系统设计', '分析推荐系统需求,设计整体架构', 17, 'done', 1, 5, '2025-04-01 10:00:00+08', '2025-04-05 16:00:00+08', false),
(1, 2, '数据预处理模块', '清洗和预处理原始数据', 18, 'done', 2, 8, '2025-04-05 10:00:00+08', '2025-04-10 17:00:00+08', false),
(1, 2, '推荐服务API开发', '开发推荐结果查询API', 17, 'in_progress', 1, 13, '2025-04-10 10:00:00+08', '2025-04-18 14:00:00+08', false),
(1, 2, '性能优化和测试', '优化推荐系统性能,进行压力测试', 18, 'todo', 2, 8, '2025-04-25 10:00:00+08', '2025-04-25 10:00:00+08', false),
-- 微服务架构项目任务
(2, 3, '微服务架构设计', '设计微服务拆分方案和服务边界', 15, 'done', 1, 8, '2025-04-15 10:00:00+08', '2025-04-20 16:00:00+08', false),
(2, 3, '用户服务开发', '开发用户管理微服务', 15, 'in_progress', 1, 13, '2025-04-20 10:00:00+08', '2025-04-25 14:00:00+08', false),
(2, 3, '订单服务开发', '开发订单管理微服务', 17, 'in_progress', 1, 13, '2025-04-20 10:00:00+08', '2025-04-25 14:00:00+08', false),
(2, 3, '服务网关配置', '配置Spring Cloud Gateway', 15, 'todo', 2, 5, '2025-04-28 10:00:00+08', '2025-04-28 10:00:00+08', false),
(2, 4, '商品服务开发', '开发商品管理微服务', 19, 'in_progress', 1, 13, '2025-04-20 10:00:00+08', '2025-04-25 14:00:00+08', false),
(2, 4, '支付服务开发', '开发支付处理微服务', 16, 'todo', 1, 13, '2025-04-25 10:00:00+08', '2025-04-25 10:00:00+08', false),
(2, 4, '服务监控和日志', '集成Prometheus和ELK', 19, 'todo', 2, 8, '2025-05-01 10:00:00+08', '2025-05-01 10:00:00+08', false),
-- 小程序开发项目任务
(3, 5, '小程序UI设计', '设计小程序界面和交互流程', 17, 'done', 1, 5, '2025-05-01 10:00:00+08', '2025-05-08 16:00:00+08', false),
(3, 5, '前端页面开发', '开发小程序前端页面', 17, 'in_progress', 1, 13, '2025-05-08 10:00:00+08', '2025-05-15 14:00:00+08', false),
(3, 5, '后端API开发', '开发小程序后端接口', 19, 'in_progress', 1, 13, '2025-05-08 10:00:00+08', '2025-05-15 14:00:00+08', false),
(3, 5, '前后端联调', '完成前后端接口联调', 17, 'todo', 1, 8, '2025-05-20 10:00:00+08', '2025-05-20 10:00:00+08', false),
(3, 6, '小程序功能测试', '进行功能测试和bug修复', 18, 'todo', 2, 8, '2025-05-25 10:00:00+08', '2025-05-25 10:00:00+08', false);

-- ============================================
-- 7. 更新序列值
-- ============================================

-- 更新各表的序列值,确保后续插入不会冲突
SELECT setval('"college_svc"."enterprise_relationship_id_seq"', (SELECT MAX(id) FROM "college_svc"."enterprise_relationship"), true);
SELECT setval('"college_svc"."enterprise_visit_id_seq"', (SELECT MAX(id) FROM "college_svc"."enterprise_visit"), true);
SELECT setval('"college_svc"."internship_inspection_id_seq"', (SELECT MAX(id) FROM "college_svc"."internship_inspection"), true);
SELECT setval('"college_svc"."enterprise_audit_id_seq"', (SELECT MAX(id) FROM "college_svc"."enterprise_audit"), true);

SELECT setval('"enterprise_svc"."enterprise_activity_id_seq"', (SELECT MAX(id) FROM "enterprise_svc"."enterprise_activity"), true);
SELECT setval('"enterprise_svc"."enterprise_todo_id_seq"', (SELECT MAX(id) FROM "enterprise_svc"."enterprise_todo"), true);
SELECT setval('"enterprise_svc"."interview_schedule_id_seq"', (SELECT MAX(id) FROM "enterprise_svc"."interview_schedule"), true);
SELECT setval('"enterprise_svc"."talent_pool_id_seq"', (SELECT MAX(id) FROM "enterprise_svc"."talent_pool"), true);

SELECT setval('"internship_svc"."internship_certificate_id_seq"', (SELECT MAX(id) FROM "internship_svc"."internship_certificate"), true);

SELECT setval('"platform_service"."sys_dict_id_seq"', (SELECT MAX(id) FROM "platform_service"."sys_dict"), true);
SELECT setval('"platform_service"."sys_tag_id_seq"', (SELECT MAX(id) FROM "platform_service"."sys_tag"), true);
SELECT setval('"platform_service"."skill_tree_id_seq"', (SELECT MAX(id) FROM "platform_service"."skill_tree"), true);
SELECT setval('"platform_service"."contract_template_id_seq"', (SELECT MAX(id) FROM "platform_service"."contract_template"), true);
SELECT setval('"platform_service"."certificate_template_id_seq"', (SELECT MAX(id) FROM "platform_service"."certificate_template"), true);
SELECT setval('"platform_service"."recommendation_banner_id_seq"', (SELECT MAX(id) FROM "platform_service"."recommendation_banner"), true);
SELECT setval('"platform_service"."recommendation_top_list_id_seq"', (SELECT MAX(id) FROM "platform_service"."recommendation_top_list"), true);
SELECT setval('"platform_service"."operation_log_id_seq"', (SELECT MAX(id) FROM "platform_service"."operation_log"), true);
SELECT setval('"platform_service"."security_log_id_seq"', (SELECT MAX(id) FROM "platform_service"."security_log"), true);

SELECT setval('"student_svc"."student_capability_id_seq"', (SELECT MAX(id) FROM "student_svc"."student_capability"), true);
SELECT setval('"student_svc"."student_recommendation_id_seq"', (SELECT MAX(id) FROM "student_svc"."student_recommendation"), true);
SELECT setval('"student_svc"."student_task_id_seq"', (SELECT MAX(id) FROM "student_svc"."student_task"), true);

SELECT setval('"training_svc"."project_enrollment_id_seq"', (SELECT MAX(id) FROM "training_svc"."project_enrollment"), true);
SELECT setval('"training_svc"."project_task_id_seq"', (SELECT MAX(id) FROM "training_svc"."project_task"), true);

-- 迁移完成
