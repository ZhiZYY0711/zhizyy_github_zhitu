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
