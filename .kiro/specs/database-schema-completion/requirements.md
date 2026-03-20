# Requirements Document: Database Schema Completion

## Introduction

This document specifies the requirements for completing and fixing the database schema design for the Zhitu (智图) Internship Management Platform. The system is a multi-tenant SaaS platform built with Spring Boot microservices, MyBatis-Plus ORM, and PostgreSQL 15+ database. The database uses schema-based isolation where each microservice has its own PostgreSQL schema. Currently, there are inconsistencies between the Java entity classes and SQL schema files that must be resolved to ensure the system functions correctly.

## Glossary

- **Entity**: Java class annotated with MyBatis-Plus annotations that maps to a database table
- **Schema**: PostgreSQL namespace that groups related tables for a microservice
- **SQL_File**: SQL script file that creates database schemas and tables
- **MyBatis_Plus**: Java ORM framework that maps entities to database tables
- **TableName_Annotation**: MyBatis-Plus annotation that specifies schema and table name for an entity
- **COMMENT_Statement**: PostgreSQL syntax for adding comments to tables and columns
- **Database_Name**: The PostgreSQL database identifier (zhitu_cloud)
- **Schema_Name**: PostgreSQL schema identifier (e.g., auth_center, student_svc, growth_service)

## Requirements

### Requirement 1: Fix Schema Naming Inconsistency

**User Story:** As a backend developer, I want consistent schema names between entity classes and SQL files, so that MyBatis-Plus can correctly map entities to database tables.

#### Acceptance Criteria

1. WHEN the WarningRecord entity is examined, THE Schema_Name SHALL be "growth_svc" in both the entity @TableName annotation and the SQL file
2. WHEN all entity files are scanned, THE System SHALL identify any schema name mismatches between @TableName annotations and SQL file schema names
3. WHEN a schema name mismatch is found, THE SQL_File SHALL be updated to match the entity @TableName annotation
4. THE System SHALL verify that all three growth-related entities (EvaluationRecord, GrowthBadge, WarningRecord) use the same schema name consistently

### Requirement 2: Fix PostgreSQL COMMENT Syntax Errors

**User Story:** As a database administrator, I want valid PostgreSQL syntax in all SQL files, so that the schema initialization scripts execute without errors.

#### Acceptance Criteria

1. WHEN a SQL file contains inline COMMENT keywords, THE System SHALL remove them
2. WHEN table or column comments are needed, THE SQL_File SHALL use "COMMENT ON TABLE" and "COMMENT ON COLUMN" statements after table creation
3. WHEN all SQL files are processed, THE System SHALL verify that no inline COMMENT keywords remain
4. THE System SHALL ensure all existing comment content is preserved in the converted COMMENT ON statements

### Requirement 3: Update Database Name References

**User Story:** As a DevOps engineer, I want all documentation to reference the correct database name, so that deployment scripts and configuration match the actual database.

#### Acceptance Criteria

1. WHEN database/README.md is examined, THE System SHALL replace all occurrences of "zhitu_db" with "zhitu_cloud"
2. WHEN database/init_database.sql is examined, THE System SHALL replace all occurrences of "zhitu_db" with "zhitu_cloud"
3. WHEN database/MIGRATION_GUIDE.md is examined, THE System SHALL replace all occurrences of "zhitu_db" with "zhitu_cloud"
4. THE System SHALL verify that backend/nacos配置内容.md already uses "zhitu_cloud" correctly

### Requirement 4: Verify Entity-to-Table Field Mapping

**User Story:** As a backend developer, I want all entity fields to have corresponding SQL table columns, so that MyBatis-Plus queries execute without field mapping errors.

#### Acceptance Criteria

1. WHEN an entity class is examined, THE System SHALL extract all field names from the entity
2. WHEN the corresponding SQL table is examined, THE System SHALL extract all column names
3. WHEN field names are compared to column names, THE System SHALL identify any missing columns in the SQL table
4. IF a missing column is found, THEN THE SQL_File SHALL be updated to add the missing column with appropriate data type and constraints
5. THE System SHALL verify that camelCase entity fields correctly map to snake_case SQL columns

### Requirement 5: Validate Foreign Key Relationships

**User Story:** As a database administrator, I want all foreign key constraints to match the entity relationships in code, so that referential integrity is enforced at the database level.

#### Acceptance Criteria

