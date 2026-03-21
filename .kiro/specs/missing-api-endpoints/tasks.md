# Implementation Plan: Missing API Endpoints

## Overview

This implementation plan covers the development of 50+ missing API endpoints across the Zhitu Cloud Platform's four user portals (Student, Enterprise, College, Platform). The implementation follows a phased approach prioritizing Student Portal → Enterprise Portal → College Portal → Platform Portal based on immediate user needs.

The platform uses Spring Boot 3.x, MyBatis Plus, PostgreSQL, Redis, and follows a microservices architecture with clear separation of concerns (Controller → Service → Mapper).

## Tasks

- [x] 1. Set up database schema and core infrastructure
  - [x] 1.1 Create database migration scripts for all 15 new tables
    - Create student_task, student_capability, student_recommendation tables in student_svc schema
    - Create project_task, project_enrollment tables in training_svc schema
    - Create enterprise_activity, enterprise_todo, interview_schedule tables in enterprise_svc schema
    - Create enterprise_relationship, enterprise_visit, enterprise_audit, internship_inspection tables in college_svc schema
    - Create sys_tag, skill_tree, certificate_template, contract_template, recommendation_banner, recommendation_top_list, operation_log, security_log, service_health, online_user_trend tables in platform_service schema
    - Add all indexes and foreign key constraints as specified in design.md
    - _Requirements: All requirements depend on proper database schema_
  
  - [ ]* 1.2 Write property test for database schema integrity
    - **Property 26: Transaction Atomicity**
    - **Validates: Requirements 46.2, 46.3**
  
  - [x] 1.3 Create entity classes for all new tables
    - Implement MyBatis Plus entity classes with @TableName, @TableId, @TableField annotations
    - Add @TableLogic for soft delete fields
    - Include proper field mappings for all 15 tables
    - _Requirements: All requirements depend on entity classes_
  
  - [x] 1.4 Create mapper interfaces for all new tables
    - Extend BaseMapper<T> for all entity classes
    - Add custom query methods where needed (complex joins, aggregations)
    - _Requirements: All requirements depend on mapper interfaces_
  
  - [x] 1.5 Set up Redis caching infrastructure
    - Create CacheService utility class with getOrSet and invalidate methods
    - Configure RedisTemplate with proper serialization
    - Define cache key patterns and TTL constants
    - _Requirements: 44.5 (caching for performance)_

