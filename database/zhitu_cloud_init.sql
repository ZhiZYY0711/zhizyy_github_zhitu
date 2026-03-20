-- =====================================================
-- 智图 (Zhitu) 实习管理平台
-- 数据库初始化脚本 (合并版，适用于 Navicat 直接执行)
-- 数据库: zhitu_cloud
-- PostgreSQL 15+
-- =====================================================

-- =====================================================
-- 1. auth_center: 认证与租户管理
-- =====================================================

CREATE SCHEMA IF NOT EXISTS auth_center;

CREATE TABLE auth_center.sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type SMALLINT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    config TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_tenant_type CHECK (type IN (0, 1, 2)),
    CONSTRAINT chk_tenant_status CHECK (status IN (0, 1, 2))
);
CREATE INDEX idx_tenant_type ON auth_center.sys_tenant(type) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant_status ON auth_center.sys_tenant(status) WHERE is_deleted = FALSE;
COMMENT ON TABLE auth_center.sys_tenant IS '租户/机构表';
COMMENT ON COLUMN auth_center.sys_tenant.type IS '0=平台运营 1=高校 2=企业';
COMMENT ON COLUMN auth_center.sys_tenant.status IS '0=待审核 1=正常 2=禁用';
COMMENT ON COLUMN auth_center.sys_tenant.config IS '租户配置(JSON格式)';

CREATE TABLE auth_center.sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    sub_role VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_user_role CHECK (role IN ('student', 'enterprise', 'college', 'platform')),
    CONSTRAINT chk_user_status CHECK (status IN (1, 2, 3))
);
CREATE INDEX idx_user_tenant ON auth_center.sys_user(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_user_role ON auth_center.sys_user(role) WHERE is_deleted = FALSE;
CREATE INDEX idx_user_phone ON auth_center.sys_user(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_user_username ON auth_center.sys_user(username) WHERE is_deleted = FALSE;
COMMENT ON TABLE auth_center.sys_user IS '系统用户表';
COMMENT ON COLUMN auth_center.sys_user.tenant_id IS '所属租户ID，平台管理员为0';
COMMENT ON COLUMN auth_center.sys_user.role IS 'student=学生 enterprise=企业 college=高校 platform=平台';
COMMENT ON COLUMN auth_center.sys_user.sub_role IS 'hr/mentor/admin(企业) counselor/dean/admin(高校)';
COMMENT ON COLUMN auth_center.sys_user.status IS '1=正常 2=锁定 3=注销';

CREATE TABLE auth_center.sys_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id) ON DELETE CASCADE
);
CREATE INDEX idx_token_user ON auth_center.sys_refresh_token(user_id);
CREATE INDEX idx_token_expires ON auth_center.sys_refresh_token(expires_at);
CREATE INDEX idx_token_hash ON auth_center.sys_refresh_token(token_hash);
COMMENT ON TABLE auth_center.sys_refresh_token IS 'Refresh Token表';
COMMENT ON COLUMN auth_center.sys_refresh_token.token_hash IS 'refresh_token的SHA-256哈希';

-- =====================================================
-- 2. platform_service: 平台公共服务
-- =====================================================

CREATE SCHEMA IF NOT EXISTS platform_service;

CREATE TABLE platform_service.sys_dict (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_dict_category_code UNIQUE (category, code)
);
CREATE INDEX idx_dict_category ON platform_service.sys_dict(category) WHERE is_deleted = FALSE;
COMMENT ON TABLE platform_service.sys_dict IS '数据字典表';
COMMENT ON COLUMN platform_service.sys_dict.category IS '分类: industry/tech_stack/job_type等';

-- =====================================================
-- 3. student_svc: 学生档案
-- =====================================================

CREATE SCHEMA IF NOT EXISTS student_svc;

