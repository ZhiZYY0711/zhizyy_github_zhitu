/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : training_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:05:37
*/


-- ----------------------------
-- Sequence structure for project_enrollment_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "training_svc"."project_enrollment_id_seq";
CREATE SEQUENCE "training_svc"."project_enrollment_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for project_task_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "training_svc"."project_task_id_seq";
CREATE SEQUENCE "training_svc"."project_task_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for training_plan_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "training_svc"."training_plan_id_seq";
CREATE SEQUENCE "training_svc"."training_plan_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for training_project_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "training_svc"."training_project_id_seq";
CREATE SEQUENCE "training_svc"."training_project_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for project_enrollment
-- ----------------------------
DROP TABLE IF EXISTS "training_svc"."project_enrollment";
CREATE TABLE "training_svc"."project_enrollment" (
  "id" int8 NOT NULL DEFAULT nextval('"training_svc".project_enrollment_id_seq'::regclass),
  "project_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "team_id" int8,
  "role" varchar(20) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "enrolled_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "training_svc"."project_enrollment" IS '项目报名表';

-- ----------------------------
-- Table structure for project_task
-- ----------------------------
DROP TABLE IF EXISTS "training_svc"."project_task";
CREATE TABLE "training_svc"."project_task" (
  "id" int8 NOT NULL DEFAULT nextval('"training_svc".project_task_id_seq'::regclass),
  "project_id" int8 NOT NULL,
  "team_id" int8,
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "assignee_id" int8,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 'todo'::character varying,
  "priority" int2 DEFAULT 2,
  "story_points" int4,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "training_svc"."project_task" IS '项目任务看板表';

-- ----------------------------
-- Table structure for training_plan
-- ----------------------------
DROP TABLE IF EXISTS "training_svc"."training_plan";
CREATE TABLE "training_svc"."training_plan" (
  "id" int8 NOT NULL DEFAULT nextval('"training_svc".training_plan_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "project_id" int8 NOT NULL,
  "plan_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "start_date" date NOT NULL,
  "end_date" date NOT NULL,
  "teacher_id" int8,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "training_svc"."training_plan"."status" IS '1=计划中 2=进行中 3=已完成';
COMMENT ON TABLE "training_svc"."training_plan" IS '实训排期计划表';

-- ----------------------------
-- Table structure for training_project
-- ----------------------------
DROP TABLE IF EXISTS "training_svc"."training_project";
CREATE TABLE "training_svc"."training_project" (
  "id" int8 NOT NULL DEFAULT nextval('"training_svc".training_project_id_seq'::regclass),
  "enterprise_id" int8 NOT NULL,
  "project_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "tech_stack" text COLLATE "pg_catalog"."default",
  "industry" varchar(50) COLLATE "pg_catalog"."default",
  "max_teams" int4 DEFAULT 10,
  "max_members" int4 DEFAULT 6,
  "start_date" date,
  "end_date" date,
  "audit_status" int2 NOT NULL DEFAULT 0,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "training_svc"."training_project"."audit_status" IS '0=待审核 1=通过 2=拒绝';
COMMENT ON COLUMN "training_svc"."training_project"."status" IS '1=招募中 2=进行中 3=已结束';
COMMENT ON TABLE "training_svc"."training_project" IS '实训项目表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "training_svc"."project_enrollment_id_seq"
OWNED BY "training_svc"."project_enrollment"."id";
SELECT setval('"training_svc"."project_enrollment_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "training_svc"."project_task_id_seq"
OWNED BY "training_svc"."project_task"."id";
SELECT setval('"training_svc"."project_task_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "training_svc"."training_plan_id_seq"
OWNED BY "training_svc"."training_plan"."id";
SELECT setval('"training_svc"."training_plan_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "training_svc"."training_project_id_seq"
OWNED BY "training_svc"."training_project"."id";
SELECT setval('"training_svc"."training_project_id_seq"', 30, true);

-- ----------------------------
-- Indexes structure for table project_enrollment
-- ----------------------------
CREATE INDEX "idx_enroll_project" ON "training_svc"."project_enrollment" USING btree (
  "project_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_enroll_student" ON "training_svc"."project_enrollment" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table project_enrollment
-- ----------------------------
ALTER TABLE "training_svc"."project_enrollment" ADD CONSTRAINT "uk_enrollment" UNIQUE ("project_id", "student_id");

-- ----------------------------
-- Checks structure for table project_enrollment
-- ----------------------------
ALTER TABLE "training_svc"."project_enrollment" ADD CONSTRAINT "chk_enroll_status" CHECK (status = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table project_enrollment
-- ----------------------------
ALTER TABLE "training_svc"."project_enrollment" ADD CONSTRAINT "project_enrollment_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table project_task
-- ----------------------------
CREATE INDEX "idx_ptask_assignee" ON "training_svc"."project_task" USING btree (
  "assignee_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_ptask_project" ON "training_svc"."project_task" USING btree (
  "project_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_ptask_status" ON "training_svc"."project_task" USING btree (
  "status" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Checks structure for table project_task
-- ----------------------------
ALTER TABLE "training_svc"."project_task" ADD CONSTRAINT "chk_ptask_priority" CHECK (priority = ANY (ARRAY[1, 2, 3]));
ALTER TABLE "training_svc"."project_task" ADD CONSTRAINT "chk_ptask_status" CHECK (status::text = ANY (ARRAY['todo'::character varying, 'in_progress'::character varying, 'done'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table project_task
-- ----------------------------
ALTER TABLE "training_svc"."project_task" ADD CONSTRAINT "project_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table training_plan
-- ----------------------------
CREATE INDEX "idx_plan_project" ON "training_svc"."training_plan" USING btree (
  "project_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_plan_status" ON "training_svc"."training_plan" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_plan_teacher" ON "training_svc"."training_plan" USING btree (
  "teacher_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_plan_tenant" ON "training_svc"."training_plan" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table training_plan
-- ----------------------------
CREATE TRIGGER "update_training_svc_training_plan_updated_at" BEFORE UPDATE ON "training_svc"."training_plan"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table training_plan
-- ----------------------------
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "chk_plan_dates" CHECK (end_date >= start_date);
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "chk_plan_status" CHECK (status = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table training_plan
-- ----------------------------
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "training_plan_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table training_project
-- ----------------------------
CREATE INDEX "idx_project_audit" ON "training_svc"."training_project" USING btree (
  "audit_status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_project_enterprise" ON "training_svc"."training_project" USING btree (
  "enterprise_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_project_status" ON "training_svc"."training_project" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table training_project
-- ----------------------------
CREATE TRIGGER "update_training_svc_training_project_updated_at" BEFORE UPDATE ON "training_svc"."training_project"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table training_project
-- ----------------------------
ALTER TABLE "training_svc"."training_project" ADD CONSTRAINT "chk_project_audit" CHECK (audit_status = ANY (ARRAY[0, 1, 2]));
ALTER TABLE "training_svc"."training_project" ADD CONSTRAINT "chk_project_dates" CHECK (end_date IS NULL OR end_date >= start_date);
ALTER TABLE "training_svc"."training_project" ADD CONSTRAINT "chk_project_status" CHECK (status = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table training_project
-- ----------------------------
ALTER TABLE "training_svc"."training_project" ADD CONSTRAINT "training_project_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table project_enrollment
-- ----------------------------
ALTER TABLE "training_svc"."project_enrollment" ADD CONSTRAINT "fk_enroll_project" FOREIGN KEY ("project_id") REFERENCES "training_svc"."training_project" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "training_svc"."project_enrollment" ADD CONSTRAINT "fk_enroll_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table project_task
-- ----------------------------
ALTER TABLE "training_svc"."project_task" ADD CONSTRAINT "fk_ptask_assignee" FOREIGN KEY ("assignee_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "training_svc"."project_task" ADD CONSTRAINT "fk_ptask_project" FOREIGN KEY ("project_id") REFERENCES "training_svc"."training_project" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table training_plan
-- ----------------------------
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "fk_plan_project" FOREIGN KEY ("project_id") REFERENCES "training_svc"."training_project" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "fk_plan_teacher" FOREIGN KEY ("teacher_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "training_svc"."training_plan" ADD CONSTRAINT "fk_plan_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table training_project
-- ----------------------------
ALTER TABLE "training_svc"."training_project" ADD CONSTRAINT "fk_project_enterprise" FOREIGN KEY ("enterprise_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