- [x] 2. Checkpoint - Verify database and infrastructure setup
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Implement Student Portal APIs (Priority 1)
  - [x] 3.1 Implement student dashboard statistics endpoint
    - Create StudentPortalService.getDashboardStats() method
    - Query training project count, internship job count, pending task count, growth score
    - Implement Redis caching with 5-minute TTL
    - Create StudentPortalController with GET /api/student-portal/v1/dashboard endpoint
    - Add DashboardStatsDTO response class
    - _Requirements: 1.1-1.7_
  
  - [ ]* 3.2 Write property test for dashboard field completeness
    - **Property 2: Dashboard Field Completeness**
    - **Validates: Requirements 1.3-1.6**
  
  - [x] 3.3 Implement capability radar chart endpoint
    - Create StudentPortalService.getCapabilityRadar() method
    - Query student_capability table for all dimensions
    - Calculate scores based on evaluation records and completed projects
    - Implement Redis caching with 10-minute TTL
    - Create GET /api/student-portal/v1/capability/radar endpoint
    - Add CapabilityRadarDTO response class
    - _Requirements: 2.1-2.5_
  
  - [ ]* 3.4 Write property test for capability score range validation
    - **Property 28: Capability Score Range**
    - **Validates: Requirements 2.4**
  
  - [x] 3.5 Implement student task management endpoint
    - Create StudentPortalService.getTasks(status, page, size) method
    - Filter tasks by status (pending/completed) and student_id
    - Implement pagination with PageResult<TaskDTO>
    - Create GET /api/student-portal/v1/tasks endpoint with query parameters
    - _Requirements: 3.1-3.6_
  
  - [ ]* 3.6 Write property test for pagination consistency
    - **Property 4: Pagination Consistency**
    - **Validates: Requirements 3.5**
  
  - [ ]* 3.7 Write property test for filtering correctness
    - **Property 5: Filtering Correctness**
    - **Validates: Requirements 3.3, 3.4**
  
  - [x] 3.8 Implement student recommendations endpoint
    - Create StudentPortalService.getRecommendations(type) method
    - Query student_recommendation table filtered by type (all/project/job/course)
    - Implement Redis caching with 15-minute TTL
    - Create GET /api/student-portal/v1/recommendations endpoint
    - Add RecommendationDTO response class
    - _Requirements: 4.1-4.6_
  
  - [ ]* 3.9 Write property test for recommendation filtering by type
    - **Property 16: Recommendation Filtering by Type**
    - **Validates: Requirements 4.3, 4.4, 4.5**
  
  - [x] 3.10 Implement training projects list endpoint
    - Create StudentPortalService.getTrainingProjects(page, size) method
    - Query training_project table with status filter (open/in_progress)
    - Join with project_enrollment to include enrollment status for current student
    - Implement Redis caching with 5-minute TTL
    - Create GET /api/student-portal/v1/training/projects endpoint
    - _Requirements: 5.1-5.5_
  
  - [x] 3.11 Implement project scrum board endpoint
    - Create StudentPortalService.getProjectBoard(projectId) method
    - Verify student enrollment via project_enrollment table (return 403 if not enrolled)
    - Query project_task table and organize by status (todo/in_progress/done)
    - Create GET /api/student-portal/v1/training/projects/{id}/board endpoint
    - Add ScrumBoardDTO response class with three columns
    - _Requirements: 6.1-6.6_
  
  - [ ]* 3.12 Write property test for authorization by enrollment
    - **Property 6: Authorization by Enrollment**
    - **Validates: Requirements 6.6**
  
  - [ ]* 3.13 Write property test for task organization by status
    - **Property 7: Task Organization by Status**
    - **Validates: Requirements 6.4, 6.5**
  
  - [x] 3.14 Implement internship jobs list endpoint
    - Create StudentPortalService.getInternshipJobs(page, size) method
    - Query job_posting table with status filter (open)
    - Join with job_application to include application status for current student
    - Implement Redis caching with 5-minute TTL
    - Create GET /api/student-portal/v1/internship/jobs endpoint
    - _Requirements: 7.1-7.5_
  
  - [x] 3.15 Implement student weekly reports endpoint
    - Create StudentPortalService.getMyReports(page, size) method
    - Query weekly_report table filtered by student_id
    - Order by submission date descending
    - Create GET /api/student-portal/v1/internship/reports/my endpoint
    - _Requirements: 8.1-8.5_
  
  - [ ]* 3.16 Write property test for ordering consistency
    - **Property 8: Ordering Consistency**
    - **Validates: Requirements 8.5**
  
  - [x] 3.17 Implement student growth evaluation endpoint
    - Create StudentPortalService.getEvaluationSummary() method
    - Query evaluation records from multiple sources (enterprise mentors, college instructors, peers)
    - Calculate average score across all evaluations
    - Create GET /api/student-portal/v1/growth/evaluation endpoint
    - _Requirements: 9.1-9.5_
  
  - [ ]* 3.18 Write property test for evaluation score calculation
    - **Property 29: Evaluation Score Calculation**
    - **Validates: Requirements 9.5**
  
  - [x] 3.19 Implement student certificates endpoint
    - Create StudentPortalService.getMyCertificates(page, size) method
    - Query certificate table filtered by student_id
    - Order by issue date descending
    - Include download URL for certificate PDF
    - Create GET /api/student-portal/v1/growth/certificates endpoint
    - _Requirements: 10.1-10.5_
  
  - [x] 3.20 Implement student badges endpoint
    - Create StudentPortalService.getMyBadges(page, size) method
    - Query badge table filtered by student_id
    - Order by earned date descending
    - Create GET /api/student-portal/v1/growth/badges endpoint
    - _Requirements: 11.1-11.5_

