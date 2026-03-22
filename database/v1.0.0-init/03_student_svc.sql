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

-- =====================================================
-- Table: student_task
-- Description: Student tasks for training, internship, and evaluation
-- =====================================================
CREATE TABLE student_svc.student_task (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    task_type VARCHAR(20) NOT NULL COMMENT 'training/internship/evaluation',
    ref_id BIGINT COMMENT 'Reference to project/job/etc',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority SMALLINT DEFAULT 1 COMMENT '1=low, 2=medium, 3=high',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '0=pending, 1=completed',
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

-- =====================================================
-- Table: student_capability
-- Description: Student capability scores across dimensions
-- =====================================================
CREATE TABLE student_svc.student_capability (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    dimension VARCHAR(50) NOT NULL COMMENT 'technical/communication/teamwork/problem_solving/leadership',
    score INTEGER NOT NULL COMMENT '0-100',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_capability_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_capability UNIQUE (student_id, dimension),
    CONSTRAINT chk_capability_score CHECK (score >= 0 AND score <= 100)
);

CREATE INDEX idx_capability_student ON student_svc.student_capability(student_id);

COMMENT ON TABLE student_svc.student_capability IS '学生能力雷达图数据表';

-- =====================================================
-- Table: student_recommendation
-- Description: Personalized recommendations for students
-- =====================================================
CREATE TABLE student_svc.student_recommendation (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    rec_type VARCHAR(20) NOT NULL COMMENT 'project/job/course',
    ref_id BIGINT NOT NULL COMMENT 'ID of recommended item',
    score DECIMAL(5,2) COMMENT 'Recommendation score',
    reason TEXT COMMENT 'Why recommended',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_rec_type CHECK (rec_type IN ('project', 'job', 'course'))
);

CREATE INDEX idx_rec_student ON student_svc.student_recommendation(student_id);
CREATE INDEX idx_rec_type ON student_svc.student_recommendation(rec_type);
CREATE INDEX idx_rec_created ON student_svc.student_recommendation(created_at);

COMMENT ON TABLE student_svc.student_recommendation IS '学生个性化推荐表';
