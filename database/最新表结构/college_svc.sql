/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : college_svc

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 15:04:41
*/


-- ----------------------------
-- Sequence structure for college_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."college_info_id_seq";
CREATE SEQUENCE "college_svc"."college_info_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_audit_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."enterprise_audit_id_seq";
CREATE SEQUENCE "college_svc"."enterprise_audit_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_relationship_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."enterprise_relationship_id_seq";
CREATE SEQUENCE "college_svc"."enterprise_relationship_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for enterprise_visit_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."enterprise_visit_id_seq";
CREATE SEQUENCE "college_svc"."enterprise_visit_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for internship_inspection_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."internship_inspection_id_seq";
CREATE SEQUENCE "college_svc"."internship_inspection_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for organization_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "college_svc"."organization_id_seq";
CREATE SEQUENCE "college_svc"."organization_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for college_info
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."college_info";
CREATE TABLE "college_svc"."college_info" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".college_info_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "college_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "college_code" varchar(50) COLLATE "pg_catalog"."default",
  "province" varchar(50) COLLATE "pg_catalog"."default",
  "city" varchar(50) COLLATE "pg_catalog"."default",
  "address" varchar(255) COLLATE "pg_catalog"."default",
  "logo_url" varchar(255) COLLATE "pg_catalog"."default",
  "contact_name" varchar(50) COLLATE "pg_catalog"."default",
  "contact_phone" varchar(20) COLLATE "pg_catalog"."default",
  "contact_email" varchar(100) COLLATE "pg_catalog"."default",
  "cooperation_level" int2 DEFAULT 1,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "college_svc"."college_info"."cooperation_level" IS '1=普通 2=重点 3=战略';
COMMENT ON COLUMN "college_svc"."college_info"."status" IS '1=正常 0=禁用';
COMMENT ON TABLE "college_svc"."college_info" IS '高校信息表';

-- ----------------------------
-- Table structure for enterprise_audit
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."enterprise_audit";
CREATE TABLE "college_svc"."enterprise_audit" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".enterprise_audit_id_seq'::regclass),
  "enterprise_tenant_id" int8 NOT NULL,
  "audit_type" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int2 NOT NULL DEFAULT 0,
  "auditor_id" int8,
  "audit_comment" text COLLATE "pg_catalog"."default",
  "audited_at" timestamptz(6),
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "college_svc"."enterprise_audit" IS '企业资质审核表';

-- ----------------------------
-- Table structure for enterprise_relationship
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."enterprise_relationship";
CREATE TABLE "college_svc"."enterprise_relationship" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".enterprise_relationship_id_seq'::regclass),
  "college_tenant_id" int8 NOT NULL,
  "enterprise_tenant_id" int8 NOT NULL,
  "cooperation_level" int2 DEFAULT 1,
  "status" int2 NOT NULL DEFAULT 1,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "college_svc"."enterprise_relationship" IS '校企合作关系表';

-- ----------------------------
-- Table structure for enterprise_visit
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."enterprise_visit";
CREATE TABLE "college_svc"."enterprise_visit" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".enterprise_visit_id_seq'::regclass),
  "college_tenant_id" int8 NOT NULL,
  "enterprise_tenant_id" int8 NOT NULL,
  "visit_date" date NOT NULL,
  "visitor_id" int8 NOT NULL,
  "visitor_name" varchar(50) COLLATE "pg_catalog"."default",
  "purpose" text COLLATE "pg_catalog"."default",
  "outcome" text COLLATE "pg_catalog"."default",
  "next_action" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON TABLE "college_svc"."enterprise_visit" IS '企业走访记录表';