- [x] 4. Checkpoint - Verify Student Portal implementation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement Enterprise Portal APIs (Priority 2)
  - [x] 5.1 Implement enterprise dashboard statistics endpoint
    - Create EnterprisePortalService.getDashboardStats() method
    - Query active job count, pending application count, active intern count, training project count
    - Filter by tenant_id for multi-tenant isolation
    - Implement Redis caching with 5-minute TTL
    - Create EnterprisePortalController with GET /api/portal-enterprise/v1/dashboard/stats endpoint
    - _Requirements: 12.1-12.6_
  
  - [ ]* 5.2 Write property test for tenant isolation
    - **Property 9: Tenant Isolation**
    - **Validates: Requirements 13.2, 17.3**
  
  - [x] 5.3 Implement enterprise todo list endpoint
    - Create EnterprisePortalService.getTodos(page, size) method
    - Query enterprise_todo table filtered by user_id and status=pending
    - Order by priority and due date
    - Create GET /api/portal-enterprise/v1/todos endpoint
    - _Requirements: 13.1-13.5_
  
  - [x] 5.4 Implement enterprise activity feed endpoint
    - Create EnterprisePortalService.getActivities(page, size) method
    - Query enterprise_activity table filtered by tenant_id
    - Limit to activities from past 30 days
    - Order by timestamp descending
    - Implement Redis caching with 3-minute TTL
    - Create GET /api/portal-enterprise/v1/activities endpoint
    - _Requirements: 14.1-14.5_
  
  - [x] 5.5 Implement enterprise job management endpoints
    - Create EnterpriseJobService.getJobs(status, page, size) method
    - Create EnterpriseJobService.createJob(request) method with validation
    - Create EnterpriseJobService.closeJob(jobId) method with notification side effect
    - Implement GET /api/internship/v1/enterprise/jobs endpoint
    - Implement POST /api/internship/v1/enterprise/jobs endpoint
    - Implement POST /api/internship/v1/enterprise/jobs/{id}/close endpoint
    - Add JobDTO and CreateJobRequest classes
    - _Requirements: 15.1-15.6_
  
  - [ ]* 5.6 Write property test for required field validation
    - **Property 10: Required Field Validation**
    - **Validates: Requirements 15.4**
  
  - [ ]* 5.7 Write property test for notification side effects
    - **Property 14: Notification Side Effects**
    - **Validates: Requirements 17.5**
  
  - [x] 5.8 Implement enterprise application management endpoints
    - Create EnterpriseApplicationService.getApplications(jobId, status, page, size) method
    - Create EnterpriseApplicationService.scheduleInterview(request) method
    - Validate interview time, location, and interviewer
    - Send notification to student when interview is scheduled
    - Implement GET /api/internship/v1/enterprise/applications endpoint
    - Implement POST /api/internship/v1/enterprise/interviews endpoint
    - _Requirements: 16.1-16.6, 19.1-19.5_
  
  - [ ]* 5.9 Write property test for date range validation
    - **Property 11: Date Range Validation**
    - **Validates: Requirements 15.4 (job date ranges)_
  
  - [x] 5.10 Implement enterprise talent pool endpoints
    - Create EnterprisePortalService.getTalentPool(page, size) method
    - Create EnterprisePortalService.removeFromTalentPool(id) method (soft delete)
    - Filter by tenant_id for multi-tenant isolation
    - Implement GET /api/portal-enterprise/v1/talent-pool endpoint
    - Implement DELETE /api/portal-enterprise/v1/talent-pool/{id} endpoint
    - _Requirements: 17.1-17.5_
  
  - [x] 5.11 Implement enterprise mentor dashboard endpoint
    - Create EnterpriseMentorService.getDashboard() method
    - Query assigned intern count, pending report count, pending code review count
    - Include list of recent intern activities
    - Implement Redis caching with 5-minute TTL
    - Create GET /api/portal-enterprise/v1/mentor/dashboard endpoint
    - _Requirements: 18.1-18.6_
  
  - [x] 5.12 Implement enterprise analytics endpoint
    - Create EnterpriseAnalyticsService.getAnalytics(range) method
    - Aggregate data by time range (week/month/quarter/year)
    - Calculate application trends, intern performance, project completion rate, mentor satisfaction
    - Implement Redis caching with 30-minute TTL
    - Create GET /api/portal-enterprise/v1/analytics endpoint
    - _Requirements: 19.1-19.6_
  
  - [ ]* 5.13 Write property test for analytics time range filtering
    - **Property 17: Analytics Time Range Filtering**
    - **Validates: Requirements 19.2**

