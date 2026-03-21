# Design Document: Missing API Endpoints Implementation

## Overview

This design document specifies the implementation of 50 missing API endpoints across the Zhitu Cloud Platform's four user portals (Student, Enterprise, College, Platform). The platform uses a microservices architecture with Spring Boot 3.x, MyBatis Plus, PostgreSQL, Redis, and Spring Cloud Gateway.

### Architecture Context

The platform consists of:
- **zhitu-gateway**: API Gateway handling authentication, routing, and rate limiting
- **zhitu-student**: Student portal services (dashboard, training, internship, growth)
- **zhitu-enterprise**: Enterprise portal services (recruitment, mentorship, analytics)
- **zhitu-college**: College portal services (student management, CRM, warnings)
- **zhitu-platform**: Platform administration (tenants, audits, monitoring, system config)
- **zhitu-system**: Cross-cutting services (authentication, logging)

### Design Principles

1. **Consistency**: All endpoints follow existing patterns (Result<T> wrapper, RESTful conventions)
2. **Security**: JWT authentication via gateway, role-based access control
3. **Performance**: Redis caching for frequently accessed data, optimized database queries
4. **Maintainability**: Clear separation of concerns (Controller → Service → Mapper)
5. **Scalability**: Stateless services, horizontal scaling capability

## Architecture

### Service Distribution


**Student Portal Service (zhitu-student)**
- Dashboard statistics and capability radar
- Task management and recommendations
- Training project browsing and scrum boards
- Internship job browsing and weekly reports
- Growth evaluation and certificates/badges

**Enterprise Portal Service (zhitu-enterprise)**
- Dashboard statistics and activity feed
- Job posting management
- Application and interview management
- Talent pool management
- Mentor dashboard and analytics

**College Portal Service (zhitu-college)**
- Employment statistics and trends
- Student management and search
- Training plan management
- Internship oversight and contract auditing
- CRM (enterprise relationships, visits)
- Warning system and interventions

**Platform Service (zhitu-platform)**
- Platform-wide dashboard and health monitoring
- Tenant management
- Enterprise and project audits
- Tag and skill tree management
- Certificate and contract templates
- Recommendation banners and top lists
- Operation and security logs

### Data Flow Pattern

```
Frontend → Gateway → Microservice → Database
                ↓
              Redis Cache
```

1. **Request Flow**: Frontend sends JWT token → Gateway validates → Routes to microservice
2. **Authentication**: Gateway extracts user context (user_id, role, tenant_id) → Passes via headers
3. **Caching**: Frequently accessed data cached in Redis with TTL
4. **Response**: Microservice returns Result<T> wrapper → Gateway forwards to frontend

## Components and Interfaces

### API Endpoint Structure

All endpoints follow this pattern:
- **Base Path**: `/api/{domain}/v1/{resource}`
- **Authentication**: JWT token in Authorization header
- **Response Format**: `Result<T>` with code, message, data fields
- **Error Handling**: Standard HTTP status codes with descriptive messages

### Common DTOs


**PageRequest**
```java
public class PageRequest {
    private Integer page = 1;
    private Integer size = 10;
}
```

**PageResult<T>**
```java
public class PageResult<T> {
    private Integer page;
    private Integer size;
    private Long total;
    private List<T> records;
}
```

