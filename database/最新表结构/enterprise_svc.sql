/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : enterprise_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:04:55
*/


-- ----------------------------
-- Sequence structure for enterprise_activity_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."enterprise_activity_id_seq";
CREATE SEQUENCE "enterprise_svc"."enterprise_activity_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."enterprise_info_id_seq";
CREATE SEQUENCE "enterprise_svc"."enterprise_info_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_staff_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."enterprise_staff_id_seq";
CREATE SEQUENCE "enterprise_svc"."enterprise_staff_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_todo_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."enterprise_todo_id_seq";
CREATE SEQUENCE "enterprise_svc"."enterprise_todo_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for interview_schedule_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."interview_schedule_id_seq";
CREATE SEQUENCE "enterprise_svc"."interview_schedule_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for talent_pool_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "enterprise_svc"."talent_pool_id_seq";
CREATE SEQUENCE "enterprise_svc"."talent_pool_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for enterprise_activity
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."enterprise_activity";
CREATE TABLE "enterprise_svc"."enterprise_activity" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".enterprise_activity_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "activity_type" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default" NOT NULL,
  "ref_type" varchar(20) COLLATE "pg_catalog"."default",
  "ref_id" int8,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "enterprise_svc"."enterprise_activity" IS '企业活动动态表';

-- ----------------------------
-- Table structure for enterprise_info
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."enterprise_info";
CREATE TABLE "enterprise_svc"."enterprise_info" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".enterprise_info_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "enterprise_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "enterprise_code" varchar(50) COLLATE "pg_catalog"."default",
  "industry" varchar(50) COLLATE "pg_catalog"."default",
  "scale" varchar(20) COLLATE "pg_catalog"."default",
  "province" varchar(50) COLLATE "pg_catalog"."default",
  "city" varchar(50) COLLATE "pg_catalog"."default",
  "address" varchar(255) COLLATE "pg_catalog"."default",
  "logo_url" varchar(255) COLLATE "pg_catalog"."default",
  "website" varchar(255) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default",
  "contact_name" varchar(50) COLLATE "pg_catalog"."default",
  "contact_phone" varchar(20) COLLATE "pg_catalog"."default",
  "contact_email" varchar(100) COLLATE "pg_catalog"."default",
  "audit_status" int2 NOT NULL DEFAULT 0,
  "audit_remark" varchar(255) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "enterprise_svc"."enterprise_info"."audit_status" IS '0=待审核 1=通过 2=拒绝';
COMMENT ON TABLE "enterprise_svc"."enterprise_info" IS '企业信息表';

-- ----------------------------
-- Table structure for enterprise_staff
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."enterprise_staff";
CREATE TABLE "enterprise_svc"."enterprise_staff" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".enterprise_staff_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "department" varchar(50) COLLATE "pg_catalog"."default",
  "position" varchar(50) COLLATE "pg_catalog"."default",
  "is_mentor" bool DEFAULT false,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "enterprise_svc"."enterprise_staff"."user_id" IS '关联sys_user.id (1:1)';
COMMENT ON TABLE "enterprise_svc"."enterprise_staff" IS '企业员工表';

-- ----------------------------
-- Table structure for enterprise_todo
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."enterprise_todo";
CREATE TABLE "enterprise_svc"."enterprise_todo" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".enterprise_todo_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "user_id" int8 NOT NULL,
  "todo_type" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "ref_type" varchar(20) COLLATE "pg_catalog"."default",
  "ref_id" int8,
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "priority" int2 DEFAULT 2,
  "due_date" timestamptz(6),
  "status" int2 NOT NULL DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "enterprise_svc"."enterprise_todo" IS '企业待办事项表';

-- ----------------------------
-- Table structure for interview_schedule
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."interview_schedule";
CREATE TABLE "enterprise_svc"."interview_schedule" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".interview_schedule_id_seq'::regclass),
  "application_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "enterprise_id" int8 NOT NULL,
  "interview_time" timestamptz(6) NOT NULL,
  "location" varchar(200) COLLATE "pg_catalog"."default",
  "interviewer_id" int8,
  "interview_type" varchar(20) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 0,
  "notes" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "enterprise_svc"."interview_schedule" IS '面试安排表';

