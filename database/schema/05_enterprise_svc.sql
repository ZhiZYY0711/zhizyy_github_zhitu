-- =====================================================
-- Schema: enterprise_svc
-- Description: Enterprise management and staff
-- =====================================================

CREATE SCHEMA IF NOT EXISTS enterprise_svc;

-- =====================================================
-- Table: enterprise_info
-- Description: Enterprise profile and verification
-- =====================================================
CREATE TABLE enterprise_svc.enterprise_info (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE COMMENT '关联租户ID',
    enterprise_name VARCHAR(100) NOT NULL COMMENT '企业名称',
    enterprise_code VARCHAR(50) COMMENT '统一社会信用代码',
    industry VARCHAR(50) COMMENT '行业领域',
    scale VARCHAR(20) COMMENT '企业规模',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    address VARCHAR(255) COMMENT '详细地址',
    logo_url VARCHAR(255) COMMENT 'Logo URL',
    website VARCHAR(255) COMMENT '官网',
    description TEXT COMMENT '企业简介',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    audit_status SMALLINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0=待审核 1=通过 2=拒绝',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 0=禁用',
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

-- =====================================================
-- Table: enterprise_staff
-- Description: Enterprise employee profiles
-- =====================================================
CREATE TABLE enterprise_svc.enterprise_staff (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属企业租户ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联sys_user.id (1:1)',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) COMMENT '职位',
    is_mentor BOOLEAN DEFAULT FALSE COMMENT '是否为导师',
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

-- =====================================================
-- Table: talent_pool
-- Description: Enterprise talent collection
-- =====================================================
CREATE TABLE enterprise_svc.talent_pool (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '企业租户ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    collected_by BIGINT NOT NULL COMMENT '收藏人ID',
    remark VARCHAR(500) COMMENT '备注',
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
-- Table: enterprise_activity
-- Description: Activity feed for enterprise portal
-- =====================================================
CREATE TABLE enterprise_svc.enterprise_activity (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    activity_type VARCHAR(30) NOT NULL COMMENT 'application/interview/report_submitted/evaluation',
    description TEXT NOT NULL,
    ref_type VARCHAR(20) COMMENT 'job/application/intern',
    ref_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_activity_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id)
);

CREATE INDEX idx_activity_tenant ON enterprise_svc.enterprise_activity(tenant_id);
CREATE INDEX idx_activity_created ON enterprise_svc.enterprise_activity(created_at);

COMMENT ON TABLE enterprise_svc.enterprise_activity IS '企业活动动态表';

-- =====================================================
-- Table: enterprise_todo
-- Description: Todo list for enterprise users
-- =====================================================
CREATE TABLE enterprise_svc.enterprise_todo (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    todo_type VARCHAR(30) NOT NULL COMMENT 'application_review/interview_schedule/report_review/evaluation_pending',
    ref_type VARCHAR(20),
    ref_id BIGINT,
    title VARCHAR(200) NOT NULL,
    priority SMALLINT DEFAULT 2,
    due_date TIMESTAMPTZ,
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '0=pending, 1=completed',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_todo_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_todo_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todo_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_todo_user ON enterprise_svc.enterprise_todo(user_id) WHERE status = 0;
CREATE INDEX idx_todo_due ON enterprise_svc.enterprise_todo(due_date) WHERE status = 0;

COMMENT ON TABLE enterprise_svc.enterprise_todo IS '企业待办事项表';

-- =====================================================
-- Table: interview_schedule
-- Description: Interview scheduling and management
-- =====================================================
CREATE TABLE enterprise_svc.interview_schedule (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    interview_time TIMESTAMPTZ NOT NULL,
    location VARCHAR(200),
    interviewer_id BIGINT,
    interview_type VARCHAR(20) COMMENT 'phone/video/onsite',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '0=scheduled, 1=completed, 2=cancelled',
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
