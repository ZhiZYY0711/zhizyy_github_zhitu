-- =====================================================
-- Zhitu (智图) Internship Management Platform
-- Database Initialization Script
-- PostgreSQL 15+
-- =====================================================

-- Execute all schema creation scripts in order
\i 01_auth_center.sql
\i 02_platform_service.sql
\i 03_student_svc.sql
\i 04_college_svc.sql
\i 05_enterprise_svc.sql
\i 06_internship_svc.sql
\i 07_training_svc.sql
\i 08_growth_svc.sql

-- =====================================================
-- Create database functions and triggers
-- =====================================================

-- Function: Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to all tables with updated_at column
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN 
        SELECT schemaname, tablename 
        FROM pg_tables 
        WHERE schemaname IN (
            'auth_center', 'platform_service', 'student_svc', 
            'college_svc', 'enterprise_svc', 'internship_svc', 
            'training_svc', 'growth_svc'
        )
    LOOP
        IF EXISTS (
            SELECT 1 
            FROM information_schema.columns 
            WHERE table_schema = r.schemaname 
            AND table_name = r.tablename 
            AND column_name = 'updated_at'
        ) THEN
            EXECUTE format('
                CREATE TRIGGER update_%I_%I_updated_at
                BEFORE UPDATE ON %I.%I
                FOR EACH ROW
                EXECUTE FUNCTION update_updated_at_column();
            ', r.schemaname, r.tablename, r.schemaname, r.tablename);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Grant permissions (adjust as needed)
-- =====================================================

-- Create application roles
CREATE ROLE zhitu_app_user LOGIN PASSWORD 'change_me_in_production';
CREATE ROLE zhitu_readonly LOGIN PASSWORD 'change_me_in_production';

-- Grant schema usage
GRANT USAGE ON SCHEMA auth_center, platform_service, student_svc, 
    college_svc, enterprise_svc, internship_svc, training_svc, growth_svc 
TO zhitu_app_user, zhitu_readonly;

-- Grant table permissions to app user
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN 
        SELECT schemaname, tablename 
        FROM pg_tables 
        WHERE schemaname IN (
            'auth_center', 'platform_service', 'student_svc', 
            'college_svc', 'enterprise_svc', 'internship_svc', 
            'training_svc', 'growth_svc'
        )
    LOOP
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON %I.%I TO zhitu_app_user', 
            r.schemaname, r.tablename);
        EXECUTE format('GRANT SELECT ON %I.%I TO zhitu_readonly', 
            r.schemaname, r.tablename);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Grant sequence permissions
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA auth_center, platform_service, 
    student_svc, college_svc, enterprise_svc, internship_svc, 
    training_svc, growth_svc TO zhitu_app_user;

-- =====================================================
-- Database statistics and verification
-- =====================================================

-- Show created schemas
SELECT schema_name 
FROM information_schema.schemata 
WHERE schema_name IN (
    'auth_center', 'platform_service', 'student_svc', 
    'college_svc', 'enterprise_svc', 'internship_svc', 
    'training_svc', 'growth_svc'
)
ORDER BY schema_name;

-- Show table counts per schema
SELECT 
    schemaname,
    COUNT(*) as table_count
FROM pg_tables 
WHERE schemaname IN (
    'auth_center', 'platform_service', 'student_svc', 
    'college_svc', 'enterprise_svc', 'internship_svc', 
    'training_svc', 'growth_svc'
)
GROUP BY schemaname
ORDER BY schemaname;

-- Show all tables
SELECT 
    schemaname,
    tablename
FROM pg_tables 
WHERE schemaname IN (
    'auth_center', 'platform_service', 'student_svc', 
    'college_svc', 'enterprise_svc', 'internship_svc', 
    'training_svc', 'growth_svc'
)
ORDER BY schemaname, tablename;

COMMENT ON DATABASE current_database() IS 'Zhitu Internship Management Platform Database';
