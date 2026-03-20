# Entity Relationship Diagram

Complete ER diagram for the Zhitu Internship Management Platform.

## Core Relationships Overview

```mermaid
erDiagram
    %% ============ Auth Center ============
    sys_tenant ||--o{ sys_user : "has users"
    sys_user ||--o{ sys_refresh_token : "has tokens"
    
    %% ============ Student Service ============
    sys_user ||--o| student_info : "extends (role=student)"
    sys_tenant ||--o{ student_info : "manages"
    
    %% ============ College Service ============
    sys_tenant ||--o| college_info : "extends (type=1)"
    sys_tenant ||--o{ organization : "manages"
    organization ||--o{ organization : "parent-child"
    organization ||--o{ student_info : "contains"
    
    %% ============ Enterprise Service ============
    sys_tenant ||--o| enterprise_info : "extends (type=2)"
    sys_tenant ||--o{ enterprise_staff : "employs"
    sys_user ||--o| enterprise_staff : "extends (role=enterprise)"
    sys_tenant ||--o{ talent_pool : "collects"
    student_info ||--o{ talent_pool : "collected in"
    
    %% ============ Internship Service ============
    sys_tenant ||--o{ internship_job : "posts"
    internship_job ||--o{ job_application : "receives"
    student_info ||--o{ job_application : "applies"
    job_application ||--o| internship_offer : "generates"
    internship_offer ||--o| internship_record : "converts to"
    student_info ||--o{ internship_record : "participates"
    sys_tenant ||--o{ internship_record : "hosts"
    internship_record ||--o{ weekly_report : "has"
    internship_record ||--o{ attendance : "tracks"
    internship_record ||--o| internship_certificate : "issues"
    
    %% ============ Training Service ============
    sys_tenant ||--o{ training_project : "publishes"
    sys_tenant ||--o{ training_plan : "schedules"
    training_project ||--o{ training_plan : "planned in"
    
    %% ============ Growth Service ============
    student_info ||--o{ evaluation_record : "receives"
    sys_user ||--o{ evaluation_record : "evaluates"
    student_info ||--o{ growth_badge : "earns"
    student_info ||--o{ warning_record : "has"
    sys_tenant ||--o{ warning_record : "monitors"
```

## Detailed Schema Relationships

### 1. Authentication & Tenant Management (auth_center)

```mermaid
erDiagram
    sys_tenant {
        bigserial id PK
        varchar name
        smallint type "0=platform 1=college 2=enterprise"
        smallint status
        text config
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    sys_user {
        bigserial id PK
        bigint tenant_id FK
        varchar username UK
        varchar password_hash
        varchar phone
        varchar role "student/enterprise/college/platform"
        varchar sub_role
        smallint status
        timestamptz last_login_at
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    sys_refresh_token {
        bigserial id PK
        bigint user_id FK
        varchar token_hash
        timestamptz expires_at
        timestamptz created_at
    }
    
    sys_tenant ||--o{ sys_user : "tenant_id"
    sys_user ||--o{ sys_refresh_token : "user_id"
```

### 2. Student Service (student_svc)

```mermaid
erDiagram
    student_info {
        bigserial id PK
        bigint user_id FK,UK
        bigint tenant_id FK
        varchar student_no
        varchar real_name
        smallint gender
        varchar phone
        varchar email
        varchar avatar_url
        bigint college_id FK
        bigint major_id FK
        bigint class_id FK
        varchar grade
        date enrollment_date
        date graduation_date
        varchar resume_url
        text skills "JSON array"
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
```

### 3. College Service (college_svc)

```mermaid
erDiagram
    college_info {
        bigserial id PK
        bigint tenant_id FK,UK
        varchar college_name
        varchar college_code
        varchar province
        varchar city
        varchar address
        varchar logo_url
        varchar contact_name
        varchar contact_phone
        varchar contact_email
        smallint cooperation_level
        smallint status
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    organization {
        bigserial id PK
        bigint tenant_id FK
        bigint parent_id FK
        smallint org_type "1=college 2=major 3=class"
        varchar org_name
        varchar org_code
        integer sort_order
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    organization ||--o{ organization : "parent_id (self-ref)"
```

