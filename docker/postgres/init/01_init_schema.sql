-- 数据库初始化脚本
-- 数据库: PostgreSQL 15+
-- 字符集: UTF8

-- 1. 创建 Schemas
CREATE SCHEMA IF NOT EXISTS auth_center;
CREATE SCHEMA IF NOT EXISTS student_service;
CREATE SCHEMA IF NOT EXISTS college_service;
CREATE SCHEMA IF NOT EXISTS enterprise_service;
CREATE SCHEMA IF NOT EXISTS project_service;
CREATE SCHEMA IF NOT EXISTS internship_service;
CREATE SCHEMA IF NOT EXISTS platform_service;

-- 2. 通用函数：自动更新 updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 3. 建表

-- ==========================================
-- Schema: auth_center (认证与基础中心)
-- ==========================================

-- sys_tenant (租户/机构表)
CREATE TABLE IF NOT EXISTS auth_center.sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '机构名称',
    type SMALLINT NOT NULL DEFAULT 1 COMMENT '类型 (1:高校, 2:企业, 0:平台运营)',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态 (0:待审核, 1:正常, 2:禁用)',
    config JSONB COMMENT '租户配置 (Logo, 主题色, 审核配置)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE auth_center.sys_tenant IS '租户/机构表';
COMMENT ON COLUMN auth_center.sys_tenant.name IS '机构名称';
COMMENT ON COLUMN auth_center.sys_tenant.type IS '类型 (1:高校, 2:企业, 0:平台运营)';

-- sys_user (系统用户表)
CREATE TABLE IF NOT EXISTS auth_center.sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role_ids BIGINT[] COMMENT '角色ID列表',
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:正常, 2:锁定)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE auth_center.sys_user IS '系统用户表';
CREATE INDEX idx_sys_user_username ON auth_center.sys_user(username);
CREATE INDEX idx_sys_user_phone ON auth_center.sys_user(phone);
CREATE INDEX idx_sys_user_tenant ON auth_center.sys_user(tenant_id);

-- sys_role (角色表)
CREATE TABLE IF NOT EXISTS auth_center.sys_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0 COMMENT '归属租户 (0代表系统通用角色)',
    code VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    permissions TEXT[] COMMENT '权限编码列表',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE auth_center.sys_role IS '角色表';

-- ==========================================
-- Schema: student_service (学生服务)
-- ==========================================

-- student_profile (学生档案)
CREATE TABLE IF NOT EXISTS student_service.student_profile (
    id BIGINT PRIMARY KEY COMMENT '关联 sys_user.id',
    student_no VARCHAR(50) NOT NULL COMMENT '学号',
    major_id BIGINT,
    class_id BIGINT,
    skills TEXT[] COMMENT '技能标签',
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:在读, 2:实习中, 3:已毕业)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE student_service.student_profile IS '学生档案';