-- ----------------------------
-- Table structure for internship_inspection
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."internship_inspection";
CREATE TABLE "college_svc"."internship_inspection" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".internship_inspection_id_seq'::regclass),
  "college_tenant_id" int8 NOT NULL,
  "internship_id" int8 NOT NULL,
  "inspector_id" int8 NOT NULL,
  "inspection_date" date NOT NULL,
  "location" varchar(200) COLLATE "pg_catalog"."default",
  "findings" text COLLATE "pg_catalog"."default",
  "issues" text COLLATE "pg_catalog"."default",
  "recommendations" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON TABLE "college_svc"."internship_inspection" IS '实习巡查记录表';

-- ----------------------------
-- Table structure for organization
-- ----------------------------
DROP TABLE IF EXISTS "college_svc"."organization";
CREATE TABLE "college_svc"."organization" (
  "id" int8 NOT NULL DEFAULT nextval('"college_svc".organization_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "parent_id" int8,
  "org_type" int2 NOT NULL,
  "org_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "org_code" varchar(50) COLLATE "pg_catalog"."default",
  "sort_order" int4 DEFAULT 0,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "college_svc"."organization"."org_type" IS '1=学院 2=专业 3=班级';
COMMENT ON TABLE "college_svc"."organization" IS '学院/专业/班级组织树';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."college_info_id_seq"
OWNED BY "college_svc"."college_info"."id";
SELECT setval('"college_svc"."college_info_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."enterprise_audit_id_seq"
OWNED BY "college_svc"."enterprise_audit"."id";
SELECT setval('"college_svc"."enterprise_audit_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."enterprise_relationship_id_seq"
OWNED BY "college_svc"."enterprise_relationship"."id";
SELECT setval('"college_svc"."enterprise_relationship_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."enterprise_visit_id_seq"
OWNED BY "college_svc"."enterprise_visit"."id";
SELECT setval('"college_svc"."enterprise_visit_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."internship_inspection_id_seq"
OWNED BY "college_svc"."internship_inspection"."id";
SELECT setval('"college_svc"."internship_inspection_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "college_svc"."organization_id_seq"
OWNED BY "college_svc"."organization"."id";
SELECT setval('"college_svc"."organization_id_seq"', 1, false);

-- ----------------------------
-- Indexes structure for table college_info
-- ----------------------------
CREATE INDEX "idx_college_city" ON "college_svc"."college_info" USING btree (
  "city" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_college_tenant" ON "college_svc"."college_info" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Triggers structure for table college_info
-- ----------------------------
CREATE TRIGGER "update_college_svc_college_info_updated_at" BEFORE UPDATE ON "college_svc"."college_info"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table college_info
-- ----------------------------
ALTER TABLE "college_svc"."college_info" ADD CONSTRAINT "college_info_tenant_id_key" UNIQUE ("tenant_id");

-- ----------------------------
-- Checks structure for table college_info
-- ----------------------------
ALTER TABLE "college_svc"."college_info" ADD CONSTRAINT "chk_college_cooperation" CHECK (cooperation_level = ANY (ARRAY[1, 2, 3]));
ALTER TABLE "college_svc"."college_info" ADD CONSTRAINT "chk_college_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table college_info
-- ----------------------------
ALTER TABLE "college_svc"."college_info" ADD CONSTRAINT "college_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_audit
-- ----------------------------
CREATE INDEX "idx_audit_enterprise" ON "college_svc"."enterprise_audit" USING btree (
  "enterprise_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_audit_status" ON "college_svc"."enterprise_audit" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
);

-- ----------------------------
-- Checks structure for table enterprise_audit
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_audit" ADD CONSTRAINT "chk_audit_status" CHECK (status = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table enterprise_audit
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_audit" ADD CONSTRAINT "enterprise_audit_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_relationship
-- ----------------------------
CREATE INDEX "idx_rel_college" ON "college_svc"."enterprise_relationship" USING btree (
  "college_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_rel_enterprise" ON "college_svc"."enterprise_relationship" USING btree (
  "enterprise_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Uniques structure for table enterprise_relationship
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "uk_relationship" UNIQUE ("college_tenant_id", "enterprise_tenant_id");

-- ----------------------------
-- Checks structure for table enterprise_relationship
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "chk_rel_level" CHECK (cooperation_level = ANY (ARRAY[1, 2, 3]));
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "chk_rel_status" CHECK (status = ANY (ARRAY[0, 1]));

-- ----------------------------
-- Primary Key structure for table enterprise_relationship
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "enterprise_relationship_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table enterprise_visit
-- ----------------------------
CREATE INDEX "idx_visit_college" ON "college_svc"."enterprise_visit" USING btree (
  "college_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_visit_date" ON "college_svc"."enterprise_visit" USING btree (
  "visit_date" "pg_catalog"."date_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_visit_enterprise" ON "college_svc"."enterprise_visit" USING btree (
  "enterprise_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Primary Key structure for table enterprise_visit
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_visit" ADD CONSTRAINT "enterprise_visit_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table internship_inspection
-- ----------------------------
CREATE INDEX "idx_inspection_college" ON "college_svc"."internship_inspection" USING btree (
  "college_tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_inspection_date" ON "college_svc"."internship_inspection" USING btree (
  "inspection_date" "pg_catalog"."date_ops" ASC NULLS LAST
);
CREATE INDEX "idx_inspection_internship" ON "college_svc"."internship_inspection" USING btree (
  "internship_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table internship_inspection
-- ----------------------------
ALTER TABLE "college_svc"."internship_inspection" ADD CONSTRAINT "internship_inspection_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table organization
-- ----------------------------
CREATE INDEX "idx_org_parent" ON "college_svc"."organization" USING btree (
  "parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_org_tenant" ON "college_svc"."organization" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_org_type" ON "college_svc"."organization" USING btree (
  "org_type" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table organization
-- ----------------------------
CREATE TRIGGER "update_college_svc_organization_updated_at" BEFORE UPDATE ON "college_svc"."organization"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table organization
-- ----------------------------
ALTER TABLE "college_svc"."organization" ADD CONSTRAINT "chk_org_type" CHECK (org_type = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table organization
-- ----------------------------
ALTER TABLE "college_svc"."organization" ADD CONSTRAINT "organization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table college_info
-- ----------------------------
ALTER TABLE "college_svc"."college_info" ADD CONSTRAINT "fk_college_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_audit
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_audit" ADD CONSTRAINT "fk_audit_auditor" FOREIGN KEY ("auditor_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."enterprise_audit" ADD CONSTRAINT "fk_audit_enterprise" FOREIGN KEY ("enterprise_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_relationship
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "fk_rel_college" FOREIGN KEY ("college_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."enterprise_relationship" ADD CONSTRAINT "fk_rel_enterprise" FOREIGN KEY ("enterprise_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table enterprise_visit
-- ----------------------------
ALTER TABLE "college_svc"."enterprise_visit" ADD CONSTRAINT "fk_visit_college" FOREIGN KEY ("college_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."enterprise_visit" ADD CONSTRAINT "fk_visit_enterprise" FOREIGN KEY ("enterprise_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."enterprise_visit" ADD CONSTRAINT "fk_visit_visitor" FOREIGN KEY ("visitor_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table internship_inspection
-- ----------------------------
ALTER TABLE "college_svc"."internship_inspection" ADD CONSTRAINT "fk_inspection_college" FOREIGN KEY ("college_tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."internship_inspection" ADD CONSTRAINT "fk_inspection_inspector" FOREIGN KEY ("inspector_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."internship_inspection" ADD CONSTRAINT "fk_inspection_internship" FOREIGN KEY ("internship_id") REFERENCES "internship_svc"."internship_record" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table organization
-- ----------------------------
ALTER TABLE "college_svc"."organization" ADD CONSTRAINT "fk_org_parent" FOREIGN KEY ("parent_id") REFERENCES "college_svc"."organization" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "college_svc"."organization" ADD CONSTRAINT "fk_org_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