### 4. Enterprise Service (enterprise_svc)

```mermaid
erDiagram
    enterprise_info {
        bigserial id PK
        bigint tenant_id FK,UK
        varchar enterprise_name
        varchar enterprise_code
        varchar industry
        varchar scale
        varchar province
        varchar city
        varchar address
        varchar logo_url
        varchar website
        text description
        varchar contact_name
        varchar contact_phone
        varchar contact_email
        smallint audit_status
        varchar audit_remark
        smallint status
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    enterprise_staff {
        bigserial id PK
        bigint tenant_id FK
        bigint user_id FK,UK
        varchar department
        varchar position
        boolean is_mentor
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    talent_pool {
        bigserial id PK
        bigint tenant_id FK
        bigint student_id FK
        bigint collected_by FK
        varchar remark
        timestamptz created_at
    }
```

### 5. Internship Service (internship_svc)

```mermaid
erDiagram
    internship_job {
        bigserial id PK
        bigint enterprise_id FK
        varchar job_title
        varchar job_type
        text description
        text requirements
        text tech_stack "JSON array"
        varchar industry
        varchar city
        integer salary_min
        integer salary_max
        integer headcount
        date start_date
        date end_date
        smallint status
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    job_application {
        bigserial id PK
        bigint job_id FK
        bigint student_id FK
        varchar resume_url
        text cover_letter
        smallint status "0=pending 1=interview 2=offer 3=rejected 4=hired"
        timestamptz applied_at
        timestamptz updated_at
    }
    
    internship_offer {
        bigserial id PK
        bigint application_id FK
        bigint student_id FK
        bigint enterprise_id FK
        bigint job_id FK
        integer salary
        date start_date
        date end_date
        smallint status
        smallint college_audit
        timestamptz created_at
        timestamptz updated_at
    }
    
    internship_record {
        bigserial id PK
        bigint student_id FK
        bigint enterprise_id FK
        bigint job_id FK
        bigint mentor_id FK
        bigint teacher_id FK
        date start_date
        date end_date
        smallint status
        timestamptz created_at
        timestamptz updated_at
    }
    
    weekly_report {
        bigserial id PK
        bigint internship_id FK
        bigint student_id FK
        date week_start
        date week_end
        text content
        decimal work_hours
        smallint status
        text review_comment
        bigint reviewed_by FK
        timestamptz reviewed_at
        timestamptz created_at
        timestamptz updated_at
    }
    
    attendance {
        bigserial id PK
        bigint internship_id FK
        bigint student_id FK
        timestamptz clock_in_time
        timestamptz clock_out_time
        decimal clock_in_lat
        decimal clock_in_lng
        smallint status
        varchar audit_remark
        bigint audited_by FK
        timestamptz created_at
    }
    
    internship_certificate {
        bigserial id PK
        bigint internship_id FK
        bigint student_id FK
        bigint enterprise_id FK
        varchar cert_no UK
        varchar cert_url
        bigint issued_by FK
        timestamptz issued_at
    }
    
    internship_job ||--o{ job_application : "job_id"
    job_application ||--o| internship_offer : "application_id"
    internship_offer ||--o| internship_record : "converts"
    internship_record ||--o{ weekly_report : "internship_id"
    internship_record ||--o{ attendance : "internship_id"
    internship_record ||--o| internship_certificate : "internship_id"
```

### 6. Training Service (training_svc)

```mermaid
erDiagram
    training_project {
        bigserial id PK
        bigint enterprise_id FK
        varchar project_name
        text description
        text tech_stack "JSON array"
        varchar industry
        integer max_teams
        integer max_members
        date start_date
        date end_date
        smallint audit_status
        smallint status
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    training_plan {
        bigserial id PK
        bigint tenant_id FK
        bigint project_id FK
        varchar plan_name
        date start_date
        date end_date
        bigint teacher_id FK
        smallint status
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    training_project ||--o{ training_plan : "project_id"
```

