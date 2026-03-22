-- =====================================================
-- Schema: training_svc
-- Description: Training project management
-- =====================================================

CREATE SCHEMA IF NOT EXISTS training_svc;

-- =====================================================
-- Table: training_project
-- Description: Enterprise training projects
-- =====================================================
CREATE TABLE training_svc.training_project (
    id BIGSERIAL PRIMARY KEY,
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    tech_stack TEXT COMMENT '技术栈(JSON数组)',
    industry VARCHAR(50) COMMENT '行业',
    max_teams INTEGER DEFAULT 10 COMMENT '最大团队数',
    max_members INTEGER DEFAULT 6 COMMENT '每队最大人数',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    audit_status SMALLINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=通过 2=拒绝',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=招募中 2=进行中 3=已结束',
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

-- =====================================================
-- Table: training_plan
-- Description: College training schedules
-- =====================================================
CREATE TABLE training_svc.training_plan (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '高校租户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    teacher_id BIGINT COMMENT '指导老师ID',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=计划中 2=进行中 3=已完成',
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

-- =====================================================
-- Table: project_task
-- Description: Scrum board tasks for training projects
-- =====================================================
CREATE TABLE training_svc.project_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    team_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'todo' COMMENT 'todo/in_progress/done',
    priority SMALLINT DEFAULT 2,
    story_points INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_ptask_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_ptask_assignee FOREIGN KEY (assignee_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_ptask_status CHECK (status IN ('todo', 'in_progress', 'done')),
    CONSTRAINT chk_ptask_priority CHECK (priority IN (1, 2, 3))
);

CREATE INDEX idx_ptask_project ON training_svc.project_task(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_ptask_status ON training_svc.project_task(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_ptask_assignee ON training_svc.project_task(assignee_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE training_svc.project_task IS '项目任务看板表';

-- =====================================================
-- Table: project_enrollment
-- Description: Student enrollment in training projects
-- =====================================================
CREATE TABLE training_svc.project_enrollment (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    team_id BIGINT,
    role VARCHAR(20) COMMENT 'member/leader',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=active, 2=completed, 3=withdrawn',
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_enroll_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_enrollment UNIQUE (project_id, student_id),
    CONSTRAINT chk_enroll_status CHECK (status IN (1, 2, 3))
);

CREATE INDEX idx_enroll_project ON training_svc.project_enrollment(project_id);
CREATE INDEX idx_enroll_student ON training_svc.project_enrollment(student_id);

COMMENT ON TABLE training_svc.project_enrollment IS '项目报名表';
