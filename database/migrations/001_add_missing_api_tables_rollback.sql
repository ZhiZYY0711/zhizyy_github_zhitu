-- =====================================================
-- Migration Rollback: 001_add_missing_api_tables_rollback.sql
-- Description: Rollback script to drop all 15 new tables
-- Date: 2024
-- Related Spec: .kiro/specs/missing-api-endpoints
-- =====================================================

-- WARNING: This will permanently delete all data in these tables
-- Execute only if you need to rollback the migration

-- =====================================================
-- Drop tables in reverse dependency order
-- =====================================================

-- Platform Service tables (no dependencies)
DROP TABLE IF EXISTS platform_service.online_user_trend CASCADE;
DROP TABLE IF EXISTS platform_service.service_health CASCADE;
DROP TABLE IF EXISTS platform_service.security_log CASCADE;
DROP TABLE IF EXISTS platform_service.operation_log CASCADE;
DROP TABLE IF EXISTS platform_service.recommendation_top_list CASCADE;
DROP TABLE IF EXISTS platform_service.recommendation_banner CASCADE;
DROP TABLE IF EXISTS platform_service.contract_template CASCADE;
DROP TABLE IF EXISTS platform_service.certificate_template CASCADE;
DROP TABLE IF EXISTS platform_service.skill_tree CASCADE;
DROP TABLE IF EXISTS platform_service.sys_tag CASCADE;

-- College Service tables
DROP TABLE IF EXISTS college_svc.internship_inspection CASCADE;
DROP TABLE IF EXISTS college_svc.enterprise_audit CASCADE;
DROP TABLE IF EXISTS college_svc.enterprise_visit CASCADE;
DROP TABLE IF EXISTS college_svc.enterprise_relationship CASCADE;

-- Enterprise Service tables
DROP TABLE IF EXISTS enterprise_svc.interview_schedule CASCADE;
DROP TABLE IF EXISTS enterprise_svc.enterprise_todo CASCADE;
DROP TABLE IF EXISTS enterprise_svc.enterprise_activity CASCADE;

-- Training Service tables
DROP TABLE IF EXISTS training_svc.project_enrollment CASCADE;
DROP TABLE IF EXISTS training_svc.project_task CASCADE;

-- Student Service tables
DROP TABLE IF EXISTS student_svc.student_recommendation CASCADE;
DROP TABLE IF EXISTS student_svc.student_capability CASCADE;
DROP TABLE IF EXISTS student_svc.student_task CASCADE;

-- =====================================================
-- Rollback Complete
-- =====================================================