1. WHEN an entity contains a field ending in "Id" (e.g., tenantId, userId), THE System SHALL check if a corresponding foreign key constraint exists in the SQL table
2. WHEN a foreign key constraint is missing, THE System SHALL determine the referenced table from the field name and entity relationships
3. IF the referenced table exists, THEN THE SQL_File SHALL add the appropriate FOREIGN KEY constraint
4. THE System SHALL verify that all existing foreign key constraints reference valid tables and columns

### Requirement 6: Verify Index Optimization

**User Story:** As a database administrator, I want indexes on frequently queried columns, so that query performance meets application requirements.

#### Acceptance Criteria

1. WHEN a table has a tenant_id column, THE System SHALL verify an index exists on tenant_id with "WHERE is_deleted = FALSE"
2. WHEN a table has foreign key columns, THE System SHALL verify indexes exist on those columns
3. WHEN a table has status or type enum columns, THE System SHALL verify indexes exist on those columns
4. THE System SHALL verify that all indexes exclude soft-deleted records using "WHERE is_deleted = FALSE" when applicable

### Requirement 7: Validate Soft Delete Implementation

**User Story:** As a backend developer, I want consistent soft delete implementation across all tables, so that deleted records are properly filtered in queries.

#### Acceptance Criteria

1. WHEN a SQL table is examined, THE System SHALL verify it contains an "is_deleted BOOLEAN NOT NULL DEFAULT FALSE" column
2. WHEN an entity class is examined, THE System SHALL verify it contains an "isDeleted" field with @TableLogic annotation
3. WHEN indexes are examined, THE System SHALL verify that indexes on non-unique columns include "WHERE is_deleted = FALSE"
4. THE System SHALL identify any tables missing the is_deleted column and add it

### Requirement 8: Validate Timestamp Columns

**User Story:** As a backend developer, I want consistent timestamp handling across all tables, so that audit trails are accurate and timezone-aware.

#### Acceptance Criteria

1. WHEN a SQL table is examined, THE System SHALL verify it contains "created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
2. WHEN a SQL table is examined, THE System SHALL verify it contains "updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP"
3. WHEN an entity class is examined, THE System SHALL verify it contains createdAt and updatedAt fields with appropriate @TableField annotations
4. THE System SHALL verify that all timestamp columns use TIMESTAMPTZ (not TIMESTAMP) for timezone awareness

### Requirement 9: Validate Check Constraints

**User Story:** As a database administrator, I want check constraints on enum-like columns, so that invalid data cannot be inserted into the database.

#### Acceptance Criteria

1. WHEN a SQL table has a status column, THE System SHALL verify a CHECK constraint exists limiting valid values
2. WHEN a SQL table has a type column, THE System SHALL verify a CHECK constraint exists limiting valid values
3. WHEN an entity has integer fields representing enums, THE System SHALL verify corresponding CHECK constraints exist in SQL
4. THE System SHALL verify that CHECK constraint values match the valid values documented in entity comments

### Requirement 10: Generate Schema Validation Report

**User Story:** As a project manager, I want a comprehensive report of all schema issues found and fixed, so that I can verify the database schema is production-ready.

#### Acceptance Criteria

1. WHEN all validation checks complete, THE System SHALL generate a report listing all issues found
2. WHEN an issue is fixed, THE System SHALL document the fix in the report with before/after examples
3. THE System SHALL categorize issues by severity (critical, warning, info)
4. THE System SHALL provide a summary count of issues by category and by schema
5. THE report SHALL be saved to database/SCHEMA_VALIDATION_REPORT.md

### Requirement 11: Update ER Diagram Documentation

**User Story:** As a developer, I want the ER diagram documentation to accurately reflect the final schema structure, so that I can understand table relationships.

#### Acceptance Criteria

1. WHEN database/ER_DIAGRAM.md is examined, THE System SHALL verify all table names match the SQL files
2. WHEN foreign key relationships are updated, THE System SHALL update the ER diagram to reflect the changes
3. THE System SHALL verify that all schemas are represented in the ER diagram
4. THE System SHALL ensure column data types in the ER diagram match the SQL definitions

### Requirement 12: Validate MyBatis-Plus Configuration

**User Story:** As a backend developer, I want MyBatis-Plus properly configured for schema-based multi-tenancy, so that queries are routed to the correct schema.

#### Acceptance Criteria

1. WHEN application.yml files are examined, THE System SHALL verify MyBatis-Plus configuration includes schema handling
2. THE System SHALL verify that all entity @TableName annotations include the schema parameter
3. THE System SHALL verify that no entity uses a default schema (all must explicitly specify schema)
4. THE System SHALL document any required MyBatis-Plus configuration changes in the validation report
