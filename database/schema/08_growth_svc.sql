-- =====================================================
-- Schema: growth_svc
-- Description: Student growth evaluation and achievements
-- =====================================================

CREATE SCHEMA IF NOT EXISTS growth_svc;

-- =====================================================
-- Table: evaluation_record
-- Description: Multi-source evaluation records
-- =====================================================
CREATE TABLE growth_svc.evaluation_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '被评价学生ID',
    evaluator_id BIGINT NOT NULL COMMENT '评价人ID',
    source_type VARCHAR(20) NOT NULL COMMENT '来源: enterprise/school/peer',
    ref_type VARCHAR(20) COMMENT '关联类型: project/internship',
    ref_id BIGINT COMMENT '关联对象ID',
    scores TEXT COMMENT '评分(JSON格式) 如{"technical":85,"attitude":90}',
    comment TEXT COMMENT '文字评语',
    hire_recommendation VARCHAR(30) COMMENT '录用建议: strongly_recommend/recommend/not_recommend',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_eval_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_eval_source CHECK (source_type IN ('enterprise', 'school', 'peer')),
    CONSTRAINT chk_eval_ref CHECK (ref_type IN ('project', 'internship') OR ref_type IS NULL),
    CONSTRAINT chk_eval_hire CHECK (hire_recommendation IN ('strongly_recommend', 'recommend', 'not_recommend') OR hire_recommendation IS NULL)
);

CREATE INDEX idx_eval_student ON growth_svc.evaluation_record(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_evaluator ON growth_svc.evaluation_record(evaluator_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_source ON growth_svc.evaluation_record(source_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_eval_ref ON growth_svc.evaluation_record(ref_type, ref_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE growth_svc.evaluation_record IS '评价记录表';
COMMENT ON COLUMN growth_svc.evaluation_record.source_type IS 'enterprise=企业评价 school=学校评价 peer=同学互评';

-- =====================================================
-- Table: growth_badge
-- Description: Certificates and badges
-- =====================================================
CREATE TABLE growth_svc.growth_badge (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    type VARCHAR(20) NOT NULL COMMENT '类型: certificate/badge',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    issue_date DATE NOT NULL COMMENT '颁发日期',
    image_url VARCHAR(255) COMMENT '图片URL',
    blockchain_hash VARCHAR(100) COMMENT '区块链存证哈希',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_badge_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_badge_type CHECK (type IN ('certificate', 'badge'))
);

CREATE INDEX idx_badge_student ON growth_svc.growth_badge(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_badge_type ON growth_svc.growth_badge(type) WHERE is_deleted = FALSE;
CREATE INDEX idx_badge_date ON growth_svc.growth_badge(issue_date) WHERE is_deleted = FALSE;

COMMENT ON TABLE growth_svc.growth_badge IS '徽章/证书表';

-- =====================================================
-- Table: warning_record
-- Description: Student warning and intervention records
-- =====================================================
CREATE TABLE growth_svc.warning_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '高校租户ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    warning_type VARCHAR(20) NOT NULL COMMENT '预警类型: attendance/report/evaluation',
    warning_level SMALLINT NOT NULL COMMENT '预警等级: 1=轻微 2=一般 3=严重',
    description TEXT COMMENT '预警描述',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理 1=已干预 2=已关闭',
    intervene_note TEXT COMMENT '干预记录',
    intervened_by BIGINT COMMENT '干预人ID',
    intervened_at TIMESTAMPTZ COMMENT '干预时间',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_warning_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_warning_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_warning_intervener FOREIGN KEY (intervened_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_warning_type CHECK (warning_type IN ('attendance', 'report', 'evaluation')),
    CONSTRAINT chk_warning_level CHECK (warning_level IN (1, 2, 3)),
    CONSTRAINT chk_warning_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_warning_tenant ON growth_svc.warning_record(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_student ON growth_svc.warning_record(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_type ON growth_svc.warning_record(warning_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_level ON growth_svc.warning_record(warning_level) WHERE is_deleted = FALSE;
CREATE INDEX idx_warning_status ON growth_svc.warning_record(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE growth_svc.warning_record IS '预警记录表';
COMMENT ON COLUMN growth_svc.warning_record.warning_type IS 'attendance=考勤异常 report=周报异常 evaluation=评价异常';
