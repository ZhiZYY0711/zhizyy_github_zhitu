/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : platform_service

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:05:19
*/


-- ----------------------------
-- Sequence structure for certificate_template_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."certificate_template_id_seq";
CREATE SEQUENCE "platform_service"."certificate_template_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for contract_template_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."contract_template_id_seq";
CREATE SEQUENCE "platform_service"."contract_template_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for online_user_trend_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."online_user_trend_id_seq";
CREATE SEQUENCE "platform_service"."online_user_trend_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for operation_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."operation_log_id_seq";
CREATE SEQUENCE "platform_service"."operation_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for recommendation_banner_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."recommendation_banner_id_seq";
CREATE SEQUENCE "platform_service"."recommendation_banner_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for recommendation_top_list_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."recommendation_top_list_id_seq";
CREATE SEQUENCE "platform_service"."recommendation_top_list_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for security_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."security_log_id_seq";
CREATE SEQUENCE "platform_service"."security_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for service_health_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."service_health_id_seq";
CREATE SEQUENCE "platform_service"."service_health_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for skill_tree_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."skill_tree_id_seq";
CREATE SEQUENCE "platform_service"."skill_tree_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dict_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."sys_dict_id_seq";
CREATE SEQUENCE "platform_service"."sys_dict_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_tag_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "platform_service"."sys_tag_id_seq";
CREATE SEQUENCE "platform_service"."sys_tag_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for certificate_template
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."certificate_template";
CREATE TABLE "platform_service"."certificate_template" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".certificate_template_id_seq'::regclass),
  "template_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "layout_config" text COLLATE "pg_catalog"."default",
  "background_url" varchar(255) COLLATE "pg_catalog"."default",
  "signature_urls" text COLLATE "pg_catalog"."default",
  "variables" text COLLATE "pg_catalog"."default",
  "usage_count" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "platform_service"."certificate_template" IS '证书模板表';

-- ----------------------------
-- Table structure for contract_template
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."contract_template";
CREATE TABLE "platform_service"."contract_template" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".contract_template_id_seq'::regclass),
  "template_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "description" text COLLATE "pg_catalog"."default",
  "contract_type" varchar(30) COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default",
  "variables" text COLLATE "pg_catalog"."default",
  "legal_terms" text COLLATE "pg_catalog"."default",
  "usage_count" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "platform_service"."contract_template" IS '合同模板表';

-- ----------------------------
-- Table structure for online_user_trend
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."online_user_trend";
CREATE TABLE "platform_service"."online_user_trend" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".online_user_trend_id_seq'::regclass),
  "timestamp" timestamptz(6) NOT NULL,
  "online_count" int4 NOT NULL,
  "student_count" int4 DEFAULT 0,
  "enterprise_count" int4 DEFAULT 0,
  "college_count" int4 DEFAULT 0
)
;
COMMENT ON TABLE "platform_service"."online_user_trend" IS '在线用户趋势表';

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."operation_log";
CREATE TABLE "platform_service"."operation_log" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".operation_log_id_seq'::regclass),
  "user_id" int8,
  "user_name" varchar(50) COLLATE "pg_catalog"."default",
  "tenant_id" int8,
  "module" varchar(50) COLLATE "pg_catalog"."default",
  "operation" varchar(100) COLLATE "pg_catalog"."default",
  "request_params" text COLLATE "pg_catalog"."default",
  "response_status" int4,
  "result" varchar(20) COLLATE "pg_catalog"."default",
  "ip_address" varchar(50) COLLATE "pg_catalog"."default",
  "user_agent" text COLLATE "pg_catalog"."default",
  "execution_time" int4,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "platform_service"."operation_log" IS '操作日志表';

-- ----------------------------
-- Table structure for recommendation_banner
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."recommendation_banner";
CREATE TABLE "platform_service"."recommendation_banner" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".recommendation_banner_id_seq'::regclass),
  "title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "image_url" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "link_url" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "target_portal" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "start_date" date NOT NULL,
  "end_date" date NOT NULL,
  "sort_order" int4 DEFAULT 0,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "platform_service"."recommendation_banner" IS '推荐横幅表';

-- ----------------------------
-- Table structure for recommendation_top_list
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."recommendation_top_list";
CREATE TABLE "platform_service"."recommendation_top_list" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".recommendation_top_list_id_seq'::regclass),
  "list_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "item_ids" text COLLATE "pg_catalog"."default" NOT NULL,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "platform_service"."recommendation_top_list" IS '推荐榜单表';

-- ----------------------------
-- Table structure for security_log
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."security_log";
CREATE TABLE "platform_service"."security_log" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".security_log_id_seq'::regclass),
  "level" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "event_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id" int8,
  "ip_address" varchar(50) COLLATE "pg_catalog"."default",
  "description" text COLLATE "pg_catalog"."default" NOT NULL,
  "details" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "platform_service"."security_log" IS '安全日志表';

-- ----------------------------
-- Table structure for service_health
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."service_health";
CREATE TABLE "platform_service"."service_health" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".service_health_id_seq'::regclass),
  "service_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "response_time" int4,
  "error_rate" numeric(5,2),
  "cpu_usage" numeric(5,2),
  "memory_usage" numeric(5,2),
  "checked_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "platform_service"."service_health" IS '服务健康监控表';

