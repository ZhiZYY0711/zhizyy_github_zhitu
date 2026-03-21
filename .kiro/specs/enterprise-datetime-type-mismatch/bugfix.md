# Bugfix Requirements Document

## Introduction

The EnterprisePortalIntegrationTest contains compilation errors due to incorrect datetime type usage. The test code uses `LocalDateTime.now()` to set values for entity fields that are declared as `OffsetDateTime` type. This causes type mismatch compilation errors at three locations in the test setup code.

The bug affects test data initialization for EnterpriseTodo and EnterpriseActivity entities, preventing the test class from compiling successfully.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN setting EnterpriseTodo.dueDate at line 629 THEN the system uses LocalDateTime.now().plusDays(3) causing compilation error "incompatible types: LocalDateTime cannot be converted to OffsetDateTime"

1.2 WHEN setting EnterpriseTodo.createdAt at line 630 THEN the system uses LocalDateTime.now() causing compilation error "incompatible types: LocalDateTime cannot be converted to OffsetDateTime"

1.3 WHEN setting EnterpriseActivity.createdAt at line 640 THEN the system uses LocalDateTime.now() causing compilation error "incompatible types: LocalDateTime cannot be converted to OffsetDateTime"

### Expected Behavior (Correct)

2.1 WHEN setting EnterpriseTodo.dueDate at line 629 THEN the system SHALL use OffsetDateTime.now().plusDays(3) to match the field type

2.2 WHEN setting EnterpriseTodo.createdAt at line 630 THEN the system SHALL use OffsetDateTime.now() to match the field type

2.3 WHEN setting EnterpriseActivity.createdAt at line 640 THEN the system SHALL use OffsetDateTime.now() to match the field type

### Unchanged Behavior (Regression Prevention)

3.1 WHEN setting other EnterpriseTodo fields (tenantId, userId, todoType, title, priority, status) THEN the system SHALL CONTINUE TO use their current value assignment methods

3.2 WHEN setting other EnterpriseActivity fields (tenantId, activityType, description, refType, refId) THEN the system SHALL CONTINUE TO use their current value assignment methods

3.3 WHEN the test executes after the fix THEN the system SHALL CONTINUE TO create the same test data with equivalent datetime values (only the type changes from LocalDateTime to OffsetDateTime)
