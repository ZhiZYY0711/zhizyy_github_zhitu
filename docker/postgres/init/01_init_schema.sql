-- ============================================================
-- 智途平台数据库初始化脚本
-- 数据库: PostgreSQL 15+  字符集: UTF8
-- ============================================================

-- 1. 创建 Schemas
CREATE SCHEMA IF NOT EXISTS auth_center;
CREATE SCHEMA IF NOT EXISTS student_service;
CREATE SCHEMA IF NOT EXISTS college_service;
CREATE SCHEMA IF NOT EXISTS enterprise_service;
CREATE SCHEMA IF NOT EXISTS project_service;
CREATE SCHEMA IF NOT EXISTS internship_service;
CREATE SCHEMA IF NOT EXISTS growth_service;
CREATE SCHEMA IF NOT EXISTS platform_service;

-- 2. 通用函数：自动更新 updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Schema: auth_center
-- ============================================================

CREATE TABLE IF NOT EXISTS auth_center.sys_tenant (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        SMALLINT     NOT NULL DEFAULT 1,
    status      SMALLINT     NOT NULL DEFAULT 0,
    config      JSONB,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  auth_center.sys_tenant        IS '租户/机构表';
COMMENT ON COLUMN auth_center.sys_tenant.type   IS '1:高校 2:企业 0:平台运营';
COMMENT ON COLUMN auth_center.sys_tenant.status IS '0:待审核 1:正常 2:禁用';

CREATE TABLE IF NOT EXISTS auth_center.sys_user (
    id             BIGSERIAL    PRIMARY KEY,
    tenant_id      BIGINT       NOT NULL DEFAULT 0,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    phone          VARCHAR(20),
    role           VARCHAR(20)  NOT NULL,
    sub_role       VARCHAR(20),
    status         SMALLINT     NOT NULL DEFAULT 1,
    last_login_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_sys_user_username UNIQUE (username)
);
COMMENT ON TABLE  auth_center.sys_user            IS '系统用户表';
COMMENT ON COLUMN auth_center.sys_user.role       IS 'student / enterprise / college / platform';
COMMENT ON COLUMN auth_center.sys_user.sub_role   IS '企业: hr/mentor/admin  高校: counselor/dean/admin';
COMMENT ON COLUMN auth_center.sys_user.status     IS '1:正常 2:锁定 3:注销';
CREATE INDEX IF NOT EXISTS idx_sys_user_username  ON auth_center.sys_user(username);
CREATE INDEX IF NOT EXISTS idx_sys_user_phone     ON auth_center.sys_user(phone);
CREATE INDEX IF NOT EXISTS idx_sys_user_tenant    ON auth_center.sys_user(tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_role      ON auth_center.sys_user(role);

CREATE TABLE IF NOT EXISTS auth_center.sys_refresh_token (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(100) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE  auth_center.sys_refresh_token            IS 'Refresh Token 表';
COMMENT ON COLUMN auth_center.sys_refresh_token.token_hash IS 'SHA-256 哈希，不存明文';
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON auth_center.sys_refresh_token(user_id);

CREATE TABLE IF NOT EXISTS auth_center.sys_role (
    id          BIGSERIAL   PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL DEFAULT 0,
    code        VARCHAR(50) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    permissions TEXT[],
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  auth_center.sys_role           IS '角色表';
COMMENT ON COLUMN auth_center.sys_role.tenant_id IS '0=系统通用角色';

-- ============================================================
-- Schema: student_service
-- ============================================================

CREATE TABLE IF NOT EXISTS student_service.student_profile (
    id          BIGINT       PRIMARY KEY,   -- 关联 auth_center.sys_user.id
    student_no  VARCHAR(50)  NOT NULL,
    real_name   VARCHAR(50),
    gender      VARCHAR(10),
    avatar      VARCHAR(255),
    major_id    BIGINT,
    class_id    BIGINT,
    enrollment_year SMALLINT,
    email       VARCHAR(100),
    skills      TEXT[],
    introduction TEXT,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  student_service.student_profile        IS '学生档案';
COMMENT ON COLUMN student_service.student_profile.status IS '1:在读 2:实习中 3:已毕业';
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_no ON student_service.student_profile(student_no);

CREATE TABLE IF NOT EXISTS student_service.career_dna (
    id          BIGSERIAL   PRIMARY KEY,
    student_id  BIGINT      NOT NULL,
    dimensions  JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  student_service.career_dna            IS '能力画像/雷达图';
COMMENT ON COLUMN student_service.career_dna.dimensions IS '{"coding":80,"comm":75,"mgmt":60}';

CREATE TABLE IF NOT EXISTS student_service.resume (
    id           BIGSERIAL    PRIMARY KEY,
    student_id   BIGINT       NOT NULL,
    version_name VARCHAR(50),
    content      JSONB,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE student_service.resume IS '在线简历';
CREATE INDEX IF NOT EXISTS idx_resume_student ON student_service.resume(student_id);

-- ============================================================
-- Schema: college_service
-- ============================================================

CREATE TABLE IF NOT EXISTS college_service.department (
    id          BIGSERIAL    PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_service.department IS '院系/部门';

CREATE TABLE IF NOT EXISTS college_service.major (
    id            BIGSERIAL    PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    department_id BIGINT,
    name          VARCHAR(100) NOT NULL,
    code          VARCHAR(50),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_service.major IS '专业';

CREATE TABLE IF NOT EXISTS college_service.class (
    id          BIGSERIAL    PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    major_id    BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    grade       SMALLINT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_service.class IS '班级';

CREATE TABLE IF NOT EXISTS college_service.teacher_profile (
    id            BIGINT       PRIMARY KEY,   -- 关联 auth_center.sys_user.id
    real_name     VARCHAR(50),
    job_number    VARCHAR(50),
    title         VARCHAR(50),
    department_id BIGINT,
    avatar        VARCHAR(255),
    email         VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_service.teacher_profile IS '教师/辅导员档案';

-- ============================================================
-- Schema: enterprise_service
-- ============================================================

CREATE TABLE IF NOT EXISTS enterprise_service.enterprise_profile (
    id              BIGINT       PRIMARY KEY,   -- 关联 auth_center.sys_tenant.id
    industry        VARCHAR(50),
    scale           VARCHAR(20),
    address         VARCHAR(255),
    description     TEXT,
    logo            VARCHAR(255),
    license_url     VARCHAR(255),
    verified_status SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  enterprise_service.enterprise_profile                 IS '企业信息';
COMMENT ON COLUMN enterprise_service.enterprise_profile.verified_status IS '0:待审核 1:已认证 2:拒绝';

CREATE TABLE IF NOT EXISTS enterprise_service.enterprise_staff_profile (
    id            BIGINT       PRIMARY KEY,   -- 关联 auth_center.sys_user.id
    enterprise_id BIGINT       NOT NULL,
    real_name     VARCHAR(50),
    department    VARCHAR(50),
    position      VARCHAR(50),
    avatar        VARCHAR(255),
    email         VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE enterprise_service.enterprise_staff_profile IS '企业员工扩展档案';
CREATE INDEX IF NOT EXISTS idx_staff_enterprise ON enterprise_service.enterprise_staff_profile(enterprise_id);

CREATE TABLE IF NOT EXISTS enterprise_service.job_post (
    id           BIGSERIAL    PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    title        VARCHAR(100) NOT NULL,
    type         SMALLINT     NOT NULL DEFAULT 1,
    description  TEXT,
    requirements TEXT,
    location     VARCHAR(100),
    salary_range JSONB,
    status       SMALLINT     NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  enterprise_service.job_post        IS '实习/就业岗位';
COMMENT ON COLUMN enterprise_service.job_post.type   IS '1:实习 2:校招';
COMMENT ON COLUMN enterprise_service.job_post.status IS '1:招聘中 0:关闭';
CREATE INDEX IF NOT EXISTS idx_job_post_tenant ON enterprise_service.job_post(tenant_id);

-- ============================================================
-- Schema: project_service
-- ============================================================

CREATE TABLE IF NOT EXISTS project_service.project (
    id           BIGSERIAL    PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  TEXT,
    tech_stack   TEXT[],
    difficulty   SMALLINT     NOT NULL DEFAULT 1,
    resource_url VARCHAR(255),
    max_teams    INT          NOT NULL DEFAULT 10,
    status       SMALLINT     NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  project_service.project           IS '实训项目库';
COMMENT ON COLUMN project_service.project.difficulty IS '1-5星';
COMMENT ON COLUMN project_service.project.status    IS '1:招募中 2:进行中 3:已结项';

CREATE TABLE IF NOT EXISTS project_service.project_team (
    id         BIGSERIAL   PRIMARY KEY,
    project_id BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    leader_id  BIGINT,
    mentor_id  BIGINT,
    teacher_id BIGINT,
    status     SMALLINT    NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  project_service.project_team        IS '实训小组';
COMMENT ON COLUMN project_service.project_team.status IS '1:组队中 2:进行中 3:已结项';
CREATE INDEX IF NOT EXISTS idx_team_project ON project_service.project_team(project_id);

CREATE TABLE IF NOT EXISTS project_service.project_team_member (
    id         BIGSERIAL   PRIMARY KEY,
    team_id    BIGINT      NOT NULL,
    student_id BIGINT      NOT NULL,
    role_name  VARCHAR(50),
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE project_service.project_team_member IS '实训小组成员';
CREATE INDEX IF NOT EXISTS idx_member_team    ON project_service.project_team_member(team_id);
CREATE INDEX IF NOT EXISTS idx_member_student ON project_service.project_team_member(student_id);

CREATE TABLE IF NOT EXISTS project_service.project_task (
    id          BIGSERIAL    PRIMARY KEY,
    team_id     BIGINT       NOT NULL,
    sprint_id   BIGINT,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'todo',
    priority    SMALLINT     NOT NULL DEFAULT 0,
    story_points SMALLINT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  project_service.project_task        IS 'Scrum 任务/看板';
COMMENT ON COLUMN project_service.project_task.status IS 'todo / in_progress / review / done';
CREATE INDEX IF NOT EXISTS idx_task_team ON project_service.project_task(team_id);

-- ============================================================
-- Schema: internship_service
-- ============================================================

CREATE TABLE IF NOT EXISTS internship_service.internship_application (
    id              BIGSERIAL   PRIMARY KEY,
    job_id          BIGINT      NOT NULL,
    student_id      BIGINT      NOT NULL,
    status          SMALLINT    NOT NULL DEFAULT 1,
    resume_snapshot JSONB,
    apply_time      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  internship_service.internship_application        IS '投递记录';
COMMENT ON COLUMN internship_service.internship_application.status IS '1:已投递 2:面试中 3:录用 4:拒绝';
CREATE INDEX IF NOT EXISTS idx_application_student ON internship_service.internship_application(student_id);
CREATE INDEX IF NOT EXISTS idx_application_job     ON internship_service.internship_application(job_id);

CREATE TABLE IF NOT EXISTS internship_service.internship_process (
    id         BIGSERIAL   PRIMARY KEY,
    student_id BIGINT      NOT NULL,
    job_id     BIGINT      NOT NULL,
    company_id BIGINT      NOT NULL,
    mentor_id  BIGINT,
    teacher_id BIGINT,
    start_date DATE,
    end_date   DATE,
    status     SMALLINT    NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  internship_service.internship_process        IS '实习履历';
COMMENT ON COLUMN internship_service.internship_process.status IS '1:实习中 2:转正 3:离职';
CREATE INDEX IF NOT EXISTS idx_process_student ON internship_service.internship_process(student_id);

CREATE TABLE IF NOT EXISTS internship_service.weekly_report (
    id              BIGSERIAL   PRIMARY KEY,
    process_id      BIGINT      NOT NULL,
    week_num        INT         NOT NULL,
    start_date      DATE,
    end_date        DATE,
    content         JSONB,
    mood_score      SMALLINT,
    status          VARCHAR(20) NOT NULL DEFAULT 'draft',
    mentor_comment  TEXT,
    teacher_comment TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  internship_service.weekly_report        IS '周报';
COMMENT ON COLUMN internship_service.weekly_report.status IS 'draft / submitted / reviewed';
CREATE INDEX IF NOT EXISTS idx_report_process ON internship_service.weekly_report(process_id);

CREATE TABLE IF NOT EXISTS internship_service.attendance (
    id            BIGSERIAL   PRIMARY KEY,
    process_id    BIGINT      NOT NULL,
    check_in_time TIMESTAMPTZ,
    location      JSONB,
    wifi_mac      VARCHAR(50),
    photo_url     VARCHAR(255),
    type          SMALLINT    NOT NULL DEFAULT 1,
    status        SMALLINT    NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  internship_service.attendance        IS '考勤';
COMMENT ON COLUMN internship_service.attendance.type   IS '1:上班 2:下班 3:外勤';
COMMENT ON COLUMN internship_service.attendance.status IS '1:正常 2:异常/迟到';
CREATE INDEX IF NOT EXISTS idx_attendance_process ON internship_service.attendance(process_id);

CREATE TABLE IF NOT EXISTS internship_service.internship_contract (
    id            BIGSERIAL    PRIMARY KEY,
    process_id    BIGINT       NOT NULL,
    student_id    BIGINT       NOT NULL,
    company_id    BIGINT       NOT NULL,
    contract_type VARCHAR(20)  NOT NULL DEFAULT 'internship',
    file_url      VARCHAR(255),
    status        VARCHAR(20)  NOT NULL DEFAULT 'pending',
    signed_at     TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  internship_service.internship_contract        IS '实习协议';
COMMENT ON COLUMN internship_service.internship_contract.status IS 'pending / signed / rejected / expired';

-- ============================================================
-- Schema: growth_service
-- ============================================================

CREATE TABLE IF NOT EXISTS growth_service.evaluation_record (
    id                  BIGSERIAL    PRIMARY KEY,
    student_id          BIGINT       NOT NULL,
    evaluator_id        BIGINT       NOT NULL,
    source_type         VARCHAR(20)  NOT NULL,
    ref_type            VARCHAR(20)  NOT NULL,
    ref_id              BIGINT       NOT NULL,
    scores              JSONB,
    comment             TEXT,
    hire_recommendation VARCHAR(30),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  growth_service.evaluation_record                     IS '评价记录';
COMMENT ON COLUMN growth_service.evaluation_record.source_type         IS 'enterprise / school / peer';
COMMENT ON COLUMN growth_service.evaluation_record.ref_type            IS 'project / internship';
COMMENT ON COLUMN growth_service.evaluation_record.hire_recommendation IS 'strongly_recommend / recommend / not_recommend';
CREATE INDEX IF NOT EXISTS idx_eval_student ON growth_service.evaluation_record(student_id);

CREATE TABLE IF NOT EXISTS growth_service.growth_badge (
    id               BIGSERIAL    PRIMARY KEY,
    student_id       BIGINT       NOT NULL,
    type             VARCHAR(20)  NOT NULL DEFAULT 'badge',
    name             VARCHAR(100) NOT NULL,
    issue_date       DATE,
    image_url        VARCHAR(255),
    blockchain_hash  VARCHAR(100),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE  growth_service.growth_badge      IS '徽章/证书';
COMMENT ON COLUMN growth_service.growth_badge.type IS 'certificate / badge';
CREATE INDEX IF NOT EXISTS idx_badge_student ON growth_service.growth_badge(student_id);

-- ============================================================
-- Schema: platform_service
-- ============================================================

CREATE TABLE IF NOT EXISTS platform_service.sys_dict (
    id         BIGSERIAL    PRIMARY KEY,
    category   VARCHAR(50)  NOT NULL,
    code       VARCHAR(50)  NOT NULL,
    label      VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_dict_category_code UNIQUE (category, code)
);
COMMENT ON TABLE platform_service.sys_dict IS '数据字典';

CREATE TABLE IF NOT EXISTS platform_service.audit_log (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT,
    action     VARCHAR(50)  NOT NULL,
    target     VARCHAR(100),
    ip_address VARCHAR(50),
    detail     JSONB,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE platform_service.audit_log IS '审计日志（不需要 updated_at / is_deleted）';
CREATE INDEX IF NOT EXISTS idx_audit_user   ON platform_service.audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON platform_service.audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_time   ON platform_service.audit_log(created_at);

-- ============================================================
-- 3. 自动更新触发器（批量注册）
-- ============================================================

DO $$
DECLARE
    t TEXT;
BEGIN
    FOR t IN
        SELECT table_schema || '.' || table_name
        FROM information_schema.tables
        WHERE table_schema IN (
            'auth_center', 'student_service', 'college_service',
            'enterprise_service', 'project_service', 'internship_service',
            'growth_service', 'platform_service'
        )
        AND table_type = 'BASE TABLE'
        AND table_name != 'audit_log'   -- audit_log 只追加，不更新
    LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS trg_update_timestamp ON %s;
             CREATE TRIGGER trg_update_timestamp
             BEFORE UPDATE ON %s
             FOR EACH ROW EXECUTE FUNCTION update_timestamp();',
            t, t
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- 4. 基础测试数据
-- ============================================================

-- 平台运营租户（id=1，固定）
INSERT INTO auth_center.sys_tenant (id, name, type, status)
VALUES (1, '智途平台运营中心', 0, 1)
ON CONFLICT DO NOTHING;

-- 测试高校租户
INSERT INTO auth_center.sys_tenant (id, name, type, status)
VALUES (2, '测试大学', 1, 1)
ON CONFLICT DO NOTHING;

-- 测试企业租户
INSERT INTO auth_center.sys_tenant (id, name, type, status)
VALUES (3, '测试科技有限公司', 2, 1)
ON CONFLICT DO NOTHING;

-- 平台超级管理员（密码: admin123，BCrypt 哈希）
INSERT INTO auth_center.sys_user (id, tenant_id, username, password_hash, role, status)
VALUES (1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'platform', 1)
ON CONFLICT DO NOTHING;

-- 测试学生账号（密码: test123）
INSERT INTO auth_center.sys_user (id, tenant_id, username, password_hash, role, status)
VALUES (2, 2, 'student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'student', 1)
ON CONFLICT DO NOTHING;

-- 测试教师账号（密码: test123）
INSERT INTO auth_center.sys_user (id, tenant_id, username, password_hash, role, sub_role, status)
VALUES (3, 2, 'teacher01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'college', 'counselor', 1)
ON CONFLICT DO NOTHING;

-- 测试企业 HR 账号（密码: test123）
INSERT INTO auth_center.sys_user (id, tenant_id, username, password_hash, role, sub_role, status)
VALUES (4, 3, 'hr01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'enterprise', 'hr', 1)
ON CONFLICT DO NOTHING;

-- 重置序列，避免手动插入 id 后自增冲突
SELECT setval('auth_center.sys_tenant_id_seq', 10);
SELECT setval('auth_center.sys_user_id_seq', 10);

-- ============================================================
-- 初始化完成
-- ============================================================
