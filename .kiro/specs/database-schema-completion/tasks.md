# Implementation Plan: Database Schema Completion

## Overview

This implementation plan breaks down the database schema validation and correction system into discrete coding tasks. The system will scan Java entity classes and SQL schema files, identify inconsistencies (schema naming, SQL syntax errors, missing fields, etc.), and generate corrected SQL files and documentation. The implementation uses Python 3.10+ with sqlparse for SQL parsing and javalang for Java parsing.

## Tasks

- [ ] 1. Set up project structure and dependencies
  - Create `tools/schema_validator/` directory structure
  - Create `requirements.txt` with dependencies: sqlparse, javalang, pytest, hypothesis, pyyaml
  - Create `schema_validation_config.yaml` configuration file
  - Create `__init__.py` files for Python package structure
  - _Requirements: 12.1_

- [ ] 2. Implement Entity Parser
  - [ ] 2.1 Create entity parser module
    - Write `entity_parser.py` with EntityMetadata and FieldMetadata dataclasses
    - Implement function to scan Java files and extract @TableName annotations
    - Implement camelCase to snake_case field name conversion
    - Handle BaseEntity inheritance (id, created_at, updated_at, is_deleted fields)
    - _Requirements: 1.2, 4.1, 4.5, 7.2, 8.3, 12.2_
  
  - [ ]* 2.2 Write unit tests for entity parser
    - Test parsing WarningRecord entity (growth_svc schema)
    - Test parsing StudentInfo entity
    - Test camelCase to snake_case conversion
    - Test BaseEntity field inheritance
    - _Requirements: 1.2, 4.5_
  
  - [ ]* 2.3 Write property test for entity parser
    - **Property 1: Schema Name Extraction**
    - **Validates: Requirements 1.2**
    - **Property 6: Field-to-Column Mapping Detection**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.5**

- [ ] 3. Implement SQL Parser
  - [ ] 3.1 Create SQL parser module
    - Write `sql_parser.py` with SQLMetadata, TableMetadata, ColumnMetadata dataclasses
    - Implement function to parse CREATE SCHEMA statements
    - Implement function to parse CREATE TABLE statements
    - Extract column definitions with data types and constraints
    - Detect inline COMMENT keywords in column definitions
    - Parse CONSTRAINT definitions (PRIMARY KEY, FOREIGN KEY, CHECK)
    - Parse CREATE INDEX statements
    - Parse COMMENT ON statements
    - _Requirements: 2.1, 4.2, 5.1, 6.1, 6.2, 7.1, 8.1, 9.1_
  
  - [ ]* 3.2 Write unit tests for SQL parser
    - Test parsing 01_auth_center.sql
    - Test parsing 08_growth_service.sql (schema name mismatch case)
    - Test detecting inline COMMENT keywords
    - Test parsing foreign key constraints
    - Test parsing indexes
    - _Requirements: 2.1, 5.1_
  
  - [ ]* 3.3 Write property test for SQL parser
    - **Property 4: Comment Syntax Elimination**
    - **Validates: Requirements 2.1, 2.3**

