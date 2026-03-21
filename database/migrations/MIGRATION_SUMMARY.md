# Migration 001: Missing API Endpoints Tables

## Summary

Created database migration scripts for all new tables required by the missing-api-endpoints specification.

## Files Created

1. **001_add_missing_api_tables.sql** - Main migration script
2. **001_add_missing_api_tables_rollback.sql** - Rollback script
3. **README.md** - Migration documentation

## Tables Created by Schema

### student_svc (3 tables)
1. **student_task** - Student tasks for training, internship, and evaluation
   - Tracks pending and completed tasks with priorities and due dates
   - Foreign key to student_info
   - Indexes on student_id, status, due_date

2. **student_capability** - Student capability scores for radar chart
   - Stores scores (0-100) across 5 dimensions
   - Unique constraint on (student_id, dimension)
   - Used for capability visualization

3. **student_recommendation** - Personalized recommendations
   - Recommendations for projects, jobs, and courses
   - Includes recommendation score and reasoning
   - Indexed by student, type, and creation date

### training_svc (2 tables)
4. **project_task** - Scrum board tasks for training projects
   - Kanban-style task management (todo/in_progress/done)
   - Supports team assignment and story points
   - Foreign keys to training_project and sys_user

5. **project_enrollment** - Student enrollment in training projects
   - Tracks student participation in projects
   - Unique constraint prevents duplicate enrollments
   - Status tracking (active/completed/withdrawn)

### enterprise_svc (3 tables)
6. **enterprise_activity** - Activity feed for enterprise portal
   - Records all enterprise activities (applications, interviews, etc.)
   - Supports polymorphic references via ref_type/ref_id
   - Indexed by tenant and creation date

7. **enterprise_todo** - Todo list for enterprise users
   - Task management for enterprise staff
   - Priority levels and due dates
   - Filtered indexes for pending tasks

8. **interview_schedule** - Interview scheduling and management
   - Links applications to interview appointments
   - Supports multiple interview types (phone/video/onsite)
   - Status tracking (scheduled/completed/cancelled)

### college_svc (4 tables)
9. **enterprise_relationship** - College-enterprise cooperation
   - Manages partnership levels (normal/key/strategic)
   - Unique constraint on college-enterprise pairs
   - Soft delete support

10. **enterprise_visit** - Enterprise visit records for CRM
    - Documents college visits to enterprises
    - Tracks purpose, outcome, and next actions
    - Indexed by college, enterprise, and visit date

11. **enterprise_audit** - Enterprise qualification audits
    - Audit workflow (pending/passed/rejected)
    - Supports multiple audit types
    - Tracks auditor and audit comments

12. **internship_inspection** - On-site internship inspections
    - Records college inspections of internship sites
    - Documents findings, issues, and recommendations
    - Links to internship_record table

### platform_service (10 tables)
13. **sys_tag** - System-wide tags for categorization
    - Hierarchical tag structure with parent_id
    - Usage count tracking
    - Unique constraint on (category, name)

14. **skill_tree** - Hierarchical skill tree structure
    - Three categories: technical/soft_skill/domain_knowledge
    - Supports skill levels and descriptions
    - Parent-child relationships for skill hierarchy

15. **certificate_template** - Certificate templates
    - Configurable layout and variables
    - Background images and signature support
    - Usage tracking for popular templates

16. **contract_template** - Contract templates
    - Templates for internship/training/employment contracts
    - Variable substitution support
    - Legal terms storage

17. **recommendation_banner** - Recommendation banners
    - Portal-specific banners (student/enterprise/college/all)
    - Date range validation
    - Active/inactive status

18. **recommendation_top_list** - Top lists
    - Curated lists for mentors/courses/projects
    - JSON array of ordered item IDs
    - Unique constraint on list_type

19. **operation_log** - Operation audit logs
    - Comprehensive operation tracking
    - Request/response logging
    - Performance metrics (execution_time)

20. **security_log** - Security event logs
    - Three severity levels (info/warning/critical)
    - Event type categorization
    - User and IP tracking

21. **service_health** - Microservice health monitoring
    - Real-time service status tracking
    - Performance metrics (response_time, error_rate, CPU, memory)
    - Status categories (healthy/degraded/down)

22. **online_user_trend** - Online user count trends
    - Time-series data for online users
    - Breakdown by user type (student/enterprise/college)
    - Used for dashboard analytics

## Schema Updates

The migration also updated the following schema files to include the new tables:
- `database/schema/02_platform_service.sql` - Added 10 tables
- `database/schema/03_student_svc.sql` - Added 3 tables
- `database/schema/04_college_svc.sql` - Added 4 tables
- `database/schema/05_enterprise_svc.sql` - Added 3 tables
- `database/schema/07_training_svc.sql` - Added 2 tables

## Key Features

### Data Integrity
- Foreign key constraints ensure referential integrity
- Check constraints validate enum values and ranges
- Unique constraints prevent duplicate records

### Performance
- Strategic indexes on frequently queried columns
- Partial indexes with WHERE clauses for filtered queries
- Composite indexes for common query patterns

### Soft Deletes
- is_deleted flag on tables requiring audit trails
- Indexes exclude deleted records for performance

### Timestamps
- TIMESTAMPTZ for timezone-aware timestamps
- created_at and updated_at tracking
- Automatic DEFAULT CURRENT_TIMESTAMP

### Documentation
- COMMENT ON TABLE for business context
- Chinese comments for business users
- Clear column descriptions

## Deployment Instructions

### Prerequisites
- PostgreSQL 15+
- Database: zhitu_cloud
- User: zhitu_user with appropriate permissions
- All dependent schemas must exist (auth_center, internship_svc, etc.)

### Apply Migration

```bash
# Method 1: Direct execution
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/migrations/001_add_missing_api_tables.sql

# Method 2: Interactive
psql -h localhost -U zhitu_user -d zhitu_cloud
\i database/migrations/001_add_missing_api_tables.sql
```

### Verify Migration

```sql
-- Count tables by schema
SELECT table_schema, COUNT(*) 
FROM information_schema.tables 
WHERE table_schema IN ('student_svc', 'training_svc', 'enterprise_svc', 'college_svc', 'platform_service')
  AND table_type = 'BASE TABLE'
GROUP BY table_schema;
```

### Rollback (if needed)

```bash
psql -h localhost -U zhitu_user -d zhitu_cloud -f database/migrations/001_add_missing_api_tables_rollback.sql
```

## Related Specification

- **Spec Path**: `.kiro/specs/missing-api-endpoints/`
- **Design Document**: `.kiro/specs/missing-api-endpoints/design.md`
- **Requirements**: `.kiro/specs/missing-api-endpoints/requirements.md`
- **Tasks**: `.kiro/specs/missing-api-endpoints/tasks.md`

## Next Steps

After applying this migration:
1. Create MyBatis Plus entity classes for each table
2. Create mapper interfaces and XML files
3. Implement service layer business logic
4. Create DTOs for API responses
5. Implement controller endpoints
6. Write unit tests and property-based tests
7. Update API documentation

## Notes

- All tables follow the existing project conventions
- Schema naming matches microservice boundaries
- Foreign keys reference existing tables in auth_center, internship_svc, and training_svc schemas
- The migration is idempotent - tables use IF NOT EXISTS where appropriate in schema files
