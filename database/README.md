# Zhitu Database Schema

Complete PostgreSQL database schema for the Zhitu (智图) Internship Management Platform.

## Overview

This database uses PostgreSQL 15+ with schema-based isolation for microservices architecture. Each service has its own schema to support future database splitting if needed.

## Schemas

| Schema | Description | Tables |
|--------|-------------|--------|
| `auth_center` | Authentication and tenant management | sys_user, sys_tenant, sys_refresh_token |
| `platform_service` | Platform-wide services | sys_dict |
| `student_svc` | Student profiles | student_info |
| `college_svc` | College management | college_info, organization |
| `enterprise_svc` | Enterprise management | enterprise_info, enterprise_staff, talent_pool |
| `internship_svc` | Internship lifecycle | internship_job, job_application, internship_offer, internship_record, weekly_report, attendance, internship_certificate |
| `training_svc` | Training projects | training_project, training_plan |
| `growth_svc` | Student growth tracking | evaluation_record, growth_badge, warning_record |

## Installation

### Prerequisites
- PostgreSQL 15 or higher
- Database user with CREATE SCHEMA privileges

### Quick Start

```bash
# Create database
createdb zhitu_cloud

# Initialize all schemas and tables
psql -d zhitu_cloud -f init_database.sql

# Or execute individual schema files
psql -d zhitu_cloud -f schema/01_auth_center.sql
psql -d zhitu_cloud -f schema/02_platform_service.sql
# ... etc
```

### Production Setup

1. Update passwords in `init_database.sql`:
   ```sql
   CREATE ROLE zhitu_app_user LOGIN PASSWORD 'your_secure_password';
   CREATE ROLE zhitu_readonly LOGIN PASSWORD 'your_secure_password';
   ```

2. Configure connection pooling (recommended: PgBouncer)

3. Set up backup strategy:
   ```bash
   # Daily backup example
   pg_dump -Fc zhitu_cloud > backup_$(date +%Y%m%d).dump
   ```

## Schema Files

- `01_auth_center.sql` - Authentication and user management
- `02_platform_service.sql` - Platform services and dictionaries
- `03_student_svc.sql` - Student information
- `04_college_svc.sql` - College/university management
- `05_enterprise_svc.sql` - Enterprise management
- `06_internship_svc.sql` - Internship management (largest schema)
- `07_training_svc.sql` - Training project management
- `08_growth_svc.sql` - Student growth and evaluation
- `init_database.sql` - Master initialization script

## Key Features

### Multi-Tenancy
- Tenant isolation via `tenant_id` foreign keys
- Separate schemas for service boundaries
- Row-level security ready (can be enabled per table)

### Soft Delete
- All tables include `is_deleted` boolean column
- Indexes exclude deleted records: `WHERE is_deleted = FALSE`
- Allows data recovery and audit trails

### Timestamps
- `created_at` - Auto-set on INSERT
- `updated_at` - Auto-updated on UPDATE via trigger
- All timestamps use `TIMESTAMPTZ` for timezone awareness

### Data Types
- **BIGSERIAL** for primary keys (supports high volume)
- **TEXT** for JSON storage (evaluation scores, config)
- **DECIMAL** for precise numeric values (coordinates, hours)
- **TIMESTAMPTZ** for timezone-aware timestamps

### Indexes
- Primary keys on all `id` columns
- Foreign key indexes for join performance
- Composite indexes for common query patterns
- Partial indexes excluding soft-deleted records

### Constraints
- Foreign key constraints for referential integrity
- Check constraints for enum-like values
- Unique constraints for business rules
- Date validation constraints

## Common Queries

### Get active students in a class
```sql
SELECT s.* 
FROM student_svc.student_info s
WHERE s.class_id = ? 
  AND s.is_deleted = FALSE;
```

### Get active internships for a student
```sql
SELECT ir.*, ij.job_title, ei.enterprise_name
FROM internship_svc.internship_record ir
JOIN internship_svc.internship_job ij ON ir.job_id = ij.id
JOIN enterprise_svc.enterprise_info ei ON ir.enterprise_id = ei.tenant_id
WHERE ir.student_id = ? 
  AND ir.status = 1;
```

### Get student evaluations
```sql
SELECT er.*, u.username as evaluator_name
FROM growth_svc.evaluation_record er
JOIN auth_center.sys_user u ON er.evaluator_id = u.id
WHERE er.student_id = ? 
  AND er.is_deleted = FALSE
ORDER BY er.created_at DESC;
```

## Maintenance

### Vacuum and Analyze
```sql
-- Regular maintenance
VACUUM ANALYZE;

-- Per schema
VACUUM ANALYZE auth_center.sys_user;
```

### Index Monitoring
```sql
-- Find unused indexes
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE schemaname IN ('auth_center', 'student_svc', 'internship_svc')
  AND idx_scan = 0
ORDER BY schemaname, tablename;
```

### Table Size Monitoring
```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname IN (
    'auth_center', 'platform_service', 'student_svc', 
    'college_svc', 'enterprise_svc', 'internship_svc', 
    'training_svc', 'growth_svc'
)
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

## Migration Strategy

### From Development to Production
1. Export schema only: `pg_dump -s zhitu_cloud > schema.sql`
2. Review and test on staging
3. Apply to production during maintenance window
4. Verify with test queries

### Schema Updates
1. Create migration script with version number
2. Test on development database
3. Apply to staging
4. Document rollback procedure
5. Apply to production

### Example Migration Script
```sql
-- migration_v1.1.0_add_student_status.sql
BEGIN;

ALTER TABLE student_svc.student_info 
ADD COLUMN status SMALLINT DEFAULT 1 
CHECK (status IN (1, 2, 3));

COMMENT ON COLUMN student_svc.student_info.status IS '1=在读 2=实习中 3=已毕业';

CREATE INDEX idx_student_status ON student_svc.student_info(status) 
WHERE is_deleted = FALSE;

COMMIT;
```

## Performance Tuning

### Recommended PostgreSQL Settings
```ini
# postgresql.conf
shared_buffers = 256MB          # 25% of RAM
effective_cache_size = 1GB      # 50-75% of RAM
work_mem = 16MB                 # Per operation
maintenance_work_mem = 128MB    # For VACUUM, CREATE INDEX
max_connections = 100           # Adjust based on load
```

### Connection Pooling
Use PgBouncer or similar:
```ini
# pgbouncer.ini
[databases]
zhitu_cloud = host=localhost port=5432 dbname=zhitu_cloud

[pgbouncer]
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 25
```

## Troubleshooting

### Slow Queries
```sql
-- Enable query logging
ALTER DATABASE zhitu_cloud SET log_min_duration_statement = 1000;

-- Check slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY mean_time DESC
LIMIT 10;
```

### Lock Monitoring
```sql
SELECT 
    pid,
    usename,
    pg_blocking_pids(pid) as blocked_by,
    query
FROM pg_stat_activity
WHERE cardinality(pg_blocking_pids(pid)) > 0;
```

## Support

For issues or questions:
1. Check application logs
2. Review PostgreSQL logs
3. Verify schema matches entity classes
4. Check foreign key constraints

## License

Internal use only - Zhitu Platform
