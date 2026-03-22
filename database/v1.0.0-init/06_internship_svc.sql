-- =====================================================
-- Schema: internship_svc
-- Description: Internship management, applications, and tracking
-- =====================================================

CREATE SCHEMA IF NOT EXISTS internship_svc;

-- =====================================================
-- Table: internship_job
-- Description: Internship job postings
-- =====================================================
CREATE TABLE internship_svc.internship_job (
    id BIGSERIAL PRIMARY KEY,
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    job_title VARCHAR(100) NOT NULL COMMENT '岗位名称',
    job_type VARCHAR(20) COMMENT '岗位类型',
    description TEXT COMMENT '岗位描述',
    requirements TEXT COMMENT '岗位要求',
    tech_stack TEXT COMMENT '技术栈(JSON数组)',
    industry VARCHAR(50) COMMENT '行业',
    city VARCHAR(50) COMMENT '工作城市',
    salary_min INTEGER COMMENT '最低薪资',
    salary_max INTEGER COMMENT '最高薪资',
    headcount INTEGER DEFAULT 1 COMMENT '招聘人数',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=招募中 0=已关闭',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_job_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT chk_job_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_job_salary CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min)
);

CREATE INDEX idx_job_enterprise ON internship_svc.internship_job(enterprise_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_status ON internship_svc.internship_job(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_city ON internship_svc.internship_job(city) WHERE is_deleted = FALSE;
CREATE INDEX idx_job_industry ON internship_svc.internship_job(industry) WHERE is_deleted = FALSE;

COMMENT ON TABLE internship_svc.internship_job IS '实习岗位表';

-- =====================================================
-- Table: job_application
-- Description: Student job applications
-- =====================================================
CREATE TABLE internship_svc.job_application (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    resume_url VARCHAR(255) COMMENT '简历URL',
    cover_letter TEXT COMMENT '求职信',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理 1=面试 2=Offer 3=拒绝 4=录用',
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT fk_application_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_application_status CHECK (status IN (0, 1, 2, 3, 4)),
    CONSTRAINT uk_application UNIQUE (job_id, student_id)
);

CREATE INDEX idx_application_job ON internship_svc.job_application(job_id);
CREATE INDEX idx_application_student ON internship_svc.job_application(student_id);
CREATE INDEX idx_application_status ON internship_svc.job_application(status);

COMMENT ON TABLE internship_svc.job_application IS '求职申请表';
COMMENT ON COLUMN internship_svc.job_application.status IS '0=待处理 1=面试 2=Offer 3=拒绝 4=录用';

-- =====================================================
-- Table: internship_offer
-- Description: Internship offers sent to students
-- =====================================================
CREATE TABLE internship_svc.internship_offer (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    job_id BIGINT NOT NULL COMMENT '岗位ID',
    salary INTEGER COMMENT '薪资',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待确认 1=已接受 2=已拒绝',
    college_audit SMALLINT DEFAULT 0 COMMENT '高校审核: 0=待审核 1=通过 2=拒绝',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_offer_application FOREIGN KEY (application_id) REFERENCES internship_svc.job_application(id),
    CONSTRAINT fk_offer_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_offer_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_offer_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT chk_offer_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_offer_audit CHECK (college_audit IN (0, 1, 2))
);

CREATE INDEX idx_offer_application ON internship_svc.internship_offer(application_id);
CREATE INDEX idx_offer_student ON internship_svc.internship_offer(student_id);
CREATE INDEX idx_offer_enterprise ON internship_svc.internship_offer(enterprise_id);
CREATE INDEX idx_offer_status ON internship_svc.internship_offer(status);

COMMENT ON TABLE internship_svc.internship_offer IS 'Offer表';

-- =====================================================
-- Table: internship_record
-- Description: Active internship records
-- =====================================================
CREATE TABLE internship_svc.internship_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    job_id BIGINT COMMENT '岗位ID',
    mentor_id BIGINT COMMENT '企业导师ID',
    teacher_id BIGINT COMMENT '校内指导老师ID',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status SMALLINT NOT NULL DEFAULT 1 COMMENT '状态: 1=实习中 2=已结束',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_record_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_record_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_record_job FOREIGN KEY (job_id) REFERENCES internship_svc.internship_job(id),
    CONSTRAINT fk_record_mentor FOREIGN KEY (mentor_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT fk_record_teacher FOREIGN KEY (teacher_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_record_status CHECK (status IN (1, 2)),
    CONSTRAINT chk_record_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_record_student ON internship_svc.internship_record(student_id);
CREATE INDEX idx_record_enterprise ON internship_svc.internship_record(enterprise_id);
CREATE INDEX idx_record_status ON internship_svc.internship_record(status);
CREATE INDEX idx_record_mentor ON internship_svc.internship_record(mentor_id);
CREATE INDEX idx_record_teacher ON internship_svc.internship_record(teacher_id);

COMMENT ON TABLE internship_svc.internship_record IS '实习记录表';

-- =====================================================
-- Table: weekly_report
-- Description: Weekly internship reports
-- =====================================================
CREATE TABLE internship_svc.weekly_report (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL COMMENT '实习记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    week_start DATE NOT NULL COMMENT '周开始日期',
    week_end DATE NOT NULL COMMENT '周结束日期',
    content TEXT COMMENT '周报内容',
    work_hours DECIMAL(5,2) COMMENT '工作时长',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿 1=已提交 2=已批阅',
    review_comment TEXT COMMENT '批阅评语',
    reviewed_by BIGINT COMMENT '批阅人ID',
    reviewed_at TIMESTAMPTZ COMMENT '批阅时间',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_report_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_report_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_report_reviewer FOREIGN KEY (reviewed_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_report_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT chk_report_dates CHECK (week_end >= week_start)
);

CREATE INDEX idx_report_internship ON internship_svc.weekly_report(internship_id);
CREATE INDEX idx_report_student ON internship_svc.weekly_report(student_id);
CREATE INDEX idx_report_status ON internship_svc.weekly_report(status);
CREATE INDEX idx_report_week ON internship_svc.weekly_report(week_start, week_end);

COMMENT ON TABLE internship_svc.weekly_report IS '实习周报表';

-- =====================================================
-- Table: attendance
-- Description: Clock-in/out records with GPS
-- =====================================================
CREATE TABLE internship_svc.attendance (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL COMMENT '实习记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    clock_in_time TIMESTAMPTZ COMMENT '打卡时间',
    clock_out_time TIMESTAMPTZ COMMENT '下班打卡时间',
    clock_in_lat DECIMAL(10,7) COMMENT '打卡纬度',
    clock_in_lng DECIMAL(10,7) COMMENT '打卡经度',
    status SMALLINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待审核 1=正常 2=异常',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    audited_by BIGINT COMMENT '审核人ID',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_attendance_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_attendance_auditor FOREIGN KEY (audited_by) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_attendance_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_attendance_internship ON internship_svc.attendance(internship_id);
CREATE INDEX idx_attendance_student ON internship_svc.attendance(student_id);
CREATE INDEX idx_attendance_date ON internship_svc.attendance(clock_in_time);
CREATE INDEX idx_attendance_status ON internship_svc.attendance(status);

COMMENT ON TABLE internship_svc.attendance IS '考勤记录表';

-- =====================================================
-- Table: internship_certificate
-- Description: Internship completion certificates
-- =====================================================
CREATE TABLE internship_svc.internship_certificate (
    id BIGSERIAL PRIMARY KEY,
    internship_id BIGINT NOT NULL COMMENT '实习记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    enterprise_id BIGINT NOT NULL COMMENT '企业ID',
    cert_no VARCHAR(50) NOT NULL UNIQUE COMMENT '证书编号',
    cert_url VARCHAR(255) COMMENT '证书URL',
    issued_by BIGINT NOT NULL COMMENT '颁发人ID',
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cert_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_cert_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_cert_issuer FOREIGN KEY (issued_by) REFERENCES auth_center.sys_user(id)
);

CREATE INDEX idx_cert_internship ON internship_svc.internship_certificate(internship_id);
CREATE INDEX idx_cert_student ON internship_svc.internship_certificate(student_id);
CREATE INDEX idx_cert_no ON internship_svc.internship_certificate(cert_no);

COMMENT ON TABLE internship_svc.internship_certificate IS '实习证明表';