-- ----------------------------
-- Table structure for talent_pool
-- ----------------------------
DROP TABLE IF EXISTS "enterprise_svc"."talent_pool";
CREATE TABLE "enterprise_svc"."talent_pool" (
  "id" int8 NOT NULL DEFAULT nextval('"enterprise_svc".talent_pool_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "collected_by" int8 NOT NULL,
  "remark" varchar(500) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "enterprise_svc"."talent_pool"."is_deleted" IS '软删除标记';
COMMENT ON TABLE "enterprise_svc"."talent_pool" IS '企业人才库';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."enterprise_activity_id_seq"
OWNED BY "enterprise_svc"."enterprise_activity"."id";
SELECT setval('"enterprise_svc"."enterprise_activity_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."enterprise_info_id_seq"
OWNED BY "enterprise_svc"."enterprise_info"."id";
SELECT setval('"enterprise_svc"."enterprise_info_id_seq"', 15, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."enterprise_staff_id_seq"
OWNED BY "enterprise_svc"."enterprise_staff"."id";
SELECT setval('"enterprise_svc"."enterprise_staff_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."enterprise_todo_id_seq"
OWNED BY "enterprise_svc"."enterprise_todo"."id";
SELECT setval('"enterprise_svc"."enterprise_todo_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."interview_schedule_id_seq"
OWNED BY "enterprise_svc"."interview_schedule"."id";
SELECT setval('"enterprise_svc"."interview_schedule_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "enterprise_svc"."talent_pool_id_seq"
OWNED BY "enterprise_svc"."talent_pool"."id";
SELECT setval('"enterprise_svc"."talent_pool_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table enterprise_activity
-- ----------------------------
CREATE INDEX "idx_activity_created" ON "enterprise_svc"."enterprise_activity" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_activity_tenant" ON "enterprise_svc"."enterprise_activity" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table enterprise_activity
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_activity" ADD CONSTRAINT "enterprise_activity_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_info
-- ----------------------------
CREATE INDEX "idx_enterprise_audit" ON "enterprise_svc"."enterprise_info" USING btree (
  "audit_status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_enterprise_city" ON "enterprise_svc"."enterprise_info" USING btree (
  "city" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_enterprise_industry" ON "enterprise_svc"."enterprise_info" USING btree (
  "industry" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_enterprise_tenant" ON "enterprise_svc"."enterprise_info" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table enterprise_info
-- ----------------------------
CREATE TRIGGER "update_enterprise_svc_enterprise_info_updated_at" BEFORE UPDATE ON "enterprise_svc"."enterprise_info"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table enterprise_info
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_info" ADD CONSTRAINT "enterprise_info_tenant_id_key" UNIQUE ("tenant_id");

-- ----------------------------
-- Checks structure for table enterprise_info
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_info" ADD CONSTRAINT "chk_enterprise_audit" CHECK (audit_status = ANY (ARRAY[0, 1, 2]));
ALTER TABLE "enterprise_svc"."enterprise_info" ADD CONSTRAINT "chk_enterprise_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table enterprise_info
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_info" ADD CONSTRAINT "enterprise_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_staff
-- ----------------------------
CREATE INDEX "idx_staff_mentor" ON "enterprise_svc"."enterprise_staff" USING btree (
  "is_mentor" "pg_catalog"."bool_ops" ASC NULLS LAST
) WHERE is_mentor = true AND is_deleted = false;
CREATE INDEX "idx_staff_tenant" ON "enterprise_svc"."enterprise_staff" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_staff_user" ON "enterprise_svc"."enterprise_staff" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table enterprise_staff
-- ----------------------------
CREATE TRIGGER "update_enterprise_svc_enterprise_staff_updated_at" BEFORE UPDATE ON "enterprise_svc"."enterprise_staff"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table enterprise_staff
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_staff" ADD CONSTRAINT "enterprise_staff_user_id_key" UNIQUE ("user_id");

-- ----------------------------
-- Primary Key structure for table enterprise_staff
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_staff" ADD CONSTRAINT "enterprise_staff_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_todo
-- ----------------------------
CREATE INDEX "idx_todo_due" ON "enterprise_svc"."enterprise_todo" USING btree (
  "due_date" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
) WHERE status = 0;
CREATE INDEX "idx_todo_user" ON "enterprise_svc"."enterprise_todo" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE status = 0;

-- ----------------------------
-- Checks structure for table enterprise_todo
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_todo" ADD CONSTRAINT "chk_todo_priority" CHECK (priority = ANY (ARRAY[1, 2, 3]));
ALTER TABLE "enterprise_svc"."enterprise_todo" ADD CONSTRAINT "chk_todo_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table enterprise_todo
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_todo" ADD CONSTRAINT "enterprise_todo_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table interview_schedule
-- ----------------------------
CREATE INDEX "idx_interview_application" ON "enterprise_svc"."interview_schedule" USING btree (
  "application_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_interview_student" ON "enterprise_svc"."interview_schedule" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_interview_time" ON "enterprise_svc"."interview_schedule" USING btree (
  "interview_time" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table interview_schedule
-- ----------------------------
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "chk_interview_status" CHECK (status = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table interview_schedule
-- ----------------------------
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "interview_schedule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table talent_pool
-- ----------------------------
CREATE INDEX "idx_talent_deleted" ON "enterprise_svc"."talent_pool" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_talent_student" ON "enterprise_svc"."talent_pool" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_talent_tenant" ON "enterprise_svc"."talent_pool" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table talent_pool
-- ----------------------------
ALTER TABLE "enterprise_svc"."talent_pool" ADD CONSTRAINT "uk_talent_pool" UNIQUE ("tenant_id", "student_id");

-- ----------------------------
-- Primary Key structure for table talent_pool
-- ----------------------------
ALTER TABLE "enterprise_svc"."talent_pool" ADD CONSTRAINT "talent_pool_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table enterprise_activity
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_activity" ADD CONSTRAINT "fk_activity_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_info
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_info" ADD CONSTRAINT "fk_enterprise_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_staff
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_staff" ADD CONSTRAINT "fk_staff_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."enterprise_staff" ADD CONSTRAINT "fk_staff_user" FOREIGN KEY ("user_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_todo
-- ----------------------------
ALTER TABLE "enterprise_svc"."enterprise_todo" ADD CONSTRAINT "fk_todo_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."enterprise_todo" ADD CONSTRAINT "fk_todo_user" FOREIGN KEY ("user_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table interview_schedule
-- ----------------------------
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "fk_interview_application" FOREIGN KEY ("application_id") REFERENCES "internship_svc"."job_application" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "fk_interview_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "fk_interview_interviewer" FOREIGN KEY ("interviewer_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."interview_schedule" ADD CONSTRAINT "fk_interview_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table talent_pool
-- ----------------------------
ALTER TABLE "enterprise_svc"."talent_pool" ADD CONSTRAINT "fk_talent_collector" FOREIGN KEY ("collected_by") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."talent_pool" ADD CONSTRAINT "fk_talent_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "enterprise_svc"."talent_pool" ADD CONSTRAINT "fk_talent_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
