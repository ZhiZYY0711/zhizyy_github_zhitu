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
