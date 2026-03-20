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
