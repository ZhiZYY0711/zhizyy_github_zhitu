-- ============================================================
-- 智途平台 PostgreSQL 初始化脚本
-- 数据库: zhitu_cloud
-- 说明: 可在 Navicat 中直接连接后运行此文件
-- ============================================================

-- 创建各业务 Schema
CREATE SCHEMA IF NOT EXISTS auth_center;   -- 认证中心
CREATE SCHEMA IF NOT EXISTS system_mgmt;   -- 系统管理
CREATE SCHEMA IF NOT EXISTS college_svc;   -- 高校服务
CREATE SCHEMA IF NOT EXISTS enterprise_svc; -- 企业服务
CREATE SCHEMA IF NOT EXISTS student_svc;   -- 学生服务
CREATE SCHEMA IF NOT EXISTS training_svc;  -- 实训服务
CREATE SCHEMA IF NOT EXISTS internship_svc; -- 实习服务
CREATE SCHEMA IF NOT EXISTS growth_svc;    -- 成长评价服务

-- ============================================================
-- auth_center: 认证中心
-- ============================================================

CREATE TABLE auth_center.sys_tenant (
    id          BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(64)  NOT NULL UNIQUE,          -- 租户唯一编码
    tenant_name VARCHAR(128) NOT NULL,                 -- 租户名称
    tenant_type SMALLINT     NOT NULL DEFAULT 1,       -- 1=高校 2=企业 3=平台
    status      SMALLINT     NOT NULL DEFAULT 1,       -- 1=正常 0=禁用
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE auth_center.sys_tenant IS '租户表';

CREATE TABLE auth_center.sys_user (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,               -- 所属租户
    username      VARCHAR(64)  NOT NULL,               -- 登录账号
    password_hash VARCHAR(128) NOT NULL,               -- BCrypt 密码
    real_name     VARCHAR(64),                         -- 真实姓名
    email         VARCHAR(128),
    phone         VARCHAR(20),
    role          VARCHAR(32)  NOT NULL,               -- platform_admin / college_admin / college_teacher / enterprise_admin / enterprise_mentor / student
    sub_role      VARCHAR(32),                         -- 角色细分
    avatar_url    VARCHAR(256),
    status        SMALLINT     NOT NULL DEFAULT 1,     -- 1=正常 0=禁用
    last_login_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, username)
);
COMMENT ON TABLE auth_center.sys_user IS '系统用户表';

