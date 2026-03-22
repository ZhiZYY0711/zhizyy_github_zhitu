-- =====================================================
-- Migration: 001_add_missing_api_tables.sql
-- Description: Add 15 new tables for missing API endpoints
-- Date: 2024
-- Related Spec: .kiro/specs/missing-api-endpoints
-- =====================================================

-- This migration adds tables required for the following features:
-- 1. Student dashboard, tasks, capabilities, and recommendations (3 tables)
-- 2. Training project scrum board and enrollment (2 tables)
-- 3. Enterprise activity feed, todos, and interviews (3 tables)
-- 4. College CRM and internship oversight (4 tables)
-- 5. Platform monitoring, logs, and recommendations (11 tables)

-- =====================================================
-- STUDENT_SVC SCHEMA - 3 tables
-- =====================================================

-- Table: student_task
CREATE TABLE student_svc.student_task (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    task_type VARCHAR(20) NOT NULL,
    ref_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority SMALLINT DEFAULT 1,
    status SMALLINT NOT NULL DEFAULT 0,
    due_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_task_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_task_type CHECK (task_type IN ('training', 'internship', 'evaluation')),
    CONSTRAINT chk_task_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_task_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_task_student ON student_svc.student_task(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_task_status ON student_svc.student_task(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_task_due ON student_svc.student_task(due_date) WHERE is_deleted = FALSE;

COMMENT ON TABLE student_svc.student_task IS '学生任务表';

-- Table: student_capability
CREATE TABLE student_svc.student_capability (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    dimension VARCHAR(50) NOT NULL,
    score INTEGER NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_capability_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_capability UNIQUE (student_id, dimension),
    CONSTRAINT chk_capability_score CHECK (score >= 0 AND score <= 100)
);

CREATE INDEX idx_capability_student ON student_svc.student_capability(student_id);

COMMENT ON TABLE student_svc.student_capability IS '学生能力雷达图数据表';

-- Table: student_recommendation
CREATE TABLE student_svc.student_recommendation (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    rec_type VARCHAR(20) NOT NULL,
    ref_id BIGINT NOT NULL,
    score DECIMAL(5,2),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_rec_type CHECK (rec_type IN ('project', 'job', 'course'))
);

CREATE INDEX idx_rec_student ON student_svc.student_recommendation(student_id);
CREATE INDEX idx_rec_type ON student_svc.student_recommendation(rec_type);
CREATE INDEX idx_rec_created ON student_svc.student_recommendation(created_at);

COMMENT ON TABLE student_svc.student_recommendation IS '学生个性化推荐表';

-- =====================================================
-- TRAINING_SVC SCHEMA - 2 tables
-- =====================================================

-- Table: project_task
CREATE TABLE training_svc.project_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    team_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'todo',
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

-- Table: project_enrollment
CREATE TABLE training_svc.project_enrollment (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    team_id BIGINT,
    role VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_enroll_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_enrollment UNIQUE (project_id, student_id),
    CONSTRAINT chk_enroll_status CHECK (status IN (1, 2, 3))
);

CREATE INDEX idx_enroll_project ON training_svc.project_enrollment(project_id);
CREATE INDEX idx_enroll_student ON training_svc.project_enrollment(student_id);

COMMENT ON TABLE training_svc.project_enrollment IS '项目报名表';

-- =====================================================
-- ENTERPRISE_SVC SCHEMA - 3 tables
-- =====================================================

-- Table: enterprise_activity
CREATE TABLE enterprise_svc.enterprise_activity (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    activity_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    ref_type VARCHAR(20),
    ref_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_activity_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id)
);

CREATE INDEX idx_activity_tenant ON enterprise_svc.enterprise_activity(tenant_id);
CREATE INDEX idx_activity_created ON enterprise_svc.enterprise_activity(created_at);

COMMENT ON TABLE enterprise_svc.enterprise_activity IS '企业活动动态表';

-- Table: enterprise_todo
CREATE TABLE enterprise_svc.enterprise_todo (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    todo_type VARCHAR(30) NOT NULL,
    ref_type VARCHAR(20),
    ref_id BIGINT,
    title VARCHAR(200) NOT NULL,
    priority SMALLINT DEFAULT 2,
    due_date TIMESTAMPTZ,
    status SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_todo_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_todo_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todo_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_todo_user ON enterprise_svc.enterprise_todo(user_id) WHERE status = 0;
CREATE INDEX idx_todo_due ON enterprise_svc.enterprise_todo(due_date) WHERE status = 0;

COMMENT ON TABLE enterprise_svc.enterprise_todo IS '企业待办事项表';

-- Table: interview_schedule
CREATE TABLE enterprise_svc.interview_schedule (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    interview_time TIMESTAMPTZ NOT NULL,
    location VARCHAR(200),
    interviewer_id BIGINT,
    interview_type VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id) REFERENCES internship_svc.job_application(id),
    CONSTRAINT fk_interview_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_interview_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_interview_interviewer FOREIGN KEY (interviewer_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_interview_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_interview_application ON enterprise_svc.interview_schedule(application_id);
CREATE INDEX idx_interview_student ON enterprise_svc.interview_schedule(student_id);
CREATE INDEX idx_interview_time ON enterprise_svc.interview_schedule(interview_time);

COMMENT ON TABLE enterprise_svc.interview_schedule IS '面试安排表';

-- =====================================================
-- COLLEGE_SVC SCHEMA - 4 tables
-- =====================================================

-- Table: enterprise_relationship
CREATE TABLE college_svc.enterprise_relationship (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    enterprise_tenant_id BIGINT NOT NULL,
    cooperation_level SMALLINT DEFAULT 1,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_rel_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_rel_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT uk_relationship UNIQUE (college_tenant_id, enterprise_tenant_id),
    CONSTRAINT chk_rel_level CHECK (cooperation_level IN (1, 2, 3)),
    CONSTRAINT chk_rel_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_rel_college ON college_svc.enterprise_relationship(college_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_rel_enterprise ON college_svc.enterprise_relationship(enterprise_tenant_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE college_svc.enterprise_relationship IS '校企合作关系表';

-- Table: enterprise_visit
CREATE TABLE college_svc.enterprise_visit (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    enterprise_tenant_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    visitor_id BIGINT NOT NULL,
    visitor_name VARCHAR(50),
    purpose TEXT,
    outcome TEXT,
    next_action TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_visit_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_visit_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_visit_visitor FOREIGN KEY (visitor_id) REFERENCES auth_center.sys_user(id)
);

CREATE INDEX idx_visit_college ON college_svc.enterprise_visit(college_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_visit_enterprise ON college_svc.enterprise_visit(enterprise_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_visit_date ON college_svc.enterprise_visit(visit_date) WHERE is_deleted = FALSE;

COMMENT ON TABLE college_svc.enterprise_visit IS '企业走访记录表';

-- Table: enterprise_audit
CREATE TABLE college_svc.enterprise_audit (
    id BIGSERIAL PRIMARY KEY,
    enterprise_tenant_id BIGINT NOT NULL,
    audit_type VARCHAR(20) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 0,
    auditor_id BIGINT,
    audit_comment TEXT,
    audited_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_audit_auditor FOREIGN KEY (auditor_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_audit_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_audit_enterprise ON college_svc.enterprise_audit(enterprise_tenant_id);
CREATE INDEX idx_audit_status ON college_svc.enterprise_audit(status);

COMMENT ON TABLE college_svc.enterprise_audit IS '企业资质审核表';

-- Table: internship_inspection
CREATE TABLE college_svc.internship_inspection (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    inspector_id BIGINT NOT NULL,
    inspection_date DATE NOT NULL,
    location VARCHAR(200),
    findings TEXT,
    issues TEXT,
    recommendations TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inspection_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_inspection_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_inspection_inspector FOREIGN KEY (inspector_id) REFERENCES auth_center.sys_user(id)
);

CREATE INDEX idx_inspection_college ON college_svc.internship_inspection(college_tenant_id);
CREATE INDEX idx_inspection_internship ON college_svc.internship_inspection(internship_id);
CREATE INDEX idx_inspection_date ON college_svc.internship_inspection(inspection_date);

COMMENT ON TABLE college_svc.internship_inspection IS '实习巡查记录表';

-- =====================================================
-- PLATFORM_SERVICE SCHEMA - 11 tables
-- =====================================================

-- Table: sys_tag
CREATE TABLE platform_service.sys_tag (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_tag_parent FOREIGN KEY (parent_id) REFERENCES platform_service.sys_tag(id),
    CONSTRAINT uk_tag UNIQUE (category, name)
);

CREATE INDEX idx_tag_category ON platform_service.sys_tag(category) WHERE is_deleted = FALSE;
CREATE INDEX idx_tag_parent ON platform_service.sys_tag(parent_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.sys_tag IS '系统标签表';

-- Table: skill_tree
CREATE TABLE platform_service.skill_tree (
    id BIGSERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    skill_category VARCHAR(30) NOT NULL,
    parent_id BIGINT,
    level INTEGER DEFAULT 1,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_skill_parent FOREIGN KEY (parent_id) REFERENCES platform_service.skill_tree(id),
    CONSTRAINT chk_skill_category CHECK (skill_category IN ('technical', 'soft_skill', 'domain_knowledge'))
);

CREATE INDEX idx_skill_category ON platform_service.skill_tree(skill_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_skill_parent ON platform_service.skill_tree(parent_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.skill_tree IS '技能树表';

-- Table: certificate_template
CREATE TABLE platform_service.certificate_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    layout_config TEXT,
    background_url VARCHAR(255),
    signature_urls TEXT,
    variables TEXT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_cert_template_usage ON platform_service.certificate_template(usage_count DESC) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.certificate_template IS '证书模板表';

-- Table: contract_template
CREATE TABLE platform_service.contract_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    contract_type VARCHAR(30),
    content TEXT,
    variables TEXT,
    legal_terms TEXT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contract_template_usage ON platform_service.contract_template(usage_count DESC) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.contract_template IS '合同模板表';

-- Table: recommendation_banner
CREATE TABLE platform_service.recommendation_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NOT NULL,
    target_portal VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sort_order INTEGER DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_banner_portal CHECK (target_portal IN ('student', 'enterprise', 'college', 'all')),
    CONSTRAINT chk_banner_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_banner_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_banner_portal ON platform_service.recommendation_banner(target_portal) WHERE is_deleted = FALSE;
CREATE INDEX idx_banner_dates ON platform_service.recommendation_banner(start_date, end_date) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.recommendation_banner IS '推荐横幅表';

-- Table: recommendation_top_list
CREATE TABLE platform_service.recommendation_top_list (
    id BIGSERIAL PRIMARY KEY,
    list_type VARCHAR(20) NOT NULL,
    item_ids TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_top_list_type UNIQUE (list_type),
    CONSTRAINT chk_list_type CHECK (list_type IN ('mentor', 'course', 'project'))
);

COMMENT ON TABLE platform_service.recommendation_top_list IS '推荐榜单表';

-- Table: operation_log
CREATE TABLE platform_service.operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(50),
    tenant_id BIGINT,
    module VARCHAR(50),
    operation VARCHAR(100),
    request_params TEXT,
    response_status INTEGER,
    result VARCHAR(20),
    ip_address VARCHAR(50),
    user_agent TEXT,
    execution_time INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oplog_user ON platform_service.operation_log(user_id);
CREATE INDEX idx_oplog_module ON platform_service.operation_log(module);
CREATE INDEX idx_oplog_created ON platform_service.operation_log(created_at);
CREATE INDEX idx_oplog_result ON platform_service.operation_log(result);

COMMENT ON TABLE platform_service.operation_log IS '操作日志表';

-- Table: security_log
CREATE TABLE platform_service.security_log (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(20) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    ip_address VARCHAR(50),
    description TEXT NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_seclog_level CHECK (level IN ('info', 'warning', 'critical'))
);

CREATE INDEX idx_seclog_level ON platform_service.security_log(level);
CREATE INDEX idx_seclog_event ON platform_service.security_log(event_type);
CREATE INDEX idx_seclog_created ON platform_service.security_log(created_at);
CREATE INDEX idx_seclog_user ON platform_service.security_log(user_id);

COMMENT ON TABLE platform_service.security_log IS '安全日志表';

-- Table: service_health
CREATE TABLE platform_service.service_health (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_time INTEGER,
    error_rate DECIMAL(5,2),
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    checked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_health_status CHECK (status IN ('healthy', 'degraded', 'down'))
);

CREATE INDEX idx_health_service ON platform_service.service_health(service_name);
CREATE INDEX idx_health_checked ON platform_service.service_health(checked_at);

COMMENT ON TABLE platform_service.service_health IS '服务健康监控表';

-- Table: online_user_trend
CREATE TABLE platform_service.online_user_trend (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ NOT NULL,
    online_count INTEGER NOT NULL,
    student_count INTEGER DEFAULT 0,
    enterprise_count INTEGER DEFAULT 0,
    college_count INTEGER DEFAULT 0
);

CREATE INDEX idx_trend_timestamp ON platform_service.online_user_trend(timestamp);

COMMENT ON TABLE platform_service.online_user_trend IS '在线用户趋势表';

-- =====================================================
-- Migration Complete
-- =====================================================
-- Total tables added: 23
-- - student_svc: 3 tables
-- - training_svc: 2 tables
-- - enterprise_svc: 3 tables
-- - college_svc: 4 tables
-- - platform_service: 11 tables
-- =====================================================
