# Database Migrations

This directory contains database migration scripts for the Zhitu Cloud Platform.

## Migration Files

### 001_add_missing_api_tables.sql
**Purpose**: Add 15 new tables required for missing API endpoints implementation

**Tables Added**:
- **student_svc** (3 tables):
  - `student_task` - Student tasks for training, internship, and evaluation
  - `student_capability` - Student capability scores for radar chart
  - `student_recommendation` - Personalized recommendations for students

- **training_svc** (2 tables):
  - `project_task` - Scrum board tasks for training projects
  - `project_enrollment` - Student enrollment in training projects

- **enterprise_svc** (3 tables):
  - `enterprise_activity` - Activity feed for enterprise portal
  - `enterprise_todo` - Todo list for enterprise users
  - `interview_schedule` - Interview scheduling and management

- **college_svc** (4 tables):
  - `enterprise_relationship` - College-enterprise cooperation relationships
  - `enterprise_visit` - Enterprise visit records for CRM
  - `enterprise_audit` - Enterprise qualification audits
  - `internship_inspection` - On-site internship inspections

- **platform_service** (11 tables):
  - `sys_tag` - System-wide tags for categorization
  - `skill_tree` - Hierarchical skill tree structure
  - `certificate_template` - Certificate templates
  - `contract_template` - Contract templates
  - `recommendation_banner` - Recommendation banners
  - `recommendation_top_list` - Top lists for mentors/courses/projects
  - `operation_log` - Operation audit logs
  - `security_log` - Security event logs
  - `service_health` - Microservice health monitoring
  - `online_user_trend` - Online user count trends

**Related Spec**: `.kiro/specs/missing-api-endpoints`

## How to Apply Migrations

### Method 1: Using psql command line

```bash
# Connect to database
psql -h localhost -U zhitu_user -d zhitu_cloud

# Apply migration
\i database/migrations/001_add_missing_api_tables.sql

# Verify tables were created
\dt student_svc.*
\dt training_svc.*
\dt enterprise_svc.*
\dt college_svc.*
\dt platform_service.*
```

### Method 2: Using psql with file redirect

```bash
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/migrations/001_add_missing_api_tables.sql
```

### Method 3: Apply all schema files (includes migrations)

```bash
# This applies the complete schema including all migrations
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/01_auth_center.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/02_platform_service.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/03_student_svc.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/04_college_svc.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/05_enterprise_svc.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/06_internship_svc.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/07_training_svc.sql
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/schema/08_growth_svc.sql
```

## Rollback

If you need to rollback the migration:

```bash
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/migrations/001_add_missing_api_tables_rollback.sql
```

**WARNING**: Rollback will permanently delete all data in the affected tables.

## Verification

After applying the migration, verify the tables exist:

```sql
-- Check student_svc tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'student_svc' 
AND table_name IN ('student_task', 'student_capability', 'student_recommendation');

-- Check training_svc tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'training_svc' 
AND table_name IN ('project_task', 'project_enrollment');

-- Check enterprise_svc tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'enterprise_svc' 
AND table_name IN ('enterprise_activity', 'enterprise_todo', 'interview_schedule');

-- Check college_svc tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'college_svc' 
AND table_name IN ('enterprise_relationship', 'enterprise_visit', 'enterprise_audit', 'internship_inspection');

-- Check platform_service tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'platform_service' 
AND table_name IN ('sys_tag', 'skill_tree', 'certificate_template', 'contract_template', 
                    'recommendation_banner', 'recommendation_top_list', 'operation_log', 
                    'security_log', 'service_health', 'online_user_trend');
```

## Notes

- All tables include appropriate indexes for query performance
- Foreign key constraints ensure referential integrity
- Check constraints validate data values
- Timestamps use TIMESTAMPTZ for timezone awareness
- Soft delete pattern (is_deleted) used where appropriate
- Comments added for documentation (Chinese for business context)

## Migration History

| Version | Date | Description | Status |
|---------|------|-------------|--------|
| 001 | 2024 | Add 15 tables for missing API endpoints | Applied |
