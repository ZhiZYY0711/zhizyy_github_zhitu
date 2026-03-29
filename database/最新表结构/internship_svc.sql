/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : internship_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:05:11
*/


-- ----------------------------
-- Sequence structure for attendance_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."attendance_id_seq";
CREATE SEQUENCE "internship_svc"."attendance_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for internship_certificate_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."internship_certificate_id_seq";
CREATE SEQUENCE "internship_svc"."internship_certificate_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for internship_job_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."internship_job_id_seq";
CREATE SEQUENCE "internship_svc"."internship_job_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for internship_offer_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."internship_offer_id_seq";
CREATE SEQUENCE "internship_svc"."internship_offer_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for internship_record_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."internship_record_id_seq";
CREATE SEQUENCE "internship_svc"."internship_record_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for job_application_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."job_application_id_seq";
CREATE SEQUENCE "internship_svc"."job_application_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for weekly_report_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "internship_svc"."weekly_report_id_seq";
CREATE SEQUENCE "internship_svc"."weekly_report_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for attendance
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."attendance";
CREATE TABLE "internship_svc"."attendance" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".attendance_id_seq'::regclass),
  "internship_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "clock_in_time" timestamptz(6),
  "clock_out_time" timestamptz(6),
  "clock_in_lat" numeric(10,7),
  "clock_in_lng" numeric(10,7),
  "status" int2 NOT NULL DEFAULT 0,
  "audit_remark" varchar(255) COLLATE "pg_catalog"."default",
  "audited_by" int8,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "internship_svc"."attendance"."status" IS '0=待审核 1=正常 2=异常';
COMMENT ON TABLE "internship_svc"."attendance" IS '考勤记录表';

-- ----------------------------
-- Table structure for internship_certificate
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."internship_certificate";
CREATE TABLE "internship_svc"."internship_certificate" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".internship_certificate_id_seq'::regclass),
  "internship_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "enterprise_id" int8 NOT NULL,
  "cert_no" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "cert_url" varchar(255) COLLATE "pg_catalog"."default",
  "issued_by" int8 NOT NULL,
  "issued_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "internship_svc"."internship_certificate" IS '实习证明表';

-- ----------------------------
-- Table structure for internship_job
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."internship_job";
CREATE TABLE "internship_svc"."internship_job" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".internship_job_id_seq'::regclass),
  "enterprise_id" int8 NOT NULL,
  "job_title" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "job_type" varchar(20) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "requirements" text COLLATE "pg_catalog"."default",
  "tech_stack" text COLLATE "pg_catalog"."default",
  "industry" varchar(50) COLLATE "pg_catalog"."default",
  "city" varchar(50) COLLATE "pg_catalog"."default",
  "salary_min" int4,
  "salary_max" int4,
  "headcount" int4 DEFAULT 1,
  "start_date" date,
  "end_date" date,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "internship_svc"."internship_job"."tech_stack" IS '技术栈(JSON数组)';
COMMENT ON COLUMN "internship_svc"."internship_job"."status" IS '1=招募中 0=已关闭';
COMMENT ON TABLE "internship_svc"."internship_job" IS '实习岗位表';

-- ----------------------------
-- Table structure for internship_offer
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."internship_offer";
CREATE TABLE "internship_svc"."internship_offer" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".internship_offer_id_seq'::regclass),
  "application_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "enterprise_id" int8 NOT NULL,
  "job_id" int8 NOT NULL,
  "salary" int4,
  "start_date" date,
  "end_date" date,
  "status" int2 NOT NULL DEFAULT 0,
  "college_audit" int2 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "internship_svc"."internship_offer"."status" IS '0=待确认 1=已接受 2=已拒绝';
COMMENT ON COLUMN "internship_svc"."internship_offer"."college_audit" IS '0=待审核 1=通过 2=拒绝';
COMMENT ON TABLE "internship_svc"."internship_offer" IS 'Offer表';

-- ----------------------------
-- Table structure for internship_record
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."internship_record";
CREATE TABLE "internship_svc"."internship_record" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".internship_record_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "enterprise_id" int8 NOT NULL,
  "job_id" int8,
  "mentor_id" int8,
  "teacher_id" int8,
  "start_date" date NOT NULL,
  "end_date" date,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "internship_svc"."internship_record"."mentor_id" IS '企业导师ID';
COMMENT ON COLUMN "internship_svc"."internship_record"."teacher_id" IS '校内指导老师ID';
COMMENT ON COLUMN "internship_svc"."internship_record"."status" IS '1=实习中 2=已结束';
COMMENT ON TABLE "internship_svc"."internship_record" IS '实习记录表';

-- ----------------------------
-- Table structure for job_application
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."job_application";
CREATE TABLE "internship_svc"."job_application" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".job_application_id_seq'::regclass),
  "job_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "resume_url" varchar(255) COLLATE "pg_catalog"."default",
  "cover_letter" text COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 0,
  "applied_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "internship_svc"."job_application"."status" IS '0=待处理 1=面试 2=Offer 3=拒绝 4=录用';
