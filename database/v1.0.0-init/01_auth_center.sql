-- =====================================================
-- Schema: auth_center
-- Description: Authentication, user management, and tenant management
-- =====================================================

CREATE SCHEMA IF NOT EXISTS auth_center;

-- =====================================================
-- Table: sys_tenant
-- Description: Multi-tenant organizations (colleges and enterprises)
-- =====================================================
CREATE TABLE auth_center.sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '机构名称',
    type SMALLINT NOT NULL COMMENT '类型: 0=平台 1=高校 2=企业',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 0=待审核 1=正常 2=禁用',
    config TEXT COMMENT '租户配置(JSON格式)',
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

-- =====================================================
-- Table: sys_user
-- Description: System users with role-based access
-- =====================================================
CREATE TABLE auth_center.sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属租户ID (平台管理员为0)',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL COMMENT '用户类型: student/enterprise/college/platform',
    sub_role VARCHAR(20) COMMENT '子角色: hr/mentor/admin(企业) counselor/dean/admin(高校)',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=正常 2=锁定 3=注销',
    last_login_at TIMESTAMPTZ COMMENT '最后登录时间',
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
COMMENT ON COLUMN auth_center.sys_user.role IS 'student=学生 enterprise=企业 college=高校 platform=平台';
COMMENT ON COLUMN auth_center.sys_user.status IS '1=正常 2=锁定 3=注销';

-- =====================================================
-- Table: sys_refresh_token
-- Description: JWT refresh token management
-- =====================================================
CREATE TABLE auth_center.sys_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    token_hash VARCHAR(100) NOT NULL COMMENT 'refresh_token的SHA-256哈希',
    expires_at TIMESTAMPTZ NOT NULL COMMENT '过期时间',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_token_user ON auth_center.sys_refresh_token(user_id);
CREATE INDEX idx_token_expires ON auth_center.sys_refresh_token(expires_at);
CREATE INDEX idx_token_hash ON auth_center.sys_refresh_token(token_hash);

COMMENT ON TABLE auth_center.sys_refresh_token IS 'Refresh Token表';
