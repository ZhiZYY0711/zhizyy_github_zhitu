/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : growth_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:05:04
*/


-- ----------------------------
-- Sequence structure for evaluation_record_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "growth_svc"."evaluation_record_id_seq";
CREATE SEQUENCE "growth_svc"."evaluation_record_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for growth_badge_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "growth_svc"."growth_badge_id_seq";
CREATE SEQUENCE "growth_svc"."growth_badge_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for warning_record_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "growth_svc"."warning_record_id_seq";
CREATE SEQUENCE "growth_svc"."warning_record_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for evaluation_record
-- ----------------------------
DROP TABLE IF EXISTS "growth_svc"."evaluation_record";
CREATE TABLE "growth_svc"."evaluation_record" (
  "id" int8 NOT NULL DEFAULT nextval('"growth_svc".evaluation_record_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "evaluator_id" int8 NOT NULL,
  "source_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "ref_type" varchar(20) COLLATE "pg_catalog"."default",
  "ref_id" int8,
  "scores" text COLLATE "pg_catalog"."default",
  "comment" text COLLATE "pg_catalog"."default",
  "hire_recommendation" varchar(30) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "growth_svc"."evaluation_record"."source_type" IS 'enterprise=企业评价 school=学校评价 peer=同学互评';
COMMENT ON COLUMN "growth_svc"."evaluation_record"."scores" IS '评分JSON: {"technical":85,"attitude":90}';
COMMENT ON COLUMN "growth_svc"."evaluation_record"."hire_recommendation" IS 'strongly_recommend/recommend/not_recommend';
COMMENT ON TABLE "growth_svc"."evaluation_record" IS '评价记录表';

-- ----------------------------
-- Table structure for growth_badge
-- ----------------------------
DROP TABLE IF EXISTS "growth_svc"."growth_badge";
CREATE TABLE "growth_svc"."growth_badge" (
  "id" int8 NOT NULL DEFAULT nextval('"growth_svc".growth_badge_id_seq'::regclass),
  "student_id" int8 NOT NULL,
  "type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "issue_date" date NOT NULL,
  "image_url" varchar(255) COLLATE "pg_catalog"."default",
  "blockchain_hash" varchar(100) COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "growth_svc"."growth_badge"."type" IS 'certificate=证书 badge=徽章';
COMMENT ON TABLE "growth_svc"."growth_badge" IS '徽章/证书表';

-- ----------------------------
-- Table structure for warning_record
-- ----------------------------
DROP TABLE IF EXISTS "growth_svc"."warning_record";
CREATE TABLE "growth_svc"."warning_record" (
  "id" int8 NOT NULL DEFAULT nextval('"growth_svc".warning_record_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "student_id" int8 NOT NULL,
  "warning_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "warning_level" int2 NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 0,
  "intervene_note" text COLLATE "pg_catalog"."default",
  "intervened_by" int8,
  "intervened_at" timestamptz(6),
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "growth_svc"."warning_record"."warning_type" IS 'attendance=考勤异常 report=周报异常 evaluation=评价异常';
COMMENT ON COLUMN "growth_svc"."warning_record"."warning_level" IS '1=轻微 2=一般 3=严重';
COMMENT ON COLUMN "growth_svc"."warning_record"."status" IS '0=待处理 1=已干预 2=已关闭';
COMMENT ON TABLE "growth_svc"."warning_record" IS '预警记录表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "growth_svc"."evaluation_record_id_seq"
OWNED BY "growth_svc"."evaluation_record"."id";
SELECT setval('"growth_svc"."evaluation_record_id_seq"', 3003, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "growth_svc"."growth_badge_id_seq"
OWNED BY "growth_svc"."growth_badge"."id";
SELECT setval('"growth_svc"."growth_badge_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "growth_svc"."warning_record_id_seq"
OWNED BY "growth_svc"."warning_record"."id";
SELECT setval('"growth_svc"."warning_record_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table evaluation_record
-- ----------------------------
CREATE INDEX "idx_eval_evaluator" ON "growth_svc"."evaluation_record" USING btree (
  "evaluator_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_eval_ref" ON "growth_svc"."evaluation_record" USING btree (
  "ref_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "ref_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_eval_source" ON "growth_svc"."evaluation_record" USING btree (
  "source_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_eval_student" ON "growth_svc"."evaluation_record" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table evaluation_record
-- ----------------------------
CREATE TRIGGER "update_growth_svc_evaluation_record_updated_at" BEFORE UPDATE ON "growth_svc"."evaluation_record"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table evaluation_record
-- ----------------------------
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "chk_eval_hire" CHECK ((hire_recommendation::text = ANY (ARRAY['strongly_recommend'::character varying, 'recommend'::character varying, 'not_recommend'::character varying]::text[])) OR hire_recommendation IS NULL);
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "chk_eval_ref" CHECK ((ref_type::text = ANY (ARRAY['project'::character varying, 'internship'::character varying]::text[])) OR ref_type IS NULL);
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "chk_eval_source" CHECK (source_type::text = ANY (ARRAY['enterprise'::character varying, 'school'::character varying, 'peer'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table evaluation_record
-- ----------------------------
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "evaluation_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table growth_badge
-- ----------------------------
CREATE INDEX "idx_badge_date" ON "growth_svc"."growth_badge" USING btree (
  "issue_date" "pg_catalog"."date_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_badge_student" ON "growth_svc"."growth_badge" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_badge_type" ON "growth_svc"."growth_badge" USING btree (
  "type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table growth_badge
-- ----------------------------
CREATE TRIGGER "update_growth_svc_growth_badge_updated_at" BEFORE UPDATE ON "growth_svc"."growth_badge"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table growth_badge
-- ----------------------------
ALTER TABLE "growth_svc"."growth_badge" ADD CONSTRAINT "chk_badge_type" CHECK (type::text = ANY (ARRAY['certificate'::character varying, 'badge'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table growth_badge
-- ----------------------------
ALTER TABLE "growth_svc"."growth_badge" ADD CONSTRAINT "growth_badge_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table warning_record
-- ----------------------------
CREATE INDEX "idx_warning_level" ON "growth_svc"."warning_record" USING btree (
  "warning_level" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_warning_status" ON "growth_svc"."warning_record" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_warning_student" ON "growth_svc"."warning_record" USING btree (
  "student_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_warning_tenant" ON "growth_svc"."warning_record" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_warning_type" ON "growth_svc"."warning_record" USING btree (
  "warning_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table warning_record
-- ----------------------------
CREATE TRIGGER "update_growth_svc_warning_record_updated_at" BEFORE UPDATE ON "growth_svc"."warning_record"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table warning_record
-- ----------------------------
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "chk_warning_status" CHECK (status = ANY (ARRAY[0, 1, 2]));
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "chk_warning_type" CHECK (warning_type::text = ANY (ARRAY['attendance'::character varying, 'report'::character varying, 'evaluation'::character varying]::text[]));
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "chk_warning_level" CHECK (warning_level = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table warning_record
-- ----------------------------
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "warning_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table evaluation_record
-- ----------------------------
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "fk_eval_evaluator" FOREIGN KEY ("evaluator_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "growth_svc"."evaluation_record" ADD CONSTRAINT "fk_eval_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table growth_badge
-- ----------------------------
ALTER TABLE "growth_svc"."growth_badge" ADD CONSTRAINT "fk_badge_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table warning_record
-- ----------------------------
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "fk_warning_intervener" FOREIGN KEY ("intervened_by") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "fk_warning_student" FOREIGN KEY ("student_id") REFERENCES "student_svc"."student_info" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "growth_svc"."warning_record" ADD CONSTRAINT "fk_warning_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