-- ----------------------------
-- Table structure for skill_tree
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."skill_tree";
CREATE TABLE "platform_service"."skill_tree" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".skill_tree_id_seq'::regclass),
  "skill_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "skill_category" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" int8,
  "level" int4 DEFAULT 1,
  "description" text COLLATE "pg_catalog"."default",
  "sort_order" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "platform_service"."skill_tree" IS '技能树表';

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."sys_dict";
CREATE TABLE "platform_service"."sys_dict" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".sys_dict_id_seq'::regclass),
  "category" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "label" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "sort_order" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "platform_service"."sys_dict"."category" IS '分类: industry/tech_stack/job_type等';
COMMENT ON TABLE "platform_service"."sys_dict" IS '数据字典表';

-- ----------------------------
-- Table structure for sys_tag
-- ----------------------------
DROP TABLE IF EXISTS "platform_service"."sys_tag";
CREATE TABLE "platform_service"."sys_tag" (
  "id" int8 NOT NULL DEFAULT nextval('"platform_service".sys_tag_id_seq'::regclass),
  "category" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "parent_id" int8,
  "sort_order" int4 DEFAULT 0,
  "usage_count" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "platform_service"."sys_tag" IS '系统标签表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."certificate_template_id_seq"
OWNED BY "platform_service"."certificate_template"."id";
SELECT setval('"platform_service"."certificate_template_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."contract_template_id_seq"
OWNED BY "platform_service"."contract_template"."id";
SELECT setval('"platform_service"."contract_template_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."online_user_trend_id_seq"
OWNED BY "platform_service"."online_user_trend"."id";
SELECT setval('"platform_service"."online_user_trend_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."operation_log_id_seq"
OWNED BY "platform_service"."operation_log"."id";
SELECT setval('"platform_service"."operation_log_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."recommendation_banner_id_seq"
OWNED BY "platform_service"."recommendation_banner"."id";
SELECT setval('"platform_service"."recommendation_banner_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."recommendation_top_list_id_seq"
OWNED BY "platform_service"."recommendation_top_list"."id";
SELECT setval('"platform_service"."recommendation_top_list_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."security_log_id_seq"
OWNED BY "platform_service"."security_log"."id";
SELECT setval('"platform_service"."security_log_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."service_health_id_seq"
OWNED BY "platform_service"."service_health"."id";
SELECT setval('"platform_service"."service_health_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."skill_tree_id_seq"
OWNED BY "platform_service"."skill_tree"."id";
SELECT setval('"platform_service"."skill_tree_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."sys_dict_id_seq"
OWNED BY "platform_service"."sys_dict"."id";
SELECT setval('"platform_service"."sys_dict_id_seq"', 33, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "platform_service"."sys_tag_id_seq"
OWNED BY "platform_service"."sys_tag"."id";
SELECT setval('"platform_service"."sys_tag_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table certificate_template
-- ----------------------------
CREATE INDEX "idx_cert_template_usage" ON "platform_service"."certificate_template" USING btree (
  "usage_count" "pg_catalog"."int4_ops" DESC NULLS FIRST
) WHERE is_deleted = false;

-- ----------------------------
-- Primary Key structure for table certificate_template
-- ----------------------------
ALTER TABLE "platform_service"."certificate_template" ADD CONSTRAINT "certificate_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table contract_template
-- ----------------------------
CREATE INDEX "idx_contract_template_usage" ON "platform_service"."contract_template" USING btree (
  "usage_count" "pg_catalog"."int4_ops" DESC NULLS FIRST
) WHERE is_deleted = false;

-- ----------------------------
-- Primary Key structure for table contract_template
-- ----------------------------
ALTER TABLE "platform_service"."contract_template" ADD CONSTRAINT "contract_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table online_user_trend
-- ----------------------------
CREATE INDEX "idx_trend_timestamp" ON "platform_service"."online_user_trend" USING btree (
  "timestamp" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table online_user_trend
-- ----------------------------
ALTER TABLE "platform_service"."online_user_trend" ADD CONSTRAINT "online_user_trend_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table operation_log
-- ----------------------------
CREATE INDEX "idx_oplog_created" ON "platform_service"."operation_log" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_oplog_module" ON "platform_service"."operation_log" USING btree (
  "module" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_oplog_result" ON "platform_service"."operation_log" USING btree (
  "result" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_oplog_user" ON "platform_service"."operation_log" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table operation_log
-- ----------------------------
ALTER TABLE "platform_service"."operation_log" ADD CONSTRAINT "operation_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table recommendation_banner
-- ----------------------------
CREATE INDEX "idx_banner_dates" ON "platform_service"."recommendation_banner" USING btree (
  "start_date" "pg_catalog"."date_ops" ASC NULLS LAST,
  "end_date" "pg_catalog"."date_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_banner_portal" ON "platform_service"."recommendation_banner" USING btree (
  "target_portal" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Checks structure for table recommendation_banner
-- ----------------------------
ALTER TABLE "platform_service"."recommendation_banner" ADD CONSTRAINT "chk_banner_dates" CHECK (end_date >= start_date);
ALTER TABLE "platform_service"."recommendation_banner" ADD CONSTRAINT "chk_banner_portal" CHECK (target_portal::text = ANY (ARRAY['student'::character varying, 'enterprise'::character varying, 'college'::character varying, 'all'::character varying]::text[]));
ALTER TABLE "platform_service"."recommendation_banner" ADD CONSTRAINT "chk_banner_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table recommendation_banner
-- ----------------------------
ALTER TABLE "platform_service"."recommendation_banner" ADD CONSTRAINT "recommendation_banner_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table recommendation_top_list
-- ----------------------------
ALTER TABLE "platform_service"."recommendation_top_list" ADD CONSTRAINT "uk_top_list_type" UNIQUE ("list_type");

-- ----------------------------
-- Checks structure for table recommendation_top_list
-- ----------------------------
ALTER TABLE "platform_service"."recommendation_top_list" ADD CONSTRAINT "chk_list_type" CHECK (list_type::text = ANY (ARRAY['mentor'::character varying, 'course'::character varying, 'project'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table recommendation_top_list
-- ----------------------------
ALTER TABLE "platform_service"."recommendation_top_list" ADD CONSTRAINT "recommendation_top_list_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table security_log
-- ----------------------------
CREATE INDEX "idx_seclog_created" ON "platform_service"."security_log" USING btree (
  "created_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_seclog_event" ON "platform_service"."security_log" USING btree (
  "event_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_seclog_level" ON "platform_service"."security_log" USING btree (
  "level" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_seclog_user" ON "platform_service"."security_log" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table security_log
-- ----------------------------
ALTER TABLE "platform_service"."security_log" ADD CONSTRAINT "chk_seclog_level" CHECK (level::text = ANY (ARRAY['info'::character varying, 'warning'::character varying, 'critical'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table security_log
-- ----------------------------
ALTER TABLE "platform_service"."security_log" ADD CONSTRAINT "security_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table service_health
-- ----------------------------
CREATE INDEX "idx_health_checked" ON "platform_service"."service_health" USING btree (
  "checked_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_health_service" ON "platform_service"."service_health" USING btree (
  "service_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table service_health
-- ----------------------------
ALTER TABLE "platform_service"."service_health" ADD CONSTRAINT "chk_health_status" CHECK (status::text = ANY (ARRAY['healthy'::character varying, 'degraded'::character varying, 'down'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table service_health
-- ----------------------------
ALTER TABLE "platform_service"."service_health" ADD CONSTRAINT "service_health_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table skill_tree
-- ----------------------------
CREATE INDEX "idx_skill_category" ON "platform_service"."skill_tree" USING btree (
  "skill_category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_skill_parent" ON "platform_service"."skill_tree" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Checks structure for table skill_tree
-- ----------------------------
ALTER TABLE "platform_service"."skill_tree" ADD CONSTRAINT "chk_skill_category" CHECK (skill_category::text = ANY (ARRAY['technical'::character varying, 'soft_skill'::character varying, 'domain_knowledge'::character varying]::text[]));

-- ----------------------------
-- Primary Key structure for table skill_tree
-- ----------------------------
ALTER TABLE "platform_service"."skill_tree" ADD CONSTRAINT "skill_tree_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_dict
-- ----------------------------
CREATE INDEX "idx_dict_category" ON "platform_service"."sys_dict" USING btree (
  "category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table sys_dict
-- ----------------------------
CREATE TRIGGER "update_platform_service_sys_dict_updated_at" BEFORE UPDATE ON "platform_service"."sys_dict"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table sys_dict
-- ----------------------------
ALTER TABLE "platform_service"."sys_dict" ADD CONSTRAINT "uk_dict_category_code" UNIQUE ("category", "code");

-- ----------------------------
-- Primary Key structure for table sys_dict
-- ----------------------------
ALTER TABLE "platform_service"."sys_dict" ADD CONSTRAINT "sys_dict_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_tag
-- ----------------------------
CREATE INDEX "idx_tag_category" ON "platform_service"."sys_tag" USING btree (
  "category" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_tag_parent" ON "platform_service"."sys_tag" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Uniques structure for table sys_tag
-- ----------------------------
ALTER TABLE "platform_service"."sys_tag" ADD CONSTRAINT "uk_tag" UNIQUE ("category", "name");

-- ----------------------------
-- Primary Key structure for table sys_tag
-- ----------------------------
ALTER TABLE "platform_service"."sys_tag" ADD CONSTRAINT "sys_tag_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table skill_tree
-- ----------------------------
ALTER TABLE "platform_service"."skill_tree" ADD CONSTRAINT "fk_skill_parent" FOREIGN KEY ("parent_id") REFERENCES "platform_service"."skill_tree" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table sys_tag
-- ----------------------------
ALTER TABLE "platform_service"."sys_tag" ADD CONSTRAINT "fk_tag_parent" FOREIGN KEY ("parent_id") REFERENCES "platform_service"."sys_tag" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