-- career_dna (能力画像/雷达图)
CREATE TABLE IF NOT EXISTS student_service.career_dna (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    dimensions JSONB COMMENT '维度得分',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE student_service.career_dna IS '能力画像';

-- resume (在线简历)
CREATE TABLE IF NOT EXISTS student_service.resume (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    version_name VARCHAR(50),
    content JSONB COMMENT '完整简历数据',
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE student_service.resume IS '在线简历';

-- ==========================================
-- Schema: college_service (院校服务)
-- ==========================================

-- major (专业) - 补充定义
CREATE TABLE IF NOT EXISTS college_service.major (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE college_service.major IS '专业';

-- class (班级) - 补充定义
CREATE TABLE IF NOT EXISTS college_service.class (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    major_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE college_service.class IS '班级';

-- teacher_profile (教师/辅导员档案)
CREATE TABLE IF NOT EXISTS college_service.teacher_profile (
    id BIGINT PRIMARY KEY COMMENT '关联 sys_user.id',
    title VARCHAR(50) COMMENT '职称',
    department_id BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE college_service.teacher_profile IS '教师档案';

-- ==========================================
-- Schema: enterprise_service (企业服务)
-- ==========================================

-- enterprise_profile (企业信息)
CREATE TABLE IF NOT EXISTS enterprise_service.enterprise_profile (
    id BIGINT PRIMARY KEY COMMENT '关联 sys_tenant.id',
    industry VARCHAR(50),
    scale VARCHAR(20),
    license_url VARCHAR(255),
    verified_status SMALLINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE enterprise_service.enterprise_profile IS '企业信息';

-- job_post (实习/就业岗位)
CREATE TABLE IF NOT EXISTS enterprise_service.job_post (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    type SMALLINT COMMENT '类型 (1:实习, 2:校招)',
    salary_range JSONB COMMENT '薪资范围',
    requirements TEXT,
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:招聘中, 0:关闭)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE enterprise_service.job_post IS '岗位信息';

-- ==========================================
-- Schema: project_service (实训项目服务)
-- ==========================================

-- project (实训项目库)
CREATE TABLE IF NOT EXISTS project_service.project (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    tech_stack TEXT[] COMMENT '技术栈',
    difficulty SMALLINT COMMENT '难度 (1-5)',
    resource_url VARCHAR(255),
    max_teams INT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE project_service.project IS '实训项目库';

-- project_team (实训小组)
CREATE TABLE IF NOT EXISTS project_service.project_team (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    leader_id BIGINT,
    mentor_id BIGINT,
    teacher_id BIGINT,
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:组队中, 2:进行中, 3:已结项)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE project_service.project_team IS '实训小组';

-- project_task (Scrum任务)
CREATE TABLE IF NOT EXISTS project_service.project_task (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    assignee_id BIGINT,
    status VARCHAR(20) DEFAULT 'todo' COMMENT 'todo, doing, done',
    priority SMALLINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE project_service.project_task IS '项目任务';

-- ==========================================
-- Schema: internship_service (实习过程服务)
-- ==========================================

-- internship_application (投递记录)
CREATE TABLE IF NOT EXISTS internship_service.internship_application (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:已投递, 2:面试中, 3:录用, 4:拒绝)',
    resume_snapshot JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE internship_service.internship_application IS '投递记录';

-- internship_process (实习履历)
CREATE TABLE IF NOT EXISTS internship_service.internship_process (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    mentor_id BIGINT,
    teacher_id BIGINT,
    start_date DATE,
    status SMALLINT DEFAULT 1 COMMENT '状态 (1:实习中, 2:转正, 3:离职)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE internship_service.internship_process IS '实习履历';

-- weekly_report (周报)
CREATE TABLE IF NOT EXISTS internship_service.weekly_report (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL,
    week_num INT NOT NULL,
    content JSONB,
    mood_score SMALLINT,
    mentor_comment TEXT,
    teacher_comment TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE internship_service.weekly_report IS '周报';

-- attendance (考勤)
CREATE TABLE IF NOT EXISTS internship_service.attendance (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL,
    check_in_time TIMESTAMPTZ,
    location JSONB,
    type SMALLINT COMMENT '类型 (1:上班, 2:下班, 3:外勤)',
    status SMALLINT COMMENT '状态 (1:正常, 2:异常/迟到)',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE internship_service.attendance IS '考勤';

-- ==========================================
-- Schema: platform_service (平台与公共服务)
-- ==========================================

-- sys_dict (数据字典)
CREATE TABLE IF NOT EXISTS platform_service.sys_dict (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL COMMENT '字典分类',
    code VARCHAR(50) NOT NULL COMMENT '字典键',
    label VARCHAR(100) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE platform_service.sys_dict IS '数据字典';

-- audit_log (审计日志)
CREATE TABLE IF NOT EXISTS platform_service.audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    target VARCHAR(100),
    ip_address VARCHAR(50),
    detail JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
COMMENT ON TABLE platform_service.audit_log IS '审计日志';

-- 4. 注册自动更新触发器 (Apply Trigger to All Tables)
-- 这是一个示例，通常需要为每个表单独创建触发器

DO $$
DECLARE
    t text;
BEGIN
    FOR t IN
        SELECT table_schema || '.' || table_name
        FROM information_schema.tables
        WHERE table_schema IN ('auth_center', 'student_service', 'college_service', 'enterprise_service', 'project_service', 'internship_service', 'platform_service')
          AND table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS set_timestamp ON %s', t);
        EXECUTE format('CREATE TRIGGER set_timestamp BEFORE UPDATE ON %s FOR EACH ROW EXECUTE PROCEDURE update_timestamp()', t);
    END LOOP;
END;
$$ language 'plpgsql';