- [x] 6. Checkpoint - Verify Enterprise Portal implementation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement College Portal APIs (Priority 3)
  - [x] 7.1 Implement college dashboard statistics endpoint
    - Create CollegePortalService.getDashboardStats(year) method
    - Query total student count, internship participation rate, employment rate, average salary
    - Calculate top hiring enterprises
    - Implement Redis caching with 1-hour TTL
    - Create CollegePortalController with GET /api/portal-college/v1/dashboard/stats endpoint
    - _Requirements: 20.1-20.7_
  
  - [x] 7.2 Implement college employment trends endpoint
    - Create CollegePortalService.getEmploymentTrends(dimension) method
    - Aggregate trends by dimension (month/quarter/year)
    - Include internship trends, employment trends, salary trends, industry distribution
    - Implement Redis caching with 1-hour TTL
    - Create GET /api/portal-college/v1/dashboard/trends endpoint
    - _Requirements: 21.1-21.6_
  
  - [x] 7.3 Implement college student management endpoint
    - Create CollegeStudentService.getStudents(filters, page, size) method
    - Support filtering by keyword, class_id, and status
    - Include current internship status in response
    - Create GET /api/user/v1/college/students endpoint
    - _Requirements: 22.1-22.6_
  
  - [ ]* 7.4 Write property test for search keyword matching
    - **Property 18: Search Keyword Matching**
    - **Validates: Requirements 22.2**
  
  - [x] 7.5 Implement college training plan management endpoints
    - Create CollegeTrainingService.getPlans(semester, page, size) method
    - Create CollegeTrainingService.createPlan(request) method with validation
    - Create CollegeTrainingService.assignMentor(planId, teacherId) method
    - Validate date range, teacher availability, and qualifications
    - Implement GET /api/training/v1/college/plans endpoint
    - Implement POST /api/training/v1/college/plans endpoint
    - Implement POST /api/training/v1/college/mentors/assign endpoint
    - _Requirements: 23.1-23.6, 29.1-29.6_
  
  - [ ]* 7.6 Write property test for foreign key validation
    - **Property 12: Foreign Key Validation**
    - **Validates: Requirements 29.6**
  
  - [x] 7.7 Implement college internship oversight endpoints
    - Create CollegeInternshipService.getInternshipStudents(status, page, size) method
    - Create CollegeInternshipService.getPendingContracts(page, size) method
    - Create CollegeInternshipService.auditContract(id, action, comment) method
    - Create CollegeInternshipService.createInspection(request) method
    - Validate contract terms and enterprise credentials during audit
    - Implement GET /api/internship/v1/college/students endpoint
    - Implement GET /api/internship/v1/college/contracts/pending endpoint
    - Implement POST /api/internship/v1/college/contracts/{id}/audit endpoint
    - Implement POST /api/internship/v1/college/inspections endpoint
    - _Requirements: 24.1-24.7_
  
  - [ ]* 7.8 Write property test for status transition validity
    - **Property 13: Status Transition Validity**
    - **Validates: Requirements 32.4**
  
  - [x] 7.9 Implement college CRM enterprise management endpoints
    - Create CollegeCRMService.getEnterprises(filters, page, size) method
    - Create CollegeCRMService.getAudits(status, page, size) method
    - Create CollegeCRMService.auditEnterprise(id, action, comment) method
    - Create CollegeCRMService.updateEnterpriseLevel(id, level, reason) method
    - Support filtering by level and industry
    - Validate business license and qualification documents during audit
    - Implement GET /api/portal-college/v1/crm/enterprises endpoint
    - Implement GET /api/portal-college/v1/crm/audits endpoint
    - Implement POST /api/portal-college/v1/crm/audits/{id} endpoint
    - Implement PUT /api/portal-college/v1/crm/enterprises/{id}/level endpoint
    - _Requirements: 25.1-25.8_
  
  - [x] 7.10 Implement college CRM visit records endpoints
    - Create CollegeCRMService.getVisits(enterpriseId, page, size) method
    - Create CollegeCRMService.createVisit(request) method
    - Validate required fields (enterprise_id, visit_date, visitor, purpose, outcome)
    - Order by visit date descending
    - Implement GET /api/portal-college/v1/crm/visits endpoint
    - Implement POST /api/portal-college/v1/crm/visits endpoint
    - _Requirements: 26.1-26.6_
  
  - [x] 7.11 Implement college warning system endpoints
    - Create CollegeWarningService.getWarnings(filters, page, size) method
    - Create CollegeWarningService.getWarningStats() method
    - Create CollegeWarningService.intervene(id, request) method
    - Support filtering by level, type, and status
    - Calculate statistics by level, type, and status
    - Update warning status to "intervened" after intervention
    - Implement Redis caching for stats with 10-minute TTL
    - Implement GET /api/portal-college/v1/warnings endpoint
    - Implement GET /api/portal-college/v1/warnings/stats endpoint
    - Implement POST /api/portal-college/v1/warnings/{id}/intervene endpoint
    - _Requirements: 27.1-27.8_
  
  - [ ]* 7.12 Write property test for warning statistics consistency
    - **Property 30: Warning Statistics Consistency**
    - **Validates: Requirements 27.2**