- [ ] 4. Implement Validator
  - [ ] 4.1 Create validator module for schema name validation
    - Write `validator.py` with ValidationIssue dataclass
    - Implement schema name consistency check (entity vs SQL)
    - Detect growth_service vs growth_svc mismatch
    - _Requirements: 1.2, 1.3_
  
  - [ ] 4.2 Add SQL syntax validation
    - Implement inline COMMENT keyword detection
    - Flag all tables with inline COMMENT as critical issues
    - _Requirements: 2.1, 2.3_
  
  - [ ] 4.3 Add field-to-column mapping validation
    - Compare entity fields against SQL columns
    - Apply camelCase to snake_case conversion
    - Identify missing columns in SQL tables
    - Handle BaseEntity fields specially
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [ ] 4.4 Add foreign key validation
    - Detect entity fields ending in "Id"
    - Check if corresponding FK constraint exists in SQL
    - Infer referenced table from field name
    - Validate referenced table exists
    - _Requirements: 5.1, 5.2, 5.3, 5.4_
  
  - [ ] 4.5 Add index validation
    - Check indexes on tenant_id columns with "WHERE is_deleted = FALSE"
    - Check indexes on all foreign key columns
    - Check indexes on status/type enum columns
    - Verify partial indexes exclude soft-deleted records
    - _Requirements: 6.1, 6.2, 6.3, 6.4_
  
  - [ ] 4.6 Add soft delete validation
    - Verify is_deleted column exists in all SQL tables
    - Verify @TableLogic annotation exists in all entities
    - Check indexes include "WHERE is_deleted = FALSE"
    - _Requirements: 7.1, 7.2, 7.3, 7.4_
  
  - [ ] 4.7 Add timestamp validation
    - Verify created_at and updated_at columns exist
    - Verify columns use TIMESTAMPTZ (not TIMESTAMP)
    - Verify entity has createdAt and updatedAt fields
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [ ] 4.8 Add check constraint validation
    - Detect status and type columns in SQL tables
    - Verify CHECK constraints exist for enum columns
    - Compare constraint values with entity comments
    - _Requirements: 9.1, 9.2, 9.3, 9.4_
  
  - [ ]* 4.9 Write unit tests for validator
    - Test schema name mismatch detection
    - Test inline COMMENT detection
    - Test missing column detection
    - Test missing FK detection
    - Test missing index detection
    - Test soft delete validation
    - Test timestamp validation
    - Test check constraint validation
    - _Requirements: 1.2, 2.1, 4.3, 5.1, 6.1, 7.1, 8.1, 9.1_
  
  - [ ]* 4.10 Write property tests for validator
    - **Property 1: Schema Name Mismatch Detection**
    - **Validates: Requirements 1.2**
    - **Property 8: Foreign Key Detection**
    - **Validates: Requirements 5.1**
    - **Property 12: Index Coverage on Foreign Keys**
    - **Validates: Requirements 6.2**
    - **Property 16: Soft Delete Column Presence**
    - **Validates: Requirements 7.1, 7.4**
    - **Property 19: Timestamp Column Presence**
    - **Validates: Requirements 8.1, 8.2**
    - **Property 22: Check Constraint Presence**
    - **Validates: Requirements 9.1, 9.2, 9.3**

- [ ] 5. Checkpoint - Ensure validation logic is complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement SQL Transformer
  - [ ] 6.1 Create SQL transformer module
    - Write `sql_transformer.py` with transformation functions
    - Implement schema name correction (growth_service → growth_svc)
    - Implement inline COMMENT to COMMENT ON conversion
    - Preserve all comment text during transformation
    - Generate ALTER TABLE statements for missing columns
    - Generate ALTER TABLE ADD CONSTRAINT for missing foreign keys
    - Generate CREATE INDEX statements for missing indexes
    - _Requirements: 1.3, 2.2, 2.4, 4.4, 5.3, 6.1, 6.2, 6.3_
  
  - [ ]* 6.2 Write unit tests for SQL transformer
    - Test schema name transformation
    - Test COMMENT syntax transformation with specific examples
    - Test missing column addition
    - Test missing FK addition
    - Test missing index addition
    - _Requirements: 1.3, 2.2, 2.4, 4.4, 5.3_
  
  - [ ]* 6.3 Write property tests for SQL transformer
    - **Property 2: Schema Name Correction**
    - **Validates: Requirements 1.3**
    - **Property 3: Comment Syntax Transformation Preserves Content**
    - **Validates: Requirements 2.4**
    - **Property 5: Comment Conversion Completeness**
    - **Validates: Requirements 2.2**
    - **Property 7: Missing Column Addition**
    - **Validates: Requirements 4.4**
    - **Property 10: Foreign Key Addition**
    - **Validates: Requirements 5.3**

- [ ] 7. Implement Documentation Updater
  - [ ] 7.1 Create documentation updater module
    - Write `doc_updater.py` with update functions
    - Implement database name replacement (zhitu_db → zhitu_cloud)
    - Implement schema name updates (growth_service → growth_svc)
    - Update database/README.md
    - Update database/init_database.sql
    - Update database/MIGRATION_GUIDE.md
    - Update database/ER_DIAGRAM.md
    - _Requirements: 3.1, 3.2, 3.3, 11.1, 11.2, 11.3, 11.4_
  
  - [ ]* 7.2 Write unit tests for documentation updater
    - Test database name replacement in README.md
    - Test schema name updates in init_database.sql
    - Test ER diagram updates
    - _Requirements: 3.1, 3.2, 3.3, 11.1_

