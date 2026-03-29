/*
 Navicat Premium Dump SQL

 Source Server         : topostgre
 Source Server Type    : PostgreSQL
 Source Server Version : 150017 (150017)
 Source Host           : localhost:15432
 Source Catalog        : zhitu_cloud
 Source Schema         : auth_center

 Target Server Type    : PostgreSQL
 Target Server Version : 150017 (150017)
 File Encoding         : 65001

 Date: 29/03/2026 14:58:37
*/


-- ----------------------------
-- Sequence structure for sys_refresh_token_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "auth_center"."sys_refresh_token_id_seq";
CREATE SEQUENCE "auth_center"."sys_refresh_token_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_tenant_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "auth_center"."sys_tenant_id_seq";
CREATE SEQUENCE "auth_center"."sys_tenant_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "auth_center"."sys_user_id_seq";
CREATE SEQUENCE "auth_center"."sys_user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for sys_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS "auth_center"."sys_refresh_token";
CREATE TABLE "auth_center"."sys_refresh_token" (
  "id" int8 NOT NULL DEFAULT nextval('"auth_center".sys_refresh_token_id_seq'::regclass),
  "user_id" int8 NOT NULL,
  "token_hash" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "expires_at" timestamptz(6) NOT NULL,
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
)
;
COMMENT ON COLUMN "auth_center"."sys_refresh_token"."token_hash" IS 'refresh_token的SHA-256哈希';
COMMENT ON TABLE "auth_center"."sys_refresh_token" IS 'Refresh Token表';

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS "auth_center"."sys_tenant";
CREATE TABLE "auth_center"."sys_tenant" (
  "id" int8 NOT NULL DEFAULT nextval('"auth_center".sys_tenant_id_seq'::regclass),
  "name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2 NOT NULL,
  "status" int2 NOT NULL DEFAULT 1,
  "config" text COLLATE "pg_catalog"."default",
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "auth_center"."sys_tenant"."type" IS '0=平台运营 1=高校 2=企业';
COMMENT ON COLUMN "auth_center"."sys_tenant"."status" IS '0=待审核 1=正常 2=禁用';
COMMENT ON COLUMN "auth_center"."sys_tenant"."config" IS '租户配置(JSON格式)';
COMMENT ON TABLE "auth_center"."sys_tenant" IS '租户/机构表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "auth_center"."sys_user";
CREATE TABLE "auth_center"."sys_user" (
  "id" int8 NOT NULL DEFAULT nextval('"auth_center".sys_user_id_seq'::regclass),
  "tenant_id" int8 NOT NULL,
  "username" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "password_hash" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "phone" varchar(20) COLLATE "pg_catalog"."default",
  "role" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "sub_role" varchar(20) COLLATE "pg_catalog"."default",
  "status" int2 NOT NULL DEFAULT 1,
  "last_login_at" timestamptz(6),
  "created_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "updated_at" timestamptz(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "is_deleted" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "auth_center"."sys_user"."tenant_id" IS '所属租户ID，平台管理员为0';
COMMENT ON COLUMN "auth_center"."sys_user"."role" IS 'student=学生 enterprise=企业 college=高校 platform=平台';
COMMENT ON COLUMN "auth_center"."sys_user"."sub_role" IS 'hr/mentor/admin(企业) counselor/dean/admin(高校)';
COMMENT ON COLUMN "auth_center"."sys_user"."status" IS '1=正常 2=锁定 3=注销';
COMMENT ON TABLE "auth_center"."sys_user" IS '系统用户表';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "auth_center"."sys_refresh_token_id_seq"
OWNED BY "auth_center"."sys_refresh_token"."id";
SELECT setval('"auth_center"."sys_refresh_token_id_seq"', 2222, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "auth_center"."sys_tenant_id_seq"
OWNED BY "auth_center"."sys_tenant"."id";
SELECT setval('"auth_center"."sys_tenant_id_seq"', 21, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "auth_center"."sys_user_id_seq"
OWNED BY "auth_center"."sys_user"."id";
SELECT setval('"auth_center"."sys_user_id_seq"', 2108, true);

-- ----------------------------
-- Indexes structure for table sys_refresh_token
-- ----------------------------
CREATE INDEX "idx_token_expires" ON "auth_center"."sys_refresh_token" USING btree (
  "expires_at" "pg_catalog"."timestamptz_ops" ASC NULLS LAST
);
CREATE INDEX "idx_token_hash" ON "auth_center"."sys_refresh_token" USING btree (
  "token_hash" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_token_user" ON "auth_center"."sys_refresh_token" USING btree (
  "user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table sys_refresh_token
-- ----------------------------
ALTER TABLE "auth_center"."sys_refresh_token" ADD CONSTRAINT "sys_refresh_token_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_tenant
-- ----------------------------
CREATE INDEX "idx_tenant_status" ON "auth_center"."sys_tenant" USING btree (
  "status" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_tenant_type" ON "auth_center"."sys_tenant" USING btree (
  "type" "pg_catalog"."int2_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table sys_tenant
-- ----------------------------
CREATE TRIGGER "update_auth_center_sys_tenant_updated_at" BEFORE UPDATE ON "auth_center"."sys_tenant"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table sys_tenant
-- ----------------------------
ALTER TABLE "auth_center"."sys_tenant" ADD CONSTRAINT "chk_tenant_status" CHECK (status = ANY (ARRAY[0, 1, 2]));
ALTER TABLE "auth_center"."sys_tenant" ADD CONSTRAINT "chk_tenant_type" CHECK (type = ANY (ARRAY[0, 1, 2]));

-- ----------------------------
-- Primary Key structure for table sys_tenant
-- ----------------------------
ALTER TABLE "auth_center"."sys_tenant" ADD CONSTRAINT "sys_tenant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table sys_user
-- ----------------------------
CREATE INDEX "idx_user_phone" ON "auth_center"."sys_user" USING btree (
  "phone" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_user_role" ON "auth_center"."sys_user" USING btree (
  "role" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_user_tenant" ON "auth_center"."sys_user" USING btree (
  "tenant_id" "pg_catalog"."int8_ops" ASC NULLS LAST
) WHERE is_deleted = false;
CREATE INDEX "idx_user_username" ON "auth_center"."sys_user" USING btree (
  "username" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE is_deleted = false;

-- ----------------------------
-- Triggers structure for table sys_user
-- ----------------------------
CREATE TRIGGER "update_auth_center_sys_user_updated_at" BEFORE UPDATE ON "auth_center"."sys_user"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Uniques structure for table sys_user
-- ----------------------------
ALTER TABLE "auth_center"."sys_user" ADD CONSTRAINT "sys_user_username_key" UNIQUE ("username");

-- ----------------------------
-- Checks structure for table sys_user
-- ----------------------------
ALTER TABLE "auth_center"."sys_user" ADD CONSTRAINT "chk_user_role" CHECK (role::text = ANY (ARRAY['student'::character varying, 'enterprise'::character varying, 'college'::character varying, 'platform'::character varying]::text[]));
ALTER TABLE "auth_center"."sys_user" ADD CONSTRAINT "chk_user_status" CHECK (status = ANY (ARRAY[1, 2, 3]));

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "auth_center"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table sys_refresh_token
-- ----------------------------
ALTER TABLE "auth_center"."sys_refresh_token" ADD CONSTRAINT "fk_token_user" FOREIGN KEY ("user_id") REFERENCES "auth_center"."sys_user" ("id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table sys_user
-- ----------------------------
ALTER TABLE "auth_center"."sys_user" ADD CONSTRAINT "fk_user_tenant" FOREIGN KEY ("tenant_id") REFERENCES "auth_center"."sys_tenant" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