- [x] 8. Checkpoint - Verify College Portal implementation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implement Platform Administration APIs (Priority 4)
  - [x] 9.1 Implement platform dashboard statistics endpoint
    - Create PlatformService.getDashboardStats() method
    - Query total tenant count, total user count, active user count, total enterprise count, pending audit count
    - Implement Redis caching with 10-minute TTL
    - Create PlatformSystemController with GET /api/system/v1/dashboard/stats endpoint
    - _Requirements: 28.1-28.7_
  
  - [x] 9.2 Implement platform system health monitoring endpoints
    - Create PlatformMonitorService.getHealth() method
    - Create PlatformMonitorService.getOnlineUserTrend() method
    - Create PlatformMonitorService.getServices() method
    - Query service_health table for all microservices
    - Query online_user_trend table for past 24 hours
    - Implement Redis caching with 1-minute TTL for health, 5-minute TTL for trends
    - Implement GET /api/monitor/v1/health endpoint
    - Implement GET /api/monitor/v1/users/online-trend endpoint
    - Implement GET /api/monitor/v1/services endpoint
    - _Requirements: 29.1-29.7_
  
  - [x] 9.3 Implement platform tenant management endpoint
    - Create PlatformService.getTenantList(type, status, page, size) method
    - Support filtering by type and status
    - Include subscription plan and expiration date
    - Create GET /api/system/v1/tenants/colleges endpoint
    - _Requirements: 30.1-30.6_
  
  - [x] 9.4 Implement platform enterprise audit endpoints
    - Create PlatformAuditService.getEnterpriseAudits(status, page, size) method
    - Create PlatformAuditService.auditEnterprise(id, action, rejectReason) method
    - Activate enterprise account on approval
    - Send notification to enterprise
    - Require reject_reason when action is "reject"
    - Implement GET /api/system/v1/audits/enterprises endpoint
    - Implement POST /api/system/v1/audits/enterprises/{id} endpoint
    - _Requirements: 31.1-31.7_
  
  - [x] 9.5 Implement platform project audit endpoints
    - Create PlatformAuditService.getProjectAudits(status, page, size) method
    - Create PlatformAuditService.auditProject(id, action, qualityRating) method
    - Support optional quality_rating parameter on approval
    - Implement GET /api/portal-platform/v1/audits/projects endpoint
    - Implement POST /api/portal-platform/v1/audits/projects/{id} endpoint
    - _Requirements: 32.1-32.7_
  
  - [x] 9.6 Implement platform recommendation banner endpoints
    - Create PlatformRecommendationService.getBanners(portal) method
    - Create PlatformRecommendationService.saveBanner(request) method
    - Filter banners by target_portal and active date range
    - Validate required fields (title, image_url, link_url, target_portal)
    - Invalidate cache on save
    - Implement Redis caching with 30-minute TTL
    - Implement GET /api/portal-platform/v1/recommendations/banner endpoint
    - Implement POST /api/portal-platform/v1/recommendations/banner endpoint
    - _Requirements: 37.1-37.6_
  
  - [ ]* 9.7 Write property test for banner scheduling logic
    - **Property 21: Banner Scheduling Logic**
    - **Validates: Requirements 37.5, 37.6**
  
  - [ ]* 9.8 Write property test for cache invalidation on mutation
    - **Property 15: Cache Invalidation on Mutation**
    - **Validates: Requirements 59, 61**
  
  - [x] 9.9 Implement platform top list endpoints
    - Create PlatformRecommendationService.getTopList(listType) method
    - Create PlatformRecommendationService.saveTopList(request) method
    - Support list types: mentor, course, project
    - Validate max 10 items in item_ids array
    - Invalidate cache on save
    - Implement Redis caching with 1-hour TTL
    - Implement GET /api/portal-platform/v1/recommendations/top-list endpoint
    - Implement POST /api/portal-platform/v1/recommendations/top-list endpoint
    - _Requirements: 38.1-38.6_
  
  - [ ]* 9.10 Write property test for top list size limit
    - **Property 22: Top List Size Limit**
    - **Validates: Requirements 38.6**