- [ ] 8. Implement Report Generator
  - [ ] 8.1 Create report generator module
    - Write `report_generator.py` with report generation functions
    - Generate validation report with all issues found
    - Categorize issues by severity (critical, warning, info)
    - Include before/after examples for each fix
    - Generate summary counts by category and schema
    - Write report to database/SCHEMA_VALIDATION_REPORT.md
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [ ]* 8.2 Write unit tests for report generator
    - Test report generation with sample issues
    - Test severity categorization
    - Test summary statistics
    - Test before/after examples
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [ ]* 8.3 Write property tests for report generator
    - **Property 24: Validation Report Completeness**
    - **Validates: Requirements 10.1, 10.3, 10.4**
    - **Property 25: Validation Report Fix Documentation**
    - **Validates: Requirements 10.2**

- [ ] 9. Implement Main Orchestrator
  - [ ] 9.1 Create main validation script
    - Write `validate_schema.py` as main entry point
    - Implement --scan mode (validation only)
    - Implement --fix mode (apply corrections)
    - Implement --verify mode (verify corrections)
    - Implement --restore mode (rollback changes)
    - Load configuration from schema_validation_config.yaml
    - Coordinate entity parser, SQL parser, validator, transformer, and report generator
    - Create backups before applying corrections
    - _Requirements: 1.2, 1.3, 2.1, 2.2, 3.1, 3.2, 3.3, 4.3, 4.4, 5.3, 10.1_
  
  - [ ]* 9.2 Write integration tests for main orchestrator
    - Test end-to-end validation of all 8 schema files
    - Test correction application
    - Test backup and restore functionality
    - Verify no inline COMMENTs remain after processing
    - Verify all schema names are consistent
    - Verify all database name references are updated
    - _Requirements: 1.3, 2.3, 3.1, 3.2, 3.3_

- [ ] 10. Checkpoint - Ensure core functionality works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Apply corrections to SQL schema files
  - [ ] 11.1 Process 01_auth_center.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.2 Process 02_platform_service.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.3 Process 03_student_svc.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.4 Process 04_college_svc.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.5 Process 05_enterprise_svc.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.6 Process 06_internship_svc.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.7 Process 07_training_svc.sql
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Verify foreign keys and indexes
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 11.8 Process 08_growth_service.sql
    - Rename schema from growth_service to growth_svc
    - Remove inline COMMENT keywords
    - Add COMMENT ON statements
    - Update all table references to use growth_svc schema
    - Verify foreign keys and indexes
    - _Requirements: 1.2, 1.3, 2.1, 2.2, 2.3, 2.4_

- [ ] 12. Update documentation files
  - [ ] 12.1 Update database/README.md
    - Replace all zhitu_db with zhitu_cloud
    - Replace growth_service with growth_svc in schema tables
    - Update example commands
    - _Requirements: 3.1, 11.1, 11.3_
  
  - [ ] 12.2 Update database/init_database.sql
    - Replace zhitu_db with zhitu_cloud
    - Replace growth_service with growth_svc in schema lists
    - Update GRANT statements to include growth_svc
    - _Requirements: 3.2, 11.1, 11.3_
  
  - [ ] 12.3 Update database/MIGRATION_GUIDE.md
    - Replace all zhitu_db with zhitu_cloud
    - Update migration examples
    - _Requirements: 3.3_
  
  - [ ] 12.4 Update database/ER_DIAGRAM.md
    - Replace growth_service with growth_svc in diagrams
    - Verify all table names match SQL files
    - Verify all foreign key relationships are documented
    - Update column data types if changed
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

- [ ] 13. Generate final validation report
  - Run validation in --verify mode
  - Generate comprehensive SCHEMA_VALIDATION_REPORT.md
  - Include all issues found and fixed
  - Include before/after examples
  - Include summary statistics by schema and severity
  - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [ ] 14. Final checkpoint - Verify all corrections are complete
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The implementation uses Python 3.10+ as specified in the design document
- All SQL corrections preserve existing comment content
- Backups are created before applying any corrections
- The validation report provides complete traceability of all changes