**Result<T>** (existing)
```java
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

### Controller Layer Pattern

All controllers follow this structure:
```java
@RestController
@RequestMapping("/api/{domain}/v1")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService service;
    
    @GetMapping("/resource")
    public Result<List<XxxDTO>> getList(@RequestParam String filter) {
        return Result.ok(service.getList(filter));
    }
}
```

### Service Layer Pattern

```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final XxxMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public List<XxxDTO> getList(String filter) {
        // Check cache first
        String cacheKey = "xxx:list:" + filter;
        List<XxxDTO> cached = (List<XxxDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;
        
        // Query database
        List<XxxDTO> result = mapper.selectList(buildQuery(filter));
        
        // Cache result
        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
        return result;
    }
}
```

## Data Models

### New Tables Required


#### 1. Student Dashboard & Tasks (student_svc schema)

**student_task**
```sql
CREATE TABLE student_svc.student_task (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    task_type VARCHAR(20) NOT NULL, -- 'training', 'internship', 'evaluation'
    ref_id BIGINT, -- Reference to project/job/etc
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority SMALLINT DEFAULT 1, -- 1=low, 2=medium, 3=high
    status SMALLINT NOT NULL DEFAULT 0, -- 0=pending, 1=completed
    due_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_task_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_task_type CHECK (task_type IN ('training', 'internship', 'evaluation')),
    CONSTRAINT chk_task_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_task_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_task_student ON student_svc.student_task(student_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_task_status ON student_svc.student_task(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_task_due ON student_svc.student_task(due_date) WHERE is_deleted = FALSE;
```

**student_capability**
```sql
CREATE TABLE student_svc.student_capability (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    dimension VARCHAR(50) NOT NULL, -- 'technical', 'communication', 'teamwork', 'problem_solving', 'leadership'
    score INTEGER NOT NULL, -- 0-100
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_capability_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_capability UNIQUE (student_id, dimension),
    CONSTRAINT chk_capability_score CHECK (score >= 0 AND score <= 100)
);

CREATE INDEX idx_capability_student ON student_svc.student_capability(student_id);
```

**student_recommendation**
```sql
CREATE TABLE student_svc.student_recommendation (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    rec_type VARCHAR(20) NOT NULL, -- 'project', 'job', 'course'
    ref_id BIGINT NOT NULL, -- ID of recommended item
    score DECIMAL(5,2), -- Recommendation score
    reason TEXT, -- Why recommended
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rec_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT chk_rec_type CHECK (rec_type IN ('project', 'job', 'course'))
);

CREATE INDEX idx_rec_student ON student_svc.student_recommendation(student_id);
CREATE INDEX idx_rec_type ON student_svc.student_recommendation(rec_type);
CREATE INDEX idx_rec_created ON student_svc.student_recommendation(created_at);
```

#### 2. Training Project Scrum Board (training_svc schema)

**project_task**
```sql
CREATE TABLE training_svc.project_task (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    team_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    assignee_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'todo', -- 'todo', 'in_progress', 'done'
    priority SMALLINT DEFAULT 2,
    story_points INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_ptask_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_ptask_assignee FOREIGN KEY (assignee_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_ptask_status CHECK (status IN ('todo', 'in_progress', 'done')),
    CONSTRAINT chk_ptask_priority CHECK (priority IN (1, 2, 3))
);

CREATE INDEX idx_ptask_project ON training_svc.project_task(project_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_ptask_status ON training_svc.project_task(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_ptask_assignee ON training_svc.project_task(assignee_id) WHERE is_deleted = FALSE;
```

**project_enrollment**
```sql
CREATE TABLE training_svc.project_enrollment (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    team_id BIGINT,
    role VARCHAR(20), -- 'member', 'leader'
    status SMALLINT NOT NULL DEFAULT 1, -- 1=active, 2=completed, 3=withdrawn
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_enroll_project FOREIGN KEY (project_id) REFERENCES training_svc.training_project(id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT uk_enrollment UNIQUE (project_id, student_id),
    CONSTRAINT chk_enroll_status CHECK (status IN (1, 2, 3))
);

CREATE INDEX idx_enroll_project ON training_svc.project_enrollment(project_id);
CREATE INDEX idx_enroll_student ON training_svc.project_enrollment(student_id);
```


#### 3. Enterprise Portal (enterprise_svc schema)

**enterprise_activity**
```sql
CREATE TABLE enterprise_svc.enterprise_activity (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    activity_type VARCHAR(30) NOT NULL, -- 'application', 'interview', 'report_submitted', 'evaluation'
    description TEXT NOT NULL,
    ref_type VARCHAR(20), -- 'job', 'application', 'intern'
    ref_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_activity_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id)
);

CREATE INDEX idx_activity_tenant ON enterprise_svc.enterprise_activity(tenant_id);
CREATE INDEX idx_activity_created ON enterprise_svc.enterprise_activity(created_at);
```

**enterprise_todo**
```sql
CREATE TABLE enterprise_svc.enterprise_todo (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    todo_type VARCHAR(30) NOT NULL, -- 'application_review', 'interview_schedule', 'report_review', 'evaluation_pending'
    ref_type VARCHAR(20),
    ref_id BIGINT,
    title VARCHAR(200) NOT NULL,
    priority SMALLINT DEFAULT 2,
    due_date TIMESTAMPTZ,
    status SMALLINT NOT NULL DEFAULT 0, -- 0=pending, 1=completed
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_todo_tenant FOREIGN KEY (tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_todo_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todo_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_todo_user ON enterprise_svc.enterprise_todo(user_id) WHERE status = 0;
CREATE INDEX idx_todo_due ON enterprise_svc.enterprise_todo(due_date) WHERE status = 0;
```

**interview_schedule**
```sql
CREATE TABLE enterprise_svc.interview_schedule (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enterprise_id BIGINT NOT NULL,
    interview_time TIMESTAMPTZ NOT NULL,
    location VARCHAR(200),
    interviewer_id BIGINT,
    interview_type VARCHAR(20), -- 'phone', 'video', 'onsite'
    status SMALLINT NOT NULL DEFAULT 0, -- 0=scheduled, 1=completed, 2=cancelled
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id) REFERENCES internship_svc.job_application(id),
    CONSTRAINT fk_interview_student FOREIGN KEY (student_id) REFERENCES student_svc.student_info(id),
    CONSTRAINT fk_interview_enterprise FOREIGN KEY (enterprise_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_interview_interviewer FOREIGN KEY (interviewer_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_interview_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_interview_application ON enterprise_svc.interview_schedule(application_id);
CREATE INDEX idx_interview_student ON enterprise_svc.interview_schedule(student_id);
CREATE INDEX idx_interview_time ON enterprise_svc.interview_schedule(interview_time);
```

#### 4. College CRM (college_svc schema)

**enterprise_relationship**
```sql
CREATE TABLE college_svc.enterprise_relationship (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    enterprise_tenant_id BIGINT NOT NULL,
    cooperation_level SMALLINT DEFAULT 1, -- 1=normal, 2=key, 3=strategic
    status SMALLINT NOT NULL DEFAULT 1, -- 1=active, 0=inactive
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_rel_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_rel_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT uk_relationship UNIQUE (college_tenant_id, enterprise_tenant_id),
    CONSTRAINT chk_rel_level CHECK (cooperation_level IN (1, 2, 3)),
    CONSTRAINT chk_rel_status CHECK (status IN (0, 1))
);

CREATE INDEX idx_rel_college ON college_svc.enterprise_relationship(college_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_rel_enterprise ON college_svc.enterprise_relationship(enterprise_tenant_id) WHERE is_deleted = FALSE;
```

**enterprise_visit**
```sql
CREATE TABLE college_svc.enterprise_visit (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    enterprise_tenant_id BIGINT NOT NULL,
    visit_date DATE NOT NULL,
    visitor_id BIGINT NOT NULL,
    visitor_name VARCHAR(50),
    purpose TEXT,
    outcome TEXT,
    next_action TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_visit_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_visit_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_visit_visitor FOREIGN KEY (visitor_id) REFERENCES auth_center.sys_user(id)
);

CREATE INDEX idx_visit_college ON college_svc.enterprise_visit(college_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_visit_enterprise ON college_svc.enterprise_visit(enterprise_tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_visit_date ON college_svc.enterprise_visit(visit_date) WHERE is_deleted = FALSE;
```

**enterprise_audit**
```sql
CREATE TABLE college_svc.enterprise_audit (
    id BIGSERIAL PRIMARY KEY,
    enterprise_tenant_id BIGINT NOT NULL,
    audit_type VARCHAR(20) NOT NULL, -- 'registration', 'qualification', 'annual'
    status SMALLINT NOT NULL DEFAULT 0, -- 0=pending, 1=passed, 2=rejected
    auditor_id BIGINT,
    audit_comment TEXT,
    audited_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_enterprise FOREIGN KEY (enterprise_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_audit_auditor FOREIGN KEY (auditor_id) REFERENCES auth_center.sys_user(id),
    CONSTRAINT chk_audit_status CHECK (status IN (0, 1, 2))
);

CREATE INDEX idx_audit_enterprise ON college_svc.enterprise_audit(enterprise_tenant_id);
CREATE INDEX idx_audit_status ON college_svc.enterprise_audit(status);
```

**internship_inspection**
```sql
CREATE TABLE college_svc.internship_inspection (
    id BIGSERIAL PRIMARY KEY,
    college_tenant_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    inspector_id BIGINT NOT NULL,
    inspection_date DATE NOT NULL,
    location VARCHAR(200),
    findings TEXT,
    issues TEXT,
    recommendations TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_inspection_college FOREIGN KEY (college_tenant_id) REFERENCES auth_center.sys_tenant(id),
    CONSTRAINT fk_inspection_internship FOREIGN KEY (internship_id) REFERENCES internship_svc.internship_record(id),
    CONSTRAINT fk_inspection_inspector FOREIGN KEY (inspector_id) REFERENCES auth_center.sys_user(id)
);

CREATE INDEX idx_inspection_college ON college_svc.internship_inspection(college_tenant_id);
CREATE INDEX idx_inspection_internship ON college_svc.internship_inspection(internship_id);
CREATE INDEX idx_inspection_date ON college_svc.internship_inspection(inspection_date);
```


#### 5. Platform Service (platform_service schema)

**sys_tag**
```sql
CREATE TABLE platform_service.sys_tag (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL, -- 'skill', 'industry', 'job_type', 'project_type', 'course_type'
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_tag_parent FOREIGN KEY (parent_id) REFERENCES platform_service.sys_tag(id),
    CONSTRAINT uk_tag UNIQUE (category, name)
);

CREATE INDEX idx_tag_category ON platform_service.sys_tag(category) WHERE is_deleted = FALSE;
CREATE INDEX idx_tag_parent ON platform_service.sys_tag(parent_id) WHERE is_deleted = FALSE;
```

**skill_tree**
```sql
CREATE TABLE platform_service.skill_tree (
    id BIGSERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL,
    skill_category VARCHAR(30) NOT NULL, -- 'technical', 'soft_skill', 'domain_knowledge'
    parent_id BIGINT,
    level INTEGER DEFAULT 1, -- Skill difficulty level
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_skill_parent FOREIGN KEY (parent_id) REFERENCES platform_service.skill_tree(id),
    CONSTRAINT chk_skill_category CHECK (skill_category IN ('technical', 'soft_skill', 'domain_knowledge'))
);

CREATE INDEX idx_skill_category ON platform_service.skill_tree(skill_category) WHERE is_deleted = FALSE;
CREATE INDEX idx_skill_parent ON platform_service.skill_tree(parent_id) WHERE is_deleted = FALSE;
```

**certificate_template**
```sql
CREATE TABLE platform_service.certificate_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    layout_config TEXT, -- JSON configuration
    background_url VARCHAR(255),
    signature_urls TEXT, -- JSON array of signature image URLs
    variables TEXT, -- JSON array: ["student_name", "certificate_type", "issue_date", "issuer_name"]
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_cert_template_usage ON platform_service.certificate_template(usage_count DESC) WHERE is_deleted = FALSE;
```

**contract_template**
```sql
CREATE TABLE platform_service.contract_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    description TEXT,
    contract_type VARCHAR(30), -- 'internship', 'training', 'employment'
    content TEXT, -- Template content with placeholders
    variables TEXT, -- JSON array: ["student_name", "enterprise_name", "position", "duration", "salary"]
    legal_terms TEXT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contract_template_usage ON platform_service.contract_template(usage_count DESC) WHERE is_deleted = FALSE;
```

**recommendation_banner**
```sql
CREATE TABLE platform_service.recommendation_banner (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NOT NULL,
    target_portal VARCHAR(20) NOT NULL, -- 'student', 'enterprise', 'college', 'all'
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    sort_order INTEGER DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1, -- 1=active, 0=inactive
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_banner_portal CHECK (target_portal IN ('student', 'enterprise', 'college', 'all')),
    CONSTRAINT chk_banner_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_banner_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_banner_portal ON platform_service.recommendation_banner(target_portal) WHERE is_deleted = FALSE;
CREATE INDEX idx_banner_dates ON platform_service.recommendation_banner(start_date, end_date) WHERE is_deleted = FALSE;
```

**recommendation_top_list**
```sql
CREATE TABLE platform_service.recommendation_top_list (
    id BIGSERIAL PRIMARY KEY,
    list_type VARCHAR(20) NOT NULL, -- 'mentor', 'course', 'project'
    item_ids TEXT NOT NULL, -- JSON array of IDs, ordered by position
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_top_list_type UNIQUE (list_type),
    CONSTRAINT chk_list_type CHECK (list_type IN ('mentor', 'course', 'project'))
);
```

**operation_log**
```sql
CREATE TABLE platform_service.operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_name VARCHAR(50),
    tenant_id BIGINT,
    module VARCHAR(50), -- 'student', 'enterprise', 'college', 'platform'
    operation VARCHAR(100), -- 'create_job', 'audit_enterprise', etc
    request_params TEXT,
    response_status INTEGER,
    result VARCHAR(20), -- 'success', 'failure'
    ip_address VARCHAR(50),
    user_agent TEXT,
    execution_time INTEGER, -- milliseconds
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oplog_user ON platform_service.operation_log(user_id);
CREATE INDEX idx_oplog_module ON platform_service.operation_log(module);
CREATE INDEX idx_oplog_created ON platform_service.operation_log(created_at);
CREATE INDEX idx_oplog_result ON platform_service.operation_log(result);
```

**security_log**
```sql
CREATE TABLE platform_service.security_log (
    id BIGSERIAL PRIMARY KEY,
    level VARCHAR(20) NOT NULL, -- 'info', 'warning', 'critical'
    event_type VARCHAR(50) NOT NULL, -- 'login_failed', 'permission_denied', 'suspicious_activity', 'data_breach_attempt'
    user_id BIGINT,
    ip_address VARCHAR(50),
    description TEXT NOT NULL,
    details TEXT, -- JSON with additional context
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_seclog_level CHECK (level IN ('info', 'warning', 'critical'))
);

CREATE INDEX idx_seclog_level ON platform_service.security_log(level);
CREATE INDEX idx_seclog_event ON platform_service.security_log(event_type);
CREATE INDEX idx_seclog_created ON platform_service.security_log(created_at);
CREATE INDEX idx_seclog_user ON platform_service.security_log(user_id);
```

**service_health**
```sql
CREATE TABLE platform_service.service_health (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(50) NOT NULL, -- 'zhitu-student', 'zhitu-enterprise', etc
    status VARCHAR(20) NOT NULL, -- 'healthy', 'degraded', 'down'
    response_time INTEGER, -- milliseconds
    error_rate DECIMAL(5,2), -- percentage
    cpu_usage DECIMAL(5,2), -- percentage
    memory_usage DECIMAL(5,2), -- percentage
    checked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_health_status CHECK (status IN ('healthy', 'degraded', 'down'))
);

CREATE INDEX idx_health_service ON platform_service.service_health(service_name);
CREATE INDEX idx_health_checked ON platform_service.service_health(checked_at);
```

**online_user_trend**
```sql
CREATE TABLE platform_service.online_user_trend (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMPTZ NOT NULL,
    online_count INTEGER NOT NULL,
    student_count INTEGER DEFAULT 0,
    enterprise_count INTEGER DEFAULT 0,
    college_count INTEGER DEFAULT 0
);

CREATE INDEX idx_trend_timestamp ON platform_service.online_user_trend(timestamp);
```


### Entity Classes

All entity classes follow MyBatis Plus conventions:

```java
@Data
@TableName("schema.table_name")
public class EntityName {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String fieldName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Boolean isDeleted;
}
```

### API Endpoint Specifications

#### Student Portal APIs (zhitu-student)

**1. GET /api/student-portal/v1/dashboard**
- **Purpose**: Get student dashboard statistics
- **Authentication**: Required (Student role)
- **Request**: None
- **Response**: 
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "trainingProjectCount": 3,
    "internshipJobCount": 15,
    "pendingTaskCount": 5,
    "growthScore": 85
  }
}
```
- **Service**: StudentPortalService.getDashboardStats()
- **Cache**: Redis key `student:dashboard:{userId}`, TTL 5 minutes

**2. GET /api/student-portal/v1/capability/radar**
- **Purpose**: Get capability radar chart data
- **Authentication**: Required (Student role)
- **Request**: None
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dimensions": [
      {"name": "technical", "score": 85},
      {"name": "communication", "score": 78},
      {"name": "teamwork", "score": 92},
      {"name": "problem_solving", "score": 80},
      {"name": "leadership", "score": 70}
    ]
  }
}
```
- **Service**: StudentPortalService.getCapabilityRadar()
- **Cache**: Redis key `student:capability:{userId}`, TTL 10 minutes

**3. GET /api/student-portal/v1/tasks?status={status}&page={page}&size={size}**
- **Purpose**: Get student tasks filtered by status
- **Authentication**: Required (Student role)
- **Request Parameters**:
  - status: "pending" | "completed"
  - page: integer (default 1)
  - size: integer (default 10)
- **Response**: PageResult<TaskDTO>
- **Service**: StudentPortalService.getTasks(status, page, size)

**4. GET /api/student-portal/v1/recommendations?type={type}**
- **Purpose**: Get personalized recommendations
- **Authentication**: Required (Student role)
- **Request Parameters**:
  - type: "all" | "project" | "job" | "course"
- **Response**: List<RecommendationDTO>
- **Service**: StudentPortalService.getRecommendations(type)
- **Cache**: Redis key `student:recommendations:{userId}:{type}`, TTL 15 minutes

**5. GET /api/student-portal/v1/training/projects?page={page}&size={size}**
- **Purpose**: Browse available training projects
- **Authentication**: Required (Student role)
- **Response**: PageResult<TrainingProjectDTO>
- **Service**: StudentPortalService.getTrainingProjects(page, size)
- **Cache**: Redis key `student:projects:list:{page}:{size}`, TTL 5 minutes

**6. GET /api/student-portal/v1/training/projects/{id}/board**
- **Purpose**: Get project scrum board
- **Authentication**: Required (Student role, must be enrolled)
- **Path Parameters**: id (project ID)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "todo": [
      {"id": 1, "title": "Task 1", "assignee": "张三", "priority": 2, "storyPoints": 5}
    ],
    "in_progress": [...],
    "done": [...]
  }
}
```
- **Service**: StudentPortalService.getProjectBoard(projectId)
- **Authorization**: Check enrollment via project_enrollment table

**7. GET /api/student-portal/v1/internship/jobs?page={page}&size={size}**
- **Purpose**: Browse internship jobs
- **Authentication**: Required (Student role)
- **Response**: PageResult<InternshipJobDTO>
- **Service**: StudentPortalService.getInternshipJobs(page, size)
- **Cache**: Redis key `student:jobs:list:{page}:{size}`, TTL 5 minutes

**8. GET /api/student-portal/v1/internship/reports/my?page={page}&size={size}**
- **Purpose**: Get my weekly reports
- **Authentication**: Required (Student role)
- **Response**: PageResult<WeeklyReportDTO>
- **Service**: StudentPortalService.getMyReports(page, size)

**9. GET /api/student-portal/v1/growth/evaluation**
- **Purpose**: Get evaluation summary
- **Authentication**: Required (Student role)
- **Response**: 
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "averageScore": 85.5,
    "evaluations": [
      {
        "evaluatorName": "李导师",
        "sourceType": "enterprise",
        "evaluationDate": "2024-01-15",
        "score": 88,
        "comment": "表现优秀"
      }
    ]
  }
}
```
- **Service**: StudentPortalService.getEvaluationSummary()

**10. GET /api/student-portal/v1/growth/certificates?page={page}&size={size}**
- **Purpose**: Get my certificates
- **Authentication**: Required (Student role)
- **Response**: PageResult<CertificateDTO>
- **Service**: StudentPortalService.getMyCertificates(page, size)

**11. GET /api/student-portal/v1/growth/badges?page={page}&size={size}**
- **Purpose**: Get my badges
- **Authentication**: Required (Student role)
- **Response**: PageResult<BadgeDTO>
- **Service**: StudentPortalService.getMyBadges(page, size)


#### Enterprise Portal APIs (zhitu-enterprise)

**12. GET /api/portal-enterprise/v1/dashboard/stats**
- **Purpose**: Get enterprise dashboard statistics
- **Authentication**: Required (Enterprise role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "activeJobCount": 8,
    "pendingApplicationCount": 25,
    "activeInternCount": 12,
    "trainingProjectCount": 3
  }
}
```
- **Service**: EnterprisePortalService.getDashboardStats()
- **Cache**: Redis key `enterprise:dashboard:{tenantId}`, TTL 5 minutes

**13. GET /api/portal-enterprise/v1/todos?page={page}&size={size}**
- **Purpose**: Get pending tasks
- **Authentication**: Required (Enterprise role)
- **Response**: PageResult<TodoDTO>
- **Service**: EnterprisePortalService.getTodos(page, size)

**14. GET /api/portal-enterprise/v1/activities?page={page}&size={size}**
- **Purpose**: Get recent activities
- **Authentication**: Required (Enterprise role)
- **Response**: PageResult<ActivityDTO>
- **Service**: EnterprisePortalService.getActivities(page, size)
- **Cache**: Redis key `enterprise:activities:{tenantId}:{page}`, TTL 3 minutes

**15. GET /api/internship/v1/enterprise/jobs?status={status}&page={page}&size={size}**
- **Purpose**: Get job postings
- **Authentication**: Required (Enterprise role)
- **Request Parameters**: status (optional)
- **Response**: PageResult<JobDTO>
- **Service**: EnterpriseJobService.getJobs(status, page, size)

**16. POST /api/internship/v1/enterprise/jobs**
- **Purpose**: Create job posting
- **Authentication**: Required (Enterprise role)
- **Request Body**:
```json
{
  "jobTitle": "Java开发实习生",
  "jobType": "internship",
  "description": "...",
  "requirements": "...",
  "techStack": ["Java", "Spring Boot"],
  "city": "北京",
  "salaryMin": 3000,
  "salaryMax": 5000,
  "headcount": 5,
  "startDate": "2024-03-01",
  "endDate": "2024-08-31"
}
```
- **Response**: Result<JobDTO>
- **Service**: EnterpriseJobService.createJob(request)
- **Validation**: Required fields, salary range, date range

**17. POST /api/internship/v1/enterprise/jobs/{id}/close**
- **Purpose**: Close job posting
- **Authentication**: Required (Enterprise role)
- **Path Parameters**: id (job ID)
- **Response**: Result<Void>
- **Service**: EnterpriseJobService.closeJob(jobId)
- **Side Effects**: Update status, notify applicants

**18. GET /api/internship/v1/enterprise/applications?jobId={jobId}&status={status}&page={page}&size={size}**
- **Purpose**: Get applications
- **Authentication**: Required (Enterprise role)
- **Request Parameters**: jobId (optional), status (optional)
- **Response**: PageResult<ApplicationDTO>
- **Service**: EnterpriseApplicationService.getApplications(jobId, status, page, size)

**19. POST /api/internship/v1/enterprise/interviews**
- **Purpose**: Schedule interview
- **Authentication**: Required (Enterprise role)
- **Request Body**:
```json
{
  "applicationId": 123,
  "interviewTime": "2024-02-15T14:00:00Z",
  "location": "公司会议室A",
  "interviewerId": 456,
  "interviewType": "onsite",
  "notes": "请携带简历"
}
```
- **Response**: Result<InterviewDTO>
- **Service**: EnterpriseApplicationService.scheduleInterview(request)
- **Side Effects**: Send notification to student

**20. GET /api/portal-enterprise/v1/talent-pool?page={page}&size={size}**
- **Purpose**: Get talent pool
- **Authentication**: Required (Enterprise role)
- **Response**: PageResult<TalentDTO>
- **Service**: EnterprisePortalService.getTalentPool(page, size)

**21. DELETE /api/portal-enterprise/v1/talent-pool/{id}**
- **Purpose**: Remove from talent pool
- **Authentication**: Required (Enterprise role)
- **Path Parameters**: id (talent pool entry ID)
- **Response**: Result<Void>
- **Service**: EnterprisePortalService.removeFromTalentPool(id)

**22. GET /api/portal-enterprise/v1/mentor/dashboard**
- **Purpose**: Get mentor dashboard
- **Authentication**: Required (Enterprise Mentor role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignedInternCount": 5,
    "pendingReportCount": 3,
    "pendingCodeReviewCount": 2,
    "recentActivities": [...]
  }
}
```
- **Service**: EnterpriseMentorService.getDashboard()
- **Cache**: Redis key `mentor:dashboard:{userId}`, TTL 5 minutes

**23. GET /api/portal-enterprise/v1/analytics?range={range}**
- **Purpose**: Get analytics data
- **Authentication**: Required (Enterprise Admin role)
- **Request Parameters**: range ("week" | "month" | "quarter" | "year")
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "applicationTrends": [...],
    "internPerformance": {...},
    "projectCompletionRate": 85.5,
    "mentorSatisfaction": 4.2
  }
}
```
- **Service**: EnterpriseAnalyticsService.getAnalytics(range)
- **Cache**: Redis key `enterprise:analytics:{tenantId}:{range}`, TTL 30 minutes


#### College Portal APIs (zhitu-college)

**24. GET /api/portal-college/v1/dashboard/stats?year={year}**
- **Purpose**: Get employment statistics
- **Authentication**: Required (College role)
- **Request Parameters**: year (optional, default current year)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalStudentCount": 1200,
    "internshipParticipationRate": 85.5,
    "employmentRate": 92.3,
    "averageSalary": 8500,
    "topHiringEnterprises": [
      {"name": "腾讯", "count": 15},
      {"name": "阿里巴巴", "count": 12}
    ]
  }
}
```
- **Service**: CollegePortalService.getDashboardStats(year)
- **Cache**: Redis key `college:dashboard:{tenantId}:{year}`, TTL 1 hour

**25. GET /api/portal-college/v1/dashboard/trends?dimension={dimension}**
- **Purpose**: Get employment trends
- **Authentication**: Required (College role)
- **Request Parameters**: dimension ("month" | "quarter" | "year")
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "internshipTrends": [
      {"period": "2024-01", "rate": 82.5},
      {"period": "2024-02", "rate": 85.0}
    ],
    "employmentTrends": [...],
    "salaryTrends": [...],
    "industryDistribution": [...]
  }
}
```
- **Service**: CollegePortalService.getEmploymentTrends(dimension)
- **Cache**: Redis key `college:trends:{tenantId}:{dimension}`, TTL 1 hour

**26. GET /api/user/v1/college/students?keyword={keyword}&classId={classId}&status={status}&page={page}&size={size}**
- **Purpose**: Search and list students
- **Authentication**: Required (College role)
- **Request Parameters**: keyword, classId, status (all optional)
- **Response**: PageResult<StudentDTO>
- **Service**: CollegeStudentService.getStudents(filters, page, size)

**27. GET /api/training/v1/college/plans?semester={semester}&page={page}&size={size}**
- **Purpose**: Get training plans
- **Authentication**: Required (College role)
- **Request Parameters**: semester (optional)
- **Response**: PageResult<TrainingPlanDTO>
- **Service**: CollegeTrainingService.getPlans(semester, page, size)

**28. POST /api/training/v1/college/plans**
- **Purpose**: Create training plan
- **Authentication**: Required (College role)
- **Request Body**:
```json
{
  "projectId": 123,
  "planName": "2024春季实训计划",
  "startDate": "2024-03-01",
  "endDate": "2024-06-30",
  "teacherId": 456
}
```
- **Response**: Result<TrainingPlanDTO>
- **Service**: CollegeTrainingService.createPlan(request)
- **Validation**: Date range, teacher availability

**29. POST /api/training/v1/college/mentors/assign**
- **Purpose**: Assign mentor to training plan
- **Authentication**: Required (College role)
- **Request Body**:
```json
{
  "planId": 123,
  "teacherId": 456
}
```
- **Response**: Result<Void>
- **Service**: CollegeTrainingService.assignMentor(planId, teacherId)
- **Validation**: Teacher qualifications, availability

**30. GET /api/internship/v1/college/students?status={status}&page={page}&size={size}**
- **Purpose**: Monitor student internships
- **Authentication**: Required (College role)
- **Request Parameters**: status (optional)
- **Response**: PageResult<InternshipStudentDTO>
- **Service**: CollegeInternshipService.getInternshipStudents(status, page, size)

**31. GET /api/internship/v1/college/contracts/pending?page={page}&size={size}**
- **Purpose**: Get pending contracts for audit
- **Authentication**: Required (College role)
- **Response**: PageResult<ContractDTO>
- **Service**: CollegeInternshipService.getPendingContracts(page, size)

**32. POST /api/internship/v1/college/contracts/{id}/audit**
- **Purpose**: Audit internship contract
- **Authentication**: Required (College role)
- **Path Parameters**: id (contract/offer ID)
- **Request Body**:
```json
{
  "action": "pass",
  "comment": "合同条款符合要求"
}
```
- **Response**: Result<Void>
- **Service**: CollegeInternshipService.auditContract(id, action, comment)
- **Validation**: Contract terms, enterprise credentials

**33. POST /api/internship/v1/college/inspections**
- **Purpose**: Create inspection record
- **Authentication**: Required (College role)
- **Request Body**:
```json
{
  "internshipId": 123,
  "inspectionDate": "2024-02-15",
  "location": "企业现场",
  "findings": "实习环境良好",
  "issues": "无",
  "recommendations": "继续保持"
}
```
- **Response**: Result<InspectionDTO>
- **Service**: CollegeInternshipService.createInspection(request)

**34. GET /api/portal-college/v1/crm/enterprises?level={level}&industry={industry}&page={page}&size={size}**
- **Purpose**: Get enterprise relationships
- **Authentication**: Required (College role)
- **Request Parameters**: level, industry (optional)
- **Response**: PageResult<EnterpriseRelationshipDTO>
- **Service**: CollegeCRMService.getEnterprises(filters, page, size)

**35. GET /api/portal-college/v1/crm/audits?status={status}&page={page}&size={size}**
- **Purpose**: Get enterprise audits
- **Authentication**: Required (College role)
- **Request Parameters**: status (optional)
- **Response**: PageResult<EnterpriseAuditDTO>
- **Service**: CollegeCRMService.getAudits(status, page, size)

**36. POST /api/portal-college/v1/crm/audits/{id}**
- **Purpose**: Audit enterprise
- **Authentication**: Required (College role)
- **Path Parameters**: id (audit ID)
- **Request Body**:
```json
{
  "action": "pass",
  "comment": "资质审核通过"
}
```
- **Response**: Result<Void>
- **Service**: CollegeCRMService.auditEnterprise(id, action, comment)
- **Validation**: Business license, qualification documents

**37. PUT /api/portal-college/v1/crm/enterprises/{id}/level**
- **Purpose**: Update enterprise cooperation level
- **Authentication**: Required (College role)
- **Path Parameters**: id (enterprise ID)
- **Request Body**:
```json
{
  "level": 2,
  "reason": "合作深化，升级为重点合作企业"
}
```
- **Response**: Result<Void>
- **Service**: CollegeCRMService.updateEnterpriseLevel(id, level, reason)

**38. GET /api/portal-college/v1/crm/visits?enterpriseId={enterpriseId}&page={page}&size={size}**
- **Purpose**: Get visit records
- **Authentication**: Required (College role)
- **Request Parameters**: enterpriseId (optional)
- **Response**: PageResult<VisitDTO>
- **Service**: CollegeCRMService.getVisits(enterpriseId, page, size)

**39. POST /api/portal-college/v1/crm/visits**
- **Purpose**: Create visit record
- **Authentication**: Required (College role)
- **Request Body**:
```json
{
  "enterpriseTenantId": 123,
  "visitDate": "2024-02-15",
  "purpose": "洽谈实习合作",
  "outcome": "达成初步合作意向",
  "nextAction": "签订合作协议"
}
```
- **Response**: Result<VisitDTO>
- **Service**: CollegeCRMService.createVisit(request)

**40. GET /api/portal-college/v1/warnings?level={level}&type={type}&status={status}&page={page}&size={size}**
- **Purpose**: Get warning records
- **Authentication**: Required (College role)
- **Request Parameters**: level, type, status (all optional)
- **Response**: PageResult<WarningDTO>
- **Service**: CollegeWarningService.getWarnings(filters, page, size)

**41. GET /api/portal-college/v1/warnings/stats**
- **Purpose**: Get warning statistics
- **Authentication**: Required (College role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 25,
    "byLevel": {"low": 10, "medium": 12, "high": 3},
    "byType": {"attendance": 8, "report": 10, "evaluation": 7},
    "byStatus": {"pending": 15, "intervened": 10}
  }
}
```
- **Service**: CollegeWarningService.getWarningStats()
- **Cache**: Redis key `college:warnings:stats:{tenantId}`, TTL 10 minutes

**42. POST /api/portal-college/v1/warnings/{id}/intervene**
- **Purpose**: Record intervention
- **Authentication**: Required (College role)
- **Path Parameters**: id (warning ID)
- **Request Body**:
```json
{
  "interveneNote": "已与学生沟通，制定改进计划",
  "expectedOutcome": "下周考勤恢复正常"
}
```
- **Response**: Result<Void>
- **Service**: CollegeWarningService.intervene(id, request)
- **Side Effects**: Update warning status to "intervened"


#### Platform Administration APIs (zhitu-platform)

**43. GET /api/system/v1/dashboard/stats**
- **Purpose**: Get platform-wide statistics
- **Authentication**: Required (Platform Admin role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalTenantCount": 150,
    "totalUserCount": 50000,
    "activeUserCount": 12000,
    "totalEnterpriseCount": 800,
    "pendingAuditCount": 25
  }
}
```
- **Service**: PlatformService.getDashboardStats()
- **Cache**: Redis key `platform:dashboard:stats`, TTL 10 minutes

**44. GET /api/monitor/v1/health**
- **Purpose**: Get system health status
- **Authentication**: Required (Platform Admin role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "services": [
      {
        "name": "zhitu-student",
        "status": "healthy",
        "responseTime": 45,
        "errorRate": 0.1,
        "cpuUsage": 35.5,
        "memoryUsage": 62.3
      }
    ]
  }
}
```
- **Service**: PlatformMonitorService.getHealth()
- **Cache**: Redis key `platform:health`, TTL 1 minute

**45. GET /api/monitor/v1/users/online-trend**
- **Purpose**: Get online user trend (24h)
- **Authentication**: Required (Platform Admin role)
- **Response**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "trend": [
      {"timestamp": "2024-02-15T00:00:00Z", "count": 1200},
      {"timestamp": "2024-02-15T01:00:00Z", "count": 800}
    ]
  }
}
```
- **Service**: PlatformMonitorService.getOnlineUserTrend()
- **Cache**: Redis key `platform:online:trend`, TTL 5 minutes

**46. GET /api/monitor/v1/services**
- **Purpose**: Get service health details
- **Authentication**: Required (Platform Admin role)
- **Response**: List<ServiceHealthDTO>
- **Service**: PlatformMonitorService.getServices()

**47. GET /api/system/v1/tenants/colleges?type={type}&status={status}&page={page}&size={size}**
- **Purpose**: Get tenant list
- **Authentication**: Required (Platform Admin role)
- **Request Parameters**: type, status (optional)
- **Response**: PageResult<TenantDTO>
- **Service**: PlatformService.getTenantList(type, status, page, size)

**48. GET /api/system/v1/audits/enterprises?status={status}&page={page}&size={size}**
- **Purpose**: Get enterprise audits
- **Authentication**: Required (Platform Admin role)
- **Request Parameters**: status (optional)
- **Response**: PageResult<EnterpriseAuditDTO>
- **Service**: PlatformAuditService.getEnterpriseAudits(status, page, size)

**49. POST /api/system/v1/audits/enterprises/{id}**
- **Purpose**: Audit enterprise registration
- **Authentication**: Required (Platform Admin role)
- **Path Parameters**: id (enterprise tenant ID)
- **Request Body**:
```json
{
  "action": "pass",
  "rejectReason": null
}
```
- **Response**: Result<Void>
- **Service**: PlatformAuditService.auditEnterprise(id, action, rejectReason)
- **Side Effects**: Activate account on pass, send notification

**50. GET /api/portal-platform/v1/audits/projects?status={status}&page={page}&size={size}**
- **Purpose**: Get project audits
- **Authentication**: Required (Platform Admin role)
- **Request Parameters**: status (optional)
- **Response**: PageResult<ProjectAuditDTO>
- **Service**: PlatformAuditService.getProjectAudits(status, page, size)

**51. POST /api/portal-platform/v1/audits/projects/{id}**
- **Purpose**: Audit training project
- **Authentication**: Required (Platform Admin role)
- **Path Parameters**: id (project ID)
- **Request Body**:
```json
{
  "action": "pass",
  "qualityRating": 4
}
```
- **Response**: Result<Void>
- **Service**: PlatformAuditService.auditProject(id, action, qualityRating)

**52-57. Tag, Skill Tree, Template Management**
- Already implemented in PlatformSystemController
- See existing implementation for details

**58. GET /api/portal-platform/v1/recommendations/banner?portal={portal}**
- **Purpose**: Get recommendation banners
- **Authentication**: Required (Platform Admin role)
- **Request Parameters**: portal (optional)
- **Response**: List<BannerDTO>
- **Service**: PlatformRecommendationService.getBanners(portal)
- **Cache**: Redis key `platform:banners:{portal}`, TTL 30 minutes

**59. POST /api/portal-platform/v1/recommendations/banner**
- **Purpose**: Save recommendation banner
- **Authentication**: Required (Platform Admin role)
- **Request Body**:
```json
{
  "title": "春季实习招聘",
  "imageUrl": "https://...",
  "linkUrl": "https://...",
  "targetPortal": "student",
  "startDate": "2024-03-01",
  "endDate": "2024-03-31"
}
```
- **Response**: Result<BannerDTO>
- **Service**: PlatformRecommendationService.saveBanner(request)
- **Cache Invalidation**: Clear `platform:banners:*`

**60. GET /api/portal-platform/v1/recommendations/top-list?listType={listType}**
- **Purpose**: Get top list
- **Authentication**: Required (Platform Admin role)
- **Request Parameters**: listType ("mentor" | "course" | "project")
- **Response**: TopListDTO
- **Service**: PlatformRecommendationService.getTopList(listType)
- **Cache**: Redis key `platform:toplist:{listType}`, TTL 1 hour

**61. POST /api/portal-platform/v1/recommendations/top-list**
- **Purpose**: Save top list
- **Authentication**: Required (Platform Admin role)
- **Request Body**:
```json
{
  "listType": "mentor",
  "itemIds": [123, 456, 789]
}
```
- **Response**: Result<Void>
- **Service**: PlatformRecommendationService.saveTopList(request)
- **Validation**: Max 10 items
- **Cache Invalidation**: Clear `platform:toplist:{listType}`

**62-63. Operation and Security Logs**
- Already implemented in PlatformSystemController
- See existing implementation for details

## Caching Strategy

### Cache Keys Pattern

```
{service}:{resource}:{identifier}[:{filter}]
```

Examples:
- `student:dashboard:123` - Student dashboard for user 123
- `enterprise:jobs:list:1:10` - Enterprise jobs page 1, size 10
- `college:warnings:stats:456` - Warning stats for college tenant 456

### Cache TTL Guidelines

| Data Type | TTL | Reason |
|-----------|-----|--------|
| Dashboard stats | 5 minutes | Frequently updated, balance freshness vs load |
| List data (paginated) | 5 minutes | Balance between freshness and performance |
| Analytics data | 30 minutes | Expensive queries, less frequent updates |
| Configuration data | 1 hour | Rarely changes |
| Health metrics | 1 minute | Need near real-time data |
| Recommendations | 15 minutes | Balance personalization vs performance |

### Cache Invalidation Strategy

1. **Time-based**: All caches use TTL for automatic expiration
2. **Event-based**: Invalidate on data modification
   - Job created/updated → Clear `enterprise:jobs:*`
   - Student enrolled → Clear `student:projects:*`
   - Warning created → Clear `college:warnings:stats:*`
3. **Pattern-based**: Use Redis KEYS or SCAN for wildcard invalidation

### Cache Implementation

```java
@Service
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public <T> T getOrSet(String key, Supplier<T> supplier, long timeout, TimeUnit unit) {
        T cached = (T) redisTemplate.opsForValue().get(key);
        if (cached != null) return cached;
        
        T value = supplier.get();
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        return value;
    }
    
    public void invalidate(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```


## Error Handling

### Error Response Format

All errors follow consistent format:

```json
{
  "code": 400,
  "message": "Validation failed: jobTitle is required",
  "data": null
}
```

### HTTP Status Codes

| Code | Scenario | Example |
|------|----------|---------|
| 200 | Success | Data retrieved successfully |
| 400 | Validation error | Missing required field, invalid format |
| 401 | Authentication failed | Missing/invalid JWT token |
| 403 | Authorization failed | User lacks required permission |
| 404 | Resource not found | Job ID does not exist |
| 429 | Rate limit exceeded | Too many requests |
| 500 | Server error | Database connection failed |

### Error Handling Implementation

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return Result.fail(400, "Validation failed: " + message);
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        String errorId = UUID.randomUUID().toString();
        return Result.fail(500, "Internal server error. Error ID: " + errorId);
    }
}
```

### Validation Rules

1. **Required Fields**: Validate presence before processing
2. **Data Types**: Ensure correct types (integer, date, email)
3. **Length Limits**: Enforce maximum lengths
4. **Format Validation**: Email, phone, URL patterns
5. **Business Rules**: Date ranges, salary ranges, status transitions
6. **Foreign Keys**: Verify referenced entities exist

### Transaction Boundaries

```java
@Service
@RequiredArgsConstructor
public class ExampleService {
    
    @Transactional(rollbackFor = Exception.class)
    public void createJobWithNotification(JobRequest request) {
        // All operations in single transaction
        Job job = jobMapper.insert(buildJob(request));
        activityMapper.insert(buildActivity(job));
        notificationService.sendToSubscribers(job);
        // Rollback all if any step fails
    }
}
```

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests for comprehensive coverage:

**Unit Tests**: Focus on specific examples, edge cases, and integration points
- Specific dashboard data scenarios
- Authentication failure cases
- Validation error cases
- Database constraint violations
- Cache hit/miss scenarios

**Property-Based Tests**: Verify universal properties across all inputs
- Response format consistency
- Filtering and pagination correctness
- Authorization enforcement
- Data validation rules
- Transaction atomicity

### Property-Based Testing Configuration

- **Library**: Use Jqwik for Java property-based testing
- **Iterations**: Minimum 100 iterations per property test
- **Tagging**: Each test references its design document property
- **Tag Format**: `@Tag("Feature: missing-api-endpoints, Property {number}: {property_text}")`

### Test Organization

```
src/test/java/
├── unit/
│   ├── controller/
│   ├── service/
│   └── mapper/
└── property/
    ├── StudentPortalPropertiesTest.java
    ├── EnterprisePortalPropertiesTest.java
    ├── CollegePortalPropertiesTest.java
    └── PlatformPropertiesTest.java
```

### Example Property Test

```java
@Tag("Feature: missing-api-endpoints, Property 1: Response format consistency")
class ResponseFormatPropertyTest {
    
    @Property(tries = 100)
    void allEndpointsReturnResultWrapper(@ForAll("validEndpoints") String endpoint) {
        Response response = callEndpoint(endpoint);
        assertThat(response).hasField("code");
        assertThat(response).hasField("message");
        assertThat(response).hasField("data");
    }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all 50 requirements, I identified the following redundancies:
- Multiple dashboard endpoints (student, enterprise, college, platform) share the same response structure validation
- All list endpoints share pagination and filtering properties
- All audit endpoints share status transition properties
- All CRUD endpoints share validation properties

These have been consolidated into comprehensive properties that apply across multiple requirements.


### Property 1: Response Format Consistency

*For any* authenticated API request to any endpoint, the response SHALL follow the Result<T> wrapper format with code, message, and data fields.

**Validates: Requirements 1.2, 2.2, 12.2, 20.1, 28.1, 42.1, 42.2**

### Property 2: Dashboard Field Completeness

*For any* dashboard endpoint (student, enterprise, college, platform), the response data SHALL include all specified statistical fields without null values for active metrics.

**Validates: Requirements 1.3-1.6, 12.3-12.6, 20.3-20.7, 28.2-28.6**

### Property 3: Authentication Enforcement

*For any* protected endpoint, when JWT token is missing or invalid, the system SHALL return 401 status with descriptive error message.

**Validates: Requirements 1.8, 41.1, 41.4, 43.2**

### Property 4: Pagination Consistency

*For any* paginated list endpoint, the response SHALL include page, size, total, and records fields, and the number of returned records SHALL NOT exceed the requested size.

**Validates: Requirements 3.5, 5.3, 7.3, 42.4**

### Property 5: Filtering Correctness

*For any* list endpoint with status filter parameter, all returned records SHALL match the specified status value.

**Validates: Requirements 3.3, 3.4, 7.2, 15.6, 22.2**

### Property 6: Authorization by Enrollment

*For any* student accessing a project scrum board, if the student is not enrolled in the project, the system SHALL return 403 status with error message.

**Validates: Requirements 6.6, 41.5**

### Property 7: Task Organization by Status

*For any* project scrum board, tasks SHALL be organized into exactly three columns (todo, in_progress, done), and each task SHALL appear in exactly one column matching its status.

**Validates: Requirements 6.4, 6.5**

### Property 8: Ordering Consistency

*For any* list endpoint with specified ordering (e.g., by date descending), the returned records SHALL be ordered according to the specification.

**Validates: Requirements 8.5, 10.5, 11.5, 14.4, 26.6**

### Property 9: Tenant Isolation

*For any* multi-tenant endpoint, the returned data SHALL only include records belonging to the authenticated user's tenant.

**Validates: Requirements 13.2, 17.3, 22.1, 30.1, 41.3**

### Property 10: Required Field Validation

*For any* POST endpoint, when required fields are missing from the request body, the system SHALL return 400 status with field-specific error messages.

**Validates: Requirements 15.4, 23.5, 26.4, 37.3, 45.1, 45.2**

### Property 11: Date Range Validation

*For any* request with start_date and end_date fields, when end_date is before start_date, the system SHALL reject the request with 400 status.

**Validates: Requirements 23.5, 37.6, 45.5**

### Property 12: Foreign Key Validation

*For any* request referencing another entity by ID, when the referenced entity does not exist, the system SHALL return 404 status with resource identifier.

**Validates: Requirements 29.6, 43.4, 45.6**

### Property 13: Status Transition Validity

*For any* audit endpoint, when action is "reject", the reject_reason field SHALL be required; when action is "pass", the status SHALL transition to approved state.

**Validates: Requirements 31.5, 31.6, 32.4, 36.4**

### Property 14: Notification Side Effects

*For any* operation that triggers notifications (job closure, interview scheduling, audit approval), the notification SHALL be sent to the affected user.

**Validates: Requirements 17.5, 19.5, 31.6**

### Property 15: Cache Invalidation on Mutation

*For any* POST, PUT, or DELETE operation, related cache entries SHALL be invalidated to ensure data consistency.

**Validates: Requirements 59, 61 (cache invalidation)**

### Property 16: Recommendation Filtering by Type

*For any* recommendation request with type parameter, all returned recommendations SHALL match the specified type.

**Validates: Requirements 4.3, 4.4, 4.5**

### Property 17: Analytics Time Range Filtering

*For any* analytics endpoint with range parameter, the returned data SHALL be aggregated according to the specified time dimension (week, month, quarter, year).

**Validates: Requirements 19.2, 21.2**

### Property 18: Search Keyword Matching

*For any* search endpoint with keyword parameter, all returned records SHALL contain the keyword in searchable fields (case-insensitive).

**Validates: Requirements 22.2**

### Property 19: Hierarchical Data Structure

*For any* hierarchical data endpoint (skill tree, organization tree), the response SHALL include parent-child relationships, and no circular references SHALL exist.

**Validates: Requirements 33.7, 34.2, 34.4**

### Property 20: Template Variable Validation

*For any* template (certificate or contract), the variables field SHALL be a valid JSON array containing only allowed placeholder names.

**Validates: Requirements 35.3, 36.3**

### Property 21: Banner Scheduling Logic

*For any* banner retrieval request, only banners where current date is between start_date and end_date SHALL be returned.

**Validates: Requirements 37.5, 37.6**

### Property 22: Top List Size Limit

*For any* top list save operation, when item_ids array contains more than 10 items, the system SHALL reject the request with 400 status.

**Validates: Requirements 38.6**

### Property 23: Log Retention Policy

*For any* log retrieval endpoint, only logs within the retention period (90 days for operation logs, 180 days for security logs) SHALL be returned.

**Validates: Requirements 39.7, 40.7**

### Property 24: Rate Limit Enforcement

*For any* user making requests, when the request count exceeds the rate limit (1000/hour for authenticated, 100/hour for unauthenticated), the system SHALL return 429 status with retry-after header.

**Validates: Requirements 49.2, 49.3, 49.4**

### Property 25: Sensitive Data Masking

*For any* log entry containing sensitive fields (phone, email, ID number), these fields SHALL be masked in the stored log.

**Validates: Requirements 50.3**

### Property 26: Transaction Atomicity

*For any* operation involving multiple database writes, either all writes SHALL succeed and commit, or all SHALL rollback on any failure.

**Validates: Requirements 46.2, 46.3**

### Property 27: Role-Based Access Control

*For any* endpoint with role restrictions, when a user without the required role attempts access, the system SHALL return 403 status.

**Validates: Requirements 41.6**

### Property 28: Capability Score Range

*For any* capability radar data, all dimension scores SHALL be between 0 and 100 inclusive.

**Validates: Requirements 2.4**

### Property 29: Evaluation Score Calculation

*For any* student evaluation summary, the average score SHALL equal the arithmetic mean of all evaluation scores for that student.

**Validates: Requirements 9.5**

### Property 30: Warning Statistics Consistency

*For any* warning statistics response, the sum of counts by level SHALL equal the total count, and the sum of counts by status SHALL equal the total count.

**Validates: Requirements 27.2 (warning stats endpoint)**


## Implementation Roadmap

### Phase 1: Database Schema (Week 1)
1. Create all new tables in respective schemas
2. Add indexes for performance
3. Set up foreign key constraints
4. Create database migration scripts
5. Test schema with sample data

### Phase 2: Core Services (Week 2-3)
1. Implement entity classes with MyBatis Plus annotations
2. Create mapper interfaces
3. Implement service layer with business logic
4. Add Redis caching to services
5. Write unit tests for services

### Phase 3: API Controllers (Week 4)
1. Implement controller classes for each portal
2. Add request/response DTOs
3. Add validation annotations
4. Implement error handling
5. Write controller unit tests

### Phase 4: Integration & Testing (Week 5)
1. Integration testing across services
2. Property-based testing implementation
3. Performance testing and optimization
4. Security testing (authentication, authorization)
5. End-to-end testing with frontend

### Phase 5: Deployment (Week 6)
1. Update API documentation (OpenAPI/Swagger)
2. Configure monitoring and alerting
3. Deploy to staging environment
4. User acceptance testing
5. Production deployment

## Monitoring and Observability

### Metrics to Track

**Request Metrics**
- Request count per endpoint
- Response time (p50, p95, p99)
- Error rate by status code
- Request rate per user/tenant

**Business Metrics**
- Active users per portal
- Job posting creation rate
- Application submission rate
- Internship enrollment rate
- Warning creation rate

**System Metrics**
- Database connection pool usage
- Redis cache hit rate
- Service health status
- Memory and CPU usage

### Logging Strategy

**Structured Logging Format**
```json
{
  "timestamp": "2024-02-15T10:30:00Z",
  "level": "INFO",
  "service": "zhitu-student",
  "traceId": "abc123",
  "userId": 123,
  "tenantId": 456,
  "endpoint": "/api/student-portal/v1/dashboard",
  "method": "GET",
  "statusCode": 200,
  "responseTime": 45,
  "message": "Dashboard retrieved successfully"
}
```

**Log Levels**
- **DEBUG**: Detailed flow information for troubleshooting
- **INFO**: Normal operations (requests, responses)
- **WARN**: Unexpected but handled situations (cache miss, retry)
- **ERROR**: Errors requiring attention (exceptions, failures)

### Alerting Rules

1. **Error Rate > 5%**: Alert on-call engineer
2. **Response Time p95 > 2s**: Alert performance team
3. **Service Down**: Immediate page to on-call
4. **Cache Hit Rate < 70%**: Alert for investigation
5. **Database Connection Pool > 80%**: Alert for scaling

## Security Considerations

### Authentication Flow

```
1. User logs in → zhitu-auth generates JWT
2. JWT contains: userId, role, tenantId, expiration
3. Frontend stores JWT in localStorage
4. Frontend sends JWT in Authorization header
5. Gateway validates JWT signature and expiration
6. Gateway extracts user context → passes to microservice
7. Microservice uses context for authorization
```

### Authorization Patterns

**Role-Based Access Control (RBAC)**
```java
@PreAuthorize("hasRole('STUDENT')")
public Result<DashboardDTO> getDashboard() {
    // Only students can access
}

@PreAuthorize("hasRole('ENTERPRISE_ADMIN')")
public Result<Void> closeJob(Long jobId) {
    // Only enterprise admins can close jobs
}
```

**Resource-Based Authorization**
```java
public Result<ProjectBoardDTO> getProjectBoard(Long projectId) {
    Long userId = UserContext.getUserId();
    if (!enrollmentService.isEnrolled(userId, projectId)) {
        throw new ForbiddenException("Not enrolled in project");
    }
    return Result.ok(projectService.getBoard(projectId));
}
```

### Data Protection

1. **Encryption at Rest**: Sensitive fields encrypted in database
2. **Encryption in Transit**: TLS 1.3 for all connections
3. **Data Masking**: PII masked in logs and non-production environments
4. **Access Auditing**: All data access logged to audit trail
5. **Tenant Isolation**: Strict filtering by tenant_id in all queries

### SQL Injection Prevention

```java
// SAFE: Using MyBatis Plus parameterized queries
LambdaQueryWrapper<Student> query = new LambdaQueryWrapper<>();
query.eq(Student::getTenantId, tenantId)
     .like(Student::getRealName, keyword); // Parameterized

// UNSAFE: Never use string concatenation
// String sql = "SELECT * FROM student WHERE name = '" + keyword + "'";
```

### XSS Prevention

1. **Input Sanitization**: Strip HTML tags from user input
2. **Output Encoding**: Encode data in responses
3. **Content Security Policy**: Set CSP headers in gateway
4. **Validation**: Reject requests with suspicious patterns

## Performance Optimization

### Database Optimization

**Indexes**
- All foreign keys indexed
- Frequently filtered columns indexed (status, tenant_id, created_at)
- Composite indexes for common query patterns

**Query Optimization**
```java
// GOOD: Select only needed fields
List<StudentDTO> students = studentMapper.selectList(
    new LambdaQueryWrapper<Student>()
        .select(Student::getId, Student::getRealName, Student::getStudentNo)
        .eq(Student::getTenantId, tenantId)
);

// BAD: Select all fields when not needed
// List<Student> students = studentMapper.selectList(...);
```

**Connection Pooling**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Caching Strategy

**Cache Warming**
- Pre-load frequently accessed data on startup
- Dashboard statistics
- Configuration data
- Tag and skill tree data

**Cache Aside Pattern**
```java
public DashboardDTO getDashboard(Long userId) {
    String key = "student:dashboard:" + userId;
    DashboardDTO cached = cacheService.get(key);
    if (cached != null) return cached;
    
    DashboardDTO data = buildDashboard(userId);
    cacheService.set(key, data, 5, TimeUnit.MINUTES);
    return data;
}
```

### Async Processing

**Non-Blocking Operations**
```java
@Async
public void sendNotification(Long userId, String message) {
    // Send notification asynchronously
    // Don't block main request thread
}

@Async
public void logActivity(ActivityDTO activity) {
    // Log activity asynchronously
}
```

## Deployment Architecture

### Service Deployment

```
┌─────────────────┐
│   Load Balancer │
└────────┬────────┘
         │
    ┌────▼────┐
    │ Gateway │ (2 instances)
    └────┬────┘
         │
    ┌────┴────────────────────────┐
    │                             │
┌───▼────┐  ┌──────────┐  ┌──────▼──────┐
│Student │  │Enterprise│  │   College   │
│Service │  │ Service  │  │   Service   │
│(3 inst)│  │(2 inst)  │  │  (2 inst)   │
└───┬────┘  └────┬─────┘  └──────┬──────┘
    │            │                │
    └────────────┴────────────────┘
                 │
         ┌───────▼────────┐
         │   PostgreSQL   │
         │   (Primary +   │
         │    Replica)    │
         └───────┬────────┘
                 │
         ┌───────▼────────┐
         │  Redis Cluster │
         └────────────────┘
```

### Configuration Management

**Nacos Configuration**
- Service discovery and registration
- Dynamic configuration updates
- Environment-specific configs (dev, staging, prod)

**Environment Variables**
```yaml
# Database
DB_HOST: postgres.internal
DB_PORT: 5432
DB_NAME: zhitu_cloud
DB_USER: ${DB_USER}
DB_PASSWORD: ${DB_PASSWORD}

# Redis
REDIS_HOST: redis.internal
REDIS_PORT: 6379
REDIS_PASSWORD: ${REDIS_PASSWORD}

# JWT
JWT_SECRET: ${JWT_SECRET}
JWT_EXPIRATION: 86400
```

## Appendix: API Quick Reference

### Student Portal Endpoints
- GET /api/student-portal/v1/dashboard
- GET /api/student-portal/v1/capability/radar
- GET /api/student-portal/v1/tasks
- GET /api/student-portal/v1/recommendations
- GET /api/student-portal/v1/training/projects
- GET /api/student-portal/v1/training/projects/{id}/board
- GET /api/student-portal/v1/internship/jobs
- GET /api/student-portal/v1/internship/reports/my
- GET /api/student-portal/v1/growth/evaluation
- GET /api/student-portal/v1/growth/certificates
- GET /api/student-portal/v1/growth/badges

### Enterprise Portal Endpoints
- GET /api/portal-enterprise/v1/dashboard/stats
- GET /api/portal-enterprise/v1/todos
- GET /api/portal-enterprise/v1/activities
- GET /api/internship/v1/enterprise/jobs
- POST /api/internship/v1/enterprise/jobs
- POST /api/internship/v1/enterprise/jobs/{id}/close
- GET /api/internship/v1/enterprise/applications
- POST /api/internship/v1/enterprise/interviews
- GET /api/portal-enterprise/v1/talent-pool
- DELETE /api/portal-enterprise/v1/talent-pool/{id}
- GET /api/portal-enterprise/v1/mentor/dashboard
- GET /api/portal-enterprise/v1/analytics

### College Portal Endpoints
- GET /api/portal-college/v1/dashboard/stats
- GET /api/portal-college/v1/dashboard/trends
- GET /api/user/v1/college/students
- GET /api/training/v1/college/plans
- POST /api/training/v1/college/plans
- POST /api/training/v1/college/mentors/assign
- GET /api/internship/v1/college/students
- GET /api/internship/v1/college/contracts/pending
- POST /api/internship/v1/college/contracts/{id}/audit
- POST /api/internship/v1/college/inspections
- GET /api/portal-college/v1/crm/enterprises
- GET /api/portal-college/v1/crm/audits
- POST /api/portal-college/v1/crm/audits/{id}
- PUT /api/portal-college/v1/crm/enterprises/{id}/level
- GET /api/portal-college/v1/crm/visits
- POST /api/portal-college/v1/crm/visits
- GET /api/portal-college/v1/warnings
- GET /api/portal-college/v1/warnings/stats
- POST /api/portal-college/v1/warnings/{id}/intervene

### Platform Administration Endpoints
- GET /api/system/v1/dashboard/stats
- GET /api/monitor/v1/health
- GET /api/monitor/v1/users/online-trend
- GET /api/monitor/v1/services
- GET /api/system/v1/tenants/colleges
- GET /api/system/v1/audits/enterprises
- POST /api/system/v1/audits/enterprises/{id}
- GET /api/portal-platform/v1/audits/projects
- POST /api/portal-platform/v1/audits/projects/{id}
- GET /api/system/v1/tags
- POST /api/system/v1/tags
- DELETE /api/system/v1/tags/{id}
- GET /api/system/v1/skills/tree
- GET /api/system/v1/certificates/templates
- GET /api/system/v1/contracts/templates
- GET /api/portal-platform/v1/recommendations/banner
- POST /api/portal-platform/v1/recommendations/banner
- GET /api/portal-platform/v1/recommendations/top-list
- POST /api/portal-platform/v1/recommendations/top-list
- GET /api/system/v1/logs/operation
- GET /api/system/v1/logs/security

---

**Document Version**: 1.0  
**Last Updated**: 2024-02-15  
**Status**: Ready for Implementation