CREATE TABLE auth_center.sys_refresh_token (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    token_hash   VARCHAR(128) NOT NULL UNIQUE,         -- refresh_token 的 SHA256 哈希
    expires_at   TIMESTAMPTZ  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_revoked   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE auth_center.sys_refresh_token IS 'Refresh Token 存储表';

CREATE INDEX idx_refresh_token_user ON auth_center.sys_refresh_token(user_id);

-- ============================================================
-- system_mgmt: 系统管理
-- ============================================================

CREATE TABLE system_mgmt.sys_dict (
    id         BIGSERIAL PRIMARY KEY,
    category   VARCHAR(64)  NOT NULL,                  -- 字典分类，如 industry / tech_stack
    dict_key   VARCHAR(64)  NOT NULL,
    dict_value VARCHAR(128) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (category, dict_key)
);
COMMENT ON TABLE system_mgmt.sys_dict IS '数据字典表';

CREATE TABLE system_mgmt.sys_tag (
    id         BIGSERIAL PRIMARY KEY,
    tag_type   VARCHAR(32)  NOT NULL,                  -- industry / tech_stack
    tag_name   VARCHAR(64)  NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE system_mgmt.sys_tag IS '标签表（行业/技术栈）';

CREATE TABLE system_mgmt.audit_log (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT,
    tenant_id    BIGINT,
    action       VARCHAR(128) NOT NULL,                -- 操作描述
    resource     VARCHAR(128),                         -- 操作资源
    resource_id  VARCHAR(64),
    ip_address   VARCHAR(64),
    user_agent   VARCHAR(256),
    request_body TEXT,
    result       SMALLINT     NOT NULL DEFAULT 1,      -- 1=成功 0=失败
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE system_mgmt.audit_log IS '操作审计日志';

CREATE INDEX idx_audit_log_user ON system_mgmt.audit_log(user_id);
CREATE INDEX idx_audit_log_created ON system_mgmt.audit_log(created_at);

-- ============================================================
-- college_svc: 高校服务
-- ============================================================

CREATE TABLE college_svc.college_info (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL UNIQUE,
    college_name    VARCHAR(128) NOT NULL,
    college_code    VARCHAR(32)  NOT NULL UNIQUE,      -- 高校代码
    province        VARCHAR(32),
    city            VARCHAR(32),
    address         VARCHAR(256),
    logo_url        VARCHAR(256),
    contact_name    VARCHAR(64),
    contact_phone   VARCHAR(20),
    contact_email   VARCHAR(128),
    cooperation_level SMALLINT   NOT NULL DEFAULT 1,   -- 1=普通 2=重点 3=战略
    status          SMALLINT     NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_svc.college_info IS '高校信息表';

CREATE TABLE college_svc.organization (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT       NOT NULL,
    parent_id   BIGINT,                                -- 上级组织，NULL 为根节点
    org_type    SMALLINT     NOT NULL,                 -- 1=学院 2=专业 3=班级
    org_name    VARCHAR(64)  NOT NULL,
    org_code    VARCHAR(32),
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE college_svc.organization IS '学院/专业/班级组织树';

CREATE INDEX idx_org_tenant ON college_svc.organization(tenant_id);
CREATE INDEX idx_org_parent ON college_svc.organization(parent_id);

CREATE TABLE college_svc.enterprise_visit (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,               -- 高校租户
    enterprise_id BIGINT       NOT NULL,
    visitor_id    BIGINT       NOT NULL,               -- 走访人 user_id
    visit_date    DATE         NOT NULL,
    visit_content TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE college_svc.enterprise_visit IS '企业走访记录';

-- ============================================================
-- enterprise_svc: 企业服务
-- ============================================================

CREATE TABLE enterprise_svc.enterprise_info (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL UNIQUE,
    enterprise_name  VARCHAR(128) NOT NULL,
    enterprise_code  VARCHAR(64)  NOT NULL UNIQUE,     -- 统一社会信用代码
    industry         VARCHAR(64),
    scale            VARCHAR(32),                      -- 企业规模
    province         VARCHAR(32),
    city             VARCHAR(32),
    address          VARCHAR(256),
    logo_url         VARCHAR(256),
    website          VARCHAR(256),
    description      TEXT,
    contact_name     VARCHAR(64),
    contact_phone    VARCHAR(20),
    contact_email    VARCHAR(128),
    audit_status     SMALLINT     NOT NULL DEFAULT 0,  -- 0=待审核 1=通过 2=拒绝
    audit_remark     VARCHAR(256),
    audited_at       TIMESTAMPTZ,
    status           SMALLINT     NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE enterprise_svc.enterprise_info IS '企业信息表';

CREATE TABLE enterprise_svc.enterprise_staff (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL,
    user_id       BIGINT      NOT NULL UNIQUE,
    department    VARCHAR(64),
    position      VARCHAR(64),
    is_mentor     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE enterprise_svc.enterprise_staff IS '企业员工表';

CREATE TABLE enterprise_svc.talent_pool (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL,
    student_id    BIGINT      NOT NULL,
    collected_by  BIGINT      NOT NULL,               -- 收藏人 user_id
    remark        VARCHAR(256),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, student_id)
);
COMMENT ON TABLE enterprise_svc.talent_pool IS '企业人才库';

-- ============================================================
-- student_svc: 学生服务
-- ============================================================

CREATE TABLE student_svc.student_info (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE,
    tenant_id       BIGINT       NOT NULL,             -- 所属高校租户
    student_no      VARCHAR(32)  NOT NULL,             -- 学号
    real_name       VARCHAR(64)  NOT NULL,
    gender          SMALLINT,                          -- 1=男 2=女
    id_card         VARCHAR(18),
    phone           VARCHAR(20),
    email           VARCHAR(128),
    avatar_url      VARCHAR(256),
    college_id      BIGINT,                            -- 学院 org_id
    major_id        BIGINT,                            -- 专业 org_id
    class_id        BIGINT,                            -- 班级 org_id
    grade           VARCHAR(16),                       -- 年级，如 2022
    enrollment_date DATE,
    graduation_date DATE,
    resume_url      VARCHAR(256),
    skills          TEXT,                              -- JSON 数组，技能标签
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, student_no)
);
COMMENT ON TABLE student_svc.student_info IS '学生档案表';

CREATE INDEX idx_student_tenant ON student_svc.student_info(tenant_id);
CREATE INDEX idx_student_class ON student_svc.student_info(class_id);

-- ============================================================
-- training_svc: 实训服务
-- ============================================================

CREATE TABLE training_svc.training_project (
    id               BIGSERIAL PRIMARY KEY,
    enterprise_id    BIGINT       NOT NULL,            -- 发布企业租户
    project_name     VARCHAR(128) NOT NULL,
    description      TEXT,
    tech_stack       TEXT,                             -- JSON 数组
    industry         VARCHAR(64),
    max_teams        INT          NOT NULL DEFAULT 1,
    max_members      INT          NOT NULL DEFAULT 5,
    start_date       DATE,
    end_date         DATE,
    audit_status     SMALLINT     NOT NULL DEFAULT 0,  -- 0=待审核 1=通过 2=拒绝
    status           SMALLINT     NOT NULL DEFAULT 1,  -- 1=招募中 2=进行中 3=已结束
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE training_svc.training_project IS '实训项目表';

CREATE TABLE training_svc.training_team (
    id            BIGSERIAL PRIMARY KEY,
    project_id    BIGINT      NOT NULL,
    college_id    BIGINT      NOT NULL,               -- 所属高校
    team_name     VARCHAR(64) NOT NULL,
    mentor_id     BIGINT,                             -- 企业导师 user_id
    teacher_id    BIGINT,                             -- 高校教师 user_id
    status        SMALLINT    NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN     NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE training_svc.training_team IS '实训团队表';

CREATE TABLE training_svc.team_member (
    id         BIGSERIAL PRIMARY KEY,
    team_id    BIGINT      NOT NULL,
    student_id BIGINT      NOT NULL,
    role       VARCHAR(32) NOT NULL DEFAULT 'member', -- leader / member
    joined_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted BOOLEAN     NOT NULL DEFAULT FALSE,
    UNIQUE (team_id, student_id)
);
COMMENT ON TABLE training_svc.team_member IS '团队成员表';

CREATE TABLE training_svc.sprint (
    id          BIGSERIAL PRIMARY KEY,
    team_id     BIGINT      NOT NULL,
    sprint_name VARCHAR(64) NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    goal        TEXT,
    status      SMALLINT    NOT NULL DEFAULT 1,       -- 1=进行中 2=已完成
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE training_svc.sprint IS '敏捷迭代 Sprint';

CREATE TABLE training_svc.task (
    id           BIGSERIAL PRIMARY KEY,
    sprint_id    BIGINT       NOT NULL,
    team_id      BIGINT       NOT NULL,
    title        VARCHAR(128) NOT NULL,
    description  TEXT,
    assignee_id  BIGINT,                              -- 负责人 student user_id
    status       VARCHAR(32)  NOT NULL DEFAULT 'todo', -- todo / in_progress / review / done
    priority     SMALLINT     NOT NULL DEFAULT 2,     -- 1=低 2=中 3=高
    story_points INT,
    due_date     DATE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE training_svc.task IS '任务看板';

CREATE INDEX idx_task_sprint ON training_svc.task(sprint_id);
CREATE INDEX idx_task_assignee ON training_svc.task(assignee_id);

CREATE TABLE training_svc.code_review (
    id           BIGSERIAL PRIMARY KEY,
    team_id      BIGINT      NOT NULL,
    task_id      BIGINT,
    reviewer_id  BIGINT      NOT NULL,               -- 评审人 user_id
    commit_url   VARCHAR(256),
    comment      TEXT,
    score        SMALLINT,
    status       SMALLINT    NOT NULL DEFAULT 0,     -- 0=待评审 1=已评审
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE training_svc.code_review IS '代码评审记录';

CREATE TABLE training_svc.peer_review (
    id           BIGSERIAL PRIMARY KEY,
    team_id      BIGINT      NOT NULL,
    reviewer_id  BIGINT      NOT NULL,               -- 评价人 student user_id
    reviewee_id  BIGINT      NOT NULL,               -- 被评价人
    dimension    VARCHAR(64),                         -- 评价维度
    score        SMALLINT    NOT NULL,
    comment      TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (team_id, reviewer_id, reviewee_id)
);
COMMENT ON TABLE training_svc.peer_review IS '360度同学互评';

CREATE TABLE training_svc.training_plan (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,              -- 高校租户
    project_id   BIGINT       NOT NULL,
    plan_name    VARCHAR(128) NOT NULL,
    start_date   DATE,
    end_date     DATE,
    teacher_id   BIGINT,
    status       SMALLINT     NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE training_svc.training_plan IS '高校实训排期计划';

-- ============================================================
-- internship_svc: 实习服务
-- ============================================================

CREATE TABLE internship_svc.internship_job (
    id              BIGSERIAL PRIMARY KEY,
    enterprise_id   BIGINT       NOT NULL,
    job_title       VARCHAR(128) NOT NULL,
    job_type        VARCHAR(32)  NOT NULL DEFAULT 'internship',
    description     TEXT,
    requirements    TEXT,
    tech_stack      TEXT,                             -- JSON 数组
    industry        VARCHAR(64),
    city            VARCHAR(32),
    salary_min      INT,
    salary_max      INT,
    headcount       INT          NOT NULL DEFAULT 1,
    start_date      DATE,
    end_date        DATE,
    status          SMALLINT     NOT NULL DEFAULT 1,  -- 1=招募中 0=已关闭
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);
COMMENT ON TABLE internship_svc.internship_job IS '实习岗位表';

CREATE INDEX idx_job_enterprise ON internship_svc.internship_job(enterprise_id);

CREATE TABLE internship_svc.job_application (
    id            BIGSERIAL PRIMARY KEY,
    job_id        BIGINT      NOT NULL,
    student_id    BIGINT      NOT NULL,
    resume_url    VARCHAR(256),
    cover_letter  TEXT,
    status        SMALLINT    NOT NULL DEFAULT 0,     -- 0=待处理 1=面试 2=Offer 3=拒绝 4=录用
    applied_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_id, student_id)
);
COMMENT ON TABLE internship_svc.job_application IS '求职申请表';

CREATE TABLE internship_svc.interview (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT      NOT NULL,
    interviewer_id  BIGINT      NOT NULL,             -- 面试官 user_id
    interview_time  TIMESTAMPTZ NOT NULL,
    interview_type  VARCHAR(32) NOT NULL DEFAULT 'online', -- online / offline
    location        VARCHAR(256),
    meeting_url     VARCHAR(256),
    result          SMALLINT,                         -- 1=通过 0=未通过
    feedback        TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.interview IS '面试记录表';

CREATE TABLE internship_svc.internship_offer (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT      NOT NULL UNIQUE,
    student_id      BIGINT      NOT NULL,
    enterprise_id   BIGINT      NOT NULL,
    job_id          BIGINT      NOT NULL,
    salary          INT,
    start_date      DATE,
    end_date        DATE,
    status          SMALLINT    NOT NULL DEFAULT 0,   -- 0=待确认 1=已接受 2=已拒绝
    college_audit   SMALLINT    NOT NULL DEFAULT 0,   -- 0=待审核 1=通过 2=拒绝
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.internship_offer IS 'Offer 表';

CREATE TABLE internship_svc.internship_record (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT      NOT NULL,
    enterprise_id   BIGINT      NOT NULL,
    job_id          BIGINT      NOT NULL,
    mentor_id       BIGINT,                           -- 企业导师
    teacher_id      BIGINT,                           -- 高校教师
    start_date      DATE        NOT NULL,
    end_date        DATE,
    status          SMALLINT    NOT NULL DEFAULT 1,   -- 1=实习中 2=已结束
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.internship_record IS '实习记录表';

CREATE TABLE internship_svc.attendance (
    id              BIGSERIAL PRIMARY KEY,
    internship_id   BIGINT      NOT NULL,
    student_id      BIGINT      NOT NULL,
    clock_in_time   TIMESTAMPTZ NOT NULL,
    clock_out_time  TIMESTAMPTZ,
    clock_in_lat    DECIMAL(10,7),
    clock_in_lng    DECIMAL(10,7),
    clock_out_lat   DECIMAL(10,7),
    clock_out_lng   DECIMAL(10,7),
    status          SMALLINT    NOT NULL DEFAULT 0,   -- 0=待审核 1=正常 2=异常
    audit_remark    VARCHAR(256),
    audited_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.attendance IS '考勤打卡记录';

CREATE INDEX idx_attendance_student ON internship_svc.attendance(student_id);
CREATE INDEX idx_attendance_date ON internship_svc.attendance(clock_in_time);

CREATE TABLE internship_svc.weekly_report (
    id              BIGSERIAL PRIMARY KEY,
    internship_id   BIGINT      NOT NULL,
    student_id      BIGINT      NOT NULL,
    week_start      DATE        NOT NULL,
    week_end        DATE        NOT NULL,
    content         TEXT        NOT NULL,
    work_hours      DECIMAL(5,1),
    status          SMALLINT    NOT NULL DEFAULT 0,   -- 0=草稿 1=已提交 2=已批阅
    review_comment  TEXT,
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.weekly_report IS '实习周报';

CREATE TABLE internship_svc.internship_certificate (
    id              BIGSERIAL PRIMARY KEY,
    internship_id   BIGINT       NOT NULL,
    student_id      BIGINT       NOT NULL,
    enterprise_id   BIGINT       NOT NULL,
    cert_no         VARCHAR(64)  NOT NULL UNIQUE,
    cert_url        VARCHAR(256),
    issued_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    issued_by       BIGINT       NOT NULL
);
COMMENT ON TABLE internship_svc.internship_certificate IS '实习证明';

CREATE TABLE internship_svc.inspection (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT      NOT NULL,             -- 高校租户
    teacher_id      BIGINT      NOT NULL,
    internship_id   BIGINT      NOT NULL,
    inspect_time    TIMESTAMPTZ NOT NULL,
    inspect_type    VARCHAR(32) NOT NULL DEFAULT 'online', -- online / offline
    content         TEXT,
    result          SMALLINT,                         -- 1=正常 0=异常
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE internship_svc.inspection IS '云巡视记录';

-- ============================================================
-- growth_svc: 成长评价服务
-- ============================================================

CREATE TABLE growth_svc.evaluation_record (
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT      NOT NULL,
    evaluator_id    BIGINT      NOT NULL,             -- 评价人 user_id
    evaluator_role  VARCHAR(32) NOT NULL,             -- enterprise_mentor / college_teacher / peer
    source_type     VARCHAR(32) NOT NULL,             -- training / internship / peer_review
    source_id       BIGINT,                           -- 关联的实训/实习 ID
    dimension       VARCHAR(64),                      -- 评价维度：技术/协作/职业素养等
    score           SMALLINT    NOT NULL,             -- 0-100
    comment         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE growth_svc.evaluation_record IS '综合评价记录';

CREATE INDEX idx_eval_student ON growth_svc.evaluation_record(student_id);
CREATE INDEX idx_eval_source ON growth_svc.evaluation_record(source_type, source_id);

CREATE TABLE growth_svc.badge (
    id           BIGSERIAL PRIMARY KEY,
    badge_name   VARCHAR(64)  NOT NULL,
    badge_type   VARCHAR(32)  NOT NULL,               -- certificate / achievement
    description  VARCHAR(256),
    icon_url     VARCHAR(256),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE growth_svc.badge IS '证书/徽章定义表';

CREATE TABLE growth_svc.student_badge (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT      NOT NULL,
    badge_id     BIGINT      NOT NULL,
    source_type  VARCHAR(32),                         -- training / internship / system
    source_id    BIGINT,
    issued_by    BIGINT,
    issued_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cert_url     VARCHAR(256),
    UNIQUE (student_id, badge_id, source_id)
);
COMMENT ON TABLE growth_svc.student_badge IS '学生获得的证书/徽章';

CREATE INDEX idx_student_badge ON growth_svc.student_badge(student_id);

CREATE TABLE growth_svc.warning_record (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT      NOT NULL,             -- 高校租户
    student_id      BIGINT      NOT NULL,
    warning_type    VARCHAR(64) NOT NULL,             -- attendance / report / evaluation
    warning_level   SMALLINT    NOT NULL DEFAULT 1,   -- 1=轻微 2=一般 3=严重
    description     TEXT,
    status          SMALLINT    NOT NULL DEFAULT 0,   -- 0=待处理 1=已干预 2=已关闭
    intervene_note  TEXT,
    intervened_by   BIGINT,
    intervened_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE growth_svc.warning_record IS '预警记录';

CREATE INDEX idx_warning_student ON growth_svc.warning_record(student_id);
CREATE INDEX idx_warning_tenant ON growth_svc.warning_record(tenant_id, status);

-- ============================================================
-- 基础测试数据
-- ============================================================

-- 平台租户
INSERT INTO auth_center.sys_tenant (tenant_code, tenant_name, tenant_type)
VALUES ('platform', '智途平台运营方', 3);

-- 测试高校租户
INSERT INTO auth_center.sys_tenant (tenant_code, tenant_name, tenant_type)
VALUES ('college_test', '测试高校', 1);

-- 测试企业租户
INSERT INTO auth_center.sys_tenant (tenant_code, tenant_name, tenant_type)
VALUES ('enterprise_test', '测试企业', 2);

-- 平台管理员（密码: Admin@123456，BCrypt 加密）
INSERT INTO auth_center.sys_user (tenant_id, username, password_hash, real_name, role)
VALUES (
    (SELECT id FROM auth_center.sys_tenant WHERE tenant_code = 'platform'),
    'admin',
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    '平台管理员',
    'platform_admin'
);

-- 测试高校管理员（密码: Admin@123456）
INSERT INTO auth_center.sys_user (tenant_id, username, password_hash, real_name, role)
VALUES (
    (SELECT id FROM auth_center.sys_tenant WHERE tenant_code = 'college_test'),
    'college_admin',
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    '高校管理员',
    'college_admin'
);

-- 测试企业管理员（密码: Admin@123456）
INSERT INTO auth_center.sys_user (tenant_id, username, password_hash, real_name, role)
VALUES (
    (SELECT id FROM auth_center.sys_tenant WHERE tenant_code = 'enterprise_test'),
    'enterprise_admin',
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    '企业管理员',
    'enterprise_admin'
);

-- 高校基础信息
INSERT INTO college_svc.college_info (tenant_id, college_name, college_code, province, city)
VALUES (
    (SELECT id FROM auth_center.sys_tenant WHERE tenant_code = 'college_test'),
    '测试高校', 'TEST_COLLEGE', '广东省', '广州市'
);

-- 企业基础信息
INSERT INTO enterprise_svc.enterprise_info (tenant_id, enterprise_name, enterprise_code, audit_status)
VALUES (
    (SELECT id FROM auth_center.sys_tenant WHERE tenant_code = 'enterprise_test'),
    '测试科技有限公司', '91440000000000000X', 1
);

-- 常用数据字典
INSERT INTO system_mgmt.sys_dict (category, dict_key, dict_value, sort_order) VALUES
('industry', 'internet',     '互联网',     1),
('industry', 'finance',      '金融',       2),
('industry', 'education',    '教育',       3),
('industry', 'manufacturing','制造业',     4),
('tech_stack','java',        'Java',       1),
('tech_stack','python',      'Python',     2),
('tech_stack','vue',         'Vue.js',     3),
('tech_stack','react',       'React',      4),
('tech_stack','springboot',  'Spring Boot',5),
('tech_stack','mysql',       'MySQL',      6);