CREATE TABLE student_svc.student_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    student_no VARCHAR(50) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    gender SMALLINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(255),
    college_id BIGINT,
    major_id BIGINT,
    class_id BIGINT,
    grade VARCHAR(20),
    enrollment_date DATE,
    graduation_date DATE,
    resume_url VARCHAR(255),
    skills TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT fk_student_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_student_gender CHECK (gender IN (1, 2))
);
CREATE INDEX idx_student_user ON student_svc.student_info(user_id);
CREATE INDEX idx_student_tenant ON student_svc.student_info(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_no ON student_svc.student_info(student_no) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_class ON student_svc.student_info(class_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_major ON student_svc.student_info(major_id) WHERE is_deleted = FALSE;
COMMENT ON TABLE student_svc.student_info IS '学生档案表';
COMMENT ON COLUMN student_svc.student_info.user_id IS '关联auth_center.sys_user.id (1:1)';
COMMENT ON COLUMN student_svc.student_info.skills IS '技能标签(JSON数组)';
COMMENT ON COLUMN student_svc.student_info.gender IS '1=男 2=女';

-- =====================================================
-- 4. college_svc: 院校管理
-- =====================================================

CREATE SCHEMA IF NOT EXISTS college_svc;

CREATE TABLE college_svc.college_info (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    college_code VARCHAR(50),
    province VARCHAR(50),
    city VARCHAR(50),
    address VARCHAR(255),
    logo_url VARCHAR(255),
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    cooperation_level SMALLINT DEFAULT 1,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_college_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_college_cooperation CHECK (cooperation_level IN (1, 2, 3)),
    CONSTRAINT chk_college_status CHECK (status IN (0, 1))
);
CREATE INDEX idx_college_tenant ON college_svc.college_info(tenant_id);
CREATE INDEX idx_college_city ON college_svc.college_info(city) WHERE is_deleted = FALSE;
COMMENT ON TABLE college_svc.college_info IS '高校信息表';
COMMENT ON COLUMN college_svc.college_info.cooperation_level IS '1=普通 2=重点 3=战略';
COMMENT ON COLUMN college_svc.college_info.status IS '1=正常 0=禁用';

CREATE TABLE college_svc.organization (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT,
    org_type SMALLINT NOT NULL,
    org_name VARCHAR(100) NOT NULL,
    org_code VARCHAR(50),
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_org_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_org_parent FOREIGN KEY (parent_id) REFERENCES college_svc.organization(id),
    CONSTRAINT chk_org_type CHECK (org_type IN (1, 2, 3))
);
CREATE INDEX idx_org_tenant ON college_svc.organization(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_org_parent ON college_svc.organization(parent_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_org_type ON college_svc.organization(org_type) WHERE is_deleted = FALSE;
COMMENT ON TABLE college_svc.organization IS '学院/专业/班级组织树';
COMMENT ON COLUMN college_svc.organization.org_type IS '1=学院 2=专业 3=班级';

-- =====================================================
-- 5. enterprise_svc: 企业管理
-- =====================================================

CREATE SCHEMA IF NOT EXISTS enterprise_svc;

CREATE TABLE enterprise_svc.enterprise_info (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    enterprise_name VARCHAR(100) NOT NULL,
    enterprise_code VARCHAR(50),
    industry VARCHAR(50),
    scale VARCHAR(20),
    province VARCHAR(50),
    city VARCHAR(50),
    address VARCHAR(255),
    logo_url VARCHAR(255),
    website VARCHAR(255),
    description TEXT,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    audit_status SMALLINT NOT NULL DEFAULT 0,
    audit_remark VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_enterprise_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_enterprise_audit CHECK (audit_status IN (0, 1, 2)),
    CONSTRAINT chk_enterprise_status CHECK (status IN (0, 1))
);
CREATE INDEX idx_enterprise_tenant ON enterprise_svc.enterprise_info(tenant_id);
CREATE INDEX idx_enterprise_audit ON enterprise_svc.enterprise_info(audit_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_enterprise_city ON enterprise_svc.enterprise_info(city) WHERE is_deleted = FALSE;
CREATE INDEX idx_enterprise_industry ON enterprise_svc.enterprise_info(industry) WHERE is_deleted = FALSE;
COMMENT ON TABLE enterprise_svc.enterprise_info IS '企业信息表';
COMMENT ON COLUMN enterprise_svc.enterprise_info.audit_status IS '0=待审核 1=通过 2=拒绝';

CREATE TABLE enterprise_svc.enterprise_staff (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    department VARCHAR(50),
    position VARCHAR(50),
    is_mentor BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_staff_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id)
);
CREATE INDEX idx_staff_tenant ON enterprise_svc.enterprise_staff(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_staff_user ON enterprise_svc.enterprise_staff(user_id);
CREATE INDEX idx_staff_mentor ON enterprise_svc.enterprise_staff(is_mentor) WHERE is_mentor = TRUE AND is_deleted = FALSE;
COMMENT ON TABLE enterprise_svc.enterprise_staff IS '企业员工表';
COMMENT ON COLUMN enterprise_svc.enterprise_staff.user_id IS '关联sys_user.id (1:1)';

CREATE TABLE enterprise_svc.talent_pool (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    collected_by BIGINT NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_talent_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_talent_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_talent_collector FOREIGN KEY (collected_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT uk_talent_pool UNIQUE (tenant_id, student_id)
);
CREATE INDEX idx_talent_tenant ON enterprise_svc.talent_pool(tenant_id);
CREATE INDEX idx_talent_student ON enterprise_svc.talent_pool(student_id);
COMMENT ON TABLE enterprise_svc.talent_pool IS '企业人才库';

-- =====================================================
-- 6. internship_svc: 实习全流程管理
-- =====================================================

CREATE SCHEMA IF NOT EXISTS internship_svc;

CREATE TABLE internship_svc.internship_job (
    id BIGSERIAL PRIMARY KEY,
    enterprise_id BIGINT NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    job_type VARCHAR(20),
    description TEXT,
    requirements TEXT,
    tech_stack TEXT,
    industry VARCHAR(50),
    city VARCHAR(50),
    salary_min INTEGER,
    salary_max INTEGER,
    headcount INTEGER DEFAULT 1,
    start_date DATE,
    end_date DATE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_job_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_job_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_job_salary CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min)
);
CREATE INDEX idx_job_enterprise ON internship_svc.internship_job(enterprise_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_status ON internship_svc.internship_job(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_city ON internship_svc.internship_job(city) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_industry ON internship_svc.internship_job(industry) WHERE is_deleted = FALSE;
COMMENT ON TABLE internship_svc.internship_job IS '实习岗位表';
COMMENT ON COLUMN internship_svc.internship_job.tech_stack IS '技术栈(JSON数组)';
COMMENT ON COLUMN internship_svc.internship_job.status IS '1=招募中 0=已关闭';

CREATE TABLE internship_svc.job_application (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_url VARCHAR(255),
    cover_letter TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT fk_application_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_application_status CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT uk_application UNIQUE (job_id, student_id)
);
CREATE INDEX idx_application_job ON internship_svc.job_application(job_id);
CREATE INDEX idx_application_student ON internship_svc.job_application(student_id);
CREATE INDEX idx_application_status ON internship_svc.job_application(status);
COMMENT ON TABLE internship_svc.job_application IS '求职申请表';
COMMENT ON COLUMN internship_svc.job_application.status IS '0=待处理 1=面试 2=Offer 3=拒绝 4=录用';

CREATE TABLE internship_svc.internship_offer (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    salary INTEGER,
    start_date DATE,
    end_date DATE,
    status SMALLINT NOT NULL DEFAULT 0,
    college_audit SMALLINT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offer_application FOREIGN KEY (application_id) REFERENCES internship_svc.job_application(id),
    CONSTRAINT fk_offer_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_offer_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_offer_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT chk_offer_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_offer_audit CHECK (college_audit IN (0, 1, 2))
);
CREATE INDEX idx_offer_application ON internship_svc.internship_offer(application_id);
CREATE INDEX idx_offer_student ON internship_svc.internship_offer(student_id);
CREATE INDEX idx_offer_enterprise ON internship_svc.internship_offer(enterprise_id);
CREATE INDEX idx_offer_status ON internship_svc.internship_offer(status);
COMMENT ON TABLE internship_svc.internship_offer IS 'Offer表';
COMMENT ON COLUMN internship_svc.internship_offer.status IS '0=待确认 1=已接受 2=已拒绝';
COMMENT ON COLUMN internship_svc.internship_offer.college_audit IS '0=待审核 1=通过 2=拒绝';

CREATE TABLE internship_svc.internship_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    job_id BIGINT,
    mentor_id BIGINT,
    teacher_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_record_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_record_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT fk_record_mentor FOREIGN KEY (mentor_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT fk_record_teacher FOREIGN KEY (teacher_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_record_status CHECK (status IN (1, 2)),
    CONSTRAINT chk_record_dates CHECK (end_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_record_student ON internship_svc.internship_record(student_id);
CREATE INDEX idx_record_enterprise ON internship_svc.internship_record(enterprise_id);
CREATE INDEX idx_record_status ON internship_svc.internship_record(status);
CREATE INDEX idx_record_mentor ON internship_svc.internship_record(mentor_id);
CREATE INDEX idx_record_teacher ON internship_svc.internship_record(teacher_id);
COMMENT ON TABLE internship_svc.internship_record IS '实习记录表';
COMMENT ON COLUMN internship_svc.internship_record.status IS '1=实习中 2=已结束';
COMMENT ON COLUMN internship_svc.internship_record.mentor_id IS '企业导师ID';
COMMENT ON COLUMN internship_svc.internship_record.teacher_id IS '校内指导老师ID';

CREATE TABLE internship_svc.weekly_report (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    week_start DATE NOT NULL,
    week_end DATE NOT NULL,
    content TEXT,
    work_hours DECIMAL(5,2),
    status SMALLINT NOT NULL DEFAULT 0,
    review_comment TEXT,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_report_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_report_reviewer FOREIGN KEY (reviewed_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_report_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_report_dates CHECK (week_end >= week_start)
);
CREATE INDEX idx_report_internship ON internship_svc.weekly_report(internship_id);
CREATE INDEX idx_report_student ON internship_svc.weekly_report(student_id);
CREATE INDEX idx_report_status ON internship_svc.weekly_report(status);
CREATE INDEX idx_report_week ON internship_svc.weekly_report(week_start, week_end);
COMMENT ON TABLE internship_svc.weekly_report IS '实习周报表';
COMMENT ON COLUMN internship_svc.weekly_report.status IS '0=草稿 1=已提交 2=已批阅';

CREATE TABLE internship_svc.attendance (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    clock_in_time TIMESTAMPTZ,
    clock_out_time TIMESTAMPTZ,
    clock_in_lat DECIMAL(10,7),
    clock_in_lng DECIMAL(10,7),
    status SMALLINT NOT NULL DEFAULT 0,
    audit_remark VARCHAR(255),
    audited_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_attendance_auditor FOREIGN KEY (audited_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_attendance_status CHECK (status IN (0, 1, 2))
);
CREATE INDEX idx_attendance_internship ON internship_svc.attendance(internship_id);
CREATE INDEX idx_attendance_student ON internship_svc.attendance(student_id);
CREATE INDEX idx_attendance_date ON internship_svc.attendance(clock_in_time);
CREATE INDEX idx_attendance_status ON internship_svc.attendance(status);
COMMENT ON TABLE internship_svc.attendance IS '考勤记录表';
COMMENT ON COLUMN internship_svc.attendance.status IS '0=待审核 1=正常 2=异常';

CREATE TABLE internship_svc.internship_certificate (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    cert_no VARCHAR(50) NOT NULL UNIQUE,
    cert_url VARCHAR(255),
    issued_by BIGINT NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_cert_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_cert_issuer FOREIGN KEY (issued_by) REFERENCES auth_center.sys_user(id)
);
CREATE INDEX idx_cert_internship ON internship_svc.internship_certificate(internship_id);
CREATE INDEX idx_cert_student ON internship_svc.internship_certificate(student_id);
CREATE INDEX idx_cert_no ON internship_svc.internship_certificate(cert_no);
COMMENT ON TABLE internship_svc.internship_certificate IS '实习证明表';

-- =====================================================
-- 7. training_svc: 实训项目管理
-- =====================================================

CREATE SCHEMA IF NOT EXISTS training_svc;

CREATE TABLE training_svc.training_project (
    id BIGSERIAL PRIMARY KEY,
    enterprise_id BIGINT NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    description TEXT,
    tech_stack TEXT,
    industry VARCHAR(50),
    max_teams INTEGER DEFAULT 10,
    max_members INTEGER DEFAULT 6,
    start_date DATE,
    end_date DATE,
    audit_status SMALLINT NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_project_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_project_audit CHECK (audit_status IN (0, 1, 2)),
    CONSTRAINT chk_project_status CHECK (status IN (1, 2, 3)),
    CONSTRAINT chk_project_dates CHECK (end_date IS NULL OR end_date >= start_date)
);
CREATE INDEX idx_project_enterprise ON training_svc.training_project(enterprise_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_project_audit ON training_svc.training_project(audit_status) WHERE is_deleted = FALSE;
CREATE INDEX idx_project_status ON training_svc.training_project(status) WHERE is_deleted = FALSE;
COMMENT ON TABLE training_svc.training_project IS '实训项目表';
COMMENT ON COLUMN training_svc.training_project.audit_status IS '0=待审核 1=通过 2=拒绝';
COMMENT ON COLUMN training_svc.training_project.status IS '1=招募中 2=进行中 3=已结束';

CREATE TABLE training_svc.training_plan (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    teacher_id BIGINT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_plan_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_plan_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_plan_teacher FOREIGN KEY (teacher_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_plan_status CHECK (status IN (1, 2, 3)),
    CONSTRAINT chk_plan_dates CHECK (end_date >= start_date)
);
CREATE INDEX idx_plan_tenant ON training_svc.training_plan(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_plan_project ON training_svc.training_plan(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_plan_teacher ON training_svc.training_plan(teacher_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_plan_status ON training_svc.training_plan(status) WHERE is_deleted = FALSE;
COMMENT ON TABLE training_svc.training_plan IS '实训排期计划表';
COMMENT ON COLUMN training_svc.training_plan.status IS '1=计划中 2=进行中 3=已完成';

-- =====================================================
-- 8. growth_svc: 学生成长评价
-- =====================================================

CREATE SCHEMA IF NOT EXISTS growth_svc;

CREATE TABLE growth_svc.evaluation_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    evaluator_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    ref_type VARCHAR(20),
    ref_id BIGINT,
    scores TEXT,
    comment TEXT,
    hire_recommendation VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_eval_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_eval_source CHECK (source_type IN ('enterprise', 'school', 'peer')),
    CONSTRAINT chk_eval_ref CHECK (ref_type IN ('project', 'internship') OR ref_type IS NULL),
    CONSTRAINT chk_eval_hire CHECK (hire_recommendation IN ('strongly_recommend', 'recommend', 'not_recommend') OR hire_recommendation IS NULL)
);
CREATE INDEX idx_eval_student ON growth_svc.evaluation_record(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_evaluator ON growth_svc.evaluation_record(evaluator_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_source ON growth_svc.evaluation_record(source_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_ref ON growth_svc.evaluation_record(ref_type, ref_id) WHERE is_deleted = FALSE;
COMMENT ON TABLE growth_svc.evaluation_record IS '评价记录表';
COMMENT ON COLUMN growth_svc.evaluation_record.source_type IS 'enterprise=企业评价 school=学校评价 peer=同学互评';
COMMENT ON COLUMN growth_svc.evaluation_record.scores IS '评分JSON: {"technical":85,"attitude":90}';
COMMENT ON COLUMN growth_svc.evaluation_record.hire_recommendation IS 'strongly_recommend/recommend/not_recommend';

CREATE TABLE growth_svc.growth_badge (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    image_url VARCHAR(255),
    blockchain_hash VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_badge_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_badge_type CHECK (type IN ('certificate', 'badge'))
);
CREATE INDEX idx_badge_student ON growth_svc.growth_badge(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_badge_type ON growth_svc.growth_badge(type) WHERE is_deleted = FALSE;
CREATE INDEX idx_badge_date ON growth_svc.growth_badge(issue_date) WHERE is_deleted = FALSE;
COMMENT ON TABLE growth_svc.growth_badge IS '徽章/证书表';
COMMENT ON COLUMN growth_svc.growth_badge.type IS 'certificate=证书 badge=徽章';

CREATE TABLE growth_svc.warning_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    warning_type VARCHAR(20) NOT NULL,
    warning_level SMALLINT NOT NULL,
    description TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    intervene_note TEXT,
    intervened_by BIGINT,
    intervened_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_warning_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_warning_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_warning_intervener FOREIGN KEY (intervened_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_warning_type CHECK (warning_type IN ('attendance', 'report', 'evaluation')),
    CONSTRAINT chk_warning_level CHECK (warning_level IN (1, 2, 3)),
    CONSTRAINT chk_warning_status CHECK (status IN (0, 1, 2))
);
CREATE INDEX idx_warning_tenant ON growth_svc.warning_record(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_student ON growth_svc.warning_record(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_type ON growth_svc.warning_record(warning_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_level ON growth_svc.warning_record(warning_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_status ON growth_svc.warning_record(status) WHERE is_deleted = FALSE;
COMMENT ON TABLE growth_svc.warning_record IS '预警记录表';
COMMENT ON COLUMN growth_svc.warning_record.warning_type IS 'attendance=考勤异常 report=周报异常 evaluation=评价异常';
COMMENT ON COLUMN growth_svc.warning_record.warning_level IS '1=轻微 2=一般 3=严重';
COMMENT ON COLUMN growth_svc.warning_record.status IS '0=待处理 1=已干预 2=已关闭';

-- =====================================================
-- 触发器: 自动更新 updated_at 字段
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT schemaname, tablename
        FROM pg_tables
        WHERE schemaname IN (
            'auth_center', 'platform_service', 'student_svc',
            'college_svc', 'enterprise_svc', 'internship_svc',
            'training_svc', 'growth_svc'
        )
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = r.schemaname
            AND table_name = r.tablename
            AND column_name = 'updated_at'
        ) THEN
            EXECUTE format('
                CREATE TRIGGER update_%I_%I_updated_at
                BEFORE UPDATE ON %I.%I
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column();
            ', r.schemaname, r.tablename, r.schemaname, r.tablename);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 验证: 查看已创建的 Schema 和表
-- =====================================================

SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN (
    'auth_center', 'platform_service', 'student_svc',
    'college_svc', 'enterprise_svc', 'internship_svc',
    'training_svc', 'growth_svc'
)
ORDER BY schema_name;

SELECT schemaname, COUNT(*) AS table_count
FROM pg_tables
WHERE schemaname IN (
    'auth_center', 'platform_service', 'student_svc',
    'college_svc', 'enterprise_svc', 'internship_svc',
    'training_svc', 'growth_svc'
)
GROUP BY schemaname
ORDER BY schemaname;
