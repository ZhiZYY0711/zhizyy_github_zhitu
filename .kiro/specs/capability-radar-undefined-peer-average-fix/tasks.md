# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Graceful Handling of Missing Peer Average Data
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility
  - Test that CapabilityRadar renders without crashing when peer_average is undefined, null, empty, or shorter than dimensions
  - Test cases: undefined peer_average, null peer_average, empty array, partial array (length 1 with 3 dimensions)
  - The test assertions should verify: no crash occurs, student radar renders, peer radar is hidden or uses fallback values
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found: "Cannot read properties of undefined (reading '0')" error at line 31
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Valid Peer Average Data Rendering
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs (valid peer_average arrays)
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements
  - Test cases: valid peer_average matching dimensions length, loading state, null data, student radar with valid dimensions
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 3. Fix for undefined peer_average crash

  - [x] 3.1 Implement the fix
    - Add peer average validation: `const hasPeerData = data.peer_average && Array.isArray(data.peer_average) && data.peer_average.length > 0`
    - Modify chartData mapping to use safe array access: `peer: data.peer_average?.[index] ?? 0`
    - Add conditional rendering for peer Radar component: `{hasPeerData && <Radar name="专业平均" ... />}`
    - Legend will automatically adapt to rendered Radar components
    - _Bug_Condition: isBugCondition(input) where peer_average is undefined, null, empty, or shorter than dimensions_
    - _Expected_Behavior: Component renders without crashing, shows student radar only or uses fallback value 0 for missing peer data_
    - _Preservation: Valid peer_average data continues to render both radars correctly; loading state, null data, and student radar rendering remain unchanged_
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Graceful Handling of Missing Peer Average Data
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Valid Peer Average Data Rendering
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
