# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - DateTime Type Mismatch Compilation Errors
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists (compilation errors)
  - **Scoped PBT Approach**: For this deterministic compilation bug, scope the property to the three concrete failing locations (lines 629, 630, 640)
  - Test that compilation fails when LocalDateTime.now() is used for OffsetDateTime fields at the three specific locations
  - The test assertions should verify that after fixing, OffsetDateTime.now() is used instead
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found: compilation errors at lines 629, 630, 640
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Non-DateTime Field Assignments
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy field assignments
  - Write property-based tests capturing that all other field assignments (tenantId, userId, todoType, title, priority, status, activityType, description, refType, refId) remain unchanged
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 3. Fix datetime type mismatch in EnterprisePortalIntegrationTest

  - [x] 3.1 Implement the fix
    - Replace LocalDateTime.now().plusDays(3) with OffsetDateTime.now().plusDays(3) at line 629 (EnterpriseTodo.dueDate)
    - Replace LocalDateTime.now() with OffsetDateTime.now() at line 630 (EnterpriseTodo.createdAt)
    - Replace LocalDateTime.now() with OffsetDateTime.now() at line 640 (EnterpriseActivity.createdAt)
    - Ensure java.time.OffsetDateTime is imported
    - Remove unused java.time.LocalDateTime import if no longer needed
    - _Bug_Condition: isBugCondition(assignment) where assignment.fieldType == OffsetDateTime AND assignment.valueType == LocalDateTime_
    - _Expected_Behavior: Use OffsetDateTime.now() for OffsetDateTime fields to ensure type compatibility and successful compilation_
    - _Preservation: All other field assignments and test logic remain unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - DateTime Type Correctness
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-DateTime Field Assignments
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
