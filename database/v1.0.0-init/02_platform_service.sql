-- =====================================================
-- Schema: platform_service
-- Description: Platform-wide services and data dictionaries
-- =====================================================

CREATE SCHEMA IF NOT EXISTS platform_service;

-- =====================================================
-- Table: sys_dict
-- Description: System data dictionary for lookups
-- =====================================================
CREATE TABLE platform_service.sys_dict (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL COMMENT '字典分类',
    code VARCHAR(50) NOT NULL COMMENT '字典编码',
    label VARCHAR(100) NOT NULL COMMENT '字典标签',
    sort_order INTEGER DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT uk_dict_category_code UNIQUE (category, code)
);

CREATE INDEX idx_dict_category ON platform_service.sys_dict(category) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.sys_dict IS '数据字典表';
COMMENT ON COLUMN platform_service.sys_dict.category IS '分类: industry/tech_stack/job_type等';

-- =====================================================
-- Table: sys_tag
-- Description: System-wide tags for categorization
-- =====================================================
CREATE TABLE platform_service.sys_tag (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL COMMENT 'skill/industry/job_type/project_type/course_type',
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

-- =====================================================
-- Table: skill_tree
-- Description: Hierarchical skill tree structure
-- =====================================================
CREATE TABLE platform_service.skill_tree (
    id BIGSERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    skill_category VARCHAR(30) NOT NULL COMMENT 'technical/soft_skill/domain_knowledge',
    parent_id BIGINT,
    level INTEGER DEFAULT 1 COMMENT 'Skill difficulty level',
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

-- =====================================================
-- Table: certificate_template
-- Description: Certificate templates for student achievements
-- =====================================================
CREATE TABLE platform_service.certificate_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    layout_config TEXT COMMENT 'JSON configuration',
    background_url VARCHAR(255),
    signature_urls TEXT COMMENT 'JSON array of signature image URLs',
    variables TEXT COMMENT 'JSON array: ["student_name", "certificate_type", "issue_date", "issuer_name"]',
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_cert_template_usage ON platform_service.certificate_template(usage_count DESC) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.certificate_template IS '证书模板表';

-- =====================================================
-- Table: contract_template
-- Description: Contract templates for internships and training
-- =====================================================
CREATE TABLE platform_service.contract_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    contract_type VARCHAR(30) COMMENT 'internship/training/employment',
    content TEXT COMMENT 'Template content with placeholders',
    variables TEXT COMMENT 'JSON array: ["student_name", "enterprise_name", "position", "duration", "salary"]',
    legal_terms TEXT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contract_template_usage ON platform_service.contract_template(usage_count DESC) WHERE is_deleted = FALSE;

COMMENT ON TABLE platform_service.contract_template IS '合同模板表';

-- =====================================================
-- Table: recommendation_banner
-- Description: Recommendation banners for different portals
-- =====================================================
CREATE TABLE platform_service.recommendation_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NOT NULL,
    target_portal VARCHAR(20) NOT NULL COMMENT 'student/enterprise/college/all',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sort_order INTEGER DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '1=active, 0=inactive',
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

-- =====================================================
-- Table: recommendation_top_list
-- Description: Top lists for mentors, courses, and projects
-- =====================================================
CREATE TABLE platform_service.recommendation_top_list (
    id BIGSERIAL PRIMARY KEY,
    list_type VARCHAR(20) NOT NULL COMMENT 'mentor/course/project',
    item_ids TEXT NOT NULL COMMENT 'JSON array of IDs, ordered by position',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_top_list_type UNIQUE (list_type),
    CONSTRAINT chk_list_type CHECK (list_type IN ('mentor', 'course', 'project'))
);

COMMENT ON TABLE platform_service.recommendation_top_list IS '推荐榜单表';

-- =====================================================
-- Table: operation_log
-- Description: Operation audit logs
-- =====================================================
CREATE TABLE platform_service.operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(50),
    tenant_id BIGINT,
    module VARCHAR(50) COMMENT 'student/enterprise/college/platform',
    operation VARCHAR(100) COMMENT 'create_job/audit_enterprise/etc',
    request_params TEXT,
    response_status INTEGER,
    result VARCHAR(20) COMMENT 'success/failure',
    ip_address VARCHAR(50),
    user_agent TEXT,
    execution_time INTEGER COMMENT 'milliseconds',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oplog_user ON platform_service.operation_log(user_id);
CREATE INDEX idx_oplog_module ON platform_service.operation_log(module);
CREATE INDEX idx_oplog_created ON platform_service.operation_log(created_at);
CREATE INDEX idx_oplog_result ON platform_service.operation_log(result);

COMMENT ON TABLE platform_service.operation_log IS '操作日志表';

-- =====================================================
-- Table: security_log
-- Description: Security event logs
-- =====================================================
CREATE TABLE platform_service.security_log (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(20) NOT NULL COMMENT 'info/warning/critical',
    event_type VARCHAR(50) NOT NULL COMMENT 'login_failed/permission_denied/suspicious_activity/data_breach_attempt',
    user_id BIGINT,
    ip_address VARCHAR(50),
    description TEXT NOT NULL,
    details TEXT COMMENT 'JSON with additional context',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_seclog_level CHECK (level IN ('info', 'warning', 'critical'))
);

CREATE INDEX idx_seclog_level ON platform_service.security_log(level);
CREATE INDEX idx_seclog_event ON platform_service.security_log(event_type);
CREATE INDEX idx_seclog_created ON platform_service.security_log(created_at);
CREATE INDEX idx_seclog_user ON platform_service.security_log(user_id);

COMMENT ON TABLE platform_service.security_log IS '安全日志表';

-- =====================================================
-- Table: service_health
-- Description: Microservice health monitoring
-- =====================================================
CREATE TABLE platform_service.service_health (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL COMMENT 'zhitu-student/zhitu-enterprise/etc',
    status VARCHAR(20) NOT NULL COMMENT 'healthy/degraded/down',
    response_time INTEGER COMMENT 'milliseconds',
    error_rate DECIMAL(5,2) COMMENT 'percentage',
    cpu_usage DECIMAL(5,2) COMMENT 'percentage',
    memory_usage DECIMAL(5,2) COMMENT 'percentage',
    checked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_health_status CHECK (status IN ('healthy', 'degraded', 'down'))
);

CREATE INDEX idx_health_service ON platform_service.service_health(service_name);
CREATE INDEX idx_health_checked ON platform_service.service_health(checked_at);

COMMENT ON TABLE platform_service.service_health IS '服务健康监控表';

-- =====================================================
-- Table: online_user_trend
-- Description: Online user count trends
-- =====================================================
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
