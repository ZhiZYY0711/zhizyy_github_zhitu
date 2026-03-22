-- =====================================================
-- Schema: college_svc
-- Description: College/university management
-- =====================================================

CREATE SCHEMA IF NOT EXISTS college_svc;

-- =====================================================
-- Table: college_info
-- Description: College/university information
-- =====================================================
CREATE TABLE college_svc.college_info (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE COMMENT '关联租户ID',
    college_name VARCHAR(100) NOT NULL COMMENT '高校名称',
    college_code VARCHAR(50) COMMENT '高校代码',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    address VARCHAR(255) COMMENT '详细地址',
    logo_url VARCHAR(255) COMMENT 'Logo URL',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    cooperation_level SMALLINT DEFAULT 1 COMMENT '合作等级: 1=普通 2=重点 3=战略',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 0=禁用',
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

-- =====================================================
-- Table: organization
-- Description: Hierarchical organization structure (college → major → class)
-- =====================================================
CREATE TABLE college_svc.organization (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属高校租户ID',
    parent_id BIGINT COMMENT '父级ID',
    org_type SMALLINT NOT NULL COMMENT '类型: 1=学院 2=专业 3=班级',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    org_code VARCHAR(50) COMMENT '组织编码',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
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
-- Table: enterprise_relationship
-- Description: College-enterprise cooperation relationships
-- =====================================================
CREATE TABLE college_svc.enterprise_relationship (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    enterprise_tenant_id BIGINT NOT NULL,
    cooperation_level SMALLINT DEFAULT 1 COMMENT '1=normal, 2=key, 3=strategic',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=inactive',
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

-- =====================================================
-- Table: enterprise_visit
-- Description: Enterprise visit records for CRM
-- =====================================================
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

-- =====================================================
-- Table: enterprise_audit
-- Description: Enterprise qualification audits by college
-- =====================================================
CREATE TABLE college_svc.enterprise_audit (
    id BIGSERIAL PRIMARY KEY,
    enterprise_tenant_id BIGINT NOT NULL,
    audit_type VARCHAR(20) NOT NULL COMMENT 'registration/qualification/annual',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '0=pending, 1=passed, 2=rejected',
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

-- =====================================================
-- Table: internship_inspection
-- Description: On-site internship inspections by college
-- =====================================================
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