### 7. Growth Service (growth_svc)

```mermaid
erDiagram
    evaluation_record {
        bigserial id PK
        bigint student_id FK
        bigint evaluator_id FK
        varchar source_type "enterprise/school/peer"
        varchar ref_type "project/internship"
        bigint ref_id
        text scores "JSON"
        text comment
        varchar hire_recommendation
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    growth_badge {
        bigserial id PK
        bigint student_id FK
        varchar type "certificate/badge"
        varchar name
        date issue_date
        varchar image_url
        varchar blockchain_hash
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
    
    warning_record {
        bigserial id PK
        bigint tenant_id FK
        bigint student_id FK
        varchar warning_type "attendance/report/evaluation"
        smallint warning_level
        text description
        smallint status
        text intervene_note
        bigint intervened_by FK
        timestamptz intervened_at
        timestamptz created_at
        timestamptz updated_at
        boolean is_deleted
    }
```

## Key Relationships Explained

### User Role Extensions
- `sys_user.role = 'student'` → extends to `student_info` (1:1 via user_id)
- `sys_user.role = 'enterprise'` → extends to `enterprise_staff` (1:1 via user_id)
- `sys_user.role = 'college'` → no extension table (uses sub_role for permissions)
- `sys_user.role = 'platform'` → no extension table (platform admin)

### Tenant Type Extensions
- `sys_tenant.type = 1` (college) → extends to `college_info` (1:1 via tenant_id)
- `sys_tenant.type = 2` (enterprise) → extends to `enterprise_info` (1:1 via tenant_id)
- `sys_tenant.type = 0` (platform) → no extension table

### Internship Lifecycle
1. Enterprise posts `internship_job`
2. Student submits `job_application`
3. Enterprise sends `internship_offer`
4. Student accepts → creates `internship_record`
5. During internship: `weekly_report` + `attendance`
6. After completion: `internship_certificate`

### Organization Hierarchy
```
college_info (tenant)
  └─ organization (type=1, college/school)
      └─ organization (type=2, major)
          └─ organization (type=3, class)
              └─ student_info (class_id)
```

### Evaluation Sources
- `source_type = 'enterprise'` → Enterprise mentor evaluation
- `source_type = 'school'` → College teacher evaluation
- `source_type = 'peer'` → Student peer evaluation

## Cardinality Summary

| Relationship | Type | Description |
|--------------|------|-------------|
| tenant → users | 1:N | One tenant has many users |
| user → refresh_tokens | 1:N | One user has many tokens |
| user → student_info | 1:1 | One-to-one extension |
| user → enterprise_staff | 1:1 | One-to-one extension |
| tenant → college_info | 1:1 | One-to-one extension |
| tenant → enterprise_info | 1:1 | One-to-one extension |
| organization → children | 1:N | Self-referencing hierarchy |
| enterprise → jobs | 1:N | One enterprise posts many jobs |
| job → applications | 1:N | One job receives many applications |
| application → offer | 1:1 | One application generates one offer |
| student → internships | 1:N | One student has many internships |
| internship → reports | 1:N | One internship has many weekly reports |
| internship → attendance | 1:N | One internship has many attendance records |
| student → evaluations | 1:N | One student receives many evaluations |
| student → badges | 1:N | One student earns many badges |

## Index Strategy

### Primary Indexes
- All `id` columns (BIGSERIAL PRIMARY KEY)
- All foreign key columns
- Unique constraints (username, cert_no, etc.)

### Performance Indexes
- Tenant-based queries: `tenant_id` with partial index `WHERE is_deleted = FALSE`
- Status-based queries: `status` columns with partial indexes
- Date range queries: `start_date`, `end_date`, `created_at`
- Location queries: `city`, `province`
- Type/category queries: `role`, `type`, `source_type`

### Composite Indexes
- `(job_id, student_id)` for application uniqueness
- `(tenant_id, student_id)` for talent pool uniqueness
- `(ref_type, ref_id)` for evaluation lookups
- `(week_start, week_end)` for report queries