- [x] 10. Checkpoint - Verify Platform Administration implementation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implement cross-cutting concerns and global features
  - [x] 11.1 Implement global error handling
    - Create GlobalExceptionHandler with @RestControllerAdvice
    - Handle BusinessException, MethodArgumentNotValidException, Exception
    - Return consistent error format with appropriate HTTP status codes
    - Log all errors with stack traces
    - Generate error IDs for support reference
    - _Requirements: 43.1-43.7_
  
  - [ ]* 11.2 Write property test for response format consistency
    - **Property 1: Response Format Consistency**
    - **Validates: Requirements 1.2, 2.2, 12.2, 20.1, 28.1, 42.1, 42.2**
  
  - [ ]* 11.3 Write property test for authentication enforcement
    - **Property 3: Authentication Enforcement**
    - **Validates: Requirements 1.8, 41.1, 41.4, 43.2**
  
  - [ ]* 11.4 Write property test for role-based access control
    - **Property 27: Role-Based Access Control**
    - **Validates: Requirements 41.6**
  
  - [x] 11.5 Implement operation logging interceptor
    - Create OperationLogInterceptor to log all API requests
    - Record timestamp, user_id, module, operation, request params, response status, IP address, execution time
    - Store logs in operation_log table asynchronously
    - _Requirements: 39.1-39.7_
  
  - [x] 11.6 Implement security logging
    - Create SecurityLogService to record security events
    - Log authentication failures, permission denials, suspicious activities
    - Store logs in security_log table
    - _Requirements: 40.1-40.7_
  
  - [ ]* 11.7 Write property test for log retention policy
    - **Property 23: Log Retention Policy**
    - **Validates: Requirements 39.7, 40.7**
  
  - [ ]* 11.8 Write property test for sensitive data masking
    - **Property 25: Sensitive Data Masking**
    - **Validates: Requirements 50.3**
  
  - [x] 11.9 Configure rate limiting in gateway
    - Implement rate limiting per user (1000 req/hour) and per IP (100 req/hour)
    - Return 429 status with retry-after header when limit exceeded
    - Use sliding window algorithm
    - _Requirements: 49.1-49.6_
  
  - [ ]* 11.10 Write property test for rate limit enforcement
    - **Property 24: Rate Limit Enforcement**
    - **Validates: Requirements 49.2, 49.3, 49.4**