COMMENT ON TABLE "internship_svc"."job_application" IS '求职申请表';

-- ----------------------------
-- Table structure for weekly_report
-- ----------------------------
DROP TABLE IF EXISTS "internship_svc"."weekly_report";
CREATE TABLE "internship_svc"."weekly_report" (
  "id" int8 NOT NULL DEFAULT nextval('"internship_svc".weekly_report_id_seq'::regclass),
  "internship_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "week_start" date NOT NULL,
  "week_end" date NOT NULL,
  "content" text COLLATE "pg_catalog"."default",
  "work_hours" numeric(5,2),
  "status" int2 NOT NULL DEFAULT 0,
  "review_comment" text COLLATE "pg_catalog"."default",
  "reviewed_by" int8,
  "reviewed_at" timestamptz(6),
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "internship_svc"."weekly_report"."status" IS '0=草稿 1=已提交 2=已批阅';
COMMENT ON TABLE "internship_svc"."weekly_report" IS '实习周报表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."attendance_id_seq"
OWNED BY "internship_svc"."attendance"."id";
SELECT setval('"internship_svc"."attendance_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."internship_certificate_id_seq"
OWNED BY "internship_svc"."internship_certificate"."id";
SELECT setval('"internship_svc"."internship_certificate_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."internship_job_id_seq"
OWNED BY "internship_svc"."internship_job"."id";
SELECT setval('"internship_svc"."internship_job_id_seq"', 195, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."internship_offer_id_seq"
OWNED BY "internship_svc"."internship_offer"."id";
SELECT setval('"internship_svc"."internship_offer_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."internship_record_id_seq"
OWNED BY "internship_svc"."internship_record"."id";
SELECT setval('"internship_svc"."internship_record_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."job_application_id_seq"
OWNED BY "internship_svc"."job_application"."id";
SELECT setval('"internship_svc"."job_application_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "internship_svc"."weekly_report_id_seq"
OWNED BY "internship_svc"."weekly_report"."id";
SELECT setval('"internship_svc"."weekly_report_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table attendance
-- ----------------------------
CREATE INDEX "idx_attendance_date" ON "internship_svc"."attendance" USING btree (
  "clock_in_time" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_attendance_internship" ON "internship_svc"."attendance" USING btree (
  "internship_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_attendance_status" ON "internship_svc"."attendance" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_attendance_student" ON "internship_svc"."attendance" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table attendance
-- ----------------------------
ALTER TABLE "internship_svc"."attendance" ADD CONSTRAINT "chk_attendance_status" CHECK (status = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table attendance
-- ----------------------------
ALTER TABLE "internship_svc"."attendance" ADD CONSTRAINT "attendance_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table internship_certificate
-- ----------------------------
CREATE INDEX "idx_cert_internship" ON "internship_svc"."internship_certificate" USING btree (
  "internship_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_cert_no" ON "internship_svc"."internship_certificate" USING btree (
  "cert_no" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_cert_student" ON "internship_svc"."internship_certificate" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table internship_certificate
-- ----------------------------
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "internship_certificate_cert_no_key" UNIQUE ("cert_no");

-- ----------------------------
-- Primary Key structure for table internship_certificate
-- ----------------------------
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "internship_certificate_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table internship_job
-- ----------------------------
CREATE INDEX "idx_job_city" ON "internship_svc"."internship_job" USING btree (
  "city" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_job_enterprise" ON "internship_svc"."internship_job" USING btree (
  "enterprise_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_job_industry" ON "internship_svc"."internship_job" USING btree (
  "industry" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_job_status" ON "internship_svc"."internship_job" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table internship_job
-- ----------------------------
CREATE TRIGGER "update_internship_svc_internship_job_updated_at" BEFORE UPDATE ON "internship_svc"."internship_job"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table internship_job
-- ----------------------------
ALTER TABLE "internship_svc"."internship_job" ADD CONSTRAINT "chk_job_salary" CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min);
ALTER TABLE "internship_svc"."internship_job" ADD CONSTRAINT "chk_job_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table internship_job
-- ----------------------------
ALTER TABLE "internship_svc"."internship_job" ADD CONSTRAINT "internship_job_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table internship_offer
-- ----------------------------
CREATE INDEX "idx_offer_application" ON "internship_svc"."internship_offer" USING btree (
  "application_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_offer_enterprise" ON "internship_svc"."internship_offer" USING btree (
  "enterprise_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_offer_status" ON "internship_svc"."internship_offer" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_offer_student" ON "internship_svc"."internship_offer" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table internship_offer
-- ----------------------------
CREATE TRIGGER "update_internship_svc_internship_offer_updated_at" BEFORE UPDATE ON "internship_svc"."internship_offer"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table internship_offer
-- ----------------------------
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "chk_offer_status" CHECK (status = ANY (ARRAY[0, 1, 2]));
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "chk_offer_audit" CHECK (college_audit = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table internship_offer
-- ----------------------------
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "internship_offer_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table internship_record
-- ----------------------------
CREATE INDEX "idx_record_enterprise" ON "internship_svc"."internship_record" USING btree (
  "enterprise_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_record_mentor" ON "internship_svc"."internship_record" USING btree (
  "mentor_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_record_status" ON "internship_svc"."internship_record" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_record_student" ON "internship_svc"."internship_record" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_record_teacher" ON "internship_svc"."internship_record" USING btree (
  "teacher_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table internship_record
-- ----------------------------
CREATE TRIGGER "update_internship_svc_internship_record_updated_at" BEFORE UPDATE ON "internship_svc"."internship_record"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table internship_record
-- ----------------------------
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "chk_record_status" CHECK (status = ANY (ARRAY[1, 2]));
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "chk_record_dates" CHECK (end_date IS NULL OR end_date >= start_date);

-- ----------------------------
-- Primary Key structure for table internship_record
-- ----------------------------
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "internship_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table job_application
-- ----------------------------
CREATE INDEX "idx_application_job" ON "internship_svc"."job_application" USING btree (
  "job_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_application_status" ON "internship_svc"."job_application" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_application_student" ON "internship_svc"."job_application" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table job_application
-- ----------------------------
CREATE TRIGGER "update_internship_svc_job_application_updated_at" BEFORE UPDATE ON "internship_svc"."job_application"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table job_application
-- ----------------------------
ALTER TABLE "internship_svc"."job_application" ADD CONSTRAINT "uk_application" UNIQUE ("job_id", "student_id");

-- ----------------------------
-- Checks structure for table job_application
-- ----------------------------
ALTER TABLE "internship_svc"."job_application" ADD CONSTRAINT "chk_application_status" CHECK (status = ANY (ARRAY[0, 1, 2, 3, 4]));

-- ----------------------------
-- Primary Key structure for table job_application
-- ----------------------------
ALTER TABLE "internship_svc"."job_application" ADD CONSTRAINT "job_application_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table weekly_report
-- ----------------------------
CREATE INDEX "idx_report_internship" ON "internship_svc"."weekly_report" USING btree (
  "internship_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_report_status" ON "internship_svc"."weekly_report" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_report_student" ON "internship_svc"."weekly_report" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_report_week" ON "internship_svc"."weekly_report" USING btree (
  "week_start" "pg_catalog"."date_ops" ASC NULLS LAST,
  "week_end" "pg_catalog"."date_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table weekly_report
-- ----------------------------
CREATE TRIGGER "update_internship_svc_weekly_report_updated_at" BEFORE UPDATE ON "internship_svc"."weekly_report"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table weekly_report
-- ----------------------------
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "chk_report_dates" CHECK (week_end >= week_start);
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "chk_report_status" CHECK (status = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table weekly_report
-- ----------------------------
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "weekly_report_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table attendance
-- ----------------------------
ALTER TABLE "internship_svc"."attendance" ADD CONSTRAINT "fk_attendance_auditor" FOREIGN KEY ("audited_by") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."attendance" ADD CONSTRAINT "fk_attendance_internship" FOREIGN KEY ("internship_id") REFERENCES "internship_svc"."internship_record" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."attendance" ADD CONSTRAINT "fk_attendance_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table internship_certificate
-- ----------------------------
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "fk_cert_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "fk_cert_internship" FOREIGN KEY ("internship_id") REFERENCES "internship_svc"."internship_record" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "fk_cert_issuer" FOREIGN KEY ("issued_by") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_certificate" ADD CONSTRAINT "fk_cert_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table internship_job
-- ----------------------------
ALTER TABLE "internship_svc"."internship_job" ADD CONSTRAINT "fk_job_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table internship_offer
-- ----------------------------
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "fk_offer_application" FOREIGN KEY ("application_id") REFERENCES "internship_svc"."job_application" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "fk_offer_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "fk_offer_job" FOREIGN KEY ("job_id") REFERENCES "internship_svc"."internship_job" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_offer" ADD CONSTRAINT "fk_offer_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table internship_record
-- ----------------------------
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "fk_record_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "fk_record_job" FOREIGN KEY ("job_id") REFERENCES "internship_svc"."internship_job" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "fk_record_mentor" FOREIGN KEY ("mentor_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "fk_record_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."internship_record" ADD CONSTRAINT "fk_record_teacher" FOREIGN KEY ("teacher_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table job_application
-- ----------------------------
ALTER TABLE "internship_svc"."job_application" ADD CONSTRAINT "fk_application_job" FOREIGN KEY ("job_id") REFERENCES "internship_svc"."internship_job" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."job_application" ADD CONSTRAINT "fk_application_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table weekly_report
-- ----------------------------
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "fk_report_internship" FOREIGN KEY ("internship_id") REFERENCES "internship_svc"."internship_record" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "fk_report_reviewer" FOREIGN KEY ("reviewed_by") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "internship_svc"."weekly_report" ADD CONSTRAINT "fk_report_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
