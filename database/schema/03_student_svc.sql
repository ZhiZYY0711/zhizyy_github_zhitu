-- =====================================================
-- Schema: student_svc
-- Description: Student profiles and information
-- =====================================================

CREATE SCHEMA IF NOT EXISTS student_svc;

-- =====================================================
-- Table: student_info
-- Description: Student profile and academic information
-- =====================================================
CREATE TABLE student_svc.student_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联sys_user.id (1:1)',
    tenant_id BIGINT NOT NULL COMMENT '所属高校租户ID',
    student_no VARCHAR(50) NOT NULL COMMENT '学号',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    gender SMALLINT COMMENT '性别: 1=男 2=女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    college_id BIGINT COMMENT '学院ID',
    major_id BIGINT COMMENT '专业ID',
    class_id BIGINT COMMENT '班级ID',
    grade VARCHAR(20) COMMENT '年级',
    enrollment_date DATE COMMENT '入学日期',
    graduation_date DATE COMMENT '毕业日期',
    resume_url VARCHAR(255) COMMENT '简历URL',
    skills TEXT COMMENT '技能标签(JSON数组)',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT fk_student_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_student_gender CHECK (gender IN (1, 2))
);

CREATE INDEX idx_student_user ON student_svc.student_info(user_id);
CREATE INDEX idx_student_tenant ON student_svc.student_info(tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_no ON student_svc.student_info(student_no) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_class ON student_svc.student_info(class_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_student_major ON student_svc.student_info(major_id) WHERE is_deleted = FALSE;

COMMENT ON TABLE student_svc.student_info IS '学生档案表';
COMMENT ON COLUMN student_svc.student_info.user_id IS '关联auth_center.sys_user.id';