- [x] 12. Integration testing and optimization
  - [x] 12.1 Write integration tests for Student Portal endpoints
    - Test complete request-response flow for all 11 Student Portal endpoints
    - Verify authentication, authorization, caching, and database interactions
    - Test error scenarios (401, 403, 404, 500)
    - _Requirements: Student Portal requirements 1-11_
  
  - [x] 12.2 Write integration tests for Enterprise Portal endpoints
    - Test complete request-response flow for all 12 Enterprise Portal endpoints
    - Verify tenant isolation, notifications, and side effects
    - Test job creation, application management, and interview scheduling flows
    - _Requirements: Enterprise Portal requirements 12-19_
  
  - [x] 12.3 Write integration tests for College Portal endpoints
    - Test complete request-response flow for all 19 College Portal endpoints
    - Verify CRM workflows, warning system, and audit processes
    - Test student management, training plans, and internship oversight
    - _Requirements: College Portal requirements 20-27_
  
  - [x] 12.4 Write integration tests for Platform Administration endpoints
    - Test complete request-response flow for all platform endpoints
    - Verify monitoring, auditing, and recommendation features
    - Test tenant management and system health monitoring
    - _Requirements: Platform requirements 28-40_
  
  - [x] 12.5 Performance testing and optimization
    - Run load tests to verify 500ms dashboard response time at p95
    - Run load tests to verify 1000ms list endpoint response time at p95
    - Verify 100 requests per second per service capacity
    - Optimize slow queries with EXPLAIN ANALYZE
    - Add missing indexes if identified during testing
    - _Requirements: 44.1-44.7_
  
  - [x] 12.6 Cache effectiveness testing
    - Verify cache hit rates meet 70% threshold
    - Test cache invalidation on data mutations
    - Verify TTL expiration behavior
    - _Requirements: 44.5_

- [x] 13. Final checkpoint and documentation
  - [x] 13.1 Generate OpenAPI 3.0 specification
    - Add Swagger/OpenAPI annotations to all controllers
    - Include request/response examples
    - Document all parameters, authentication requirements, and error codes
    - _Requirements: 47.1-47.6_
  
  - [x] 13.2 Verify all property-based tests pass
    - Run all 30 property tests with minimum 100 iterations each
    - Verify all properties hold across generated test cases
    - Fix any property violations discovered
  
  - [x] 13.3 Final integration verification
    - Test end-to-end flows across all four portals
    - Verify frontend-backend integration
    - Test with realistic data volumes
    - Verify all 50+ endpoints return expected responses
  
  - [x] 13.4 Security audit
    - Verify JWT authentication on all protected endpoints
    - Verify role-based access control enforcement
    - Verify tenant isolation in multi-tenant endpoints
    - Verify input validation and SQL injection prevention
    - Verify sensitive data encryption and masking
    - _Requirements: 41.1-41.7, 45.1-45.7, 50.1-50.7_

- [x] 14. Final checkpoint - Production readiness verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Implementation follows priority order: Student → Enterprise → College → Platform
- Checkpoints ensure incremental validation at major milestones
- Property tests validate universal correctness properties across all inputs
- Integration tests validate specific examples and end-to-end flows
- All endpoints use Java with Spring Boot 3.x, MyBatis Plus, PostgreSQL, and Redis
- Database schema must be created first as all other tasks depend on it
- Caching strategy uses Redis with TTL-based expiration and event-based invalidation
- Error handling follows consistent Result<T> wrapper pattern across all endpoints
