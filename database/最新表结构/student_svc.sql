/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : student_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:05:29
*/


-- ----------------------------
-- Sequence structure for student_capability_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "student_svc"."student_capability_id_seq";
CREATE SEQUENCE "student_svc"."student_capability_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for student_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "student_svc"."student_info_id_seq";
CREATE SEQUENCE "student_svc"."student_info_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for student_recommendation_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "student_svc"."student_recommendation_id_seq";
CREATE SEQUENCE "student_svc"."student_recommendation_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for student_task_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "student_svc"."student_task_id_seq";
CREATE SEQUENCE "student_svc"."student_task_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for student_capability
-- ----------------------------
DROP TABLE IF EXISTS "student_svc"."student_capability";
CREATE TABLE "student_svc"."student_capability" (
  "id" int8 NOT NULL DEFAULT nextval('"student_svc".student_capability_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "dimension" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "score" int4 NOT NULL,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "student_svc"."student_capability" IS '学生能力雷达图数据表';

-- ----------------------------
-- Table structure for student_info
-- ----------------------------
DROP TABLE IF EXISTS "student_svc"."student_info";
CREATE TABLE "student_svc"."student_info" (
  "id" int8 NOT NULL DEFAULT nextval('"student_svc".student_info_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "tenant_id" int8 NOT NULL,
  "student_no" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "real_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "gender" int2,
  "phone" varchar(20) COLLATE "pg_catalog"."default",
  "email" varchar(100) COLLATE "pg_catalog"."default",
  "avatar_url" varchar(255) COLLATE "pg_catalog"."default",
  "college_id" int8,
  "major_id" int8,
  "class_id" int8,
  "grade" varchar(20) COLLATE "pg_catalog"."default",
  "enrollment_date" date,
  "graduation_date" date,
  "resume_url" varchar(255) COLLATE "pg_catalog"."default",
  "skills" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "student_svc"."student_info"."user_id" IS '关联auth_center.sys_user.id (1:1)';
COMMENT ON COLUMN "student_svc"."student_info"."gender" IS '1=男 2=女';
COMMENT ON COLUMN "student_svc"."student_info"."skills" IS '技能标签(JSON数组)';
COMMENT ON TABLE "student_svc"."student_info" IS '学生档案表';

-- ----------------------------
-- Table structure for student_recommendation
-- ----------------------------
DROP TABLE IF EXISTS "student_svc"."student_recommendation";
CREATE TABLE "student_svc"."student_recommendation" (
  "id" int8 NOT NULL DEFAULT nextval('"student_svc".student_recommendation_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "rec_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ref_id" int8 NOT NULL,
  "score" numeric(5,2),
  "reason" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "student_svc"."student_recommendation" IS '学生个性化推荐表';

-- ----------------------------
-- Table structure for student_task
-- ----------------------------
DROP TABLE IF EXISTS "student_svc"."student_task";
CREATE TABLE "student_svc"."student_task" (
  "id" int8 NOT NULL DEFAULT nextval('"student_svc".student_task_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "task_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ref_id" int8,
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "priority" int2 DEFAULT 1,
  "status" int2 NOT NULL DEFAULT 0,
  "due_date" timestamptz(6),
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "student_svc"."student_task" IS '学生任务表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "student_svc"."student_capability_id_seq"
OWNED BY "student_svc"."student_capability"."id";
SELECT setval('"student_svc"."student_capability_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "student_svc"."student_info_id_seq"
OWNED BY "student_svc"."student_info"."id";
SELECT setval('"student_svc"."student_info_id_seq"', 2000, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "student_svc"."student_recommendation_id_seq"
OWNED BY "student_svc"."student_recommendation"."id";
SELECT setval('"student_svc"."student_recommendation_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "student_svc"."student_task_id_seq"
OWNED BY "student_svc"."student_task"."id";
SELECT setval('"student_svc"."student_task_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table student_capability
-- ----------------------------
CREATE INDEX "idx_capability_student" ON "student_svc"."student_capability" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table student_capability
-- ----------------------------
ALTER TABLE "student_svc"."student_capability" ADD CONSTRAINT "uk_capability" UNIQUE ("student_id", "dimension");

-- ----------------------------
-- Checks structure for table student_capability
-- ----------------------------
ALTER TABLE "student_svc"."student_capability" ADD CONSTRAINT "chk_capability_score" CHECK (score >= 0 AND score <= 100);

-- ----------------------------
-- Primary Key structure for table student_capability
-- ----------------------------
ALTER TABLE "student_svc"."student_capability" ADD CONSTRAINT "student_capability_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table student_info
-- ----------------------------
CREATE INDEX "idx_student_class" ON "student_svc"."student_info" USING btree (
  "class_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_student_major" ON "student_svc"."student_info" USING btree (
  "major_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_student_no" ON "student_svc"."student_info" USING btree (
  "student_no" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_student_tenant" ON "student_svc"."student_info" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_student_user" ON "student_svc"."student_info" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table student_info
-- ----------------------------
CREATE TRIGGER "update_student_svc_student_info_updated_at" BEFORE UPDATE ON "student_svc"."student_info"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table student_info
-- ----------------------------
ALTER TABLE "student_svc"."student_info" ADD CONSTRAINT "student_info_user_id_key" UNIQUE ("user_id");

-- ----------------------------
-- Checks structure for table student_info
-- ----------------------------
ALTER TABLE "student_svc"."student_info" ADD CONSTRAINT "chk_student_gender" CHECK (gender = ANY (ARRAY[1, 2]));

-- ----------------------------
-- Primary Key structure for table student_info
-- ----------------------------
ALTER TABLE "student_svc"."student_info" ADD CONSTRAINT "student_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table student_recommendation
-- ----------------------------
CREATE INDEX "idx_rec_created" ON "student_svc"."student_recommendation" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rec_student" ON "student_svc"."student_recommendation" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_rec_type" ON "student_svc"."student_recommendation" USING btree (
  "rec_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table student_recommendation
-- ----------------------------
ALTER TABLE "student_svc"."student_recommendation" ADD CONSTRAINT "chk_rec_type" CHECK (rec_type::text = ANY (ARRAY['project'::character varying, 'job'::character varying, 'course'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table student_recommendation
-- ----------------------------
ALTER TABLE "student_svc"."student_recommendation" ADD CONSTRAINT "student_recommendation_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table student_task
-- ----------------------------
CREATE INDEX "idx_task_due" ON "student_svc"."student_task" USING btree (
  "due_date" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_task_status" ON "student_svc"."student_task" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_task_student" ON "student_svc"."student_task" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Checks structure for table student_task
-- ----------------------------
ALTER TABLE "student_svc"."student_task" ADD CONSTRAINT "chk_task_priority" CHECK (priority = ANY (ARRAY[1, 2, 3]));
ALTER TABLE "student_svc"."student_task" ADD CONSTRAINT "chk_task_status" CHECK (status = ANY (ARRAY[0, 1]));
ALTER TABLE "student_svc"."student_task" ADD CONSTRAINT "chk_task_type" CHECK (task_type::text = ANY (ARRAY['training'::character varying, 'internship'::character varying, 'evaluation'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table student_task
-- ----------------------------
ALTER TABLE "student_svc"."student_task" ADD CONSTRAINT "student_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table student_capability
-- ----------------------------
ALTER TABLE "student_svc"."student_capability" ADD CONSTRAINT "fk_capability_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table student_info
-- ----------------------------
ALTER TABLE "student_svc"."student_info" ADD CONSTRAINT "fk_student_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "student_svc"."student_info" ADD CONSTRAINT "fk_student_user" FOREIGN KEY ("user_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table student_recommendation
-- ----------------------------
ALTER TABLE "student_svc"."student_recommendation" ADD CONSTRAINT "fk_rec_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table student_task
-- ----------------------------
ALTER TABLE "student_svc"."student_task" ADD CONSTRAINT "fk_task_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
